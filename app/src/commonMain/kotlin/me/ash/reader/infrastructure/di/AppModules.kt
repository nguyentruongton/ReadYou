package me.ash.reader.infrastructure.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.infrastructure.datastore.createPreferenceDataStore
import me.ash.reader.infrastructure.db.AndroidDatabase
import me.ash.reader.infrastructure.net.NetworkDataSource
import org.koin.core.module.Module
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
}
