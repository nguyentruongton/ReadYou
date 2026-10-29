package me.ash.reader.ui.page.settings.color

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import me.ash.reader.infrastructure.preference.BasicFontsPreference
import me.ash.reader.infrastructure.preference.CustomPrimaryColorPreference
import me.ash.reader.infrastructure.preference.LocalBasicFonts
import me.ash.reader.infrastructure.preference.LocalCustomPrimaryColor
import me.ash.reader.infrastructure.preference.LocalDarkTheme
import me.ash.reader.infrastructure.preference.LocalThemeIndex
import me.ash.reader.infrastructure.preference.ThemeIndexPreference
import me.ash.reader.infrastructure.preference.not
import me.ash.reader.infrastructure.preference.toDesc
import me.ash.reader.ui.component.base.BlockRadioButton
import me.ash.reader.ui.component.base.BlockRadioGroupButtonItem
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.DynamicSVGImage
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.RYSwitch
import me.ash.reader.ui.component.base.RadioDialog
import me.ash.reader.ui.component.base.RadioDialogOption
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.component.base.TextFieldDialog
import me.ash.reader.ui.ext.rememberBasicFontPicker
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.svg.PALETTE
import me.ash.reader.ui.svg.SVGString
import me.ash.reader.ui.theme.palette.TonalPalettes
import me.ash.reader.ui.theme.palette.TonalPalettes.Companion.toTonalPalettes
import me.ash.reader.ui.theme.palette.checkColorHex
import me.ash.reader.ui.theme.palette.dynamic.extractTonalPalettesFromUserWallpaper
import me.ash.reader.ui.theme.palette.onDark
import me.ash.reader.ui.theme.palette.onLight
import me.ash.reader.ui.theme.palette.safeHexToColor

@Composable
fun ColorAndStylePage(
    onBack: () -> Unit,
    navigateToDarkTheme: () -> Unit,
    navigateToFeedsPageStyle: () -> Unit,
    navigateToFlowPageStyle: () -> Unit,
    navigateToReadingPageStyle: () -> Unit,
) {
    val darkTheme = LocalDarkTheme.current
    val darkThemeNot = !darkTheme
    val themeIndex = LocalThemeIndex.current
    val customPrimaryColor = LocalCustomPrimaryColor.current
    val fonts = LocalBasicFonts.current
    val scope = rememberCoroutineScope()

    val wallpaperTonalPalettes = extractTonalPalettesFromUserWallpaper()
    var radioButtonSelected by remember { mutableStateOf(if (themeIndex > 4) 0 else 1) }
    var fontsDialogVisible by remember { mutableStateOf(false) }

    val fontPicker = rememberBasicFontPicker {
        BasicFontsPreference.External.put(scope)
    }

    RYScaffold(
        containerColor =
            MaterialTheme.colorScheme.surface onLight
                MaterialTheme.colorScheme.inverseOnSurface,
        navigationIcon = {
            FeedbackIconButton(
                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = stringResource(Res.string.back),
                tint = MaterialTheme.colorScheme.onSurface,
                onClick = onBack,
            )
        },
        content = {
            LazyColumn {
                item {
                    DisplayText(text = stringResource(Res.string.color_and_style), desc = "")
                }
                item {
                    Row(
                        modifier =
                            Modifier.fillMaxWidth()
                                .padding(horizontal = 24.dp)
                                .aspectRatio(1.38f)
                                .clip(RoundedCornerShape(24.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                .clickable {},
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        DynamicSVGImage(
                            modifier = Modifier.padding(60.dp),
                            svgImageString = SVGString.PALETTE,
                            contentDescription = stringResource(Res.string.color_and_style),
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    BlockRadioButton(
                        selected = radioButtonSelected,
                        onSelected = { radioButtonSelected = it },
                        itemRadioGroups =
                            listOf(
                                BlockRadioGroupButtonItem(
                                    text = stringResource(Res.string.wallpaper_colors),
                                    onClick = {},
                                ) {
                                    Palettes(
                                        palettes =
                                            if (wallpaperTonalPalettes.size > 5) {
                                                wallpaperTonalPalettes.subList(5, wallpaperTonalPalettes.size)
                                            } else {
                                                emptyList()
                                            },
                                        themeIndex = themeIndex,
                                        themeIndexPrefix = 5,
                                        customPrimaryColor = customPrimaryColor,
                                    )
                                },
                                BlockRadioGroupButtonItem(
                                    text = stringResource(Res.string.basic_colors),
                                    onClick = {},
                                ) {
                                    Palettes(
                                        themeIndex = themeIndex,
                                        palettes =
                                            if (wallpaperTonalPalettes.isNotEmpty()) {
                                                wallpaperTonalPalettes.subList(0, minOf(5, wallpaperTonalPalettes.size))
                                            } else {
                                                emptyList()
                                            },
                                        customPrimaryColor = customPrimaryColor,
                                    )
                                },
                            ),
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.appearance),
                    )
                    SettingItem(
                        title = stringResource(Res.string.dark_theme),
                        desc = darkTheme.toDesc(),
                        separatedActions = true,
                        onClick = navigateToDarkTheme,
                    ) {
                        RYSwitch(
                            activated = darkTheme.isDarkTheme()
                        ) {
                            darkThemeNot.put(scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(Res.string.basic_fonts),
                        desc = fonts.toDesc(),
                        onClick = { fontsDialogVisible = true },
                    ) {}
                    Spacer(modifier = Modifier.height(24.dp))
                }
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.style)
                    )
                    SettingItem(
                        title = stringResource(Res.string.feeds_page),
                        onClick = navigateToFeedsPageStyle,
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.flow_page),
                        onClick = navigateToFlowPageStyle,
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.reading_page),
                        onClick = navigateToReadingPageStyle,
                    ) {}
                }
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(
                        modifier =
                            Modifier.windowInsetsBottomHeight(
                                WindowInsets.navigationBars
                            )
                    )
                }
            }
        },
    )

    RadioDialog(
        visible = fontsDialogVisible,
        title = stringResource(Res.string.basic_fonts),
        options =
            BasicFontsPreference.values.map {
                RadioDialogOption(
                    text = it.toDesc(),
                    style = TextStyle(fontFamily = it.asFontFamily()),
                    selected = it == fonts,
                ) {
                    if (it.value == BasicFontsPreference.External.value) {
                        fontPicker()
                    } else {
                        it.put(scope)
                    }
                }
            },
    ) {
        fontsDialogVisible = false
    }
}

