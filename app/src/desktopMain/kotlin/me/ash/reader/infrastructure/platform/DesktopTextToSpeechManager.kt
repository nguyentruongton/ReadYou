package me.ash.reader.infrastructure.platform

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DesktopTextToSpeechManager : TextToSpeechManager {
    private val _stateFlow = MutableStateFlow<TextToSpeechManager.State>(TextToSpeechManager.State.Idle)
    override val stateFlow: StateFlow<TextToSpeechManager.State> = _stateFlow.asStateFlow()

    override var state: TextToSpeechManager.State
        get() = _stateFlow.value
        set(value) {
            _stateFlow.value = value
        }

    override fun readHtml(htmlContent: String) {
        // Desktop TTS no-op
    }

    override fun stop() {
        state = TextToSpeechManager.State.Idle
    }
}
