package com.brahma.assistant.heal

import com.brahma.assistant.planner.PlanStep

enum class FailureCategory {
    NODE_NOT_FOUND,
    APP_NOT_INSTALLED,
    PERMISSION_DENIED,
    NETWORK_TIMEOUT,
    STALE_STATE,
    INVALID_PARAMETERS,
    UNKNOWN
}

data class DiagnosisResult(
    val category: FailureCategory,
    val failureCause: String,
    val shouldRetry: Boolean,
    val adjustedParameters: Map<String, String>,
    val remedyPlan: String
)

class ExecutionReflectionEngine {
    fun reflectOnFailure(step: PlanStep, rawError: String): DiagnosisResult {
        val lowerError = rawError.lowercase()

        return when {
            lowerError.contains("not installed") || lowerError.contains("namenotfound") -> {
                DiagnosisResult(
                    category = FailureCategory.APP_NOT_INSTALLED,
                    failureCause = "Target application is missing from device runtime.",
                    shouldRetry = false,
                    adjustedParameters = step.actionParameters,
                    remedyPlan = "Notify user to install package or fallback to web interface."
                )
            }
            lowerError.contains("permission") || lowerError.contains("securityexception") -> {
                DiagnosisResult(
                    category = FailureCategory.PERMISSION_DENIED,
                    failureCause = "Missing runtime OS permission.",
                    shouldRetry = false,
                    adjustedParameters = step.actionParameters,
                    remedyPlan = "Request permission prompt on UI thread."
                )
            }
            lowerError.contains("timeout") || lowerError.contains("socket") -> {
                DiagnosisResult(
                    category = FailureCategory.NETWORK_TIMEOUT,
                    failureCause = "Transient network glitch.",
                    shouldRetry = true,
                    adjustedParameters = step.actionParameters,
                    remedyPlan = "Execute exponential backoff retry."
                )
            }
            lowerError.contains("node") || lowerError.contains("not found") -> {
                // Adjust parameter: fallback from exact match to partial or lower-case match
                val targetText = step.actionParameters["targetText"] ?: ""
                val adjusted = step.actionParameters.toMutableMap()
                adjusted["targetText"] = targetText.lowercase()
                adjusted["fuzzyMatch"] = "true"

                DiagnosisResult(
                    category = FailureCategory.NODE_NOT_FOUND,
                    failureCause = "Exact accessibility node text not found in current window.",
                    shouldRetry = true,
                    adjustedParameters = adjusted,
                    remedyPlan = "Enable fuzzy matching and re-traverse active accessibility window."
                )
            }
            else -> {
                DiagnosisResult(
                    category = FailureCategory.UNKNOWN,
                    failureCause = rawError,
                    shouldRetry = false,
                    adjustedParameters = step.actionParameters,
                    remedyPlan = "Log diagnostic trace to disk."
                )
            }
        }
    }
}
