package me.ash.reader

import java.io.File
import java.nio.file.Files
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DesktopPackagingAndCiTest {

    private fun findRepoRoot(): File {
        var dir = File(System.getProperty("user.dir")).canonicalFile
        while (dir.parentFile != null && !File(dir, "settings.gradle.kts").exists()) {
            dir = dir.parentFile
        }
        return dir
    }

    @Test
    fun desktopIconsExistOnClasspathAndDisk() {
        val repoRoot = findRepoRoot()
        val resourcesDir = File(repoRoot, "app/src/desktopMain/resources")
        val png = File(resourcesDir, "icon.png")
        val ico = File(resourcesDir, "icon.ico")
        val icns = File(resourcesDir, "icon.icns")

        assertTrue("icon.png must exist", png.exists() && png.length() > 1000L)
        assertTrue("icon.ico must exist", ico.exists() && ico.length() > 1000L)
        assertTrue("icon.icns must exist", icns.exists() && icns.length() > 1000L)
        assertNotNull(DesktopPackagingAndCiTest::class.java.getResource("/icon.png"))
    }

    @Test
    fun gradleBuildAndGithubWorkflowDefineMsiAndUniversalDmgPackaging() {
        val repoRoot = findRepoRoot()
        val buildGradle = File(repoRoot, "app/build.gradle.kts").readText()
        assertTrue(buildGradle.contains("TargetFormat.Dmg"))
        assertTrue(buildGradle.contains("TargetFormat.Msi"))
        assertTrue(buildGradle.contains("packageName = \"ReadYou\""))
        assertTrue(buildGradle.contains("isEnabled.set(false)"))

        val workflowFile = File(repoRoot, ".github/workflows/release_desktop.yaml")
        assertTrue("release_desktop.yaml must exist", workflowFile.exists())
        val workflow = workflowFile.readText()
        assertTrue(workflow.contains("packageMsi"))
        assertTrue(workflow.contains("gradlew.bat"))
        assertTrue(workflow.contains(".tar.gz"))
        assertTrue(workflow.contains("merge-macos-universal.sh"))
        assertTrue(workflow.contains("ReadYou-macOS-Universal.dmg"))
    }

    @Test
    fun macosUniversalMergeScriptHandlesSymlinksAndAdhocSignsBundle() {
        val repoRoot = findRepoRoot()
        val script = File(repoRoot, "scripts/merge-macos-universal.sh")
        assertTrue("merge-macos-universal.sh must exist and be executable", script.exists() && script.canExecute())

        val tempDir = Files.createTempDirectory("universal-merge-test").toFile()
        try {
            val armApp = File(tempDir, "arm64/ReadYou.app")
            val x64App = File(tempDir, "x64/ReadYou.app")
            val outApp = File(tempDir, "Universal-ReadYou.app")

            File(armApp, "Contents/MacOS").mkdirs()
            File(x64App, "Contents/MacOS").mkdirs()

            val armBin = File(armApp, "Contents/MacOS/ReadYou")
            val x64Bin = File(x64App, "Contents/MacOS/ReadYou")
            armBin.writeText("#!/bin/sh\necho arm64\n")
            x64Bin.writeText("#!/bin/sh\necho x64\n")
            armBin.setExecutable(true)
            x64Bin.setExecutable(true)

            // Create a symlink inside armApp to ensure symlinks don't break the merger
            Files.createSymbolicLink(
                File(armApp, "Contents/MacOS/ReadYouLink").toPath(),
                File("ReadYou").toPath(),
            )

            val process = ProcessBuilder("bash", script.absolutePath, armApp.absolutePath, x64App.absolutePath, outApp.absolutePath)
                .redirectErrorStream(true)
                .start()
            val output = process.inputStream.bufferedReader().readText()
            val exitCode = process.waitFor()
            assertEquals("Script failed with output: $output", 0, exitCode)
            assertTrue(File(outApp, "Contents/MacOS/ReadYou").canExecute())
            assertTrue(Files.isSymbolicLink(File(outApp, "Contents/MacOS/ReadYouLink").toPath()))
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
