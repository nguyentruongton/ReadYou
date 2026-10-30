package me.ash.reader.ui.theme

import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import me.ash.reader.R

@OptIn(ExperimentalTextApi::class)
private val GoogleSansRegular =
    Font(
        R.font.google_sans_flex,
        weight = FontWeight.W400,
        variationSettings = FontVariation.Settings(FontVariation.weight(400)),
    )

@OptIn(ExperimentalTextApi::class)
private val GoogleSansMedium =
    Font(
        R.font.google_sans_flex,
        weight = FontWeight.W500,
        variationSettings = FontVariation.Settings(FontVariation.weight(500)),
    )

@OptIn(ExperimentalTextApi::class)
private val GoogleSansSemiBold =
    Font(
        R.font.google_sans_flex,
        weight = FontWeight.W600,
        variationSettings = FontVariation.Settings(FontVariation.weight(600)),
    )

@OptIn(ExperimentalTextApi::class)
private val GoogleSansBold =
    Font(
        R.font.google_sans_flex,
        weight = FontWeight.W700,
        variationSettings = FontVariation.Settings(FontVariation.weight(700)),
    )

@OptIn(ExperimentalTextApi::class)
private val GoogleSansItalic =
    Font(
        R.font.google_sans_flex,
        weight = FontWeight.W400,
        style = FontStyle.Italic,
        variationSettings =
            FontVariation.Settings(FontVariation.weight(400), FontVariation.italic(.5f)),
    )

@OptIn(ExperimentalTextApi::class)
private val GoogleSansMediumItalic =
    Font(
        R.font.google_sans_flex,
        weight = FontWeight.W500,
        style = FontStyle.Italic,
        variationSettings =
            FontVariation.Settings(FontVariation.weight(500), FontVariation.italic(.5f)),
    )

@OptIn(ExperimentalTextApi::class)
private val GoogleSansSemiBoldItalic =
    Font(
        R.font.google_sans_flex,
        weight = FontWeight.W600,
        style = FontStyle.Italic,
        variationSettings =
            FontVariation.Settings(FontVariation.weight(600), FontVariation.italic(.5f)),
    )

@OptIn(ExperimentalTextApi::class)
private val GoogleSansBoldItalic =
    Font(
        R.font.google_sans_flex,
        weight = FontWeight.W700,
        style = FontStyle.Italic,
        variationSettings =
            FontVariation.Settings(FontVariation.weight(700), FontVariation.italic(.5f)),
    )

actual val GoogleSansFontFamily: FontFamily =
    FontFamily(
        Font(R.font.google_sans_flex),
        GoogleSansRegular,
        GoogleSansMedium,
        GoogleSansSemiBold,
        GoogleSansBold,
        GoogleSansItalic,
        GoogleSansMediumItalic,
        GoogleSansSemiBoldItalic,
        GoogleSansBoldItalic,
    )

actual object PlatformFontLoader {
    actual fun loadBasicFontFamily(): FontFamily =
        me.ash.reader.ui.ext.ExternalFonts.loadBasicTypography(me.ash.reader.infrastructure.android.AndroidApp.instance).displayLarge.fontFamily
            ?: FontFamily.Default

    actual fun loadReadingFontFamily(): FontFamily =
        me.ash.reader.ui.ext.ExternalFonts.loadReadingTypography(me.ash.reader.infrastructure.android.AndroidApp.instance).displayLarge.fontFamily
            ?: FontFamily.Default

    actual fun resolveWebViewFontPath(readingFonts: me.ash.reader.infrastructure.preference.ReadingFontsPreference): String? =
        when (readingFonts) {
            is me.ash.reader.infrastructure.preference.ReadingFontsPreference.External ->
                me.ash.reader.ui.ext.ExternalFonts.FontType.ReadingFont.toPath(me.ash.reader.infrastructure.android.AndroidApp.instance)
            is me.ash.reader.infrastructure.preference.ReadingFontsPreference.GoogleSans ->
                "/android_res/font/google_sans_flex.ttf"
            else -> null
        }
}

