# KMP Phase 2: Multiplatform UI, Resources & Coil 3 Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate ReadYou's Compose UI layer, resources, Coil image loading, preferences, and domain services to `commonMain` so the complete application UI (`AppEntry`) runs on both Android and Compose Desktop.

**Architecture:** Maintain the single `:app` module layout. Set up `app/src/commonMain/composeResources/` for multiplatform strings (42 languages), drawables, fonts, and files while keeping `src/main/res/` for Android AAPT compatibility. Upgrade Coil 2 to Coil 3 (`io.coil-kt.coil3`) in `commonMain`. Decouple all 67 preference classes and domain services from Android `Context`, migrate theme/graphics/motion and `ui/page` composables to `commonMain`, and wire `Main.kt` to render `AppEntry` in a desktop window.

**Tech Stack:** Compose Multiplatform `1.8.2`, Coil 3 (`3.1.0`), Kotlin `2.2.0`, AGP `8.13.0`, Koin `4.1.0`, Room KMP `2.7.2`, DataStore `1.1.4`, Paging `3.3.6`, Java 17 toolchain.

**Spec:** `docs/superpowers/specs/2026-10-29-kmp-phase2-ui-resources-design.md`

## Global Constraints

- Keep the single `:app` module layout (`commonMain`, `androidMain`, `desktopMain`).
- Zero Dagger/Hilt annotations or dependencies.
- Compose Multiplatform Resources generated class package must be `me.ash.reader`.
- Retain Android AAPT `src/main/res/` for Android-specific assets (app launcher icons, notifications, Glance widgets).
- Coil 3 must use `io.coil-kt.coil3` artifacts and share `OkHttpNetworkFetcherFactory` across both Android and Desktop.
- All non-Android-specific preferences, services, use cases, and UI pages must reside in `commonMain`.
- Any Android-only APIs (`WallpaperManager`, `WorkManager`, `TextToSpeech`, Glance widgets) must be isolated behind `expect`/`actual` interfaces or kept in `androidMain`.

## Review Focus

- Compose Multiplatform `Res` class must generate properly without namespace collisions between Android AAPT `R` and CMP `Res`.
- Localized strings from all 42 languages in `values-*/strings.xml` must be accessible via CMP `Res.string.<id>` and `stringResource(Res.string.<id>)`.
- Coil 3's `ImageLoader` must properly decode SVG files (`SvgDecoder`), GIF files (`GifDecoder`), and remote HTTP/HTTPS images via OkHttp on both targets.
- Decoupled preferences must read/write cleanly to `DataStore<Preferences>` on both Android and Desktop without requiring an Android `Context`.
- Desktop `Main.kt` must display `AppEntry` inside a native OS window without throwing `ClassNotFoundException` or `UnresolvedReferenceException` for Android APIs.

---

### Task 1: Compose Multiplatform Resources Setup (`composeResources`)

**Files:**
- Create: `app/src/commonMain/composeResources/values/strings.xml`
- Create: `app/src/commonMain/composeResources/values-*/strings.xml` (all 42 languages)
- Create: `app/src/commonMain/composeResources/drawable/animation.webp`
- Create: `app/src/commonMain/composeResources/drawable/ic_*.xml`
- Create: `app/src/commonMain/composeResources/font/google_sans_flex.ttf`
- Create: `app/src/commonMain/composeResources/files/reeder.html`
- Modify: `app/build.gradle.kts`
- Create: `app/src/commonMain/kotlin/me/ash/reader/infrastructure/platform/StringsHelper.kt`

**Interfaces:**
- Consumes: `compose.components.resources`
- Produces: `me.ash.reader.generated.resources.Res`, `Res.string.*`, `Res.drawable.*`, `Res.font.*`, `Res.readBytes("files/reeder.html")`

- [ ] **Step 1: Create `composeResources` directory structure and copy assets**

Copy vector drawables and `animation.webp` from `app/src/main/res/drawable/` to `app/src/commonMain/composeResources/drawable/`.
Copy `google_sans_flex.ttf` from `app/src/main/res/font/` to `app/src/commonMain/composeResources/font/`.
Copy `reeder.html` from `app/src/androidMain/kotlin/me/ash/reader/ui/component/webview/` to `app/src/commonMain/composeResources/files/`.
Copy `values/strings.xml` and all 42 `values-*/strings.xml` from `app/src/main/res/` to `app/src/commonMain/composeResources/`.

