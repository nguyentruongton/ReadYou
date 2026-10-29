package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.style.TextAlign
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingSubheadAlign
import me.ash.reader.ui.ext.put

val LocalReadingSubheadAlign =
    compositionLocalOf<ReadingSubheadAlignPreference> { ReadingSubheadAlignPreference.default }

sealed class ReadingSubheadAlignPreference(val value: Int) : Preference() {
    object Start : ReadingSubheadAlignPreference(0)
    object End : ReadingSubheadAlignPreference(1)
    object Center : ReadingSubheadAlignPreference(2)
    object Justify : ReadingSubheadAlignPreference(3)

    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(
                DataStoreKey.readingSubheadAlign,
                value
            )
        }
    }

    

    fun toTextAlign(): TextAlign =
        when (this) {
            Start -> TextAlign.Start
            End -> TextAlign.End
            Center -> TextAlign.Center
            Justify -> TextAlign.Justify
        }

    fun toTextAlignCSS(): String =
        when (this) {
            Start -> "left"
            End -> "right"
            Center -> "center"
            Justify -> "justify"
        }

    companion object {

        val default = Start
        val values = listOf(Start, End, Center, Justify)

        fun fromPreferences(preferences: Preferences): ReadingSubheadAlignPreference =
            when (preferences[DataStoreKey.keys[readingSubheadAlign]?.key as Preferences.Key<Int>]) {
                0 -> Start
                1 -> End
                2 -> Center
                3 -> Justify
                else -> default
            }
    }
}
