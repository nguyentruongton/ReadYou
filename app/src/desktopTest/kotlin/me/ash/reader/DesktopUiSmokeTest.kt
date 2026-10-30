package me.ash.reader

import me.ash.reader.domain.data.FilterStateUseCase
import me.ash.reader.domain.service.AccountService
import me.ash.reader.infrastructure.di.commonModule
import me.ash.reader.infrastructure.di.platformModule
import me.ash.reader.infrastructure.preference.SettingsProvider
import me.ash.reader.ui.page.adaptive.ArticleListReaderViewModel
import me.ash.reader.ui.page.home.feeds.FeedsViewModel
import me.ash.reader.ui.page.settings.accounts.AccountViewModel
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewModel
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class DesktopUiSmokeTest {

    @Test
    fun desktopKoinGraphResolvesUiServicesAndViewModels() {
        val app = startKoin {
            modules(commonModule, platformModule)
        }
        try {
            val koin = app.koin
            assertNotNull(koin.get<SettingsProvider>())
            assertNotNull(koin.get<AccountService>())
            assertNotNull(koin.get<FilterStateUseCase>())
            assertNotNull(koin.get<FeedsViewModel>())
            assertNotNull(koin.get<SubscribeViewModel>())
            assertNotNull(koin.get<AccountViewModel>())
            assertNotNull(koin.get<ArticleListReaderViewModel>())
        } finally {
            stopKoin()
        }
    }
}