- [ ] **Step 2: Configure `compose.resources` in `app/build.gradle.kts`**

Add the `compose.resources` configuration:
```kotlin
compose.resources {
    publicResClass = true
    packageOfResClass = "me.ash.reader"
    generateResClass = always
}
```

- [ ] **Step 3: Create `StringsHelper.kt` in `commonMain`**

Create `app/src/commonMain/kotlin/me/ash/reader/infrastructure/platform/StringsHelper.kt` providing helper functions for fetching strings from `Res.string` in non-composable contexts if needed.

- [ ] **Step 4: Verify resource generation**

Run:
```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home GRADLE_USER_HOME=/tmp/.gradle ./gradlew generateComposeResClass
```
Verify `BUILD SUCCESSFUL` and `Res` class is generated.

- [ ] **Step 5: Commit changes**

Commit: `feat(kmp): configure Compose Multiplatform composeResources and Res generator`

---

### Task 2: Coil 3 Multiplatform Migration

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Create: `app/src/commonMain/kotlin/me/ash/reader/infrastructure/image/ImageLoaderFactory.kt`
- Modify: `app/src/androidMain/kotlin/me/ash/reader/infrastructure/di/PlatformModule.android.kt`
- Modify: `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/di/PlatformModule.desktop.kt`
- Modify: `app/src/commonMain/kotlin/me/ash/reader/infrastructure/di/AppModules.kt`
- Modify: `app/src/androidMain/kotlin/me/ash/reader/ui/component/base/RYAsyncImage.kt`
- Modify: `app/src/androidMain/kotlin/me/ash/reader/ui/component/base/DynamicSVGImage.kt`
- Modify: `app/src/androidMain/kotlin/me/ash/reader/ui/component/base/Base64Image.kt`
- Modify: `app/src/androidMain/kotlin/me/ash/reader/ui/page/home/reading/ReaderImagePage.kt`
- Modify: `app/src/androidMain/kotlin/me/ash/reader/ui/page/home/flow/ArticleItem.kt`
- Modify: `app/src/androidMain/kotlin/me/ash/reader/ui/component/reader/Image.kt`
- Modify: `app/src/androidMain/kotlin/me/ash/reader/ui/widget/WidgetRepository.kt`
- Modify: `app/src/androidMain/kotlin/me/ash/reader/infrastructure/android/NotificationHelper.kt`
- Modify: `app/src/androidMain/kotlin/me/ash/reader/infrastructure/android/AndroidApp.kt`
- Modify: `app/src/androidMain/kotlin/me/ash/reader/infrastructure/android/MainActivity.kt`

**Interfaces:**
- Consumes: `io.coil-kt.coil3:coil-compose`, `coil-network-okhttp`, `coil-svg`, `coil-gif`
- Produces: `coil3.ImageLoader` provided via Koin `commonModule` / `platformModule`

- [ ] **Step 1: Update `gradle/libs.versions.toml` with Coil 3 coordinates**

```toml
[versions]
coil = "3.1.0"

[libraries]
coil-compose = { group = "io.coil-kt.coil3", name = "coil-compose", version.ref = "coil" }
coil-compose-core = { group = "io.coil-kt.coil3", name = "coil-compose-core", version.ref = "coil" }
coil-network-okhttp = { group = "io.coil-kt.coil3", name = "coil-network-okhttp", version.ref = "coil" }
coil-svg = { group = "io.coil-kt.coil3", name = "coil-svg", version.ref = "coil" }
coil-gif = { group = "io.coil-kt.coil3", name = "coil-gif", version.ref = "coil" }
```

- [ ] **Step 2: Update `app/build.gradle.kts` dependencies**

Add to `commonMain.dependencies`:
```kotlin
implementation(libs.coil.compose)
implementation(libs.coil.network.okhttp)
implementation(libs.coil.svg)
implementation(libs.coil.gif)
```
Remove legacy Coil 2 dependencies from `androidMain.dependencies`.

- [ ] **Step 3: Implement multiplatform `ImageLoaderFactory.kt`**

