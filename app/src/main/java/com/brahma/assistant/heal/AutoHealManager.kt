package com.brahma.assistant.heal

import android.content.Context
import android.util.Log
import com.brahma.assistant.planner.PlanStep

class AutoHealManager(
    private val context: Context,
    private val reflectionEngine: ExecutionReflectionEngine = ExecutionReflectionEngine()
) {
    companion object {
        private const val TAG = "AutoHealManager"
    }

    fun diagnose(step: PlanStep, rawError: String): DiagnosisResult {
        Log.d(TAG, "Diagnosing failure for step ${step.stepId} (${step.actionType}): $rawError")
        return reflectionEngine.reflectOnFailure(step, rawError)
    }
}
