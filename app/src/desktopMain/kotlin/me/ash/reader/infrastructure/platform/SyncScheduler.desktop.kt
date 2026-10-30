package me.ash.reader.infrastructure.platform

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.account.Account

class DesktopSyncScheduler(
    private val coroutineScope: CoroutineScope,
    private val onSyncRequest: suspend (accountId: Int?, feedId: String?, groupId: String?) -> Unit = { _, _, _ -> },
) : SyncScheduler {

    private val _isSyncing = MutableStateFlow(false)
    override val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    private var activeSyncJob: Job? = null
    private var periodicSyncJob: Job? = null

    private suspend fun executeSync(accountId: Int?, feedId: String?, groupId: String?) {
        _isSyncing.value = true
        try {
            onSyncRequest(accountId, feedId, groupId)
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            _isSyncing.value = false
        }
    }

    override fun triggerImmediateSync(accountId: Int?, feedId: String?, groupId: String?) {
        activeSyncJob?.cancel()
        activeSyncJob = coroutineScope.launch {
            executeSync(accountId, feedId, groupId)
        }
    }

    fun schedulePeriodicSync(accountId: Int?, intervalMinutes: Long) {
        periodicSyncJob?.cancel()
        if (intervalMinutes <= 0L) return

        periodicSyncJob = coroutineScope.launch {
            while (isActive) {
                delay(intervalMinutes * 60_000L)
                executeSync(accountId, null, null)
            }
        }
    }

    override fun cancelSync() {
        activeSyncJob?.cancel()
        periodicSyncJob?.cancel()
        _isSyncing.value = false
    }

    override fun initSync(account: Account) {
        schedulePeriodicSync(account.id, account.syncInterval.value)
        if (account.syncOnStart.value) {
            triggerImmediateSync(account.id)
        }
    }
}
