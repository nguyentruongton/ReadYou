package me.ash.reader.infrastructure.platform

import me.ash.reader.domain.model.account.Account

interface SyncScheduler {
    fun triggerImmediateSync(accountId: Int? = null, feedId: String? = null, groupId: String? = null)
    fun cancelSync()
    fun initSync(account: Account)
}
