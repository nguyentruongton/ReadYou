package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingImageRoundedCorners
import me.ash.reader.ui.ext.put

val LocalReadingImageRoundedCorners =
    compositionLocalOf { ReadingImageRoundedCornersPreference.default }

object ReadingImageRoundedCornersPreference {

    const val default = 24

    fun put(scope: CoroutineScope, value: Int) {
        scope.launch {
            preferenceDataStore().put(DataStoreKey.readingImageRoundedCorners, value)
        }
    }

    fun put(context: Any?, scope: CoroutineScope, value: Int) = put(scope, value)

    fun fromPreferences(preferences: Preferences) =
        preferences[DataStoreKey.keys[readingImageRoundedCorners]?.key as Preferences.Key<Int>] ?: default
}
