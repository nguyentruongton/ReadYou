package me.ash.reader.infrastructure.platform

import kotlinx.coroutines.flow.StateFlow

interface TextToSpeechManager {
    sealed interface State {
        object Idle : State
        object Preparing : State
        class Reading(val current: Int = 0, val total: Int = 100) : State {
            val progress: Float
                get() = if (total > 0) current.toFloat() / total else 0f
        }
        object Error : State
    }

    val stateFlow: StateFlow<State>
    var state: State
    fun readHtml(htmlContent: String)
    fun stop()
}
