package me.ash.reader.infrastructure.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import me.ash.reader.infrastructure.platform.AppPaths
import okio.Path.Companion.toPath

fun createPreferenceDataStore(appPaths: AppPaths, scope: CoroutineScope): DataStore<Preferences> {
    return PreferenceDataStoreFactory.createWithPath(
        scope = scope,
        produceFile = {
            appPaths.dataStoreFile.parentFile?.mkdirs()
            appPaths.dataStoreFile.absolutePath.toPath()
        }
    )
}