@Composable
fun Palettes(
    palettes: List<TonalPalettes>,
    themeIndex: Int = 0,
    themeIndexPrefix: Int = 0,
    customPrimaryColor: String = "",
) {
    val scope = rememberCoroutineScope()
    var addDialogVisible by remember { mutableStateOf(false) }
    var customColorValue by remember { mutableStateOf(customPrimaryColor) }

    val tonalPalettes = customPrimaryColor.safeHexToColor().toTonalPalettes()

    if (palettes.isEmpty()) {
        Row(
            modifier =
                Modifier.padding(horizontal = 24.dp)
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        MaterialTheme.colorScheme.inverseOnSurface onLight
                            MaterialTheme.colorScheme.surfaceContainer,
                    )
                    .clickable {},
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(Res.string.no_palettes),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.inverseSurface,
            )
        }
    } else {
        Row(
            modifier =
                Modifier.horizontalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            palettes.forEachIndexed { index, palette ->
                val isCustom = index == palettes.lastIndex && themeIndexPrefix == 0
                val i = themeIndex - themeIndexPrefix
                SelectableMiniPalette(
                    selected = if (i >= palettes.size) i == 0 else i == index,
                    isCustom = isCustom,
                    onClick = {
                        if (isCustom) {
                            customColorValue = customPrimaryColor
                            addDialogVisible = true
                        } else {
                            ThemeIndexPreference.put(scope, themeIndexPrefix + index)
                        }
                    },
                    palette = if (isCustom) tonalPalettes else palette,
                )
            }
        }
    }

    TextFieldDialog(
        visible = addDialogVisible,
        title = stringResource(Res.string.primary_color),
        icon = Icons.Outlined.Palette,
        value = customColorValue,
        placeholder = stringResource(Res.string.primary_color_hint),
        onValueChange = { customColorValue = it },
        onDismissRequest = { addDialogVisible = false },
        onConfirm = {
            it.checkColorHex()?.let {
                CustomPrimaryColorPreference.put(scope, it)
                ThemeIndexPreference.put(scope, 4)
                addDialogVisible = false
            }
        },
    )
}

@Composable
fun SelectableMiniPalette(
    modifier: Modifier = Modifier,
    selected: Boolean,
    isCustom: Boolean = false,
    onClick: () -> Unit,
    palette: TonalPalettes,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color =
            if (isCustom) {
                MaterialTheme.colorScheme.primaryContainer.copy(0.5f) onDark
                    MaterialTheme.colorScheme.onPrimaryContainer.copy(0.3f)
            } else {
                MaterialTheme.colorScheme.inverseOnSurface onLight
                    MaterialTheme.colorScheme.surfaceContainer
            },
    ) {
        Surface(
            modifier =
                Modifier.clickable { onClick() }
                    .padding(16.dp)
                    .size(48.dp),
            shape = CircleShape,
            color = palette primary 90,
        ) {
            Box {
                Surface(
                    modifier = Modifier.size(48.dp).offset((-24).dp, 24.dp),
                    color = palette tertiary 90,
                ) {}
                Surface(
                    modifier = Modifier.size(48.dp).offset(24.dp, 24.dp),
                    color = palette secondary 60,
                ) {}
                AnimatedVisibility(
                    visible = selected,
                    modifier =
                        Modifier.align(Alignment.Center)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                    enter = fadeIn() + expandIn(expandFrom = Alignment.Center),
                    exit = shrinkOut(shrinkTowards = Alignment.Center) + fadeOut(),
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Check,
                        contentDescription = "Checked",
                        modifier = Modifier.padding(8.dp).size(16.dp),
                        tint = MaterialTheme.colorScheme.surface,
                    )
                }
            }
        }
    }
}
