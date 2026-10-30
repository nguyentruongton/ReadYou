package me.ash.reader.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.platform.Font
import java.io.File
import me.ash.reader.infrastructure.platform.createDesktopAppPaths
import me.ash.reader.infrastructure.preference.ReadingFontsPreference

object DesktopFontManager {

    fun defaultFontsDir(): File {
        val appPaths = createDesktopAppPaths()
        return File(appPaths.dataDir, "fonts").apply { mkdirs() }
    }

    fun getBundledGoogleSansBytes(): ByteArray? {
        val paths = listOf(
            "/composeResources/me.ash.reader/font/google_sans_flex.ttf",
            "/font/google_sans_flex.ttf",
            "composeResources/me.ash.reader/font/google_sans_flex.ttf",
            "font/google_sans_flex.ttf",
        )
        for (path in paths) {
            val stream = DesktopFontManager::class.java.getResourceAsStream(if (path.startsWith("/")) path else "/$path")
                ?: Thread.currentThread().contextClassLoader?.getResourceAsStream(path)
            if (stream != null) {
                return stream.use { it.readBytes() }
            }
        }
        return null
    }

    fun isValidFontBytes(bytes: ByteArray): Boolean {
        if (bytes.size < 4) return false
        val b0 = bytes[0].toInt() and 0xFF
        val b1 = bytes[1].toInt() and 0xFF
        val b2 = bytes[2].toInt() and 0xFF
        val b3 = bytes[3].toInt() and 0xFF
        return (b0 == 0 && b1 == 1 && b2 == 0 && b3 == 0) ||
            (b0 == 'O'.code && b1 == 'T'.code && b2 == 'T'.code && b3 == 'O'.code) ||
            (b0 == 't'.code && b1 == 't'.code && b2 == 'c'.code && b3 == 'f'.code) ||
            (b0 == 't'.code && b1 == 'r'.code && b2 == 'u'.code && b3 == 'e'.code)
    }

    fun loadFontFamilyFromBytes(identity: String, bytes: ByteArray): FontFamily {
        if (!isValidFontBytes(bytes)) return FontFamily.Default
        return runCatching {
            FontFamily(Font(identity = identity, data = bytes))
        }.getOrDefault(FontFamily.Default)
    }

    fun loadFontFamilyFromFile(file: File): FontFamily {
        if (!file.exists() || !file.isFile || file.length() < 4) return FontFamily.Default
        val bytes = runCatching { file.readBytes() }.getOrNull() ?: return FontFamily.Default
        return loadFontFamilyFromBytes(file.nameWithoutExtension, bytes)
    }

    fun installExternalFont(bytes: ByteArray, isReadingFont: Boolean, targetDir: File = defaultFontsDir()): File {
        val fileName = if (isReadingFont) "reading_font.ttf" else "basic_font.ttf"
        targetDir.mkdirs()
        val targetFile = File(targetDir, fileName)
        targetFile.writeBytes(bytes)
        return targetFile
    }

    fun ensureBundledGoogleSansExtracted(targetDir: File = defaultFontsDir()): File? {
        targetDir.mkdirs()
        val targetFile = File(targetDir, "google_sans_flex.ttf")
        if (!targetFile.exists() || targetFile.length() == 0L) {
            val bytes = getBundledGoogleSansBytes() ?: return null
            targetFile.writeBytes(bytes)
        }
        return targetFile
    }

    fun loadBundledGoogleSansFontFamily(): FontFamily {
        val bytes = getBundledGoogleSansBytes()
        return if (bytes != null && bytes.isNotEmpty()) {
            loadFontFamilyFromBytes("google_sans_flex", bytes)
        } else {
            FontFamily.SansSerif
        }
    }
}

actual val GoogleSansFontFamily: FontFamily = DesktopFontManager.loadBundledGoogleSansFontFamily()

actual object PlatformFontLoader {
    actual fun loadBasicFontFamily(): FontFamily {
        val file = File(DesktopFontManager.defaultFontsDir(), "basic_font.ttf")
        return DesktopFontManager.loadFontFamilyFromFile(file)
    }

    actual fun loadReadingFontFamily(): FontFamily {
        val file = File(DesktopFontManager.defaultFontsDir(), "reading_font.ttf")
        return DesktopFontManager.loadFontFamilyFromFile(file)
    }

    actual fun resolveWebViewFontPath(readingFonts: ReadingFontsPreference): String? {
        return when (readingFonts) {
            is ReadingFontsPreference.External -> {
                val file = File(DesktopFontManager.defaultFontsDir(), "reading_font.ttf")
                if (file.exists() && file.length() > 0) file.absolutePath else null
            }
            is ReadingFontsPreference.GoogleSans -> {
                val extracted = DesktopFontManager.ensureBundledGoogleSansExtracted()
                extracted?.absolutePath
            }
            else -> null
        }
    }
}
