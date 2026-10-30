package me.ash.reader.ui.theme

import java.nio.file.Files
import me.ash.reader.domain.model.general.Version
import me.ash.reader.infrastructure.preference.BasicFontsPreference
import me.ash.reader.infrastructure.preference.ReadingFontsPreference
import me.ash.reader.ui.ext.getAppVersion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DesktopFontAndVersionTest {

    @Test
    fun desktopAppVersionMatchesReleaseVersion() {
        assertEquals(Version("0.16.2"), getAppVersion())
    }

    @Test
    fun googleSansFontFamilyLoadsOnDesktopAndReadingFontsPreferenceUsesIt() {
        assertNotNull(GoogleSansFontFamily)
        assertEquals(GoogleSansFontFamily, BasicFontsPreference.GoogleSans.asFontFamily())
        assertEquals(GoogleSansFontFamily, ReadingFontsPreference.GoogleSans.asFontFamily())
    }

    @Test
    fun externalFontsFallbackGracefullyWhenMissingOrCorruptedAndExtractBundledFont() {
        val tempDir = Files.createTempDirectory("readyou-fonts-test").toFile()
        try {
            // Missing external font falls back without throwing
            assertNotNull(BasicFontsPreference.External.asFontFamily())
            assertNotNull(ReadingFontsPreference.External.asFontFamily())

            // Corrupted font bytes in targetDir do not crash FontFamily loading
            val corruptedFile = DesktopFontManager.installExternalFont(
                bytes = "not-a-valid-ttf".encodeToByteArray(),
                isReadingFont = true,
                targetDir = tempDir,
            )
            assertTrue(corruptedFile.exists())
            assertNotNull(DesktopFontManager.loadFontFamilyFromFile(corruptedFile))

            // Bundled Google Sans Flex can be extracted for WebView @font-face and has normalized path
            val extracted = DesktopFontManager.ensureBundledGoogleSansExtracted(tempDir)
            assertNotNull(extracted)
            assertTrue(extracted!!.exists() && extracted.length() > 10_000L)
            val fontPath = PlatformFontLoader.resolveWebViewFontPath(ReadingFontsPreference.GoogleSans)
            assertNotNull(fontPath)
            assertTrue("fontPath must not contain backslashes: $fontPath", !fontPath!!.contains('\\'))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
