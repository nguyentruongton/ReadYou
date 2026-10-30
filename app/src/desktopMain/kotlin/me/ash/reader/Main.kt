package me.ash.reader

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.savedstate.serialization.SavedStateConfiguration
import coil3.ImageLoader
import coil3.SingletonImageLoader
import me.ash.reader.domain.service.AccountService
import me.ash.reader.infrastructure.di.commonModule
import me.ash.reader.infrastructure.di.platformModule
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

    val windowState = rememberWindowState(size = DpSize(1200.dp, 800.dp))

    Window(
        onCloseRequest = ::exitApplication,
        state = windowState,
        title = "ReadYou",
    ) {
        AccountSettingsProvider(accountService = accountService) {
            settingsProvider.ProvidesSettings {
                AppTheme(useDarkTheme = LocalDarkTheme.current.isDarkTheme()) {
                    val backStack = rememberNavBackStack(SavedStateConfiguration.DEFAULT, Route.Feeds as NavKey)
                    AppEntry(backStack = backStack)
                }
            }
        }
    }
}
