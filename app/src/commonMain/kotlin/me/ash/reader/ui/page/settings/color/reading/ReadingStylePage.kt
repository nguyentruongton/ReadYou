package me.ash.reader.ui.page.settings.color.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Segment
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.stringResource
import me.ash.reader.*
import me.ash.reader.infrastructure.preference.LocalPullToSwitchArticle
import me.ash.reader.infrastructure.preference.LocalReadingAutoHideToolbar
import me.ash.reader.infrastructure.preference.LocalReadingBoldCharacters
import me.ash.reader.infrastructure.preference.LocalReadingFonts
import me.ash.reader.infrastructure.preference.LocalReadingPageTonalElevation
import me.ash.reader.infrastructure.preference.LocalReadingRenderer
import me.ash.reader.infrastructure.preference.LocalReadingTheme
import me.ash.reader.infrastructure.preference.ReadingFontsPreference
import me.ash.reader.infrastructure.preference.ReadingPageTonalElevationPreference
import me.ash.reader.infrastructure.preference.ReadingRendererPreference
import me.ash.reader.infrastructure.preference.ReadingThemePreference
import me.ash.reader.infrastructure.preference.not
import me.ash.reader.infrastructure.preference.toDesc
import me.ash.reader.ui.component.ReadingThemePrev
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.RYSwitch
import me.ash.reader.ui.component.base.RadioDialog
import me.ash.reader.ui.component.base.RadioDialogOption
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.ext.rememberReadingFontPicker
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@Composable
fun ReadingStylePage(
    onBack: () -> Unit,
    navigateToReadingBoldCharacters: () -> Unit,
    navigateToReadingPageTitle: () -> Unit,
    navigateToReadingPageText: () -> Unit,
    navigateToReadingPageImage: () -> Unit,
    navigateToReadingPageVideo: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val readingTheme = LocalReadingTheme.current
    val tonalElevation = LocalReadingPageTonalElevation.current
    val fonts = LocalReadingFonts.current
    val autoHideToolbar = LocalReadingAutoHideToolbar.current
    val pullToSwitchArticle = LocalPullToSwitchArticle.current
    val renderer = LocalReadingRenderer.current
    val boldCharacters = LocalReadingBoldCharacters.current

    var tonalElevationDialogVisible by remember { mutableStateOf(false) }
    var rendererDialogVisible by remember { mutableStateOf(false) }
    var fontsDialogVisible by remember { mutableStateOf(false) }

    val fontPicker = rememberReadingFontPicker {
        ReadingFontsPreference.External.put(scope)
    }

    RYScaffold(
        containerColor = MaterialTheme.colorScheme.surface onLight MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(Res.string.back),
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onBack
            )
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(Res.string.reading_page), desc = "")
                }

                // Themes
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ReadingThemePreference.values.forEachIndexed { index, theme ->
                            ReadingThemePrev(
                                selected = readingTheme,
                                theme = theme,
                                onClick = {
                                    theme.put(scope)
                                }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Preview
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                MaterialTheme.colorScheme.inverseOnSurface
                                        onLight MaterialTheme.colorScheme.surface.copy(0.7f)
                            )
                            .clickable { },
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {

                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // General
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.general)
                    )
                    SettingItem(
                        title = stringResource(Res.string.content_renderer),
                        desc = renderer.toDesc(),
                        onClick = { rendererDialogVisible = true },
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.bold_characters),
                        separatedActions = renderer == ReadingRendererPreference.WebView,
                        enabled = renderer == ReadingRendererPreference.WebView,
                        desc = if (renderer == ReadingRendererPreference.WebView) null
                        else stringResource(Res.string.only_available_on_webview),
                        onClick = navigateToReadingBoldCharacters,
                    ) {
                        if (renderer == ReadingRendererPreference.WebView) {
                            RYSwitch(
                                enable = renderer == ReadingRendererPreference.WebView,
                                activated = boldCharacters.value,
                            ) {
                                (!boldCharacters).put(scope)
                            }
                        }
                    }
                    SettingItem(
                        title = stringResource(Res.string.reading_fonts),
                        desc = fonts.toDesc(),
                        onClick = { fontsDialogVisible = true },
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.auto_hide_toolbars),
                        onClick = {
                            (!autoHideToolbar).put(scope)
                        },
                    ) {
                        RYSwitch(activated = autoHideToolbar.value) {
                            (!autoHideToolbar).put(scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(Res.string.rearrange_buttons),
                        enabled = false,
                        onClick = {},
                    ) {}

                    SettingItem(
                        title = stringResource(Res.string.pull_to_switch_article),
                        onClick = { pullToSwitchArticle.toggle(scope) }) {
                        RYSwitch(activated = pullToSwitchArticle.value, onClick = {
                            pullToSwitchArticle.toggle(scope)
                        })
                    }
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.toolbars)
                    )
                    SettingItem(
                        title = stringResource(Res.string.tonal_elevation),
                        desc = "${tonalElevation.value}dp",
                        onClick = {
                            tonalElevationDialogVisible = true
                        },
                    ) {}

                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Advanced
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.advanced)
                    )
                    SettingItem(
                        title = stringResource(Res.string.title),
                        desc = stringResource(Res.string.title_desc),
                        icon = Icons.Rounded.Title,
                        onClick = navigateToReadingPageTitle,
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.text),
                        desc = stringResource(Res.string.text_desc),
                        icon = Icons.AutoMirrored.Rounded.Segment,
                        onClick = navigateToReadingPageText,
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.images),
                        desc = stringResource(Res.string.images_desc),
                        icon = Icons.Outlined.Image,
                        onClick = navigateToReadingPageImage,
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.videos),
                        desc = stringResource(Res.string.videos_desc),
                        icon = Icons.Outlined.Movie,
                        enabled = false,
                        onClick = navigateToReadingPageVideo,
                    ) {}
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )

    RadioDialog(
        visible = tonalElevationDialogVisible,
        title = stringResource(Res.string.tonal_elevation),
        options = ReadingPageTonalElevationPreference.values.map {
            RadioDialogOption(
                text = it.toDesc(),
                selected = it == tonalElevation,
            ) {
                it.put(scope)
            }
        }
    ) {
        tonalElevationDialogVisible = false
    }

    RadioDialog(
        visible = rendererDialogVisible,
        title = stringResource(Res.string.content_renderer),
        options = ReadingRendererPreference.values.map {
            RadioDialogOption(
                text = it.toDesc(),
                selected = it == renderer,
            ) {
                it.put(scope)
            }
        }
    ) {
        rendererDialogVisible = false
    }

    RadioDialog(
        visible = fontsDialogVisible,
        title = stringResource(Res.string.reading_fonts),
        options = ReadingFontsPreference.values.map {
            RadioDialogOption(
                text = it.toDesc(),
                style = TextStyle(fontFamily = it.asFontFamily()),
                selected = it == fonts,
            ) {
                if (it.value == ReadingFontsPreference.External.value) {
                    fontPicker()
                } else {
                    it.put(scope)
                }
            }
        }
    ) {
        fontsDialogVisible = false
    }
}