Create `app/src/commonMain/kotlin/me/ash/reader/infrastructure/image/ImageLoaderFactory.kt`:
```kotlin
package me.ash.reader.infrastructure.image

import coil3.ImageLoader
import coil3.PlatformContext
import coil3.disk.DiskCache
import coil3.gif.GifDecoder
import coil3.memory.MemoryCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.svg.SvgDecoder
import okhttp3.OkHttpClient
import okio.Path.Companion.toOkioPath
import java.io.File

object ImageLoaderFactory {
    fun create(
        okHttpClient: OkHttpClient,
        cacheDir: File? = null,
    ): ImageLoader {
        return ImageLoader.Builder(PlatformContext.INSTANCE)
            .components {
                add(OkHttpNetworkFetcherFactory(okHttpClient))
                add(SvgDecoder.Factory())
                add(GifDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder()
                    .maxSizePercent(PlatformContext.INSTANCE, 0.25)
                    .build()
            }
            .apply {
                if (cacheDir != null) {
                    diskCache {
                        DiskCache.Builder()
                            .directory(cacheDir.resolve("image_cache").toOkioPath())
                            .maxSizeBytes(128L * 1024 * 1024)
                            .build()
                    }
                }
            }
            .build()
    }
}
```

- [ ] **Step 4: Register `ImageLoader` in Koin `commonModule` / `platformModule`**

In `AppModules.kt`, register `single { ImageLoaderFactory.create(get(), get<AppPaths>().cacheDir) }`.
Update `AndroidApp.kt` and `MainActivity.kt` to use `coil3.ImageLoader`.

- [ ] **Step 5: Migrate all Coil composable and helper callers to Coil 3**

Update `coil.*` imports to `coil3.*` in:
- `RYAsyncImage.kt`
- `DynamicSVGImage.kt`
- `Base64Image.kt`
- `ReaderImagePage.kt`
- `ArticleItem.kt`
- `Image.kt`
- `WidgetRepository.kt`
- `NotificationHelper.kt`

- [ ] **Step 6: Verify compilation and tests**

Run:
```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home GRADLE_USER_HOME=/tmp/.gradle ./gradlew compileKotlinDesktop compileGithubDebugKotlinAndroid
```
Verify both targets compile cleanly with Coil 3.

- [ ] **Step 7: Commit changes**

Commit: `feat(kmp): migrate image loading pipeline to Coil 3 multiplatform`

---

### Task 3: Migrate Preferences & Domain Services to `commonMain`

**Files:**
- Move: `app/src/androidMain/kotlin/me/ash/reader/infrastructure/preference/*` -> `app/src/commonMain/kotlin/me/ash/reader/infrastructure/preference/*`
- Create: `app/src/androidMain/kotlin/me/ash/reader/infrastructure/preference/PreferenceContextExt.android.kt`
- Move: `app/src/androidMain/kotlin/me/ash/reader/domain/data/*` -> `app/src/commonMain/kotlin/me/ash/reader/domain/data/*`
- Move: `app/src/androidMain/kotlin/me/ash/reader/domain/service/AccountService.kt` -> `commonMain`
- Move: `app/src/androidMain/kotlin/me/ash/reader/domain/service/LocalRssService.kt` -> `commonMain`
- Move: `app/src/androidMain/kotlin/me/ash/reader/domain/service/FeverRssService.kt` -> `commonMain`
- Move: `app/src/androidMain/kotlin/me/ash/reader/domain/service/GoogleReaderRssService.kt` -> `commonMain`
- Move: `app/src/androidMain/kotlin/me/ash/reader/domain/service/OpmlService.kt` -> `commonMain`
- Move: `app/src/androidMain/kotlin/me/ash/reader/domain/service/RssService.kt` -> `commonMain`
- Move: `app/src/androidMain/kotlin/me/ash/reader/domain/service/AppService.kt` -> `commonMain`
- Move: `app/src/androidMain/kotlin/me/ash/reader/domain/service/AbstractRssRepository.kt` -> `commonMain`
- Create: `app/src/commonMain/kotlin/me/ash/reader/infrastructure/platform/SyncScheduler.kt`
- Create: `app/src/androidMain/kotlin/me/ash/reader/infrastructure/platform/SyncScheduler.android.kt`
- Create: `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/platform/SyncScheduler.desktop.kt`

