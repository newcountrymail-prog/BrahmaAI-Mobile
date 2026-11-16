package com.brahma.assistant.actions

import android.content.Context
import com.brahma.assistant.automation.BrahmaAccessibilityService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BrahmaActionRegistry(private val context: Context) {

    private val actions = mutableMapOf<String, BrahmaAction>()

    init {
        registerAction(AppLauncherAction(context))
        registerAction(SystemDiagnosticsAction(context))
        registerAction(DeviceSettingsAction(context))
        registerAction(MessagingAction(context))
        registerAction(BrowserAction(context))
        registerAction(com.brahma.assistant.code.CodingAgentAction(context))
        registerAction(BashCommandAction(context))
        registerAction(FileControllerAction(context))
    }

    fun registerAction(action: BrahmaAction) {
        actions[action.actionType.uppercase()] = action
    }

    suspend fun dispatchAction(actionType: String, parameters: Map<String, String>): Result<String> = withContext(Dispatchers.Default) {
        val upperType = actionType.uppercase()

        // Handle Accessibility UI Automation Actions dynamically
        if (upperType == "UI_CLICK" || upperType == "TAP_TEXT") {
            val service = BrahmaAccessibilityService.instance
                ?: return@withContext Result.failure(IllegalStateException("Accessibility Service is not enabled in Android Settings"))

            val targetText = parameters["targetText"]
                ?: return@withContext Result.failure(IllegalArgumentException("Missing 'targetText' parameter for UI_CLICK"))

            val success = service.findAndClickText(targetText)
            return@withContext if (success) {
                Result.success("Successfully tapped UI node matching: $targetText")
            } else {
                Result.failure(NoSuchElementException("UI node with text '$targetText' not found in active window"))
            }
        }

        val handler = actions[upperType]
            ?: return@withContext Result.failure(UnsupportedOperationException("Action type '$actionType' is not registered"))

        handler.execute(parameters)
    }

    fun getRegisteredActionTypes(): List<String> = actions.keys.toList()
}
