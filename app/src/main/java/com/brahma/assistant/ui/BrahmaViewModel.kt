package com.brahma.assistant.ui

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.brahma.assistant.actions.BrahmaActionRegistry
import com.brahma.assistant.ai.client.GeminiMobileClient
import com.brahma.assistant.ai.model.AIState
import com.brahma.assistant.ai.model.ChatMessage
import com.brahma.assistant.ai.model.MessageRole
import com.brahma.assistant.ai.preferences.PreferencesManager
import com.brahma.assistant.audio.SentenceChunker
import com.brahma.assistant.audio.VoiceManager
import com.brahma.assistant.heal.AutoHealManager
import com.brahma.assistant.memory.BrahmaChatHistoryStore
import com.brahma.assistant.memory.BrahmaMemoryCortex
import com.brahma.assistant.memory.BrahmaMemoryStore
import com.brahma.assistant.planner.OmniPlanner
import com.brahma.assistant.planner.OmniPlanReport
import com.brahma.assistant.planner.PlanStep
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class BrahmaViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        private const val TAG = "BrahmaViewModel"
    }

    val preferencesManager = PreferencesManager(application)
    private val geminiClient = GeminiMobileClient()
    private val memoryStore = BrahmaMemoryStore(application)
    private val historyStore = BrahmaChatHistoryStore(application)
    val memoryCortex = BrahmaMemoryCortex(memoryStore, historyStore)
    private val actionRegistry = BrahmaActionRegistry(application)
    private val autoHealManager = AutoHealManager(application)
    private val omniPlanner = OmniPlanner(geminiClient, actionRegistry, autoHealManager)
    val voiceManager = VoiceManager(application)

    private val _aiState = MutableStateFlow<AIState>(AIState.Idle)
    val aiState: StateFlow<AIState> = _aiState.asStateFlow()

    private val _toastEvents = MutableSharedFlow<String>()
    val toastEvents: SharedFlow<String> = _toastEvents.asSharedFlow()

    val chatMessages: StateFlow<List<ChatMessage>> = historyStore.historyFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val memoriesList = memoryStore.memoriesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Dispatches user prompt to Gemini with full SSE streaming and Memory Cortex integration.
     */
    fun sendPrompt(rawPrompt: String) {
        val trimmed = rawPrompt.trim()
        if (trimmed.isBlank()) return

        viewModelScope.launch(Dispatchers.IO) {
            val apiKey = preferencesManager.geminiApiKey.first()
            if (apiKey.isBlank()) {
                _aiState.value = AIState.Error("Gemini API key is not configured. Please set it in Settings.")
                return@launch
            }

            // Save user message to history
            val userMsg = ChatMessage(role = MessageRole.USER, text = trimmed)
            historyStore.appendMessage(userMsg)

            _aiState.value = AIState.Thinking(trimmed)

            try {
                // If the user request suggests an actionable workflow, trigger OmniPlanner
                val lower = trimmed.lowercase()
                if (lower.startsWith("plan:") || lower.startsWith("execute:") || lower.startsWith("automate:")) {
                    executeAutonomousPlan(apiKey, trimmed)
                    return@launch
                }

                // Synthesize enriched prompt with semantic memories & history
                val enriched = memoryCortex.buildEnrichedPrompt(trimmed)
                val selectedModel = preferencesManager.selectedModel.first()
                val voiceEnabled = preferencesManager.isVoiceFeedbackEnabled.first()

                val responseAccumulator = StringBuilder()
                val chunker = SentenceChunker()
                _aiState.value = AIState.Streaming("")

                geminiClient.streamGenerateContent(
                    apiKey = apiKey,
                    model = selectedModel,
                    prompt = enriched,
                    systemInstruction = "You are Brahma AI, an elite autonomous mobile systems intelligence. Be concise, hyper-competent, and authoritative."
                ).collect { chunk ->
                    responseAccumulator.append(chunk)
                    _aiState.value = AIState.Streaming(responseAccumulator.toString())

                    if (voiceEnabled) {
                        val sentences = chunker.processChunk(chunk)
                        sentences.forEach { sentence ->
                            voiceManager.speak(sentence, android.speech.tts.TextToSpeech.QUEUE_ADD)
                        }
                    }
                }

                if (voiceEnabled) {
                    chunker.flushRemaining()?.let {
                        voiceManager.speak(it, android.speech.tts.TextToSpeech.QUEUE_ADD)
                    }
                }

                val finalResponse = responseAccumulator.toString()
                val assistantMsg = ChatMessage(role = MessageRole.ASSISTANT, text = finalResponse)
                historyStore.appendMessage(assistantMsg)

                // Consolidate memory facts in background
                memoryCortex.analyzeAndConsolidate(trimmed, finalResponse)

                _aiState.value = AIState.Success(finalResponse)
            } catch (e: Exception) {
                Log.e(TAG, "Streaming request failed", e)
                _aiState.value = AIState.Error(e.message ?: "Network communication error")
            }
        }
    }

    private suspend fun executeAutonomousPlan(apiKey: String, goal: String) {
        val selectedModel = preferencesManager.selectedModel.first()
        _aiState.value = AIState.Thinking("Deconstructing goal into execution graph...")

        val planResult = omniPlanner.createPlan(apiKey, goal, selectedModel)
        if (planResult.isFailure) {
            _aiState.value = AIState.Error("Planner decomposition failed: ${planResult.exceptionOrNull()?.message}")
            return
        }

        val steps = planResult.getOrThrow()
        val report = omniPlanner.executePlan(steps) { currentStep, progress ->
            _aiState.value = AIState.ExecutingPlan("Step ${currentStep.stepId}: ${currentStep.title}", progress)
        }

        val summary = buildString {
            appendLine("Plan Execution Report:")
            appendLine("Steps Completed: ${report.completedSteps}/${report.totalSteps}")
            report.stepResults.forEach { res ->
                val icon = if (res.isSuccess) "✓" else "✗"
                appendLine("$icon Step ${res.stepId}: ${res.outputData ?: res.errorMessage}")
            }
        }

        historyStore.appendMessage(ChatMessage(role = MessageRole.ASSISTANT, text = summary))
        _aiState.value = AIState.Success(summary)
    }

    fun clearChat() {
        viewModelScope.launch(Dispatchers.IO) {
            historyStore.clearHistory()
            _aiState.value = AIState.Idle
        }
    }

    override fun onCleared() {
        super.onCleared()
        voiceManager.shutdown()
    }
}
