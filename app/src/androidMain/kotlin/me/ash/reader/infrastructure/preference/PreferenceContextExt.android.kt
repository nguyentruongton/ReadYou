package me.ash.reader.infrastructure.preference

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import me.ash.reader.R
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.ui.ext.ExternalFonts
import me.ash.reader.ui.ext.orNotEmpty
import me.ash.reader.ui.theme.GoogleSansFontFamily
import me.ash.reader.ui.theme.SystemTypography
import me.ash.reader.ui.theme.applyFontFamily

fun BasicFontsPreference.asFontFamily(context: Context): FontFamily =
    when (this) {
        BasicFontsPreference.System -> FontFamily.Default
        BasicFontsPreference.GoogleSans -> GoogleSansFontFamily
        BasicFontsPreference.External ->
            ExternalFonts.loadBasicTypography(context).displayLarge.fontFamily
                ?: FontFamily.Default
    }

fun BasicFontsPreference.asTypography(context: Context): Typography =
    when (this) {
        BasicFontsPreference.System -> SystemTypography
        BasicFontsPreference.GoogleSans -> SystemTypography.applyFontFamily(GoogleSansFontFamily)
        BasicFontsPreference.External -> ExternalFonts.loadBasicTypography(context)
    }

fun ReadingFontsPreference.asFontFamily(context: Context): FontFamily =
    when (this) {
        ReadingFontsPreference.GoogleSans -> GoogleSansFontFamily
        ReadingFontsPreference.System -> FontFamily.Default
        ReadingFontsPreference.Serif -> FontFamily.Serif
        ReadingFontsPreference.SansSerif -> FontFamily.SansSerif
        ReadingFontsPreference.Monospace -> FontFamily.Monospace
        ReadingFontsPreference.Cursive -> FontFamily.Cursive
        ReadingFontsPreference.External ->
            ExternalFonts.loadReadingTypography(context).displayLarge.fontFamily
                ?: FontFamily.Default
    }

fun OpenLinkSpecificBrowserPreference.toDesc(context: Context): String {
    val pm = context.packageManager
    return runCatching {
        pm.run {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getApplicationInfo(
                    this@toDesc.packageName!!,
                    PackageManager.ApplicationInfoFlags.of(0)
                )
            } else {
                getApplicationInfo(this@toDesc.packageName!!, 0)
            }
        }
    }.map {
        it.loadLabel(pm)
    }.getOrDefault(context.getString(R.string.open_link_specific_browser_not_selected)).let {
        context.getString(R.string.specific_browser_name, it)
    }
}

fun ReadingTextFontSizePreference.put(context: Any?, scope: kotlinx.coroutines.CoroutineScope, value: Int) =
    put(scope, value)

fun ReadingTextLineHeightPreference.put(context: Any?, scope: kotlinx.coroutines.CoroutineScope, value: Float) =
    put(scope, value)

fun ReadingTextLetterSpacingPreference.put(context: Any?, scope: kotlinx.coroutines.CoroutineScope, value: Float) =
    put(scope, value)

fun ReadingTextHorizontalPaddingPreference.put(context: Any?, scope: kotlinx.coroutines.CoroutineScope, value: Int) =
    put(scope, value)

fun ReadingImageHorizontalPaddingPreference.put(context: Any?, scope: kotlinx.coroutines.CoroutineScope, value: Int) =
    put(scope, value)

fun ReadingImageRoundedCornersPreference.put(context: Any?, scope: kotlinx.coroutines.CoroutineScope, value: Int) =
    put(scope, value)

fun FeedsFilterBarPaddingPreference.put(context: Any?, scope: kotlinx.coroutines.CoroutineScope, value: Int) =
    put(scope, value)

fun FlowFilterBarPaddingPreference.put(context: Any?, scope: kotlinx.coroutines.CoroutineScope, value: Int) =
    put(scope, value)

fun SkipVersionNumberPreference.put(context: Any?, scope: kotlinx.coroutines.CoroutineScope, value: String) =
    put(scope, value)
