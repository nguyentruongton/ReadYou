package me.ash.reader.ui.theme

import androidx.compose.ui.text.font.FontFamily
import me.ash.reader.infrastructure.preference.ReadingFontsPreference

expect object PlatformFontLoader {
    fun loadBasicFontFamily(): FontFamily
    fun loadReadingFontFamily(): FontFamily
    fun resolveWebViewFontPath(readingFonts: ReadingFontsPreference): String?
}
