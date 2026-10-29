package me.ash.reader.infrastructure.platform

import androidx.work.WorkManager
import androidx.work.workDataOf
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.service.SyncWorker
import me.ash.reader.domain.service.WidgetUpdateWorker
import me.ash.reader.infrastructure.preference.SyncIntervalPreference

class AndroidSyncScheduler(
    private val workManager: WorkManager,
) : SyncScheduler {
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
