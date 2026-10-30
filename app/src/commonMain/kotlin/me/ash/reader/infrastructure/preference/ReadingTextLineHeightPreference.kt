package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingTextLineHeight
import me.ash.reader.ui.ext.put

val LocalReadingTextLineHeight = compositionLocalOf { ReadingTextLineHeightPreference.default }

data object ReadingTextLineHeightPreference {
    const val default = 1.0F
    private val range = 0.8F..2F

    fun put(scope: CoroutineScope, value: Float) {
        scope.launch {
            preferenceDataStore().put(DataStoreKey.readingTextLineHeight, value)
        }
    }

    fun put(context: Any?, scope: CoroutineScope, value: Float) = put(scope, value)

    fun Float.coerceToRange() = coerceIn(range)

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[readingTextLineHeight]?.key as Preferences.Key<Float>] ?: default
}
