package me.ash.reader.ui.theme.palette.dynamic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import me.ash.reader.infrastructure.preference.LocalCustomPrimaryColor
import me.ash.reader.ui.theme.palette.TonalPalettes
import me.ash.reader.ui.theme.palette.TonalPalettes.Companion.toTonalPalettes
import me.ash.reader.ui.theme.palette.safeHexToColor

@Composable
@Stable
actual fun extractTonalPalettesFromUserWallpaper(): List<TonalPalettes> {
    val customPrimaryColor = LocalCustomPrimaryColor.current

    return listOf(
        PresetColor.blue.toTonalPalettes(),
        PresetColor.pink.toTonalPalettes(),
        PresetColor.purple.toTonalPalettes(),
        PresetColor.yellow.toTonalPalettes(),
        customPrimaryColor.safeHexToColor().toTonalPalettes()
    )
}
