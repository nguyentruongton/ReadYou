package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.newVersionDownloadUrl
import me.ash.reader.ui.ext.put

val LocalNewVersionDownloadUrl = compositionLocalOf { NewVersionDownloadUrlPreference.default }

object NewVersionDownloadUrlPreference {

    const val default = ""

    fun put(scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            preferenceDataStore().put(DataStoreKey.newVersionDownloadUrl, value)
        }
    }

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[newVersionDownloadUrl]?.key as Preferences.Key<String>] ?: default
}
