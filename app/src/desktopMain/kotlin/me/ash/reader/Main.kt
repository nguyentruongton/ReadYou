package me.ash.reader

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyShortcut
import androidx.compose.ui.input.key.isAltPressed
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.isShiftPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.type
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.window.Tray
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.isTraySupported
import androidx.compose.ui.window.rememberWindowState
import me.ash.reader.Res
import me.ash.reader.ic_launcher_pure
import org.jetbrains.compose.resources.painterResource
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey
import coil3.ImageLoader
import coil3.SingletonImageLoader
import me.ash.reader.domain.service.AccountService
import me.ash.reader.infrastructure.di.commonModule
import me.ash.reader.infrastructure.di.platformModule
import me.ash.reader.infrastructure.platform.DesktopShortcutCoordinator
import me.ash.reader.infrastructure.platform.DesktopShortcutEvent
import me.ash.reader.infrastructure.platform.LocalDesktopShortcutCoordinator
import me.ash.reader.infrastructure.platform.SyncScheduler
import me.ash.reader.infrastructure.preference.AccountSettingsProvider
import me.ash.reader.infrastructure.preference.LocalDarkTheme
import me.ash.reader.infrastructure.preference.SettingsProvider
import me.ash.reader.ui.page.nav3.AppEntry
import me.ash.reader.ui.page.nav3.key.Route
import me.ash.reader.ui.theme.AppTheme
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin

@OptIn(coil3.annotation.DelicateCoilApi::class)
fun main() = application {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            modules(commonModule, platformModule)
        }
    }

    val koin = GlobalContext.get()
    val imageLoader = koin.get<ImageLoader>()
    SingletonImageLoader.setUnsafe(imageLoader)

    val settingsProvider = koin.get<SettingsProvider>()
    val accountService = koin.get<AccountService>()
    val shortcutCoordinator = koin.get<DesktopShortcutCoordinator>()
    val syncScheduler = koin.get<SyncScheduler>()

    var isWindowVisible by remember { mutableStateOf(true) }
    val windowState = rememberWindowState(size = DpSize(1200.dp, 800.dp))

    val appIcon = painterResource(Res.drawable.ic_launcher_pure)

    if (isTraySupported) {
        Tray(
            icon = appIcon,
            tooltip = "ReadYou",
            menu = {
                Item(
                    text = if (isWindowVisible) "Hide ReadYou" else "Show ReadYou",
                    onClick = { isWindowVisible = !isWindowVisible },
                )
                Item(
                    text = "Sync Now",
                    onClick = { syncScheduler.triggerImmediateSync() },
                )
                Separator()
                Item(
                    text = "Quit ReadYou",
                    onClick = ::exitApplication,
                )
            },
        )
    }

    Window(
        onCloseRequest = {
            if (isTraySupported) {
                isWindowVisible = false
            } else {
                exitApplication()
            }
        },
        visible = isWindowVisible,
        state = windowState,
        title = "ReadYou",
        icon = appIcon,
        onKeyEvent = { keyEvent ->
            val mapped = shortcutCoordinator.mapKeyEvent(
                key = keyEvent.key,
                type = keyEvent.type,
                isMetaPressed = keyEvent.isMetaPressed,
                isCtrlPressed = keyEvent.isCtrlPressed,
                isAltPressed = keyEvent.isAltPressed,
                isShiftPressed = keyEvent.isShiftPressed,
            )
            if (mapped != null) {
                shortcutCoordinator.tryEmit(mapped)
                true
            } else {
                false
            }
        },
    ) {
        MenuBar {
            Menu("File") {
                Item(
                    "Sync Now",
                    shortcut = KeyShortcut(Key.R, meta = true),
                    onClick = { syncScheduler.triggerImmediateSync() },
                )
                Item(
                    "Settings",
                    shortcut = KeyShortcut(Key.Comma, meta = true),
                    onClick = { shortcutCoordinator.tryEmit(DesktopShortcutEvent.OpenSettings) },
                )
                Separator()
                Item(
                    "Hide Window",
                    shortcut = KeyShortcut(Key.W, meta = true),
                    onClick = {
                        if (isTraySupported) {
                            isWindowVisible = false
                        }
                    },
                )
                Item(
                    "Quit ReadYou",
                    shortcut = KeyShortcut(Key.Q, meta = true),
                    onClick = ::exitApplication,
                )
            }
            Menu("View") {
                Item(
                    "Toggle Bionic Reading",
                    shortcut = KeyShortcut(Key.B),
                    onClick = { shortcutCoordinator.tryEmit(DesktopShortcutEvent.ToggleBionicReading) },
                )
            }
            Menu("Article") {
                Item(
                    "Next Article",
                    shortcut = KeyShortcut(Key.J),
                    onClick = { shortcutCoordinator.tryEmit(DesktopShortcutEvent.NextArticle) },
                )
                Item(
                    "Previous Article",
                    shortcut = KeyShortcut(Key.K),
                    onClick = { shortcutCoordinator.tryEmit(DesktopShortcutEvent.PreviousArticle) },
                )
                Item(
                    "Toggle Read / Unread",
                    shortcut = KeyShortcut(Key.M),
                    onClick = { shortcutCoordinator.tryEmit(DesktopShortcutEvent.ToggleRead) },
                )
                Item(
                    "Toggle Starred",
                    shortcut = KeyShortcut(Key.S),
                    onClick = { shortcutCoordinator.tryEmit(DesktopShortcutEvent.ToggleStarred) },
                )
                Separator()
                Item(
                    "Read Aloud / Stop TTS",
                    shortcut = KeyShortcut(Key.T),
                    onClick = { shortcutCoordinator.tryEmit(DesktopShortcutEvent.ToggleTts) },
                )
                Item(
                    "Open in Browser",
                    shortcut = KeyShortcut(Key.O),
                    onClick = { shortcutCoordinator.tryEmit(DesktopShortcutEvent.OpenInBrowser) },
                )
            }
        }

        CompositionLocalProvider(LocalDesktopShortcutCoordinator provides shortcutCoordinator) {
            AccountSettingsProvider(accountService = accountService) {
                settingsProvider.ProvidesSettings {
                    AppTheme(useDarkTheme = LocalDarkTheme.current.isDarkTheme()) {
                        val backStack = remember { NavBackStack<NavKey>(Route.Feeds) }
                        LaunchedEffect(shortcutCoordinator) {
                            shortcutCoordinator.events.collect { event ->
                                if (event == DesktopShortcutEvent.OpenSettings) {
                                    if (!backStack.contains(Route.Settings)) {
                                        backStack.add(Route.Settings)
                                    }
                                }
                            }
                        }
                        AppEntry(backStack = backStack)
                    }
                }
            }
        }
    }
}
