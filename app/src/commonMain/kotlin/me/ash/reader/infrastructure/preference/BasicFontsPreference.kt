package me.ash.reader.infrastructure.preference

import androidx.compose.material3.Typography
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.text.font.FontFamily
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.basicFonts
import me.ash.reader.ui.ext.put
import me.ash.reader.ui.theme.GoogleSansFontFamily
import me.ash.reader.ui.theme.SystemTypography
import me.ash.reader.ui.theme.applyFontFamily

val LocalBasicFonts = compositionLocalOf<BasicFontsPreference> { BasicFontsPreference.default }

sealed class BasicFontsPreference(val value: Int) : Preference() {
    object System : BasicFontsPreference(0)

    object GoogleSans : BasicFontsPreference(1)

    object External : BasicFontsPreference(5)

    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(DataStoreKey.basicFonts, value)
        }
    }

    fun asFontFamily(): FontFamily =
        when (this) {
            System -> FontFamily.Default
            GoogleSans -> GoogleSansFontFamily
            External -> FontFamily.Default
        }

    fun asTypography(): Typography =
        when (this) {
            System -> SystemTypography
            GoogleSans -> SystemTypography.applyFontFamily(GoogleSansFontFamily)
            External -> SystemTypography
        }

    companion object {

        val default = GoogleSans
        val values = listOf(GoogleSans, System, External)

        fun fromPreferences(preferences: Preferences): BasicFontsPreference =
            when (preferences[DataStoreKey.keys[basicFonts]?.key as Preferences.Key<Int>]) {
                0 -> System
                1 -> GoogleSans
                5 -> External
                else -> default
            }
    }
}
