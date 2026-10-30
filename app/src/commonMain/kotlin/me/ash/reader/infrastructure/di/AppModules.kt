package me.ash.reader.infrastructure.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.ash.reader.domain.data.ArticlePagingListUseCase
import me.ash.reader.domain.data.DiffMapHolder
import me.ash.reader.domain.data.FilterStateUseCase
import me.ash.reader.domain.data.GroupWithFeedsListUseCase
import me.ash.reader.domain.data.SyncLogger
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.domain.service.AccountService
import me.ash.reader.domain.service.AppService
import me.ash.reader.domain.service.FeverRssService
import me.ash.reader.domain.service.GoogleReaderRssService
import me.ash.reader.domain.service.LocalRssService
import me.ash.reader.domain.service.OpmlService
import me.ash.reader.domain.service.RssService
import me.ash.reader.infrastructure.datastore.createPreferenceDataStore
import me.ash.reader.infrastructure.db.AndroidDatabase
import me.ash.reader.infrastructure.net.NetworkDataSource
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
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

expect val platformModule: Module

val commonModule = module {
    single<CoroutineDispatcher>(named(DEFAULT_DISPATCHER)) { Dispatchers.Default }
    single<CoroutineDispatcher>(named(IO_DISPATCHER)) { Dispatchers.IO }
    single<CoroutineDispatcher>(named(MAIN_DISPATCHER)) { Dispatchers.Main }
    single<CoroutineDispatcher>(named(MAIN_IMMEDIATE_DISPATCHER)) { Dispatchers.Main.immediate }
    single<CoroutineScope>(named(APPLICATION_SCOPE)) {
        CoroutineScope(SupervisorJob() + get<CoroutineDispatcher>(named(DEFAULT_DISPATCHER)))
    }
    single<AccountDao> { get<AndroidDatabase>().accountDao() }
    single<FeedDao> { get<AndroidDatabase>().feedDao() }
    single<ArticleDao> { get<AndroidDatabase>().articleDao() }
    single<GroupDao> { get<AndroidDatabase>().groupDao() }
    single<DataStore<Preferences>> {
        createPreferenceDataStore(get(), get(named(APPLICATION_SCOPE)))
    }
    single<NetworkDataSource> { NetworkDataSource.getInstance() }
    single {
        SettingsProvider(
            get<DataStore<Preferences>>(),
            get(named(APPLICATION_SCOPE)),
            get(named(IO_DISPATCHER)),
        )
    }
    single { OPMLDataSource(get(named(IO_DISPATCHER))) }
    single { RssHelper(get(), get(named(IO_DISPATCHER)), get()) }
    single { ReaderCacheHelper(get(), get(named(IO_DISPATCHER)), get(), get()) }
    single { SyncLogger(get(), get(named(APPLICATION_SCOPE))) }
    single {
        AccountService(
            get(), get(), get(), get(), get(), get(named(APPLICATION_SCOPE)), get()
        )
    }
    single {
        LocalRssService(
            get(), get(), get(), get(), get(), get(named(IO_DISPATCHER)), get(named(DEFAULT_DISPATCHER)), get(), get(), get()
        )
    }
    single {
        FeverRssService(
            get(), get(), get(), get(), get(), get(named(IO_DISPATCHER)), get(named(MAIN_DISPATCHER)), get(named(DEFAULT_DISPATCHER)), get(), get()
        )
    }
    single {
        GoogleReaderRssService(
            get(), get(), get(), get(), get(), get(named(IO_DISPATCHER)), get(named(MAIN_DISPATCHER)), get(named(DEFAULT_DISPATCHER)), get(), get(), get()
        )
    }
    single {
        OpmlService(
            get(), get(), get(), get(), get(), get(named(IO_DISPATCHER))
        )
    }
    single {
        AppService(
            get(), get(), get(named(IO_DISPATCHER)), get(named(MAIN_DISPATCHER)), get()
        )
    }
    single {
        RssService(
            get(named(APPLICATION_SCOPE)), get(), get(), get(), get()
        )
    }
    single {
        DiffMapHolder(
            get(), get(named(APPLICATION_SCOPE)), get(named(IO_DISPATCHER)), get(), get()
        )
    }
    single {
        FilterStateUseCase(
            get(), get(), get(), get(named(APPLICATION_SCOPE))
        )
    }
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
    viewModel { SubscribeViewModel(get(), get(), get(), get(), get(named(APPLICATION_SCOPE)), get()) }
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
    viewModel { GroupOptionViewModel(get(), get(named(MAIN_DISPATCHER)), get(named(IO_DISPATCHER)), get(named(APPLICATION_SCOPE))) }
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
    viewModel { UpdateViewModel(get()) }
}
