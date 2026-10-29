package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.themeIndex
import me.ash.reader.ui.ext.put

val LocalThemeIndex =
    compositionLocalOf { ThemeIndexPreference.default }

object ThemeIndexPreference {

    const val default = 5

    fun put(scope: CoroutineScope, value: Int) {
        scope.launch(Dispatchers.IO) {
            preferenceDataStore().put(DataStoreKey.themeIndex, value)
        }
    }

    fun put(context: Any?, scope: CoroutineScope, value: Int) = put(scope, value)

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[themeIndex]?.key as Preferences.Key<Int>] ?: default
}
