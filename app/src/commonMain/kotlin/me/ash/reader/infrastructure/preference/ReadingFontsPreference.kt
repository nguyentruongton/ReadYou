package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingFonts
import me.ash.reader.ui.ext.put
import me.ash.reader.ui.theme.GoogleSansFontFamily
import me.ash.reader.ui.theme.PlatformFontLoader

val LocalReadingFonts =
    compositionLocalOf<ReadingFontsPreference> { ReadingFontsPreference.default }

sealed class ReadingFontsPreference(val value: Int) : Preference() {
    object GoogleSans : ReadingFontsPreference(6)

    object System : ReadingFontsPreference(0)

    object Serif : ReadingFontsPreference(1)

    object SansSerif : ReadingFontsPreference(2)

    object Monospace : ReadingFontsPreference(3)

    object Cursive : ReadingFontsPreference(4)

    object External : ReadingFontsPreference(5)

    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(DataStoreKey.readingFonts, value)
        }
    }

    fun asFontFamily(): FontFamily =
        when (this) {
            GoogleSans -> GoogleSansFontFamily
            System -> FontFamily.Default
            Serif -> FontFamily.Serif
            SansSerif -> FontFamily.SansSerif
            Monospace -> FontFamily.Monospace
            Cursive -> FontFamily.Cursive
            External -> PlatformFontLoader.loadReadingFontFamily()
        }

    companion object {

        val default = GoogleSans
        val values = listOf(GoogleSans, System, Serif, SansSerif, Monospace, Cursive, External)

        fun fromPreferences(preferences: Preferences): ReadingFontsPreference =
            when (preferences[DataStoreKey.keys[readingFonts]?.key as Preferences.Key<Int>]) {
                0 -> System
                1 -> Serif
                2 -> SansSerif
                3 -> Monospace
                4 -> Cursive
                5 -> External
                6 -> GoogleSans
                else -> default
            }
    }
}
