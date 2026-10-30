package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.domain.model.constant.ElevationTokens
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingPageTonalElevation
import me.ash.reader.ui.ext.put

val LocalReadingPageTonalElevation =
    compositionLocalOf<ReadingPageTonalElevationPreference> { ReadingPageTonalElevationPreference.default }

sealed class ReadingPageTonalElevationPreference(val value: Int) : Preference() {
    data object Outlined : ReadingPageTonalElevationPreference(ElevationTokens.Level0)
    data object Elevated : ReadingPageTonalElevationPreference(ElevationTokens.Level2)

    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(readingPageTonalElevation, value)
        }
    }

    fun toDesc(context: Any? = null): String =
        when (this) {
            Outlined -> "${ElevationTokens.Level0}dp"
            Elevated -> "${ElevationTokens.Level2}dp"
        }

    companion object {

        val default = Outlined
        val values = listOf(Outlined, Elevated)

        fun fromPreferences(preferences: Preferences) =
            when (preferences[DataStoreKey.keys[readingPageTonalElevation]?.key as Preferences.Key<Int>]) {
                ElevationTokens.Level0 -> Outlined
                ElevationTokens.Level2 -> Elevated
                else -> default
            }
    }
}

