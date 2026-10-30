package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.newVersionLog
import me.ash.reader.ui.ext.put

val LocalNewVersionLog = compositionLocalOf { NewVersionLogPreference.default }

object NewVersionLogPreference {

    const val default = ""

    fun put(scope: CoroutineScope, value: String) {
        scope.launch(Dispatchers.IO) {
            preferenceDataStore().put(DataStoreKey.newVersionLog, value)
        }
    }

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[newVersionLog]?.key as Preferences.Key<String>] ?: default
}
