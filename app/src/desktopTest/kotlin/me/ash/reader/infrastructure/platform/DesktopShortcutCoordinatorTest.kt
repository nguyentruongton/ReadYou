package me.ash.reader.infrastructure.platform

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DesktopShortcutCoordinatorTest {

    private val coordinator = DesktopShortcutCoordinator()

    @Test
    fun `mapKeyEvent ignores KeyUp and maps standard single keys on KeyDown`() {
        assertNull(coordinator.mapKeyEvent(Key.J, KeyEventType.KeyUp))
        assertEquals(DesktopShortcutEvent.NextArticle, coordinator.mapKeyEvent(Key.J, KeyEventType.KeyDown))
        assertEquals(DesktopShortcutEvent.PreviousArticle, coordinator.mapKeyEvent(Key.K, KeyEventType.KeyDown))
        assertEquals(DesktopShortcutEvent.ToggleRead, coordinator.mapKeyEvent(Key.M, KeyEventType.KeyDown))
        assertEquals(DesktopShortcutEvent.ToggleStarred, coordinator.mapKeyEvent(Key.S, KeyEventType.KeyDown))
        assertEquals(DesktopShortcutEvent.Sync, coordinator.mapKeyEvent(Key.R, KeyEventType.KeyDown))
        assertEquals(DesktopShortcutEvent.ToggleBionicReading, coordinator.mapKeyEvent(Key.B, KeyEventType.KeyDown))
        assertEquals(DesktopShortcutEvent.OpenInBrowser, coordinator.mapKeyEvent(Key.O, KeyEventType.KeyDown))
        assertEquals(DesktopShortcutEvent.ToggleTts, coordinator.mapKeyEvent(Key.T, KeyEventType.KeyDown))
        assertEquals(DesktopShortcutEvent.EscapeBack, coordinator.mapKeyEvent(Key.Escape, KeyEventType.KeyDown))
    }

    @Test
    fun `mapKeyEvent maps Space to PageDown and Shift+Space to PageUp`() {
        assertEquals(DesktopShortcutEvent.PageDown, coordinator.mapKeyEvent(Key.Spacebar, KeyEventType.KeyDown))
        assertEquals(
            DesktopShortcutEvent.PageUp,
            coordinator.mapKeyEvent(Key.Spacebar, KeyEventType.KeyDown, isShiftPressed = true)
        )
    }

    @Test
    fun `mapKeyEvent maps Cmd+R and Ctrl+R to Sync and Cmd+Comma to OpenSettings while ignoring Cmd+C and Cmd+A`() {
        assertEquals(
            DesktopShortcutEvent.Sync,
            coordinator.mapKeyEvent(Key.R, KeyEventType.KeyDown, isMetaPressed = true)
        )
        assertEquals(
            DesktopShortcutEvent.Sync,
            coordinator.mapKeyEvent(Key.R, KeyEventType.KeyDown, isCtrlPressed = true)
        )
        assertEquals(
            DesktopShortcutEvent.OpenSettings,
            coordinator.mapKeyEvent(Key.Comma, KeyEventType.KeyDown, isMetaPressed = true)
        )
        assertNull(coordinator.mapKeyEvent(Key.C, KeyEventType.KeyDown, isMetaPressed = true))
        assertNull(coordinator.mapKeyEvent(Key.A, KeyEventType.KeyDown, isCtrlPressed = true))
    }

    @Test
    fun `tryEmit delivers event to subscribers`() = runTest {
        var received: DesktopShortcutEvent? = null
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            received = coordinator.events.first()
        }
        coordinator.tryEmit(DesktopShortcutEvent.NextArticle)
        assertEquals(DesktopShortcutEvent.NextArticle, received)
        job.cancel()
    }
}
