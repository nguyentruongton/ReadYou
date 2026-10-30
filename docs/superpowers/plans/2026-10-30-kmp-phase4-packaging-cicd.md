# KMP Phase 4: Desktop Native Packaging, CI/CD & Final OS Integration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Configure Desktop native installer packaging (`.msi` and macOS Universal `.dmg`), add the 100% free multi-architecture GitHub Actions workflow with `lipo` universal merging and ad-hoc code signing, and complete Desktop periodic background sync, OS notifications, and Google Sans / External Custom Font support.

**Architecture:** We extend the existing single `:app` Kotlin Multiplatform module (`commonMain`, `androidMain`, `desktopMain`). Desktop OS features (`DesktopSyncScheduler`, `DesktopNotificationManager`, `DesktopFontManager`) are implemented in `desktopMain` with constructor-injected or callback seams so all behavior is verifiable in headless unit tests (`desktopTest`). Native packaging is configured in `compose.desktop.application.nativeDistributions` (`app/build.gradle.kts`) with ProGuard disabled on Desktop release builds, accompanied by `scripts/merge-macos-universal.sh` and `.github/workflows/release_desktop.yaml`.

**Tech Stack:** Kotlin Multiplatform 2.2.0, Compose Multiplatform 1.8.2 (`compose.desktop`), Coroutines Test (`StandardTestDispatcher`), JLink / `jpackage` (`.msi`, `.dmg`), Xcode `lipo` / `codesign` / `hdiutil`, GitHub Actions.

