package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.compositionLocalOf
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.ash.reader.ui.ext.DataStoreKey
import me.ash.reader.ui.ext.DataStoreKey.Companion.readingTheme
import me.ash.reader.ui.ext.put

val LocalReadingTheme =
    compositionLocalOf<ReadingThemePreference> { ReadingThemePreference.default }

@Immutable
sealed class ReadingThemePreference(val value: Int) : Preference() {

    object MaterialYou : ReadingThemePreference(0)
    object Reeder : ReadingThemePreference(1)
    object Paper : ReadingThemePreference(2)
    object Custom : ReadingThemePreference(3)

    override fun put(scope: CoroutineScope) {
        scope.launch {
            preferenceDataStore().put(DataStoreKey.readingTheme, value)
        }
    }

    fun toDesc(context: Any? = null): String =
        when (this) {
            MaterialYou -> "Material You"
            Reeder -> "Reeder"
            Paper -> "Paper"
            Custom -> "Custom"
        }

    fun applyTheme(context: Any?, scope: CoroutineScope) = applyTheme(scope)

    fun applyTheme(scope: CoroutineScope) {
        when (this) {
            MaterialYou -> {
                ReadingTitleBoldPreference.default.put(scope)
                ReadingTitleUpperCasePreference.default.put(scope)
                ReadingTitleAlignPreference.default.put(scope)
                ReadingSubheadBoldPreference.default.put(scope)
                ReadingSubheadUpperCasePreference.default.put(scope)
                ReadingSubheadAlignPreference.default.put(scope)
                ReadingTextBoldPreference.default.put(scope)
                ReadingTextHorizontalPaddingPreference.put(scope,
                    ReadingTextHorizontalPaddingPreference.default)
                ReadingTextAlignPreference.default.put(scope)
                ReadingTextLetterSpacingPreference.put(scope, ReadingTextLetterSpacingPreference.default)
                ReadingTextLineHeightPreference.put(scope, ReadingTextLineHeightPreference.default)
                ReadingTextFontSizePreference.put(scope, ReadingTextFontSizePreference.default)
                ReadingImageRoundedCornersPreference.put(scope, ReadingImageRoundedCornersPreference.default)
                ReadingImageHorizontalPaddingPreference.put(scope,
                    ReadingImageHorizontalPaddingPreference.default)
                ReadingImageMaximizePreference.default.put(scope)
            }

            Reeder -> {
                ReadingTitleBoldPreference.ON.put(scope)
                ReadingTitleUpperCasePreference.default.put(scope)
                ReadingTitleAlignPreference.default.put(scope)
                ReadingSubheadBoldPreference.ON.put(scope)
                ReadingSubheadUpperCasePreference.default.put(scope)
                ReadingSubheadAlignPreference.default.put(scope)
                ReadingTextBoldPreference.default.put(scope)
                ReadingTextHorizontalPaddingPreference.put(scope,
                    ReadingTextHorizontalPaddingPreference.default)
                ReadingTextAlignPreference.default.put(scope)
                ReadingTextLetterSpacingPreference.put(scope, ReadingTextLetterSpacingPreference.default)
                ReadingTextLineHeightPreference.put(scope, ReadingTextLineHeightPreference.default)
                ReadingTextFontSizePreference.put(scope, ReadingTextFontSizePreference.default)
                ReadingImageRoundedCornersPreference.put(scope, 0)
                ReadingImageHorizontalPaddingPreference.put(scope, 0)
                ReadingImageMaximizePreference.default.put(scope)
            }

            Paper -> {
                ReadingTitleBoldPreference.ON.put(scope)
                ReadingTitleUpperCasePreference.ON.put(scope)
                ReadingTitleAlignPreference.Center.put(scope)
                ReadingSubheadBoldPreference.ON.put(scope)
                ReadingSubheadUpperCasePreference.ON.put(scope)
                ReadingSubheadAlignPreference.Center.put(scope)
                ReadingTextBoldPreference.default.put(scope)
                ReadingTextHorizontalPaddingPreference.put(scope,
                    ReadingTextHorizontalPaddingPreference.default)
                ReadingTextAlignPreference.Center.put(scope)
                ReadingTextLetterSpacingPreference.put(scope, ReadingTextLetterSpacingPreference.default)
                ReadingTextLineHeightPreference.put(scope, ReadingTextLineHeightPreference.default)
                ReadingTextFontSizePreference.put(scope, 20)
                ReadingImageRoundedCornersPreference.put(scope, 0)
                ReadingImageHorizontalPaddingPreference.put(scope,
                    ReadingImageHorizontalPaddingPreference.default)
                ReadingImageMaximizePreference.default.put(scope)
            }

            Custom -> {
                ReadingTitleBoldPreference.default.put(scope)
                ReadingTitleUpperCasePreference.default.put(scope)
                ReadingTitleAlignPreference.default.put(scope)
                ReadingSubheadBoldPreference.default.put(scope)
                ReadingSubheadUpperCasePreference.default.put(scope)
                ReadingSubheadAlignPreference.default.put(scope)
                ReadingTextBoldPreference.default.put(scope)
                ReadingTextHorizontalPaddingPreference.put(scope,
                    ReadingTextHorizontalPaddingPreference.default)
                ReadingTextAlignPreference.default.put(scope)
                ReadingTextLetterSpacingPreference.put(scope, ReadingTextLetterSpacingPreference.default)
                ReadingTextLineHeightPreference.put(scope, ReadingTextLineHeightPreference.default)
                ReadingTextFontSizePreference.put(scope, ReadingTextFontSizePreference.default)
                ReadingImageRoundedCornersPreference.put(scope, ReadingImageRoundedCornersPreference.default)
                ReadingImageHorizontalPaddingPreference.put(scope,
                    ReadingImageHorizontalPaddingPreference.default)
                ReadingImageMaximizePreference.default.put(scope)
            }
        }
    }

    companion object {

        val default = Reeder
        val values = listOf(MaterialYou, Reeder, Paper, Custom)

        fun fromPreferences(preferences: Preferences): ReadingThemePreference =
            when (preferences[DataStoreKey.keys[readingTheme]?.key as Preferences.Key<Int>]) {
                0 -> MaterialYou
                1 -> Reeder
                2 -> Paper
                3 -> Custom
                else -> default
            }
    }
}
