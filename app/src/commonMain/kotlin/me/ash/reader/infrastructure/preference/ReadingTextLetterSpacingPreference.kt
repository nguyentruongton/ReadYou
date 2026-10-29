package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingTextLetterSpacing
import me.ash.reader.ui.ext.put

val LocalReadingTextLetterSpacing = compositionLocalOf { ReadingTextLetterSpacingPreference.default }

object ReadingTextLetterSpacingPreference {

    const val default = 0.5F

    fun put(scope: CoroutineScope, value: Float) {
        scope.launch {
            preferenceDataStore().put(DataStoreKey.readingTextLetterSpacing, value)
        }
    }

    fun put(context: Any?, scope: CoroutineScope, value: Float) = put(scope, value)

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[readingTextLetterSpacing]?.key as Preferences.Key<Float>] ?: default
}
