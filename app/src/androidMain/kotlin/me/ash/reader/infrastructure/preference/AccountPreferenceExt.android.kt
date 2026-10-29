package me.ash.reader.infrastructure.preference

import android.content.Context
import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit
import me.ash.reader.R
import me.ash.reader.domain.service.SyncWorker
import me.ash.reader.ui.page.settings.accounts.AccountViewModel

fun SyncIntervalPreference.put(accountId: Int, viewModel: AccountViewModel) {
    val pref = this
    viewModel.update(accountId) { copy(syncInterval = pref) }
}

fun SyncIntervalPreference.toDesc(context: Context): String =
    when (this) {
        SyncIntervalPreference.Manually -> context.getString(R.string.manually)
        SyncIntervalPreference.Every15Minutes -> context.getString(R.string.every_15_minutes)
        SyncIntervalPreference.Every30Minutes -> context.getString(R.string.every_30_minutes)
        SyncIntervalPreference.Every1Hour -> context.getString(R.string.every_1_hour)
        SyncIntervalPreference.Every2Hours -> context.getString(R.string.every_2_hours)
        SyncIntervalPreference.Every3Hours -> context.getString(R.string.every_3_hours)
        SyncIntervalPreference.Every6Hours -> context.getString(R.string.every_6_hours)
        SyncIntervalPreference.Every12Hours -> context.getString(R.string.every_12_hours)
        SyncIntervalPreference.Every1Day -> context.getString(R.string.every_1_day)
    }

fun SyncIntervalPreference.toPeriodicWorkRequestBuilder(): PeriodicWorkRequest.Builder =
    PeriodicWorkRequestBuilder<SyncWorker>(value, TimeUnit.MINUTES)

fun SyncOnStartPreference.put(accountId: Int, viewModel: AccountViewModel) {
    val pref = this
    viewModel.update(accountId) { copy(syncOnStart = pref) }
}

fun SyncOnStartPreference.toDesc(context: Context): String =
    when (this) {
        SyncOnStartPreference.On -> context.getString(R.string.on)
        SyncOnStartPreference.Off -> context.getString(R.string.off)
    }

fun SyncOnlyOnWiFiPreference.put(accountId: Int, viewModel: AccountViewModel) {
    val pref = this
    viewModel.update(accountId) { copy(syncOnlyOnWiFi = pref) }
}

fun SyncOnlyOnWiFiPreference.toDesc(context: Context): String =
    when (this) {
        SyncOnlyOnWiFiPreference.On -> context.getString(R.string.on)
        SyncOnlyOnWiFiPreference.Off -> context.getString(R.string.off)
    }

fun SyncOnlyWhenChargingPreference.put(accountId: Int, viewModel: AccountViewModel) {
    val pref = this
    viewModel.update(accountId) { copy(syncOnlyWhenCharging = pref) }
}

fun SyncOnlyWhenChargingPreference.toDesc(context: Context): String =
    when (this) {
        SyncOnlyWhenChargingPreference.On -> context.getString(R.string.on)
        SyncOnlyWhenChargingPreference.Off -> context.getString(R.string.off)
    }

fun KeepArchivedPreference.put(accountId: Int, viewModel: AccountViewModel) {
    val pref = this
    viewModel.update(accountId) { copy(keepArchived = pref) }
}

fun KeepArchivedPreference.toDesc(context: Context): String =
    when (this) {
        KeepArchivedPreference.Always -> context.getString(R.string.always)
        KeepArchivedPreference.For1Day -> context.getString(R.string.for_1_day)
        KeepArchivedPreference.For2Days -> context.getString(R.string.for_2_days)
        KeepArchivedPreference.For3Days -> context.getString(R.string.for_3_days)
        KeepArchivedPreference.For1Week -> context.getString(R.string.for_1_week)
        KeepArchivedPreference.For2Weeks -> context.getString(R.string.for_2_weeks)
        KeepArchivedPreference.For1Month -> context.getString(R.string.for_1_month)
    }

fun SyncBlockListPreference.put(accountId: Int, viewModel: AccountViewModel, syncBlockList: SyncBlockList) {
    viewModel.update(accountId) { copy(syncBlockList = syncBlockList) }
}
