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

fun BasicFontsPreference.toDesc(context: Context): String =
    when (this) {
        BasicFontsPreference.System -> context.getString(R.string.system_default)
        BasicFontsPreference.GoogleSans -> context.getString(R.string.google_sans)
        BasicFontsPreference.External -> context.getString(R.string.external_fonts)
    }

fun DarkThemePreference.toDesc(context: Context): String =
    when (this) {
        DarkThemePreference.UseDeviceTheme -> context.getString(R.string.use_device_theme)
        DarkThemePreference.ON -> context.getString(R.string.on)
        DarkThemePreference.OFF -> context.getString(R.string.off)
    }

fun FeedsFilterBarStylePreference.toDesc(context: Context): String =
    when (this) {
        FeedsFilterBarStylePreference.Icon -> context.getString(R.string.icons)
        FeedsFilterBarStylePreference.IconLabel -> context.getString(R.string.icons_and_labels)
        FeedsFilterBarStylePreference.IconLabelOnlySelected -> context.getString(R.string.icons_and_label_only_selected)
    }

fun FlowFilterBarStylePreference.toDesc(context: Context): String =
    when (this) {
        FlowFilterBarStylePreference.Icon -> context.getString(R.string.icons)
        FlowFilterBarStylePreference.IconLabel -> context.getString(R.string.icons_and_labels)
        FlowFilterBarStylePreference.IconLabelOnlySelected -> context.getString(R.string.icons_and_label_only_selected)
    }

fun OpenLinkPreference.toDesc(context: Context): String =
    when (this) {
        OpenLinkPreference.AutoPreferCustomTabs -> context.getString(R.string.auto_customtabs)
        OpenLinkPreference.AutoPreferDefaultBrowser -> context.getString(R.string.auto_default_browser)
        OpenLinkPreference.CustomTabs -> context.getString(R.string.custom_tabs)
        OpenLinkPreference.DefaultBrowser -> context.getString(R.string.default_browser)
        OpenLinkPreference.SpecificBrowser -> context.getString(R.string.specific_browser)
        OpenLinkPreference.AlwaysAsk -> context.getString(R.string.always_ask)
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

fun ReadingFontsPreference.toDesc(context: Context): String =
    when (this) {
        ReadingFontsPreference.System -> context.getString(R.string.system_default)
        ReadingFontsPreference.GoogleSans -> context.getString(R.string.google_sans)
        ReadingFontsPreference.Serif -> "Serif"
        ReadingFontsPreference.SansSerif -> "Sans-Serif"
        ReadingFontsPreference.Monospace -> "Monospace"
        ReadingFontsPreference.Cursive -> "Cursive"
        ReadingFontsPreference.External -> context.getString(R.string.external_fonts)
    }

fun ReadingRendererPreference.toDesc(context: Context): String =
    when (this) {
        ReadingRendererPreference.NativeComponent -> context.getString(R.string.native_component)
        ReadingRendererPreference.WebView -> context.getString(R.string.webview)
    }

fun ReadingSubheadAlignPreference.toDesc(context: Context): String =
    when (this) {
        ReadingSubheadAlignPreference.Start -> context.getString(R.string.align_start)
        ReadingSubheadAlignPreference.End -> context.getString(R.string.align_end)
        ReadingSubheadAlignPreference.Center -> context.getString(R.string.center_text)
        ReadingSubheadAlignPreference.Justify -> context.getString(R.string.justify)
    }

fun ReadingTextAlignPreference.toDesc(context: Context): String =
    when (this) {
        ReadingTextAlignPreference.Start -> context.getString(R.string.align_start)
        ReadingTextAlignPreference.End -> context.getString(R.string.align_end)
        ReadingTextAlignPreference.Center -> context.getString(R.string.center_text)
        ReadingTextAlignPreference.Justify -> context.getString(R.string.justify)
    }

fun ReadingTitleAlignPreference.toDesc(context: Context): String =
    when (this) {
        ReadingTitleAlignPreference.Start -> context.getString(R.string.align_start)
        ReadingTitleAlignPreference.End -> context.getString(R.string.align_end)
        ReadingTitleAlignPreference.Center -> context.getString(R.string.center_text)
        ReadingTitleAlignPreference.Justify -> context.getString(R.string.justify)
    }

fun SharedContentPreference.toDesc(context: Context): String =
    when (this) {
        SharedContentPreference.OnlyLink -> context.getString(R.string.only_link)
        SharedContentPreference.TitleAndLink -> context.getString(R.string.title_and_link)
    }

fun SharedContentPreference.share(context: Context, title: String?, link: String?) {
    val textToShare = when (this) {
        SharedContentPreference.OnlyLink -> link.orEmpty()
        SharedContentPreference.TitleAndLink -> title.orNotEmpty { it + "\n" } + link.orEmpty()
    }
    context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, textToShare)
        type = "text/plain"
    }, context.getString(R.string.share)))
}

fun InitialPagePreference.toDesc(context: Context): String =
    when (this) {
        InitialPagePreference.FeedsPage -> context.getString(R.string.feeds_page)
        InitialPagePreference.FlowPage -> context.getString(R.string.flow_page)
    }

fun InitialFilterPreference.toDesc(context: Context): String =
    when (this) {
        InitialFilterPreference.All -> context.getString(R.string.all)
        InitialFilterPreference.Unread -> context.getString(R.string.unread)
        InitialFilterPreference.Starred -> context.getString(R.string.starred)
    }

fun ReadingThemePreference.toDesc(context: Context): String =
    when (this) {
        ReadingThemePreference.MaterialYou -> "Material You"
        ReadingThemePreference.Reeder -> "Reeder"
        ReadingThemePreference.Paper -> "Paper"
        ReadingThemePreference.Custom -> "Custom"
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
