package me.ash.reader.domain.service

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.runBlocking
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.group.Group
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.infrastructure.preference.SettingsProvider
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.getDefaultGroupId
import me.ash.reader.ui.ext.put
import me.ash.reader.ui.ext.spacerDollar

class AccountService(
    private val dataStore: DataStore<Preferences>,
    private val accountDao: AccountDao,
    private val groupDao: GroupDao,
    private val feedDao: FeedDao,
    private val articleDao: ArticleDao,
    private val coroutineScope: CoroutineScope,
    settingsProvider: SettingsProvider,
) {

    constructor(
        context: Any?,
        accountDao: AccountDao,
        groupDao: GroupDao,
        feedDao: FeedDao,
        articleDao: ArticleDao,
        coroutineScope: CoroutineScope,
        settingsProvider: SettingsProvider,
    ) : this(
        org.koin.core.context.GlobalContext.get().get(),
        accountDao,
        groupDao,
        feedDao,
        articleDao,
        coroutineScope,
        settingsProvider,
    )

    private val accountIdKey = intPreferencesKey(DataStoreKey.currentAccountId)

    val currentAccountIdFlow =
        settingsProvider.preferencesFlow
            .map { it[accountIdKey] }
            .stateIn(scope = coroutineScope, started = SharingStarted.Eagerly, initialValue = null)

    val currentAccountFlow =
        currentAccountIdFlow
            .combine(getAccounts()) { id, accounts ->
                id?.let { accounts.firstOrNull { it.id == id } }
            }
            .stateIn(scope = coroutineScope, SharingStarted.Eagerly, initialValue = null)

    fun getAccounts(): Flow<List<Account>> = accountDao.queryAllAsFlow()

    fun getAccountFlowById(accountId: Int): Flow<Account?> = accountDao.queryAccount(accountId)

    suspend fun getAccountById(accountId: Int): Account? = accountDao.queryById(accountId)

    fun getCurrentAccount(): Account = runBlocking {
        currentAccountFlow.first { it != null } as Account
    }

    fun getCurrentAccountId(): Int = runBlocking {
        currentAccountIdFlow.first { it != null } as Int
    }

    suspend fun isNoAccount(): Boolean = accountDao.queryAll().isEmpty()

    suspend fun addAccount(account: Account): Account {
        val id = accountDao.insert(account).toInt()
        return account.copy(id = id).also {
            when (it.type) {
                AccountType.Local -> {
                    groupDao.insert(
                        Group(
                            id = it.id!!.getDefaultGroupId(),
                            name = "Default",
                            accountId = it.id!!,
                        )
                    )
                }
            }
            dataStore.put(DataStoreKey.currentAccountId, it.id!!)
            dataStore.put(DataStoreKey.currentAccountType, it.type.id)
        }
    }

    private fun getDefaultAccount(): Account =
        Account(type = AccountType.Local, name = "Read You")

    private suspend fun addDefaultAccount(): Account = addAccount(getDefaultAccount())

    suspend fun initWithDefaultAccount() {
        val account = addDefaultAccount()
        val group = getDefaultGroup()
        val initialFeed = getInitialFeed(account, group)
        feedDao.insert(initialFeed)
    }

    private fun getInitialFeed(account: Account, group: Group): Feed =
        Feed(
            id = account.id!!.spacerDollar(UUID.randomUUID().toString()),
            name = "ReadYou Releases",
            icon = "https://github.com/ReadYouApp.png",
            url = "https://github.com/ReadYouApp/ReadYou/releases.atom",
            groupId = group.id,
            accountId = account.id,
        )

    fun getDefaultGroup(): Group =
        getCurrentAccountId().let {
            Group(
                id = it.getDefaultGroupId(),
                name = "Default",
                accountId = it,
            )
        }

    suspend fun update(accountId: Int, block: Account.() -> Account) {
        accountDao.queryById(accountId)?.let { accountDao.update(it.run(block)) }
    }

    suspend fun update(account: Account) = accountDao.update(account)

    suspend fun delete(accountId: Int) {
        if (accountDao.queryAll().size <= 1) {
            return
        }
        accountDao.queryById(accountId)?.let {
            articleDao.deleteByAccountId(accountId)
            feedDao.deleteByAccountId(accountId)
            groupDao.deleteByAccountId(accountId)
            accountDao.delete(it)
            accountDao.queryAll().getOrNull(0)?.let { firstRemaining ->
                dataStore.put(DataStoreKey.currentAccountId, firstRemaining.id!!)
                dataStore.put(DataStoreKey.currentAccountType, firstRemaining.type.id)
            }
        }
    }

    suspend fun switch(account: Account) {
        dataStore.put(DataStoreKey.currentAccountId, account.id!!)
        dataStore.put(DataStoreKey.currentAccountType, account.type.id)
    }
}
