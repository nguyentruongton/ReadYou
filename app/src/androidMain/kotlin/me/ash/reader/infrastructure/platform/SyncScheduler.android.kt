package me.ash.reader.infrastructure.platform

import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.service.SyncWorker
import me.ash.reader.domain.service.WidgetUpdateWorker
import me.ash.reader.infrastructure.preference.SyncIntervalPreference

class AndroidSyncScheduler(
    private val workManager: WorkManager,
    coroutineScope: CoroutineScope,
) : SyncScheduler {

    override val isSyncing: StateFlow<Boolean> =
        workManager
            .getWorkInfosByTagFlow(SyncWorker.SYNC_TAG)
            .map { it.any { workInfo -> workInfo.state == WorkInfo.State.RUNNING } }
            .stateIn(coroutineScope, SharingStarted.Eagerly, false)

    override fun triggerImmediateSync(accountId: Int?, feedId: String?, groupId: String?) {
        if (accountId != null) {
            SyncWorker.enqueueOneTimeWork(
                workManager,
                workDataOf("accountId" to accountId, "feedId" to feedId, "groupId" to groupId)
            )
        } else {
            SyncWorker.enqueueOneTimeWork(workManager)
        }
    }

    override fun cancelSync() {
        SyncWorker.cancelPeriodicWork(workManager)
        SyncWorker.cancelOneTimeWork(workManager)
    }

    override fun initSync(account: Account) {
        if (account.syncOnStart.value) {
            triggerImmediateSync(account.id)
        }
        if (account.syncInterval.value != SyncIntervalPreference.Manually.value) {
            SyncWorker.enqueuePeriodicWork(account = account, workManager = workManager)
            WidgetUpdateWorker.enqueuePeriodicWork(
                workManager = workManager,
                syncInterval = account.syncInterval,
                syncOnlyWhenCharging = account.syncOnlyWhenCharging,
                syncOnlyOnWiFi = account.syncOnlyOnWiFi,
            )
        } else {
            SyncWorker.cancelPeriodicWork(workManager)
            WidgetUpdateWorker.cancelPeriodicWork(workManager)
        }
    }
}
