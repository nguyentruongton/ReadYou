package me.ash.reader.infrastructure.platform

import kotlinx.coroutines.flow.StateFlow

interface PlatformTts {
    sealed interface State {
        object Idle : State
        object Preparing : State
        data class Reading(val progress: Float) : State
        object Error : State
    }

    val stateFlow: StateFlow<State>
    fun speak(text: String, title: String? = null)
    fun stop()
    fun release()
    fun openSettings()
}