**Interfaces:**
- Consumes: `DataStore<Preferences>`, `AndroidDatabase`, `OkHttpClient`
- Produces: `SettingsProvider`, `AccountService`, `RssService`, `SyncScheduler` in `commonMain`

- [ ] **Step 1: Move and decouple all Preference classes to `commonMain`**

Move all preference files from `androidMain` to `commonMain`.
In each preference:
- Add `fun put(dataStore: DataStore<Preferences>, scope: CoroutineScope, value: T)`
- In `androidMain`, create `PreferenceContextExt.android.kt` keeping `fun Preference.put(context: Context, scope: CoroutineScope, value: T)` which calls `context.dataStore.put(...)`.
Move `SettingsProvider.kt` and `Settings.kt` to `commonMain`.

- [ ] **Step 2: Define `SyncScheduler` expect/actual abstraction**

In `commonMain`:
```kotlin
package me.ash.reader.infrastructure.platform

interface SyncScheduler {
    fun schedulePeriodicSync(intervalHours: Long, onlyOnWifi: Boolean, onlyWhenCharging: Boolean)
    fun cancelSync()
    fun triggerImmediateSync()
}
```
In `androidMain`, `WorkManagerSyncScheduler` implements `SyncScheduler` using `WorkManager`.
In `desktopMain`, `DesktopSyncScheduler` implements `SyncScheduler` using Coroutines.
Register `SyncScheduler` in `platformModule.android` and `platformModule.desktop`.

- [ ] **Step 3: Move Domain Services and Use Cases to `commonMain`**

Move `FilterStateUseCase.kt`, `GroupWithFeedsListUseCase.kt`, `ArticlePagingListUseCase.kt`, and `DiffMapHolder.kt` to `commonMain`.
Move `AccountService.kt`, `LocalRssService.kt`, `FeverRssService.kt`, `GoogleReaderRssService.kt`, `OpmlService.kt`, `RssService.kt`, `AppService.kt`, `AbstractRssRepository.kt` to `commonMain`.
Replace `Log.e/d/i` with multiplatform logging or `Timber` (or `println`).
Replace direct `WorkManager` calls in `AbstractRssRepository` with `SyncScheduler.triggerImmediateSync()`.

- [ ] **Step 4: Register services in Koin `commonModule`**

Update `AppModules.kt` in `commonMain` to include definitions for:
`AccountService`, `LocalRssService`, `FeverRssService`, `GoogleReaderRssService`, `OpmlService`, `AppService`, `FilterStateUseCase`, `GroupWithFeedsListUseCase`, `DiffMapHolder`.

- [ ] **Step 5: Verify compilation on both targets**

Run:
```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home GRADLE_USER_HOME=/tmp/.gradle ./gradlew compileKotlinDesktop compileGithubDebugKotlinAndroid
```
Verify both targets compile successfully.

- [ ] **Step 6: Commit changes**

Commit: `feat(kmp): migrate all preferences, domain services and use cases to commonMain`

---

### Task 4: Migrate Themes, Shapes & Motion to `commonMain`