**Spec:** [`docs/superpowers/specs/2026-10-30-kmp-phase4-packaging-cicd-design.md`](file:///Users/stark/Documents/GitHub/ReadYou/docs/superpowers/specs/2026-10-30-kmp-phase4-packaging-cicd-design.md)

## Global Constraints

- Do not enable ProGuard obfuscation on Desktop release builds (`buildTypes.release.proguard.isEnabled.set(false)`).
- Keep `packageVersion = "1.16.2"` (or `msiPackageVersion = "1.16.2"` / `dmgPackageVersion = "1.16.2"`) in `nativeDistributions` so Windows WiX `jpackage` (`MAJOR >= 1` requirement) and macOS `jpackage` both succeed, while `getAppVersion()` in `PlatformUiHelper.desktop.kt` returns `Version("0.16.2")` to match Android's `versionName = "0.16.2"`.
- Include JDK modules `"java.sql"`, `"java.naming"`, `"jdk.unsupported"`, `"java.desktop"`, `"java.management"` in `nativeDistributions`.
- All Desktop unit tests (`./gradlew :app:desktopTest`) must pass in headless mode without requiring an active display server or user interaction.
- Android compilation (`./gradlew :app:compileGithubDebugKotlinAndroid`) must continue to pass with zero errors.

## Review Focus

1. **Re-initializing periodic sync (`initSync` called multiple times when switching accounts or changing interval):** Calling `initSync` a second time must cancel the previous periodic coroutine loop before starting a new one so duplicate timers never accumulate.
2. **Manual sync interval (`SyncIntervalPreference.Manually` where `value == 0L`):** Passing `SyncIntervalPreference.Manually` (or any interval `<= 0L`) must not enter an infinite `delay(0)` tight loop; it must cancel any active periodic timer and not schedule a periodic loop.
3. **Notification filtering and escaping (`DesktopNotificationManager`):** Feeds with `isNotification == false` or an empty `articles` list must never trigger OS notifications, and article titles containing double quotes (`"`) or backslashes (`\`) must be escaped safely for AppleScript (`osascript`).
4. **Corrupted or missing custom external font files (`basic_font.ttf` / `reading_font.ttf`):** If the user selects `BasicFontsPreference.External` or `ReadingFontsPreference.External` before picking a font file (or if the file is unreadable), `asFontFamily()` and `asTypography()` must fall back cleanly to `FontFamily.Default` / `SystemTypography` without throwing an uncaught exception.
5. **macOS Universal merger handling of symlinks and already-fat Mach-O binaries (`scripts/merge-macos-universal.sh`):** App bundles contain symbolic links inside `Contents/runtime` and potentially pre-universal `.dylib` files; `merge-macos-universal.sh` must skip symlinks (`! -L`) and only run `lipo -create` when the `arm64` and `x86_64` Mach-O files have distinct architecture sets.

---

### Task 1: Periodic Background Sync in `DesktopSyncScheduler` & Desktop Notification Manager

**Files:**
- Modify: `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/platform/SyncScheduler.desktop.kt`
- Create: `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/platform/DesktopNotificationManager.kt`
- Modify: `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/di/PlatformModule.desktop.kt`
- Create: `app/src/desktopTest/kotlin/me/ash/reader/infrastructure/platform/DesktopSyncAndNotificationTest.kt`

**Interfaces:**
- Consumes: `SyncScheduler`, `NotificationManager`, `Account`, `SyncIntervalPreference`, `SyncOnStartPreference`, `FeedWithArticle` from `commonMain`
- Produces:
  - `DesktopSyncScheduler.schedulePeriodicSync(accountId: Int?, intervalMinutes: Long)`
  - `DesktopNotificationManager(onSendNotification: (title: String, message: String) -> Unit = DesktopNotificationManager::sendOsNotification)`
  - `DesktopNotificationManager.buildAppleScriptNotificationCommand(title: String, message: String): List<String>`

- [ ] **Step 1: Write the failing tests in `app/src/desktopTest/kotlin/me/ash/reader/infrastructure/platform/DesktopSyncAndNotificationTest.kt`**

```kotlin
package me.ash.reader.infrastructure.platform

import java.util.Date
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.AccountType
import me.ash.reader.domain.model.article.Article
import me.ash.reader.domain.model.feed.Feed
import me.ash.reader.domain.model.feed.FeedWithArticle
import me.ash.reader.infrastructure.preference.SyncIntervalPreference
import me.ash.reader.infrastructure.preference.SyncOnStartPreference
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DesktopSyncAndNotificationTest {

    @Test
    fun initSyncTriggersImmediateSyncWhenSyncOnStartEnabledAndSchedulesPeriodicTimer() = runTest {
        val syncedAccounts = mutableListOf<Int?>()
        val schedulerScope = TestScope(StandardTestDispatcher(testScheduler))
        val scheduler = DesktopSyncScheduler(
            coroutineScope = schedulerScope,
            onSyncRequest = { accountId, _, _ -> syncedAccounts.add(accountId) },
        )

        val account = Account(
            id = 7,
            name = "Local",
            type = AccountType.Local,
            syncOnStart = SyncOnStartPreference(true),
            syncInterval = SyncIntervalPreference.Every15Minutes,
        )

        scheduler.initSync(account)
        runCurrent()
        assertEquals(listOf<Int?>(7), syncedAccounts)

        advanceTimeBy(15 * 60_000L)
        runCurrent()
        assertEquals(listOf<Int?>(7, 7), syncedAccounts)

        advanceTimeBy(15 * 60_000L)
        runCurrent()
        assertEquals(listOf<Int?>(7, 7, 7), syncedAccounts)

        scheduler.cancelSync()
    }

    @Test
    fun initSyncReplacesPreviousPeriodicTimerAndDoesNotLoopWhenIntervalIsManual() = runTest {
        val syncedAccounts = mutableListOf<Int?>()
        val schedulerScope = TestScope(StandardTestDispatcher(testScheduler))
        val scheduler = DesktopSyncScheduler(
            coroutineScope = schedulerScope,
            onSyncRequest = { accountId, _, _ -> syncedAccounts.add(accountId) },
        )

        val firstAccount = Account(
            id = 1,
            name = "First",
            type = AccountType.Local,
            syncOnStart = SyncOnStartPreference(false),
            syncInterval = SyncIntervalPreference.Every15Minutes,
        )
        val manualAccount = Account(
            id = 2,
            name = "Manual",
            type = AccountType.Local,
            syncOnStart = SyncOnStartPreference(false),
            syncInterval = SyncIntervalPreference.Manually,
        )

        scheduler.initSync(firstAccount)
        scheduler.initSync(manualAccount)

        advanceTimeBy(60 * 60_000L)
        runCurrent()
        assertTrue(syncedAccounts.isEmpty())
    }

    @Test
    fun notificationManagerOnlyNotifiesWhenFeedHasNotificationEnabledAndArticlesNonEmpty() {
        val delivered = mutableListOf<Pair<String, String>>()
        val manager = DesktopNotificationManager(
            onSendNotification = { title, message -> delivered.add(title to message) }
        )

        val disabledFeed = Feed(
            id = "f1",
            name = "Disabled Feed",
            url = "https://example.com/rss",
            groupId = "g1",
            accountId = 1,
            isNotification = false,
        )
        val enabledFeed = disabledFeed.copy(id = "f2", name = "Kotlin Blog", isNotification = true)
        val article1 = Article(
            id = "a1",
            date = Date(),
            title = "Kotlin \"2.2\" Released",
            rawDescription = "Desc",
            shortDescription = "Short",
            link = "https://kotlinlang.org",
            feedId = "f2",
            accountId = 1,
        )
        val article2 = article1.copy(id = "a2", title = "Compose Desktop Update")

        manager.notify(FeedWithArticle(disabledFeed, listOf(article1)))
        manager.notify(FeedWithArticle(enabledFeed, emptyList()))
        assertTrue(delivered.isEmpty())

        manager.notify(FeedWithArticle(enabledFeed, listOf(article1)))
        assertEquals(1, delivered.size)
        assertEquals("Kotlin Blog" to "Kotlin \"2.2\" Released", delivered[0])

        manager.notify(FeedWithArticle(enabledFeed, listOf(article1, article2)))
        assertEquals(2, delivered.size)
        assertEquals("Kotlin Blog" to "2 new articles: Kotlin \"2.2\" Released", delivered[1])

        val cmd = DesktopNotificationManager.buildAppleScriptNotificationCommand(
            title = "Kotlin \"Blog\"",
            message = "Line\\1 \"Quote\"",
        )
        assertEquals(
            listOf(
                "/usr/bin/osascript",
                "-e",
                "display notification \"Line\\\\1 \\\"Quote\\\"\" with title \"Kotlin \\\"Blog\\\"\"",
            ),
            cmd,
        )
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"; export PATH="$JAVA_HOME/bin:$PATH"; export GRADLE_USER_HOME="/tmp/.gradle"; ./gradlew :app:desktopTest --tests "me.ash.reader.infrastructure.platform.DesktopSyncAndNotificationTest"`
Expected: FAIL with compilation error (`DesktopNotificationManager` unresolved).

- [ ] **Step 3: Implement `DesktopSyncScheduler` periodic timer in `SyncScheduler.desktop.kt` and `DesktopNotificationManager` in `DesktopNotificationManager.kt`, then wire `PlatformModule.desktop.kt`**

In `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/platform/SyncScheduler.desktop.kt`:
- Track `private var activeSyncJob: Job? = null` and `private var periodicSyncJob: Job? = null`.
- Extract `private suspend fun executeSync(accountId: Int?, feedId: String?, groupId: String?)` setting `_isSyncing.value = true` in `try` and `false` in `finally`.
- In `triggerImmediateSync`, launch on `coroutineScope` and store in `activeSyncJob`.
- Add `fun schedulePeriodicSync(accountId: Int?, intervalMinutes: Long)`: cancel `periodicSyncJob`; if `intervalMinutes <= 0L`, return; otherwise launch a `while (isActive)` loop on `coroutineScope` that calls `delay(intervalMinutes * 60_000L)` then `executeSync(accountId, null, null)`.
- In `initSync(account: Account)`, call `schedulePeriodicSync(account.id, account.syncInterval.value)` and if `account.syncOnStart.value` is true, call `triggerImmediateSync(account.id)`.
- In `cancelSync()`, cancel `activeSyncJob` and `periodicSyncJob` and set `_isSyncing.value = false`.

In `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/platform/DesktopNotificationManager.kt`:
- Implement `class DesktopNotificationManager(private val onSendNotification: (title: String, message: String) -> Unit = ::sendOsNotification) : NotificationManager`.
- In `override fun notify(feedWithArticle: FeedWithArticle)`: return early if `!feedWithArticle.feed.isNotification || feedWithArticle.articles.isEmpty()`; compute `title = feedWithArticle.feed.name` and `message = if (articles.size == 1) articles.first().title else "${articles.size} new articles: ${articles.first().title}"`; invoke `onSendNotification(title, message)`.
- In `companion object`:
  - `fun buildAppleScriptNotificationCommand(title: String, message: String): List<String>` escaping `\` as `\\` and `"` as `\"`.
  - `fun sendOsNotification(title: String, message: String)` checking `if (GraphicsEnvironment.isHeadless()) return`, using `ProcessBuilder(buildAppleScriptNotificationCommand(title, message)).start()` on macOS, or `SystemTray.getSystemTray().trayIcons.firstOrNull()?.displayMessage(title, message, TrayIcon.MessageType.INFO)` when `SystemTray.isSupported()`.

In `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/di/PlatformModule.desktop.kt`:
- Replace the anonymous no-op `NotificationManager` singleton with `me.ash.reader.infrastructure.platform.DesktopNotificationManager()`.

- [ ] **Step 4: Run test to verify it passes**

Run: `export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"; export PATH="$JAVA_HOME/bin:$PATH"; export GRADLE_USER_HOME="/tmp/.gradle"; ./gradlew :app:desktopTest --tests "me.ash.reader.infrastructure.platform.DesktopSyncAndNotificationTest"`
Expected: PASS (3 tests passing).

- [ ] **Step 5: Commit**

```bash
git add app/src/desktopMain/kotlin/me/ash/reader/infrastructure/platform/SyncScheduler.desktop.kt \
        app/src/desktopMain/kotlin/me/ash/reader/infrastructure/platform/DesktopNotificationManager.kt \
        app/src/desktopMain/kotlin/me/ash/reader/infrastructure/di/PlatformModule.desktop.kt \
        app/src/desktopTest/kotlin/me/ash/reader/infrastructure/platform/DesktopSyncAndNotificationTest.kt
git commit -m "feat(desktop): add periodic background sync timer and OS notification manager"
```

---

### Task 2: Desktop Typography (`GoogleSansFontFamily`), Custom External Fonts & Version Alignment

**Files:**
- Create: `app/src/commonMain/kotlin/me/ash/reader/ui/theme/ExternalFontManager.kt`
- Modify: `app/src/androidMain/kotlin/me/ash/reader/ui/theme/Type.android.kt`
- Modify: `app/src/desktopMain/kotlin/me/ash/reader/ui/theme/Type.desktop.kt`
- Modify: `app/src/commonMain/kotlin/me/ash/reader/infrastructure/preference/BasicFontsPreference.kt`
- Modify: `app/src/commonMain/kotlin/me/ash/reader/infrastructure/preference/ReadingFontsPreference.kt`
- Modify: `app/src/desktopMain/kotlin/me/ash/reader/ui/ext/PlatformUiHelper.desktop.kt`
- Modify: `app/src/desktopMain/kotlin/me/ash/reader/ui/component/webview/RYWebView.desktop.kt`
- Create: `app/src/desktopTest/kotlin/me/ash/reader/ui/theme/DesktopFontAndVersionTest.kt`

**Interfaces:**
- Consumes: `GoogleSansFontFamily` (`Type.kt`), `AppPaths` (`AppPaths.desktop.kt`)
- Produces:
  - `expect object PlatformFontLoader` in `app/src/commonMain/kotlin/me/ash/reader/ui/theme/ExternalFontManager.kt` with:
    - `fun loadBasicFontFamily(): FontFamily`
    - `fun loadReadingFontFamily(): FontFamily`
    - `fun resolveWebViewFontPath(readingFonts: ReadingFontsPreference): String?`
  - `DesktopFontManager` in `app/src/desktopMain/kotlin/me/ash/reader/ui/theme/Type.desktop.kt`:
    - `fun installExternalFont(bytes: ByteArray, isReadingFont: Boolean, targetDir: File = defaultFontsDir()): File`
    - `fun ensureBundledGoogleSansExtracted(targetDir: File = defaultFontsDir()): File?`

- [ ] **Step 1: Write the failing test in `app/src/desktopTest/kotlin/me/ash/reader/ui/theme/DesktopFontAndVersionTest.kt`**

```kotlin
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

            // Bundled Google Sans Flex can be extracted for WebView @font-face
            val extracted = DesktopFontManager.ensureBundledGoogleSansExtracted(tempDir)
            assertNotNull(extracted)
            assertTrue(extracted!!.exists() && extracted.length() > 10_000L)
        } finally {
            tempDir.deleteRecursively()
        }
    }
}
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"; export PATH="$JAVA_HOME/bin:$PATH"; export GRADLE_USER_HOME="/tmp/.gradle"; ./gradlew :app:desktopTest --tests "me.ash.reader.ui.theme.DesktopFontAndVersionTest"`
Expected: FAIL (`DesktopFontManager` unresolved and `getAppVersion()` is `0.7.2`).

- [ ] **Step 3: Implement `ExternalFontManager.kt`, `Type.desktop.kt`, `Type.android.kt`, `BasicFontsPreference.kt`, `ReadingFontsPreference.kt`, `PlatformUiHelper.desktop.kt`, and `RYWebView.desktop.kt`**

1. Create `app/src/commonMain/kotlin/me/ash/reader/ui/theme/ExternalFontManager.kt`:
   ```kotlin
   package me.ash.reader.ui.theme

   import androidx.compose.ui.text.font.FontFamily
   import me.ash.reader.infrastructure.preference.ReadingFontsPreference

   expect object PlatformFontLoader {
       fun loadBasicFontFamily(): FontFamily
       fun loadReadingFontFamily(): FontFamily
       fun resolveWebViewFontPath(readingFonts: ReadingFontsPreference): String?
   }
   ```
2. In `app/src/androidMain/kotlin/me/ash/reader/ui/theme/Type.android.kt`, add `actual object PlatformFontLoader` delegating to `ExternalFonts.loadBasicTypography(AndroidApp.instance).displayLarge.fontFamily ?: FontFamily.Default`, `ExternalFonts.loadReadingTypography(AndroidApp.instance).displayLarge.fontFamily ?: FontFamily.Default`, and returning `ExternalFonts.FontType.ReadingFont.toPath(AndroidApp.instance)` for `ReadingFontsPreference.External` or `"/android_res/font/google_sans_flex.ttf"` for `ReadingFontsPreference.GoogleSans`.
3. In `app/src/desktopMain/kotlin/me/ash/reader/ui/theme/Type.desktop.kt`:
   - Implement `object DesktopFontManager`:
     - Loads `/composeResources/me.ash.reader/font/google_sans_flex.ttf` (with fallback to `/font/google_sans_flex.ttf`) from classpath.
     - `fun loadFontFamilyFromBytes(identity: String, bytes: ByteArray): FontFamily`: validates with `org.jetbrains.skia.Data.makeFromBytes(bytes)` and `org.jetbrains.skia.Typeface.makeFromData(...)` (or checks magic header / `runCatching { FontFamily(androidx.compose.ui.text.platform.Font(identity = identity, data = bytes)) }`) so invalid/corrupted font bytes safely return `FontFamily.Default`.
     - `fun loadFontFamilyFromFile(file: File): FontFamily`: if `file.exists()` and `file.length() > 4`, reads bytes and checks TTF/OTF signature (`0x00, 0x01, 0x00, 0x00` or `'O','T','T','O'` or `'t','t','c','f'`) before calling `loadFontFamilyFromBytes`, falling back to `FontFamily.Default`.
     - `fun installExternalFont(bytes: ByteArray, isReadingFont: Boolean, targetDir: File = defaultFontsDir()): File` writes `basic_font.ttf` or `reading_font.ttf` into `targetDir`.
     - `fun ensureBundledGoogleSansExtracted(targetDir: File = defaultFontsDir()): File?` extracts bundled `google_sans_flex.ttf` to `File(targetDir, "google_sans_flex.ttf")` if not already present.
   - Define `actual val GoogleSansFontFamily: FontFamily` using `DesktopFontManager.loadBundledGoogleSansFontFamily()`.
   - Define `actual object PlatformFontLoader` using `DesktopFontManager`.
4. Update `BasicFontsPreference.kt` and `ReadingFontsPreference.kt`:
   - In `BasicFontsPreference`: `External -> PlatformFontLoader.loadBasicFontFamily()` and in `asTypography()`: `External -> SystemTypography.applyFontFamily(PlatformFontLoader.loadBasicFontFamily())`.
   - In `ReadingFontsPreference`: `GoogleSans -> GoogleSansFontFamily` and `External -> PlatformFontLoader.loadReadingFontFamily()`.
5. Update `app/src/desktopMain/kotlin/me/ash/reader/ui/ext/PlatformUiHelper.desktop.kt`:
   - Change `actual fun getAppVersion(): Version = Version("0.16.2")`.
   - Implement `rememberBasicFontPicker` and `rememberReadingFontPicker` using `java.awt.FileDialog` to pick a `.ttf`/`.otf` file, calling `DesktopFontManager.installExternalFont(file.readBytes(), isReadingFont = ...)` and `onLoaded()`.
6. Update `app/src/desktopMain/kotlin/me/ash/reader/ui/component/webview/RYWebView.desktop.kt`:
   - Add `fontPath: String? = null` parameter to `buildDesktopWebViewHtml(...)` and pass it to `WebViewStyle.get(fontSize = fontSize, fontPath = fontPath, ...)`.
   - Read `val readingFonts = LocalReadingFonts.current` and `val fontPath = remember(readingFonts) { PlatformFontLoader.resolveWebViewFontPath(readingFonts) }`, passing `fontPath` into `buildDesktopWebViewHtml(...)`.

- [ ] **Step 4: Run test to verify it passes**

Run: `export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"; export PATH="$JAVA_HOME/bin:$PATH"; export GRADLE_USER_HOME="/tmp/.gradle"; ./gradlew :app:desktopTest --tests "me.ash.reader.ui.theme.DesktopFontAndVersionTest"`
Expected: PASS (3 tests passing).

- [ ] **Step 5: Commit**

```bash
git add app/src/commonMain/kotlin/me/ash/reader/ui/theme/ExternalFontManager.kt \
        app/src/androidMain/kotlin/me/ash/reader/ui/theme/Type.android.kt \
        app/src/desktopMain/kotlin/me/ash/reader/ui/theme/Type.desktop.kt \
        app/src/commonMain/kotlin/me/ash/reader/infrastructure/preference/BasicFontsPreference.kt \
        app/src/commonMain/kotlin/me/ash/reader/infrastructure/preference/ReadingFontsPreference.kt \
        app/src/desktopMain/kotlin/me/ash/reader/ui/ext/PlatformUiHelper.desktop.kt \
        app/src/desktopMain/kotlin/me/ash/reader/ui/component/webview/RYWebView.desktop.kt \
        app/src/desktopTest/kotlin/me/ash/reader/ui/theme/DesktopFontAndVersionTest.kt
git commit -m "feat(desktop): load Google Sans Flex font, wire external font pickers, and align Desktop version"
```

---

### Task 3: Desktop Native Packaging (`app/build.gradle.kts`), App Icons (`icon.icns`, `icon.ico`, `icon.png`), Universal Merger Script & GitHub Actions CI/CD

**Files:**
- Create: `app/src/desktopMain/resources/icon.png`
- Create: `app/src/desktopMain/resources/icon.ico`
- Create: `app/src/desktopMain/resources/icon.icns`
- Modify: `app/build.gradle.kts`
- Create: `scripts/merge-macos-universal.sh`
- Create: `.github/workflows/release_desktop.yaml`
- Create: `app/src/desktopTest/kotlin/me/ash/reader/DesktopPackagingAndCiTest.kt`

**Interfaces:**
- Consumes: `app/src/main/ic_launcher-playstore.png` (512x512 source icon)
- Produces:
  - `app/src/desktopMain/resources/icon.png`, `icon.ico`, `icon.icns`
  - `compose.desktop.application.nativeDistributions` in `app/build.gradle.kts`
  - `scripts/merge-macos-universal.sh <arm64-app> <x86_64-app> <universal-output-app>`
  - `.github/workflows/release_desktop.yaml`

- [ ] **Step 1: Write the failing test in `app/src/desktopTest/kotlin/me/ash/reader/DesktopPackagingAndCiTest.kt`**

```kotlin
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
```

- [ ] **Step 2: Run the test to verify it fails**

Run: `export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"; export PATH="$JAVA_HOME/bin:$PATH"; export GRADLE_USER_HOME="/tmp/.gradle"; ./gradlew :app:desktopTest --tests "me.ash.reader.DesktopPackagingAndCiTest"`
Expected: FAIL (`icon.png must exist`).

- [ ] **Step 3: Generate Desktop Icons, configure `nativeDistributions` in `app/build.gradle.kts`, create `scripts/merge-macos-universal.sh` and `.github/workflows/release_desktop.yaml`**

1. Generate `app/src/desktopMain/resources/icon.png`, `icon.ico`, and `icon.icns` from `app/src/main/ic_launcher-playstore.png` using `sips` / `iconutil` and Python ICO header packing.
2. Update `app/build.gradle.kts`:
   - Import `org.jetbrains.compose.desktop.application.dsl.TargetFormat`.
   - Configure `compose.desktop.application`:
     - `mainClass = "me.ash.reader.MainKt"`
     - `buildTypes.release.proguard { isEnabled.set(false) }`
     - `nativeDistributions`:
       - `targetFormats(TargetFormat.Dmg, TargetFormat.Msi)`
       - `packageName = "ReadYou"`
       - `packageVersion = "1.16.2"`
       - `description = "Modern Material You RSS Reader for Android and Desktop"`
       - `vendor = "ReadYou"`
       - `licenseFile.set(rootProject.file("LICENSE"))`
       - `modules("java.sql", "java.naming", "jdk.unsupported", "java.desktop", "java.management")`
       - `macOS { bundleID = "me.ash.reader"; dockName = "ReadYou"; iconFile.set(project.file("src/desktopMain/resources/icon.icns")) }`
       - `windows { menuGroup = "ReadYou"; shortcut = true; dirChooser = true; upgradeUuid = "8f4e2a1b-9c3d-4e5f-a6b7-c8d9e0f1a2b3"; iconFile.set(project.file("src/desktopMain/resources/icon.ico")) }`
       - `linux { iconFile.set(project.file("src/desktopMain/resources/icon.png")) }`
3. Create executable `scripts/merge-macos-universal.sh`:
   - Validates arguments `<arm64-app> <x86_64-app> <output-app>`.
   - Copies `<arm64-app>` to `<output-app>` using `cp -R` (preserving symlinks).
   - Iterates regular non-symlink files (`find "$ARM_APP" -type f ! -L`):
     - When `file -b "$arm_file" | grep -q "Mach-O"` and `[ -f "$x64_file" ] && [ ! -L "$x64_file" ]` and `file -b "$x64_file" | grep -q "Mach-O"`:
       - Compares `lipo -archs "$arm_file"` and `lipo -archs "$x64_file"`. If they differ and neither already contains both, runs `lipo -create "$arm_file" "$x64_file" -output "$univ_file"`.
   - Ensures `chmod +x "$OUT_APP/Contents/MacOS/ReadYou"`.
   - If `codesign` is available on PATH and `$OUT_APP/Contents/Info.plist` exists, runs `codesign --force --deep --sign "${MACOS_SIGN_IDENTITY:--}" "$OUT_APP"`.
4. Create `.github/workflows/release_desktop.yaml`:
   - Triggers on `workflow_dispatch` and `push: tags: ['v*']`.
   - `build-windows` (`windows-latest`): sets up JDK 17 (`temurin`), sets up Gradle, runs `./gradlew :app:packageMsi`, uploads `app/build/compose/binaries/main/msi/*.msi`.
   - `build-macos-arch` (matrix `{ arch: arm64, runner: macos-15 }` and `{ arch: x86_64, runner: macos-13 }`): sets up JDK 17 (`temurin`), sets up Gradle, runs `./gradlew :app:createDistributable`, uploads `app/build/compose/binaries/main/app/ReadYou.app` as `ReadYou-macOS-${{ matrix.arch }}`.
   - `package-macos-universal` (`macos-15`, `needs: build-macos-arch`): downloads both artifacts, runs `bash scripts/merge-macos-universal.sh build-arm64/ReadYou.app build-x64/ReadYou.app Universal-ReadYou.app`, creates `ReadYou-macOS-Universal.dmg` with `hdiutil create -volname "ReadYou" -srcfolder Universal-ReadYou.app -ov -format UDZO ReadYou-macOS-Universal.dmg`, and uploads `ReadYou-macOS-Universal.dmg`.

- [ ] **Step 4: Run test to verify it passes**

Run: `export JAVA_HOME="/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home"; export PATH="$JAVA_HOME/bin:$PATH"; export GRADLE_USER_HOME="/tmp/.gradle"; ./gradlew :app:desktopTest && ./gradlew :app:compileGithubDebugKotlinAndroid`
Expected: PASS (All Desktop unit tests and Android debug compilation succeed).

- [ ] **Step 5: Commit**

```bash
git add app/src/desktopMain/resources/icon.png \
        app/src/desktopMain/resources/icon.ico \
        app/src/desktopMain/resources/icon.icns \
        app/build.gradle.kts \
        scripts/merge-macos-universal.sh \
        .github/workflows/release_desktop.yaml \
        app/src/desktopTest/kotlin/me/ash/reader/DesktopPackagingAndCiTest.kt
git commit -m "feat(packaging): configure MSI & macOS Universal DMG packaging and GitHub Actions workflow"
```
