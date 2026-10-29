package me.ash.reader.infrastructure.di

import androidx.work.WorkManager
import coil.ImageLoader
import me.ash.reader.BuildConfig
import me.ash.reader.domain.data.ArticlePagingListUseCase
import me.ash.reader.domain.data.DiffMapHolder
import me.ash.reader.domain.data.FilterStateUseCase
import me.ash.reader.domain.data.GroupWithFeedsListUseCase
import me.ash.reader.domain.data.SyncLogger
import me.ash.reader.domain.service.AccountService
import me.ash.reader.domain.service.AppService
import me.ash.reader.domain.service.FeverRssService
import me.ash.reader.domain.service.GoogleReaderRssService
import me.ash.reader.domain.service.LocalRssService
import me.ash.reader.domain.service.OpmlService
import me.ash.reader.domain.service.ReaderWorker
import me.ash.reader.domain.service.RssService
import me.ash.reader.domain.service.SyncWorker
import me.ash.reader.domain.service.WidgetUpdateWorker
import me.ash.reader.infrastructure.android.AndroidImageDownloader
import me.ash.reader.infrastructure.android.AndroidStringsHelper
import me.ash.reader.infrastructure.android.NotificationHelper
import me.ash.reader.infrastructure.android.TextToSpeechManager
import me.ash.reader.infrastructure.db.AndroidDatabase
import me.ash.reader.infrastructure.db.createAndroidDatabase
import me.ash.reader.infrastructure.net.createOkHttpClient
import me.ash.reader.infrastructure.platform.AppPaths
import me.ash.reader.infrastructure.platform.createAndroidAppPaths
import me.ash.reader.infrastructure.preference.SettingsProvider
import me.ash.reader.infrastructure.rss.OPMLDataSource
import me.ash.reader.infrastructure.rss.ReaderCacheHelper
import me.ash.reader.infrastructure.rss.RssHelper
import me.ash.reader.ui.page.adaptive.ArticleListReaderViewModel
import me.ash.reader.ui.page.home.feeds.FeedsViewModel
import me.ash.reader.ui.page.home.feeds.drawer.feed.FeedOptionViewModel
import me.ash.reader.ui.page.home.feeds.drawer.group.GroupOptionViewModel
import me.ash.reader.ui.page.home.feeds.subscribe.SubscribeViewModel
import me.ash.reader.ui.page.settings.accounts.AccountViewModel
import me.ash.reader.ui.page.settings.accounts.addition.AdditionViewModel
import me.ash.reader.ui.page.settings.tips.UpdateViewModel
import me.ash.reader.ui.page.settings.troubleshooting.TroubleshootingViewModel
import me.ash.reader.ui.widget.WidgetRepository
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.androidx.workmanager.dsl.worker
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<AppPaths> { createAndroidAppPaths(androidContext()) }
    single<AndroidDatabase> { createAndroidDatabase(androidContext()) }
    single<OkHttpClient> {
        createOkHttpClient(
            get<AppPaths>(),
            BuildConfig.USER_AGENT_STRING
        )
    }
    single<ImageLoader> { provideImageLoader(androidContext(), get()) }
    single<WorkManager> { WorkManager.getInstance(androidContext()) }

    singleOf(::AndroidStringsHelper)
    single { NotificationHelper(androidContext(), get(named(IO_DISPATCHER)), get(named(APPLICATION_SCOPE))) }
    single { TextToSpeechManager(androidContext(), get(named(APPLICATION_SCOPE))) }
    single { AndroidImageDownloader(androidContext(), get(named(IO_DISPATCHER)), get()) }
    single { SettingsProvider(androidContext(), get(named(APPLICATION_SCOPE)), get(named(IO_DISPATCHER))) }
    single { OPMLDataSource(androidContext(), get(named(IO_DISPATCHER))) }
    single { RssHelper(androidContext(), get(named(IO_DISPATCHER)), get()) }
    single { ReaderCacheHelper(androidContext(), get(named(IO_DISPATCHER)), get(), get()) }
    single { SyncLogger(androidContext(), get(named(APPLICATION_SCOPE))) }
    single { AccountService(androidContext(), get(), get(), get(), get(), get(named(APPLICATION_SCOPE)), get()) }
    single {
        LocalRssService(
            androidContext(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(named(IO_DISPATCHER)),
            get(named(DEFAULT_DISPATCHER)),
            get(),
            get(),
            get(),
        )
    }
    single {
        FeverRssService(
            androidContext(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(named(IO_DISPATCHER)),
            get(named(MAIN_DISPATCHER)),
            get(named(DEFAULT_DISPATCHER)),
            get(),
            get(),
        )
    }
    single {
        GoogleReaderRssService(
            androidContext(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(named(IO_DISPATCHER)),
            get(named(MAIN_DISPATCHER)),
            get(named(DEFAULT_DISPATCHER)),
            get(),
            get(),
            get(),
        )
    }
    single { OpmlService(androidContext(), get(), get(), get(), get(), get(), get(named(IO_DISPATCHER))) }
    single { AppService(androidContext(), get(), get(named(IO_DISPATCHER)), get(named(MAIN_DISPATCHER))) }
    single { RssService(get(named(APPLICATION_SCOPE)), get(), get(), get(), get()) }
    single { DiffMapHolder(androidContext(), get(named(APPLICATION_SCOPE)), get(named(IO_DISPATCHER)), get(), get()) }
    single { FilterStateUseCase(get(), get(), get(), get(named(APPLICATION_SCOPE))) }
    single {
        GroupWithFeedsListUseCase(
            get(named(APPLICATION_SCOPE)),
            get(named(IO_DISPATCHER)),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    single {
        ArticlePagingListUseCase(
            get(),
            get(),
            get(named(APPLICATION_SCOPE)),
            get(named(IO_DISPATCHER)),
            get(),
            get(),
            get(),
        )
    }
    single { WidgetRepository(androidContext(), get(named(APPLICATION_SCOPE)), get(), get(), get(), get()) }

    worker { SyncWorker(get(), get(), get(), get(), get()) }
    worker { ReaderWorker(get(), get(), get(), get()) }
    worker { WidgetUpdateWorker(get(), get(), get()) }

    viewModel {
        TroubleshootingViewModel(
            get(),
            get(),
            get(),
            get(named(IO_DISPATCHER)),
            get(named(DEFAULT_DISPATCHER)),
            get(named(MAIN_DISPATCHER)),
            get(named(APPLICATION_SCOPE)),
            get(),
            get(),
        )
    }
    viewModel {
        AccountViewModel(
            get(),
            get(),
            get(),
            get(named(IO_DISPATCHER)),
            get(named(DEFAULT_DISPATCHER)),
            get(named(MAIN_DISPATCHER)),
            get(named(APPLICATION_SCOPE)),
        )
    }
    viewModel { AdditionViewModel(get(), get(), get(), get()) }
    viewModel { UpdateViewModel(get()) }
    viewModel { GroupOptionViewModel(get(), get(named(MAIN_DISPATCHER)), get(named(IO_DISPATCHER)), get(named(APPLICATION_SCOPE))) }
    viewModel {
        FeedOptionViewModel(
            get(),
            get(named(MAIN_DISPATCHER)),
            get(named(IO_DISPATCHER)),
            get(named(APPLICATION_SCOPE)),
            get(),
            get(),
        )
    }
    viewModel { SubscribeViewModel(get(), get(), get(), get(), get(named(APPLICATION_SCOPE)), get()) }
    viewModel {
        FeedsViewModel(
            get(),
            get(),
            get(),
            get(),
            get(named(DEFAULT_DISPATCHER)),
            get(named(IO_DISPATCHER)),
            get(named(APPLICATION_SCOPE)),
            get(),
            get(),
            get(),
            get(),
        )
    }
    viewModel {
        ArticleListReaderViewModel(
            get(),
            get(named(IO_DISPATCHER)),
            get(named(APPLICATION_SCOPE)),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
}
