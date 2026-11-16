package com.brahma.assistant.planner

import kotlinx.serialization.Serializable

@Serializable
enum class SubagentRole {
    ARCHITECT,     // High-level strategy decomposition
    NAVIGATOR,     // Screen hierarchy traversal & accessibility locator
    EXECUTOR,      // Direct hardware/OS action dispatcher
    AUDITOR,       // Verification and post-condition validator
    HEALER         // Fault recovery and parameter readjustment
}

@Serializable
enum class StepStatus {
    PENDING,
    RUNNING,
    COMPLETED,
    FAILED,
    SKIPPED
}

@Serializable
data class PlanStep(
    val stepId: Int,
    val title: String,
    val description: String,
    val assignedRole: SubagentRole,
    val actionType: String,
    val actionParameters: Map<String, String> = emptyMap(),
    val status: StepStatus = StepStatus.PENDING
)

@Serializable
data class StepExecutionResult(
    val stepId: Int,
    val isSuccess: Boolean,
    val outputData: String? = null,
    val errorMessage: String? = null,
    val executionDurationMs: Long = 0L
)

@Serializable
data class OmniPlanReport(
    val planId: String,
    val originalGoal: String,
    val totalSteps: Int,
    val completedSteps: Int,
    val isAllSuccessful: Boolean,
    val stepResults: List<StepExecutionResult>
)
