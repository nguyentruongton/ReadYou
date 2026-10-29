package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingTextFontSize
import me.ash.reader.ui.ext.put

val LocalReadingTextFontSize = compositionLocalOf { ReadingTextFontSizePreference.default }

object ReadingTextFontSizePreference {

    const val default = 17

    fun put(scope: CoroutineScope, value: Int) {
        scope.launch {
            preferenceDataStore().put(DataStoreKey.readingTextFontSize, value)
        }
    }

    fun put(context: Any?, scope: CoroutineScope, value: Int) = put(scope, value)

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[readingTextFontSize]?.key as Preferences.Key<Int>] ?: default
}