**Files:**
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/theme/*` -> `app/src/commonMain/kotlin/me/ash/reader/ui/theme/*`
- Create: `app/src/commonMain/kotlin/me/ash/reader/ui/theme/palette/dynamic/WallpaperColors.kt` (expect fun)
- Create: `app/src/androidMain/kotlin/me/ash/reader/ui/theme/palette/dynamic/WallpaperColors.android.kt` (actual fun)
- Create: `app/src/desktopMain/kotlin/me/ash/reader/ui/theme/palette/dynamic/WallpaperColors.desktop.kt` (actual fun)
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/graphics/*` -> `app/src/commonMain/kotlin/me/ash/reader/ui/graphics/*`
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/motion/*` -> `app/src/commonMain/kotlin/me/ash/reader/ui/motion/*`
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/svg/*` -> `app/src/commonMain/kotlin/me/ash/reader/ui/svg/*`
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/interaction/*` -> `app/src/commonMain/kotlin/me/ash/reader/ui/interaction/*`

**Interfaces:**
- Consumes: Material 3 Compose Multiplatform, Skia
- Produces: `AppTheme`, `LocalTonalPalettes`, `LocalShapes`, `MaterialTheme`

- [ ] **Step 1: Migrate `ui/theme/` and dynamic wallpaper abstraction**

Move `Shapes.kt`, `Theme.kt`, `Type.kt`, and `palette/` to `commonMain`.
For `WallpaperColors`:
Declare in `commonMain`:
```kotlin
@Composable
expect fun rememberWallpaperPrimaryColor(): Color?
```
In `androidMain`:
Implement with `android.app.WallpaperManager` (API 27+).
In `desktopMain`:
Return `null` (falling back cleanly to the user's custom primary color preference).

- [ ] **Step 2: Migrate `ui/graphics/` and `ui/motion/`**

Move `MorphPolygonShape.kt` and `RoundedPolygonShape.kt` to `commonMain`.
Move `ExpressiveTransitions.kt`, `MaterialEasing.kt`, `MaterialSharedAxis.kt`, and `MotionConstants.kt` to `commonMain`.

- [ ] **Step 3: Migrate `ui/svg/` and `ui/interaction/`**

Move `SVGString.kt`, `welcome.kt`, `palette.kt` to `commonMain`.
Move `Clickable.kt` to `commonMain`.

- [ ] **Step 4: Verify theme compilation on both targets**

Run:
```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home GRADLE_USER_HOME=/tmp/.gradle ./gradlew compileKotlinDesktop compileGithubDebugKotlinAndroid
```
Verify theme and styling build cleanly.

- [ ] **Step 5: Commit changes**

Commit: `feat(kmp): migrate Material 3 theme, palettes, graphics and motion to commonMain`

---

### Task 5: Migrate UI Components to `commonMain`

**Files:**
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/component/base/*` -> `app/src/commonMain/kotlin/me/ash/reader/ui/component/base/*`
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/component/menu/*` -> `app/src/commonMain/kotlin/me/ash/reader/ui/component/menu/*`
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/component/scrollbar/*` -> `app/src/commonMain/kotlin/me/ash/reader/ui/component/scrollbar/*`
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/component/swipe/*` -> `app/src/commonMain/kotlin/me/ash/reader/ui/component/swipe/*`
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/component/ChangeUrlDialog.kt` -> `commonMain`
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/component/FeedIcon.kt` -> `commonMain`
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/component/FilterBar.kt` -> `commonMain`
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/component/RenameDialog.kt` -> `commonMain`
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/component/ReadingThemePrev.kt` -> `commonMain`
- Create: `app/src/commonMain/kotlin/me/ash/reader/infrastructure/platform/HapticFeedbackHelper.kt`

**Interfaces:**
- Consumes: Compose Multiplatform, Coil 3, Compose Resources
- Produces: Reusable UI widgets and dialogs in `commonMain`

- [ ] **Step 1: Abstract platform-specific effects (haptics and sound)**

In `FilterBar.kt` and `Clickable.kt`, abstract `SoundEffectConstants` and Android View haptics:
Create `HapticFeedbackHelper` / `SoundEffectHelper` with no-op on Desktop and standard system feedback on Android.

- [ ] **Step 2: Update resource references in UI components**

Replace `androidx.compose.ui.res.stringResource(R.string.<id>)` with `org.jetbrains.compose.resources.stringResource(Res.string.<id>)`.
Replace `androidx.compose.ui.res.painterResource(R.drawable.<id>)` with `org.jetbrains.compose.resources.painterResource(Res.drawable.<id>)`.

- [ ] **Step 3: Move components to `commonMain`**

Move `base/`, `menu/`, `scrollbar/`, `swipe/`, `ChangeUrlDialog.kt`, `FeedIcon.kt`, `FilterBar.kt`, `RenameDialog.kt`, `ReadingThemePrev.kt` into `app/src/commonMain/kotlin/me/ash/reader/ui/component/`.

- [ ] **Step 4: Verify components compile on both targets**

Run:
```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home GRADLE_USER_HOME=/tmp/.gradle ./gradlew compileKotlinDesktop compileGithubDebugKotlinAndroid
```
Verify UI components build cleanly on both platforms.

- [ ] **Step 5: Commit changes**

Commit: `feat(kmp): migrate core UI components, dialogs and widgets to commonMain`

---

### Task 6: Migrate Navigation & Pages to `commonMain` and Bootstrap Desktop AppEntry

**Files:**
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/page/nav3/*` -> `app/src/commonMain/kotlin/me/ash/reader/ui/page/nav3/*`
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/page/adaptive/*` -> `app/src/commonMain/kotlin/me/ash/reader/ui/page/adaptive/*`
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/page/startup/*` -> `app/src/commonMain/kotlin/me/ash/reader/ui/page/startup/*`
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/page/home/*` -> `app/src/commonMain/kotlin/me/ash/reader/ui/page/home/*`
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/page/settings/*` -> `app/src/commonMain/kotlin/me/ash/reader/ui/page/settings/*`
- Move: `app/src/androidMain/kotlin/me/ash/reader/ui/page/common/*` -> `app/src/commonMain/kotlin/me/ash/reader/ui/page/common/*`
- Modify: `app/src/desktopMain/kotlin/me/ash/reader/Main.kt`
- Modify: `app/src/androidMain/kotlin/me/ash/reader/infrastructure/android/MainActivity.kt`
- Create: `app/src/desktopTest/kotlin/me/ash/reader/DesktopUiSmokeTest.kt`

**Interfaces:**
- Consumes: `AppEntry`, `AppTheme`, `SettingsProvider`, Koin ViewModels
- Produces: Fully functional Compose UI running on both Android and Desktop

- [ ] **Step 1: Move Navigation & Adaptive Layouts to `commonMain`**

Move `AppEntry.kt`, `Route.kt` and `adaptive/` (`ArticleListReaderPage.kt`, `ArticleListReaderViewModel.kt`, `PaneMotion.kt`, `TopBar.kt`) to `commonMain`.
Ensure `rememberListDetailPaneScaffoldNavigator` and adaptive components compile in `commonMain`.

- [ ] **Step 2: Move all Screens / Pages to `commonMain`**

Move:
- `startup/StartupPage.kt`, `StartupViewModel.kt`
- `home/feeds/` (FeedsPage, SubscribeViewModel, FeedItem, etc.)
- `home/flow/` (FlowPage, ArticleItem, FlowViewModel, etc.)
- `home/reading/` (ReadingPage, ReaderImagePage, ReadingViewModel, etc.)
- `settings/` (all settings sub-pages: Accounts, Colors, Interaction, Languages, Tips, Troubleshooting)
- `common/ExtraName.kt`
Update resource references from `R.string.*` to `Res.string.*`.

- [ ] **Step 3: Update Desktop `Main.kt` to launch `AppEntry`**

In `app/src/desktopMain/kotlin/me/ash/reader/Main.kt`:
Wrap the window content in `SettingsProvider` and `AppTheme`:
```kotlin
fun main() = application {
    if (GlobalContext.getOrNull() == null) {
        startKoin {
            modules(commonModule, platformModule)
        }
    }

    Window(
        onCloseRequest = ::exitApplication,
        title = "ReadYou",
    ) {
        val settingsProvider: SettingsProvider = koinInject()
        settingsProvider.ProvideCompositionLocals {
            AppTheme {
                AppEntry()
            }
        }
    }
}
```

- [ ] **Step 4: Create Desktop UI Smoke Test**

Create `app/src/desktopTest/kotlin/me/ash/reader/DesktopUiSmokeTest.kt`:
Verify that Koin resolves `SettingsProvider`, `AccountService`, `FilterStateUseCase`, and all UI ViewModels without errors.

- [ ] **Step 5: Execute Dual-Target Build & Verification**

Run:
```bash
JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk-17.jdk/Contents/Home GRADLE_USER_HOME=/tmp/.gradle ./gradlew desktopTest packageUberJarForCurrentOS assembleGithubDebug
```
Verify:
1. All Desktop unit/smoke tests pass.
2. Desktop Uber JAR builds successfully.
3. Android debug APK builds successfully.

- [ ] **Step 6: Commit changes**

Commit: `feat(kmp): complete Phase 2 - migrate full UI layer to commonMain & launch AppEntry on Desktop`
