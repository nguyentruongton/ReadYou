package me.ash.reader.infrastructure.platform

import kotlinx.coroutines.flow.StateFlow
import me.ash.reader.domain.model.account.Account

interface SyncScheduler {
    val isSyncing: StateFlow<Boolean>
    fun triggerImmediateSync(accountId: Int? = null, feedId: String? = null, groupId: String? = null)
    fun cancelSync()
    fun initSync(account: Account)
}
