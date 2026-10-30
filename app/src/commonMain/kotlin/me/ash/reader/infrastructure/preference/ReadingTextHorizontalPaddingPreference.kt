package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingTextHorizontalPadding
import me.ash.reader.ui.ext.put

val LocalReadingTextHorizontalPadding =
    compositionLocalOf { ReadingTextHorizontalPaddingPreference.default }

object ReadingTextHorizontalPaddingPreference {

    const val default = 24

    fun put(scope: CoroutineScope, value: Int) {
        scope.launch {
            preferenceDataStore().put(DataStoreKey.readingTextHorizontalPadding, value)
        }
    }

    fun put(context: Any?, scope: CoroutineScope, value: Int) = put(scope, value)

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[readingTextHorizontalPadding]?.key as Preferences.Key<Int>] ?: default
}
