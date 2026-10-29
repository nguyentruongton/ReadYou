package me.ash.reader.ui.page.settings.color.reading

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
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
import org.jetbrains.compose.resources.stringResource
import androidx.compose.ui.unit.dp
import me.ash.reader.*
import me.ash.reader.infrastructure.preference.LocalReadingTextLetterSpacing
import me.ash.reader.infrastructure.preference.LocalReadingTextAlign
import me.ash.reader.infrastructure.preference.LocalReadingTextBold
import me.ash.reader.infrastructure.preference.LocalReadingTextFontSize
import me.ash.reader.infrastructure.preference.LocalReadingTextHorizontalPadding
import me.ash.reader.infrastructure.preference.LocalReadingTextLineHeight
import me.ash.reader.infrastructure.preference.ReadingTextLetterSpacingPreference
import me.ash.reader.infrastructure.preference.ReadingTextAlignPreference
import me.ash.reader.infrastructure.preference.ReadingTextFontSizePreference
import me.ash.reader.infrastructure.preference.ReadingTextHorizontalPaddingPreference
import me.ash.reader.infrastructure.preference.ReadingTextLineHeightPreference
import me.ash.reader.infrastructure.preference.ReadingTextLineHeightPreference.coerceToRange
import me.ash.reader.infrastructure.preference.ReadingThemePreference
import me.ash.reader.infrastructure.preference.not
import me.ash.reader.infrastructure.preference.toDesc
import me.ash.reader.infrastructure.preference.put
import me.ash.reader.ui.component.base.DisplayText
import me.ash.reader.ui.component.base.FeedbackIconButton
import me.ash.reader.ui.component.base.RYScaffold
import me.ash.reader.ui.component.base.RYSwitch
import me.ash.reader.ui.component.base.RadioDialog
import me.ash.reader.ui.component.base.RadioDialogOption
import me.ash.reader.ui.component.base.Subtitle
import me.ash.reader.ui.component.base.TextFieldDialog
import me.ash.reader.ui.page.settings.SettingItem
import me.ash.reader.ui.theme.palette.onLight

@Composable
fun ReadingTextPage(
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()

    val fontSize = LocalReadingTextFontSize.current
    val lineHeight = LocalReadingTextLineHeight.current
    val letterSpacing = LocalReadingTextLetterSpacing.current
    val horizontalPadding = LocalReadingTextHorizontalPadding.current
    val align = LocalReadingTextAlign.current
    val bold = LocalReadingTextBold.current

    var fontSizeDialogVisible by remember { mutableStateOf(false) }
    var lineHeightDialogVisible by remember { mutableStateOf(false) }
    var letterSpacingDialogVisible by remember { mutableStateOf(false) }
    var horizontalPaddingDialogVisible by remember { mutableStateOf(false) }
    var alignDialogVisible by remember { mutableStateOf(false) }

    var fontSizeValue: Int? by remember { mutableStateOf(fontSize) }
    var letterSpacingValue: String? by remember { mutableStateOf(letterSpacing.toString()) }
    var lineHeightMultipleValue: String by remember(lineHeight) { mutableStateOf(lineHeight.toString()) }
    var horizontalPaddingValue: Int? by remember { mutableStateOf(horizontalPadding) }

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
                    DisplayText(text = stringResource(Res.string.text), desc = "")
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
                        TitleAndTextPreview()
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Text
                item {
                    Subtitle(
                        modifier = Modifier.padding(horizontal = 24.dp),
                        text = stringResource(Res.string.text)
                    )
                    SettingItem(
                        title = stringResource(Res.string.font_size),
                        desc = "${fontSize}sp",
                        onClick = { fontSizeDialogVisible = true },
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.bold),
                        onClick = {
                            (!bold).put(scope)
                            ReadingThemePreference.Custom.put(scope)
                        },
                    ) {
                        RYSwitch(activated = bold.value) {
                            (!bold).put(scope)
                            ReadingThemePreference.Custom.put(scope)
                        }
                    }
                    SettingItem(
                        title = stringResource(Res.string.letter_spacing),
                        desc = "${letterSpacing}sp",
                        onClick = { letterSpacingDialogVisible = true },
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.line_height_multiple),
                        desc = lineHeightMultipleValue,
                        onClick = { lineHeightDialogVisible = true },
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.horizontal_padding),
                        desc = "${horizontalPadding}dp",
                        onClick = { horizontalPaddingDialogVisible = true },
                    ) {}
                    SettingItem(
                        title = stringResource(Res.string.alignment),
                        desc = align.toDesc(),
                        onClick = { alignDialogVisible = true },
                    ) {}
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
                }
            }
        }
    )

    TextFieldDialog(
        visible = fontSizeDialogVisible,
        title = stringResource(Res.string.font_size),
        value = (fontSizeValue ?: "").toString(),
        placeholder = stringResource(Res.string.value),
        onValueChange = {
            fontSizeValue = it.filter { it.isDigit() }.toIntOrNull()
        },
        onDismissRequest = {
            fontSizeDialogVisible = false
        },
        onConfirm = {
            ReadingTextFontSizePreference.put(scope, fontSizeValue ?: 0)
            ReadingThemePreference.Custom.put(scope)
            fontSizeDialogVisible = false
        }
    )

    TextFieldDialog(
        visible = lineHeightDialogVisible,
        title = stringResource(Res.string.line_height_multiple),
        value = lineHeightMultipleValue,
        placeholder = stringResource(Res.string.value),
        onValueChange = {
            lineHeightMultipleValue = it
        },
        onDismissRequest = {
            lineHeightDialogVisible = false
        },
        onConfirm = {
            ReadingTextLineHeightPreference.put(
                scope,
                (lineHeightMultipleValue.toFloatOrNull()
                    ?: ReadingTextLineHeightPreference.default).coerceToRange()
            )
            ReadingThemePreference.Custom.put(scope)
            lineHeightDialogVisible = false
        }
    )

    TextFieldDialog(
        visible = letterSpacingDialogVisible,
        title = stringResource(Res.string.letter_spacing),
        value = (letterSpacingValue ?: "").toString(),
        placeholder = stringResource(Res.string.value),
        onValueChange = {
            letterSpacingValue = it
        },
        onDismissRequest = {
            letterSpacingDialogVisible = false
        },
        onConfirm = {
            ReadingTextLetterSpacingPreference.put(
                scope,
                letterSpacingValue?.toFloatOrNull() ?: 0F
            )
            ReadingThemePreference.Custom.put(scope)
            letterSpacingDialogVisible = false
        }
    )

    TextFieldDialog(
        visible = horizontalPaddingDialogVisible,
        title = stringResource(Res.string.horizontal_padding),
        value = (horizontalPaddingValue ?: "").toString(),
        placeholder = stringResource(Res.string.value),
        onValueChange = {
            horizontalPaddingValue = it.filter { it.isDigit() }.toIntOrNull()
        },
        onDismissRequest = {
            horizontalPaddingDialogVisible = false
        },
        onConfirm = {
            ReadingTextHorizontalPaddingPreference.put(scope, horizontalPaddingValue ?: 0)
            ReadingThemePreference.Custom.put(scope)
            horizontalPaddingDialogVisible = false
        }
    )

    RadioDialog(
        visible = alignDialogVisible,
        title = stringResource(Res.string.alignment),
        options = ReadingTextAlignPreference.values.map {
            RadioDialogOption(
                text = it.toDesc(),
                selected = it == align,
            ) {
                it.put(scope)
                ReadingThemePreference.Custom.put(scope)
            }
        }
    ) {
        alignDialogVisible = false
    }
}
