package me.ash.reader.infrastructure.preference

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource
import me.ash.reader.*

@Composable
fun BasicFontsPreference.toDesc(context: Any? = null): String = when (this) {
    BasicFontsPreference.System -> stringResource(Res.string.system_default)
    BasicFontsPreference.GoogleSans -> stringResource(Res.string.google_sans)
    BasicFontsPreference.External -> stringResource(Res.string.external_fonts)
}

@Composable
fun DarkThemePreference.toDesc(context: Any? = null): String = when (this) {
    DarkThemePreference.UseDeviceTheme -> stringResource(Res.string.use_device_theme)
    DarkThemePreference.ON -> stringResource(Res.string.on)
    DarkThemePreference.OFF -> stringResource(Res.string.off)
}

@Composable
fun FeedsFilterBarStylePreference.toDesc(context: Any? = null): String = when (this) {
    FeedsFilterBarStylePreference.Icon -> stringResource(Res.string.icons)
    FeedsFilterBarStylePreference.IconLabel -> stringResource(Res.string.icons_and_labels)
    FeedsFilterBarStylePreference.IconLabelOnlySelected -> stringResource(Res.string.icons_and_label_only_selected)
}

@Composable
fun FlowFilterBarStylePreference.toDesc(context: Any? = null): String = when (this) {
    FlowFilterBarStylePreference.Icon -> stringResource(Res.string.icons)
    FlowFilterBarStylePreference.IconLabel -> stringResource(Res.string.icons_and_labels)
    FlowFilterBarStylePreference.IconLabelOnlySelected -> stringResource(Res.string.icons_and_label_only_selected)
}

@Composable
fun OpenLinkPreference.toDesc(context: Any? = null): String = when (this) {
    OpenLinkPreference.AutoPreferCustomTabs -> stringResource(Res.string.auto_customtabs)
    OpenLinkPreference.AutoPreferDefaultBrowser -> stringResource(Res.string.auto_default_browser)
    OpenLinkPreference.CustomTabs -> stringResource(Res.string.custom_tabs)
    OpenLinkPreference.DefaultBrowser -> stringResource(Res.string.default_browser)
    OpenLinkPreference.SpecificBrowser -> stringResource(Res.string.specific_browser)
    OpenLinkPreference.AlwaysAsk -> stringResource(Res.string.always_ask)
}

@Composable
fun ReadingFontsPreference.toDesc(context: Any? = null): String = when (this) {
    ReadingFontsPreference.System -> stringResource(Res.string.system_default)
    ReadingFontsPreference.GoogleSans -> stringResource(Res.string.google_sans)
    ReadingFontsPreference.Serif -> "Serif"
    ReadingFontsPreference.SansSerif -> "Sans-Serif"
    ReadingFontsPreference.Monospace -> "Monospace"
    ReadingFontsPreference.Cursive -> "Cursive"
    ReadingFontsPreference.External -> stringResource(Res.string.external_fonts)
}

@Composable
fun ReadingRendererPreference.toDesc(context: Any? = null): String = when (this) {
    ReadingRendererPreference.NativeComponent -> stringResource(Res.string.native_component)
    ReadingRendererPreference.WebView -> stringResource(Res.string.webview)
}

@Composable
fun ReadingSubheadAlignPreference.toDesc(context: Any? = null): String = when (this) {
    ReadingSubheadAlignPreference.Start -> stringResource(Res.string.align_start)
    ReadingSubheadAlignPreference.End -> stringResource(Res.string.align_end)
    ReadingSubheadAlignPreference.Center -> stringResource(Res.string.center_text)
    ReadingSubheadAlignPreference.Justify -> stringResource(Res.string.justify)
}

@Composable
fun ReadingTextAlignPreference.toDesc(context: Any? = null): String = when (this) {
    ReadingTextAlignPreference.Start -> stringResource(Res.string.align_start)
    ReadingTextAlignPreference.End -> stringResource(Res.string.align_end)
    ReadingTextAlignPreference.Center -> stringResource(Res.string.center_text)
    ReadingTextAlignPreference.Justify -> stringResource(Res.string.justify)
}

@Composable
fun ReadingTitleAlignPreference.toDesc(context: Any? = null): String = when (this) {
    ReadingTitleAlignPreference.Start -> stringResource(Res.string.align_start)
    ReadingTitleAlignPreference.End -> stringResource(Res.string.align_end)
    ReadingTitleAlignPreference.Center -> stringResource(Res.string.center_text)
    ReadingTitleAlignPreference.Justify -> stringResource(Res.string.justify)
}

@Composable
fun SharedContentPreference.toDesc(context: Any? = null): String = when (this) {
    SharedContentPreference.OnlyLink -> stringResource(Res.string.only_link)
    SharedContentPreference.TitleAndLink -> stringResource(Res.string.title_and_link)
}

