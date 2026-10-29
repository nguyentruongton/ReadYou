package me.ash.reader.infrastructure.platform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.account.Account

class DesktopSyncScheduler(
    private val coroutineScope: CoroutineScope,
    private val onSyncRequest: suspend (accountId: Int?, feedId: String?, groupId: String?) -> Unit = { _, _, _ -> },
) : SyncScheduler {
    override fun triggerImmediateSync(accountId: Int?, feedId: String?, groupId: String?) {
        coroutineScope.launch {
            try {
                onSyncRequest(accountId, feedId, groupId)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override fun cancelSync() {
        // No-op on desktop
    }

    override fun initSync(account: Account) {
        if (account.syncOnStart.value) {
            triggerImmediateSync(account.id)
        }
    }
}
