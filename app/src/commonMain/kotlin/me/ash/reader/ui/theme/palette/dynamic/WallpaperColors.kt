/**
 * Copyright (C) 2021 Kyant0
 *
 * @link https://github.com/Kyant0/MusicYou
 * @author Kyant0
 * @modifier Ashinch
 */

package me.ash.reader.ui.theme.palette.dynamic

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import me.ash.reader.ui.theme.palette.TonalPalettes

object PresetColor {
    val blue = Color(0xFF80BBFF)
    val pink = Color(0xFFFFD8E4)
    val purple = Color(0xFF62539f)
    val yellow = Color(0xFFE9B666)
}

@Composable
@Stable
expect fun extractTonalPalettesFromUserWallpaper(): List<TonalPalettes>
