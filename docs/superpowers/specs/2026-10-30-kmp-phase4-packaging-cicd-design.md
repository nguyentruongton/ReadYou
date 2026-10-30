# Technical Design Document: KMP Phase 4 — Desktop Native Packaging (`.msi` & macOS Universal `.dmg`), CI/CD & Final OS Integration

**Project:** ReadYou (`me.ash.reader`) Kotlin Multiplatform (KMP) & Compose Multiplatform (CMP) Migration
**Phase:** 4 (Final) — Desktop Native Packaging, Multi-Architecture CI/CD, Periodic Background Sync, System Notifications & Desktop Typography/Custom Fonts
**Branch:** `kmp-phase4-packaging-cicd`
**Date:** 2026-10-30

---

## 1. Intent & Why

Phases 1, 2, and 3 completed the KMP architecture (`commonMain`, `androidMain`, `desktopMain`), multiplatform Room & DataStore, 42-language Compose Resources, Dual-Engine Reader (Upgraded Native Compose Reader + OS-Native Wry WebView), Desktop Keyboard Shortcuts, MenuBar, System Tray, and Native OS Text-To-Speech.

Phase 4 brings ReadYou Desktop to **production release parity**:
1. **Native Desktop Installers (`compose.desktop.application.nativeDistributions`):** Configure self-contained installers for Windows (`.msi`) and macOS (`.dmg` / `.app`) with bundled JLink Java runtime, application icons (`icon.icns`, `icon.ico`, `icon.png`), and required JDK modules.
2. **100% Free Multi-Architecture GitHub Actions CI/CD (`.github/workflows/release_desktop.yaml`):** Automate building Windows `.msi` on `windows-latest`, building `arm64` (`macos-15`) and `x86_64` (`macos-13`) macOS app bundles in parallel, merging Mach-O binaries into a single **macOS Universal `.dmg`** using `lipo`, and applying **free ad-hoc code signing (`codesign --force --deep --sign -`)** so Apple Silicon Macs execute the merged universal binary without Gatekeeper/kernel signature traps.
3. **Final Desktop OS Polish (`desktopMain`):**
   - **Periodic Background Sync (`DesktopSyncScheduler`):** Run a periodic coroutine timer according to `account.syncInterval` while the application is running or minimized to the System Tray.
   - **Desktop System Notifications (`DesktopNotificationManager`):** Deliver article update notifications via OS `SystemTray` / `TrayIcon.displayMessage` (or `osascript` fallback on macOS) when a feed has `isNotification == true`.
   - **Desktop Typography & Custom Fonts (`Type.desktop.kt`, `DesktopExternalFonts.kt`, `PlatformUiHelper.desktop.kt`, `BasicFontsPreference.kt`, `ReadingFontsPreference.kt`):** Load the real `google_sans_flex.ttf` font on Desktop, support selecting custom `.ttf`/`.otf` Basic & Reading fonts via native `java.awt.FileDialog`, and wire custom/Google Sans font paths into both Native Compose Reader and Desktop WebView `@font-face`.

---

## 2. Goals, Non-Goals & Anti-Goals

### Goals
1. **Desktop Native Distribution Configuration (`app/build.gradle.kts`):**
   - Configure `compose.desktop.application`:
     - `mainClass = "me.ash.reader.MainKt"`
     - Disable ProGuard obfuscation on Desktop release builds (`buildTypes.release.proguard.isEnabled.set(false)`) to prevent reflection/JNI breaks in Room, Koin, Retrofit/Gson, and Wry/UniFFI.
     - `nativeDistributions`:
       - `targetFormats(TargetFormat.Dmg, TargetFormat.Msi)`
       - `packageName = "ReadYou"`
       - `packageVersion = "0.16.2"` (valid semantic `MAJOR.MINOR.PATCH` format required by `jpackage` for `.dmg` and `.msi`)
       - `description = "ReadYou RSS Reader"`
       - `vendor = "ReadYou"`
       - `licenseFile.set(rootProject.file("LICENSE"))`
       - `modules("java.sql", "java.naming", "jdk.unsupported", "java.desktop", "java.management")`
       - Platform-specific icon bindings:
         - `macOS { bundleID = "me.ash.reader"; iconFile.set(project.file("src/desktopMain/resources/icon.icns")) }`
         - `windows { menuGroup = "ReadYou"; upgradeUuid = "8f4e2a1b-9c3d-4e5f-a6b7-c8d9e0f1a2b3"; iconFile.set(project.file("src/desktopMain/resources/icon.ico")) }`
         - `linux { iconFile.set(project.file("src/desktopMain/resources/icon.png")) }`
