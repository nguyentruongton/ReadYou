package me.ash.reader.infrastructure.platform

import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

enum class DesktopShortcutEvent {
    NextArticle,
    PreviousArticle,
    ToggleRead,
    ToggleStarred,
    Sync,
    ToggleBionicReading,
    OpenInBrowser,
    ToggleTts,
    PageDown,
    PageUp,
    EscapeBack,
    OpenSettings,
}

class DesktopShortcutCoordinator {
    private val _events = MutableSharedFlow<DesktopShortcutEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<DesktopShortcutEvent> = _events.asSharedFlow()

    fun tryEmit(event: DesktopShortcutEvent): Boolean = _events.tryEmit(event)

    fun mapKeyEvent(
        key: Key,
        type: KeyEventType,
        isMetaPressed: Boolean = false,
        isCtrlPressed: Boolean = false,
        isAltPressed: Boolean = false,
        isShiftPressed: Boolean = false,
    ): DesktopShortcutEvent? {
        if (type != KeyEventType.KeyDown) return null

        val isCommandOrControl = isMetaPressed || isCtrlPressed

        if (isCommandOrControl) {
            return when (key) {
                Key.R -> DesktopShortcutEvent.Sync
                Key.Comma -> DesktopShortcutEvent.OpenSettings
                else -> null
            }
        }

        if (isAltPressed) return null

        return when (key) {
            Key.J -> DesktopShortcutEvent.NextArticle
            Key.K -> DesktopShortcutEvent.PreviousArticle
            Key.M -> DesktopShortcutEvent.ToggleRead
            Key.S -> DesktopShortcutEvent.ToggleStarred
            Key.R -> DesktopShortcutEvent.Sync
            Key.B -> DesktopShortcutEvent.ToggleBionicReading
            Key.O, Key.V -> DesktopShortcutEvent.OpenInBrowser
            Key.T -> DesktopShortcutEvent.ToggleTts
            Key.Escape -> DesktopShortcutEvent.EscapeBack
            Key.Spacebar ->
                if (isShiftPressed) DesktopShortcutEvent.PageUp else DesktopShortcutEvent.PageDown
            else -> null
        }
    }
}

val LocalDesktopShortcutCoordinator = staticCompositionLocalOf { DesktopShortcutCoordinator() }
