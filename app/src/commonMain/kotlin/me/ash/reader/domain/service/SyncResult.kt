package me.ash.reader.domain.service

sealed class SyncResult {
    data object Success : SyncResult()
    data object Failure : SyncResult()
    data object Retry : SyncResult()
}