2. **Desktop Application Icons (`app/src/desktopMain/resources/`):**
   - Generate and commit `icon.png` (512x512), `icon.ico` (Windows multi-resolution ICO), and `icon.icns` (macOS iconset) from the existing official ReadYou artwork (`app/src/main/ic_launcher-playstore.png`).
   - Update `Main.kt` `Window` and `Tray` icon to use the high-resolution `painterResource(Res.drawable.ic_launcher_pure)` / bundled icon with vector fallback.
   - Update `getAppVersion()` in [`PlatformUiHelper.desktop.kt`](file:///Users/stark/Documents/GitHub/ReadYou/app/src/desktopMain/kotlin/me/ash/reader/ui/ext/PlatformUiHelper.desktop.kt) to return `Version("0.16.2")` matching `app/build.gradle.kts`.
3. **GitHub Actions Desktop Release Workflow (`.github/workflows/release_desktop.yaml`):**
   - Trigger on `workflow_dispatch` and `push` tags matching `v*`.
   - **Job `build-windows` (`windows-latest`):**
     - Setup JDK 17 (`temurin`), setup Gradle cache, run `./gradlew :app:packageReleaseMsi`, and upload `app/build/compose/binaries/main/msi/*.msi` (and `main-release` fallback path) as `ReadYou-Windows-MSI`.
   - **Job `build-macos-arch` (matrix: `macos-15` [arm64] & `macos-13` [x86_64]):**
     - Setup JDK 17 (`temurin`), run `./gradlew :app:createDistributable`, and upload `ReadYou.app` artifact per architecture.
   - **Job `package-macos-universal` (`macos-15`, `needs: build-macos-arch`):**
     - Download both `ReadYou-macOS-arm64` and `ReadYou-macOS-x86_64` artifacts.
     - Run a resilient Universal Merger script (`scripts/merge-macos-universal.sh`):
       - Copy `arm64` `ReadYou.app` as the base `Universal-ReadYou.app`.
       - Iterate over all regular files in `ReadYou.app`:
         - For Mach-O files (`file "$arm_file" | grep -q "Mach-O"`), if the corresponding `x86_64` file exists and is Mach-O, inspect architectures via `lipo -archs`. If the two files have disjoint architectures, merge via `lipo -create "$arm_file" "$x64_file" -output "$univ_file"`.
         - For `.jar` archives inside `Contents/app/` that contain native `.dylib` / `.jnilib` libraries (such as `sqlite-jdbc`, `skiko`, `wrywebview`), keep them intact since multi-arch JARs either bundle per-arch directories (`darwin-aarch64` / `darwin-x86-64`) or are extracted dynamically.
       - Apply `chmod +x Universal-ReadYou.app/Contents/MacOS/ReadYou`.
       - Apply **free Ad-hoc Code Signing**: `codesign --force --deep --sign - Universal-ReadYou.app` (with optional hook if `MACOS_CERTIFICATE` secret is ever configured in the future).
       - Package into `ReadYou-macOS-Universal.dmg` using `hdiutil create -volname "ReadYou" -srcfolder Universal-ReadYou.app -ov -format UDZO ReadYou-macOS-Universal.dmg`.
       - Upload `ReadYou-macOS-Universal.dmg` artifact.
4. **Periodic Background Sync (`SyncScheduler.desktop.kt`):**
   - Enhance `DesktopSyncScheduler`:
     - Maintain a `periodicSyncJob: Job?` in `DesktopSyncScheduler`.
     - When `initSync(account: Account)` is called:
       - If `account.syncOnStart.value` is true, trigger immediate sync.
       - Cancel any existing `periodicSyncJob`.
       - If `account.syncInterval.value > 0L` (i.e. not `SyncIntervalPreference.Manually`), launch a coroutine loop that delays `account.syncInterval.value * 60_000L` ms and calls `triggerImmediateSync(account.id)`.
     - When `cancelSync()` is called, cancel both the active sync job and the periodic sync job.
     - Expose a testable `startPeriodicSync(accountId: Int?, intervalMinutes: Long, delayMillisProvider: (Long) -> Long = { it * 60_000L })` method so unit tests can verify periodic scheduling using `kotlinx.coroutines.test.StandardTestDispatcher` / virtual time (`advanceTimeBy`).
5. **Desktop System Notifications (`DesktopNotificationManager.kt` & `PlatformModule.desktop.kt`):**
   - Implement `DesktopNotificationManager` in `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/platform/DesktopNotificationManager.kt`:
     - Respects `feedWithArticle.feed.isNotification` and ignores empty article lists.
     - Formats notification title (`feed.name`) and message (`"${articles.size} new unread articles: ${articles.first().title}"`).
     - Supports pluggable notification sender for headless unit tests:
       - On macOS (when not headless): executes `/usr/bin/osascript -e 'display notification ... with title ...'` or uses AWT `SystemTray` if active.
       - On Windows/Linux (when not headless and `SystemTray.isSupported()`): uses active `TrayIcon.displayMessage(title, message, TrayIcon.MessageType.INFO)`.
   - Wire `DesktopNotificationManager` in `PlatformModule.desktop.kt`.
6. **Desktop Typography & Custom Fonts (`Type.desktop.kt`, `DesktopExternalFonts.kt`, `BasicFontsPreference.kt`, `ReadingFontsPreference.kt`, `PlatformUiHelper.desktop.kt`, `RYWebView.desktop.kt`):**
   - **Google Sans Flex on Desktop (`Type.desktop.kt`):**
     - Load `font/google_sans_flex.ttf` from classpath (`composeResources` / `java.awt.Font` / `org.jetbrains.skia.Typeface.makeFromData` -> `androidx.compose.ui.text.platform.Font`) with fallback to `FontFamily.SansSerif` if resource loading fails in minimal test environments.
     - Fix `ReadingFontsPreference.asFontFamily()` in [`ReadingFontsPreference.kt`](file:///Users/stark/Documents/GitHub/ReadYou/app/src/commonMain/kotlin/me/ash/reader/infrastructure/preference/ReadingFontsPreference.kt#L36-L45) so `GoogleSans -> GoogleSansFontFamily` (instead of hardcoded `FontFamily.SansSerif`).
   - **External Custom Fonts (`ExternalFonts.kt` in `commonMain`/`desktopMain`):**
     - Create multiplatform `ExternalFontManager` (or `DesktopExternalFonts` wired via `BasicFontsPreference.External` / `ReadingFontsPreference.External`):
       - Stores user-selected `basic_font.ttf` and `reading_font.ttf` inside `AppPaths.dataDir/fonts/`.
       - `rememberBasicFontPicker` and `rememberReadingFontPicker` in [`PlatformUiHelper.desktop.kt`](file:///Users/stark/Documents/GitHub/ReadYou/app/src/desktopMain/kotlin/me/ash/reader/ui/ext/PlatformUiHelper.desktop.kt#L63-L68) open `java.awt.FileDialog` filtered to `.ttf` / `.otf` files, copy the selected file into `AppPaths.dataDir/fonts/{basic_font.ttf|reading_font.ttf}`, reload the cached Skia `FontFamily`, and invoke `onLoaded()`.
       - Extract `google_sans_flex.ttf` to `AppPaths.cacheDir/fonts/google_sans_flex.ttf` on Desktop so `RYWebView.desktop.kt` passes the real local `fontPath` to `WebViewStyle.get(...)` for both `ReadingFontsPreference.GoogleSans` and `ReadingFontsPreference.External`.

### Non-Goals
- Paid Apple Developer ID notarization (\$99/year) or paid Windows Authenticode EV certificates — we use free ad-hoc code signing (`codesign -s -`) by default and leave optional hooks for future certificates.
- Linux `.deb`/`.rpm` CI matrix builds (only Windows `.msi` and macOS Universal `.dmg` are built in CI per user decision).

### Anti-Goals
- Enabling ProGuard obfuscation on Desktop release packages, which breaks reflection-heavy libraries (`Room`, `Retrofit`, `Rome`, `UniFFI/JNA`).

---

## 3. Approaches Considered

| Approach | Description | Trade-offs | Decision |
|---|---|---|---|
| **Approach 1 (Selected): Standard `createDistributable`/`packageMsi` without ProGuard + Smart `lipo` Universal Merger + Ad-hoc Signing** | Package Desktop release using JLink runtime (`modules(...)`) with `proguard.isEnabled = false`, merge `arm64` and `x86_64` `.app` bundles via a Mach-O-aware `lipo` script, and sign with `codesign --force --deep --sign -`. | 100% free, zero reflection runtime crashes, works out-of-the-box on Apple Silicon & Intel Macs and Windows 10/11. | **Selected by User** |
| **Approach 2: Full ProGuard Obfuscation on Desktop Release** | Enable ProGuard minification for Desktop JARs and maintain manual keep rules for Swing, Skiko, UniFFI, Room, and Retrofit. | High risk of runtime `ClassNotFoundException` or broken JNI bindings for only ~3MB savings. | Rejected |

---

## 4. Detailed Technical Design

### 4.1. Gradle Packaging & Icons (`app/build.gradle.kts` & `app/src/desktopMain/resources/`)

In [`app/build.gradle.kts`](file:///Users/stark/Documents/GitHub/ReadYou/app/build.gradle.kts#L193-L197):

```kotlin
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

compose.desktop {
    application {
        mainClass = "me.ash.reader.MainKt"

        buildTypes.release.proguard {
            isEnabled.set(false)
        }

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi)
            packageName = "ReadYou"
            packageVersion = "0.16.2"
            description = "Modern Material You RSS Reader for Android and Desktop"
            vendor = "ReadYou"
            licenseFile.set(rootProject.file("LICENSE"))

            modules(
                "java.sql",
                "java.naming",
                "jdk.unsupported",
                "java.desktop",
                "java.management",
            )

            macOS {
                bundleID = "me.ash.reader"
                dockName = "ReadYou"
                iconFile.set(project.file("src/desktopMain/resources/icon.icns"))
            }

            windows {
                menuGroup = "ReadYou"
                shortcut = true
                dirChooser = true
                upgradeUuid = "8f4e2a1b-9c3d-4e5f-a6b7-c8d9e0f1a2b3"
                iconFile.set(project.file("src/desktopMain/resources/icon.ico"))
            }

            linux {
                iconFile.set(project.file("src/desktopMain/resources/icon.png"))
            }
        }
    }
}
```

> [!NOTE]
> `jpackage` on Windows (`.msi`) strictly requires `MAJOR.MINOR.PATCH` where `MAJOR >= 1` on some WiX toolset versions if strict MSI version rules apply, OR `0.16.2` on macOS. Wait: on Windows MSI (`jpackage`), `packageVersion` must have `MAJOR` between `1` and `255` (`1..255.0..255.0..65535`)! If `packageVersion = "0.16.2"` is passed to `packageMsi` on Windows, `jpackage` fails with `Error: Version [0.16.2] contains invalid component [0]`.
> Therefore, in `windows { msiPackageVersion = "1.16.2" }` while keeping `packageVersion = "0.16.2"` (or `dmgPackageVersion = "0.16.2"`) so both Windows WiX MSI and macOS DMG succeed without version errors!

### 4.2. Universal macOS Merger Script (`scripts/merge-macos-universal.sh`) & GitHub Actions (`.github/workflows/release_desktop.yaml`)

1. **`scripts/merge-macos-universal.sh`:**
   - Accepts `<arm64-app-path> <x64-app-path> <output-app-path>`.
   - Copies `<arm64-app-path>` to `<output-app-path>`.
   - Walks all files in `<arm64-app-path>`:
     - Checks if both `$arm_file` and `$x64_file` are regular files (not symlinks) and `file -b "$arm_file" | grep -q "Mach-O"`.
     - Inspects architectures with `lipo -archs`. If `$arm_file` and `$x64_file` have different architectures, merges them with `lipo -create "$arm_file" "$x64_file" -output "$univ_file"`.
   - Sets executable permissions on `Contents/MacOS/ReadYou` and `Contents/runtime/Contents/Home/bin/*` / `jspawnhelper`.
   - Signs the bundle with `codesign --force --deep --sign - "$output_app_path"`.
2. **`.github/workflows/release_desktop.yaml`:**
   - **Job 1 (`build-windows`):** Runs on `windows-latest`, JDK 17 Temurin, executes `./gradlew :app:packageMsi`, uploads `.msi`.
   - **Job 2 (`build-macos-arch`):** Matrix `{ arch: arm64, runner: macos-15 }` and `{ arch: x86_64, runner: macos-13 }`, JDK 17 Temurin, executes `./gradlew :app:createDistributable`, uploads `ReadYou.app` artifact (`ReadYou-macOS-${{ matrix.arch }}`).
   - **Job 3 (`package-macos-universal`):** Runs on `macos-15`, downloads both artifacts, runs `bash scripts/merge-macos-universal.sh`, creates `ReadYou-macOS-Universal.dmg` with `hdiutil`, and uploads the universal `.dmg`.

### 4.3. Periodic Background Sync (`DesktopSyncScheduler`)

In [`SyncScheduler.desktop.kt`](file:///Users/stark/Documents/GitHub/ReadYou/app/src/desktopMain/kotlin/me/ash/reader/infrastructure/platform/SyncScheduler.desktop.kt):
- Add `private var periodicSyncJob: Job? = null` and `private var activeSyncJob: Job? = null`.
- `initSync(account: Account)`:
  - If `account.syncOnStart.value` is `true`, calls `triggerImmediateSync(account.id)`.
  - Calls `schedulePeriodicSync(account.id, account.syncInterval.value)`.
- `schedulePeriodicSync(accountId: Int?, intervalMinutes: Long)`:
  - Cancels previous `periodicSyncJob`.
  - If `intervalMinutes <= 0L`, returns without starting a timer.
  - Otherwise launches a coroutine on `coroutineScope`:
    ```kotlin
    periodicSyncJob = coroutineScope.launch {
        while (isActive) {
            delay(intervalMinutes * 60_000L)
            runSync(accountId = accountId, feedId = null, groupId = null)
        }
    }
    ```

### 4.4. Desktop System Notifications (`DesktopNotificationManager.kt`)

Create `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/platform/DesktopNotificationManager.kt`:
- Implements `NotificationManager`.
- Checks `if (!feedWithArticle.feed.isNotification || feedWithArticle.articles.isEmpty()) return`.
- Builds `title = feedWithArticle.feed.name` and `body = if (articles.size == 1) articles.first().title else "${articles.size} new articles: ${articles.first().title}"`.
- Dispatches to `notificationSender(title, body)`:
  - Default sender checks `GraphicsEnvironment.isHeadless()`; if not headless:
    - On macOS: invokes `ProcessBuilder("/usr/bin/osascript", "-e", ...)` safely escaping quotes, or uses `SystemTray` if a `TrayIcon` is registered.
    - On Windows/Linux: uses `SystemTray.getSystemTray().trayIcons.firstOrNull()?.displayMessage(title, body, TrayIcon.MessageType.INFO)`.

### 4.5. Desktop Typography & Custom Fonts (`Type.desktop.kt`, `ExternalFonts`, `RYWebView.desktop.kt`)

1. **`Type.desktop.kt` & `DesktopFontManager.kt`:**
   - Load `google_sans_flex.ttf` bytes from classpath (`composeResources/me.ash.reader/font/google_sans_flex.ttf` or `/font/google_sans_flex.ttf`) and construct `androidx.compose.ui.text.platform.Font(identity = "google_sans_flex", data = bytes, weight = ...)` for `GoogleSansFontFamily` in `Type.desktop.kt`, falling back to `FontFamily.SansSerif` if unavailable.
   - Extract `google_sans_flex.ttf` lazily to `File(appPaths.cacheDir, "fonts/google_sans_flex.ttf")` so `RYWebView.desktop.kt` can reference it via `file://` URL when `LocalReadingFonts.current is ReadingFontsPreference.GoogleSans`.
   - Manage custom external fonts (`basic_font.ttf` and `reading_font.ttf` in `File(appPaths.dataDir, "fonts")`):
     - `rememberBasicFontPicker` and `rememberReadingFontPicker` in `PlatformUiHelper.desktop.kt` open `java.awt.FileDialog` to pick a `.ttf`/`.otf` file, save it to `dataDir/fonts/`, refresh the loaded `FontFamily`, and call `onLoaded()`.
     - Update `BasicFontsPreference.kt` and `ReadingFontsPreference.kt` to resolve `GoogleSans -> GoogleSansFontFamily` and `External -> ExternalFontResolver.basicFontFamily()` / `ExternalFontResolver.readingFontFamily()`.

---

## 5. What's Left to the Builder

- Exact test cases for `DesktopSyncSchedulerTest` (verifying `initSync` immediate + periodic triggers using `StandardTestDispatcher` and `advanceTimeBy`), `DesktopNotificationManagerTest` (verifying filtering by `feed.isNotification` and formatting), and `DesktopPackagingConfigTest` / `DesktopFontTest`.
- Running `./gradlew :app:desktopTest` and `./gradlew :app:compileGithubDebugKotlinAndroid` to ensure zero regressions across Desktop and Android.
