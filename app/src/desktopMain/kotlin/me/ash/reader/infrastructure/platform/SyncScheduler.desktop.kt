package me.ash.reader.infrastructure.platform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.account.Account

class DesktopSyncScheduler(
    private val coroutineScope: CoroutineScope,
    private val onSyncRequest: suspend (accountId: Int?, feedId: String?, groupId: String?) -> Unit = { _, _, _ -> },
) : SyncScheduler {

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    override fun triggerImmediateSync(accountId: Int?, feedId: String?, groupId: String?) {
        coroutineScope.launch {
            _isSyncing.value = true
            try {
                onSyncRequest(accountId, feedId, groupId)
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isSyncing.value = false
            }
        }
    }

    override fun cancelSync() {
        _isSyncing.value = false
    }

    override fun initSync(account: Account) {
        if (account.syncOnStart.value) {
            triggerImmediateSync(account.id)
        }
    }
}
