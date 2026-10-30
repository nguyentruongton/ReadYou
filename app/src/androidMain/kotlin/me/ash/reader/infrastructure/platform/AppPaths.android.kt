package me.ash.reader.infrastructure.platform

import android.content.Context

fun createAndroidAppPaths(context: Context): AppPaths {
    return AppPaths(
        dataDir = context.filesDir,
        cacheDir = context.cacheDir,
        dataStoreFile = context.filesDir.resolve("datastore/settings.preferences_pb"),
    )
}
