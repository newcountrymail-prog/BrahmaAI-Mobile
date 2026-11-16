package com.brahma.assistant.planner

import android.util.Log
import com.brahma.assistant.actions.BrahmaActionRegistry
import com.brahma.assistant.ai.client.GeminiMobileClient
import com.brahma.assistant.heal.AutoHealManager
import com.brahma.assistant.heal.DiagnosisResult
import com.brahma.assistant.heal.FailureCategory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class RawPlanStepJson(
    val stepId: Int,
    val title: String,
    val description: String,
    val assignedRole: String,
    val actionType: String,
    val actionParameters: Map<String, String> = emptyMap()
)

@Serializable
data class RawPlanResponseJson(
    val steps: List<RawPlanStepJson>
)

class OmniPlanner(
    private val geminiClient: GeminiMobileClient,
    private val actionRegistry: BrahmaActionRegistry,
    private val autoHealManager: AutoHealManager
) {
    companion object {
        private const val TAG = "OmniPlanner"

        private val SYSTEM_PLANNER_PROMPT = """
        You are Brahma OmniPlanner, an elite autonomous multi-agent task planning engine for Android.
        Given a complex user directive, decompose it into an ordered sequence of executable steps.
        Available Action Types:
        - "LAUNCH_APP": parameters: {"packageName": "<package_name>"}
        - "SYSTEM_DIAGNOSTICS": parameters: {"metric": "battery|storage|memory"}
        - "DEVICE_SETTINGS": parameters: {"setting": "wifi|bluetooth|battery_saver|volume", "value": "on|off|toggle|level"}
        - "SEND_MESSAGE": parameters: {"recipient": "<number/name>", "body": "<text>"}
        - "OPEN_URL": parameters: {"url": "<http_url>"}
        - "UI_CLICK": parameters: {"targetText": "<node_text_or_id>"}

        Output STRICT JSON conforming to:
        {
          "steps": [
            {
              "stepId": 1,
              "title": "Short title",
              "description": "Precise execution logic",
              "assignedRole": "ARCHITECT|NAVIGATOR|EXECUTOR|AUDITOR",
              "actionType": "ACTION_TYPE",
              "actionParameters": {"param1": "val1"}
            }
          ]
        }
        """.trimIndent()
    }

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    /**
     * Deconstructs a natural language goal into a structured plan graph.
     */
    suspend fun createPlan(
        apiKey: String,
        goal: String,
        model: String = "gemini-1.5-flash"
    ): Result<List<PlanStep>> = withContext(Dispatchers.IO) {
        try {
            val responseResult = geminiClient.generateStructuredContent(
                apiKey = apiKey,
                model = model,
                prompt = "Deconstruct this goal into an execution plan: $goal",
                systemInstruction = SYSTEM_PLANNER_PROMPT
            )

            responseResult.mapCatching { jsonText ->
                val cleanedJson = jsonText.substringAfter("{").substringBeforeLast("}")
                val parsed = json.decodeFromString<RawPlanResponseJson>("{$cleanedJson}")

                parsed.steps.map { raw ->
                    val role = try {
                        SubagentRole.valueOf(raw.assignedRole.uppercase())
                    } catch (e: Exception) {
                        SubagentRole.EXECUTOR
                    }

                    PlanStep(
                        stepId = raw.stepId,
                        title = raw.title,
                        description = raw.description,
                        assignedRole = role,
                        actionType = raw.actionType.uppercase(),
                        actionParameters = raw.actionParameters
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Plan generation failed", e)
            Result.failure(e)
        }
    }

    /**
     * Executes the generated plan sequentially, invoking auto-healing when a step encounters failure.
     */
    suspend fun executePlan(
        steps: List<PlanStep>,
        onStepProgress: ((PlanStep, Float) -> Unit)? = null
    ): OmniPlanReport = withContext(Dispatchers.Default) {
        val planId = UUID.randomUUID().toString()
        val results = mutableListOf<StepExecutionResult>()
        val totalSteps = steps.size

        for ((index, step) in steps.withIndex()) {
            val progress = (index.toFloat() / totalSteps.toFloat()).coerceIn(0f, 1f)
            onStepProgress?.invoke(step, progress)

            val startTime = System.currentTimeMillis()
            var stepSuccess = false
            var outputData: String? = null
            var errorMsg: String? = null

            try {
                val actionResult = actionRegistry.dispatchAction(step.actionType, step.actionParameters)
                if (actionResult.isSuccess) {
                    stepSuccess = true
                    outputData = actionResult.getOrNull()
                } else {
                    errorMsg = actionResult.exceptionOrNull()?.message ?: "Unknown action error"
                    // Trigger Auto-Healing
                    val diagnosis = autoHealManager.diagnose(step, errorMsg)
                    if (diagnosis.shouldRetry) {
                        Log.i(TAG, "AutoHealManager attempting remediation for Step ${step.stepId}: ${diagnosis.remedyPlan}")
                        val healedResult = actionRegistry.dispatchAction(step.actionType, diagnosis.adjustedParameters)
                        if (healedResult.isSuccess) {
                            stepSuccess = true
                            outputData = "Healed: " + healedResult.getOrNull()
                            errorMsg = null
                        }
                    }
                }
            } catch (e: Exception) {
                errorMsg = e.message
            }

            val duration = System.currentTimeMillis() - startTime
            results.add(
                StepExecutionResult(
                    stepId = step.stepId,
                    isSuccess = stepSuccess,
                    outputData = outputData,
                    errorMessage = errorMsg,
                    executionDurationMs = duration
                )
            )

            if (!stepSuccess) {
                Log.w(TAG, "Plan execution halted at step ${step.stepId}: $errorMsg")
                break
            }
        }

        val completedCount = results.count { it.isSuccess }
        OmniPlanReport(
            planId = planId,
            originalGoal = steps.firstOrNull()?.title.orEmpty(),
            totalSteps = totalSteps,
            completedSteps = completedCount,
            isAllSuccessful = completedCount == totalSteps,
            stepResults = results
        )
    }
}
