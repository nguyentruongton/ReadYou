package me.ash.reader.infrastructure.preference

import androidx.work.PeriodicWorkRequest
import androidx.work.PeriodicWorkRequestBuilder
import java.util.concurrent.TimeUnit
import me.ash.reader.domain.service.SyncWorker

fun SyncIntervalPreference.toPeriodicWorkRequestBuilder(): PeriodicWorkRequest.Builder =
    PeriodicWorkRequestBuilder<SyncWorker>(value, TimeUnit.MINUTES)