@Composable
fun InitialPagePreference.toDesc(context: Any? = null): String = when (this) {
    InitialPagePreference.FeedsPage -> stringResource(Res.string.feeds_page)
    InitialPagePreference.FlowPage -> stringResource(Res.string.flow_page)
}

@Composable
fun InitialFilterPreference.toDesc(context: Any? = null): String = when (this) {
    InitialFilterPreference.All -> stringResource(Res.string.all)
    InitialFilterPreference.Unread -> stringResource(Res.string.unread)
    InitialFilterPreference.Starred -> stringResource(Res.string.starred)
}

fun ReadingThemePreference.toDesc(context: Any? = null): String = when (this) {
    ReadingThemePreference.MaterialYou -> "Material You"
    ReadingThemePreference.Reeder -> "Reeder"
    ReadingThemePreference.Paper -> "Paper"
    ReadingThemePreference.Custom -> "Custom"
}

@Composable
fun SyncIntervalPreference.toDesc(context: Any? = null): String = when (this) {
    SyncIntervalPreference.Manually -> stringResource(Res.string.manually)
    SyncIntervalPreference.Every15Minutes -> stringResource(Res.string.every_15_minutes)
    SyncIntervalPreference.Every30Minutes -> stringResource(Res.string.every_30_minutes)
    SyncIntervalPreference.Every1Hour -> stringResource(Res.string.every_1_hour)
    SyncIntervalPreference.Every2Hours -> stringResource(Res.string.every_2_hours)
    SyncIntervalPreference.Every3Hours -> stringResource(Res.string.every_3_hours)
    SyncIntervalPreference.Every6Hours -> stringResource(Res.string.every_6_hours)
    SyncIntervalPreference.Every12Hours -> stringResource(Res.string.every_12_hours)
    SyncIntervalPreference.Every1Day -> stringResource(Res.string.every_1_day)
}

fun SyncIntervalPreference.put(accountId: Int, viewModel: me.ash.reader.ui.page.settings.accounts.AccountViewModel) {
    val pref = this
    viewModel.update(accountId) { copy(syncInterval = pref) }
}

@Composable
fun SyncOnStartPreference.toDesc(context: Any? = null): String = when (this) {
    SyncOnStartPreference.On -> stringResource(Res.string.on)
    SyncOnStartPreference.Off -> stringResource(Res.string.off)
}

fun SyncOnStartPreference.put(accountId: Int, viewModel: me.ash.reader.ui.page.settings.accounts.AccountViewModel) {
    val pref = this
    viewModel.update(accountId) { copy(syncOnStart = pref) }
}

@Composable
fun SyncOnlyOnWiFiPreference.toDesc(context: Any? = null): String = when (this) {
    SyncOnlyOnWiFiPreference.On -> stringResource(Res.string.on)
    SyncOnlyOnWiFiPreference.Off -> stringResource(Res.string.off)
}

fun SyncOnlyOnWiFiPreference.put(accountId: Int, viewModel: me.ash.reader.ui.page.settings.accounts.AccountViewModel) {
    val pref = this
    viewModel.update(accountId) { copy(syncOnlyOnWiFi = pref) }
}

@Composable
fun SyncOnlyWhenChargingPreference.toDesc(context: Any? = null): String = when (this) {
    SyncOnlyWhenChargingPreference.On -> stringResource(Res.string.on)
    SyncOnlyWhenChargingPreference.Off -> stringResource(Res.string.off)
}

fun SyncOnlyWhenChargingPreference.put(accountId: Int, viewModel: me.ash.reader.ui.page.settings.accounts.AccountViewModel) {
    val pref = this
    viewModel.update(accountId) { copy(syncOnlyWhenCharging = pref) }
}

@Composable
fun KeepArchivedPreference.toDesc(context: Any? = null): String = when (this) {
    KeepArchivedPreference.Always -> stringResource(Res.string.always)
    KeepArchivedPreference.For1Day -> stringResource(Res.string.for_1_day)
    KeepArchivedPreference.For2Days -> stringResource(Res.string.for_2_days)
    KeepArchivedPreference.For3Days -> stringResource(Res.string.for_3_days)
    KeepArchivedPreference.For1Week -> stringResource(Res.string.for_1_week)
    KeepArchivedPreference.For2Weeks -> stringResource(Res.string.for_2_weeks)
    KeepArchivedPreference.For1Month -> stringResource(Res.string.for_1_month)
}

fun KeepArchivedPreference.put(accountId: Int, viewModel: me.ash.reader.ui.page.settings.accounts.AccountViewModel) {
    val pref = this
    viewModel.update(accountId) { copy(keepArchived = pref) }
}

fun SyncBlockListPreference.put(accountId: Int, viewModel: me.ash.reader.ui.page.settings.accounts.AccountViewModel, syncBlockList: SyncBlockList) {
    viewModel.update(accountId) { copy(syncBlockList = syncBlockList) }
}
