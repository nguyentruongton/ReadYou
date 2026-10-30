# Technical Design Document: KMP Phase 2 — Multiplatform UI, Resources & Coil 3 Migration

**Project:** ReadYou (`me.ash.reader`) Kotlin Multiplatform (KMP) & Compose Multiplatform (CMP) Migration
**Phase:** 2 — Compose Multiplatform Resources (`composeResources`), Coil 3 (`3.1.0` / `3.2.0`), Domain Services, Preferences, and UI Pages in `commonMain`
**Branch:** `kmp-phase2-ui-resources`

---

## 1. Objectives & Scope

In Phase 1, the core multiplatform build setup, Room KMP database (v1..v7), DataStore factory, and complete migration from Dagger Hilt to Koin 4.1.0 were implemented and verified on both Android and Desktop targets.

Phase 2 moves the application's UI, resource handling, image loading pipeline, preference system, and domain services into `commonMain`, enabling Compose Desktop to render the complete ReadYou user interface (`AppEntry`) while preserving 100% Android compatibility.

### Key Deliverables
1. **Compose Multiplatform Resources (`composeResources`):**
   - Place all localized strings (42 languages), vector XML drawables, fonts (`google_sans_flex.ttf`), and web reader template (`reeder.html`) into `app/src/commonMain/composeResources/`.
   - Configure `compose.resources { packageOfResClass = "me.ash.reader" }` so `Res` is generated.
   - Maintain Android AAPT `src/main/res/` for Android-specific requirements (Manifest icons, Glance widgets, Notification channels).
2. **Coil 3 Migration (`io.coil-kt.coil3`):**
   - Upgrade Coil 2.5.0 to Coil 3 (`3.1.0` / `3.2.0`) with Multiplatform support.
   - Provide a shared multiplatform `ImageLoader` configured with `OkHttpNetworkFetcherFactory`, `SvgDecoder`, and `GifDecoder`.
   - Migrate all image composables (`RYAsyncImage`, `DynamicSVGImage`, `Base64Image`, `ReaderImagePage`, `ArticleItem`) to Coil 3.
3. **Multiplatform Preferences & Domain Services:**
   - Move all 67 preference classes and `SettingsProvider.kt` into `commonMain`.
   - Decouple preferences from `android.content.Context` by operating on `DataStore<Preferences>` while maintaining backward-compatible extension methods for Android.
   - Move `AccountService`, `LocalRssService`, `FeverRssService`, `GoogleReaderRssService`, `OpmlService`, `AppService`, and use cases (`FilterStateUseCase`, `GroupWithFeedsListUseCase`, `DiffMapHolder`) into `commonMain`.
   - Introduce `SyncScheduler` expect/actual abstraction for background synchronization (`WorkManager` on Android, Coroutine scheduler on Desktop).
4. **Theme, Graphics, Motion & Core UI Components in `commonMain`:**
   - Move Material 3 Expressive theme, typography, shapes, and ZCAM/Oklab color palettes into `commonMain`.
   - Implement `rememberWallpaperPrimaryColor()` expect/actual (`WallpaperManager` on Android, static/system accent fallback on Desktop).
   - Move all reusable UI components (`RYScaffold`, `RYTextField`, `RYSwitch`, dialogs, buttons, `FilterBar`, `FeedIcon`) into `commonMain`.
5. **UI Pages, Navigation & Desktop Application Entry:**
   - Move `ui/page/nav3/` (`AppEntry.kt`, `Route.kt`) and `ui/page/adaptive/` to `commonMain`.
   - Move all screens (startup, home feeds/flow/reading, settings) to `commonMain`.
   - Update Desktop `Main.kt` to launch `ProvideCompositionLocals` -> `AppTheme` -> `AppEntry`.
   - Verify Android debug APK and Desktop Uber JAR builds and tests pass.

---

## 2. Directory Layout & Source Sets

```text
app/src/
├── commonMain/
│   ├── composeResources/
│   │   ├── drawable/                         # animation.webp, ic_*.xml
│   │   ├── files/
│   │   │   └── reeder.html                   # HTML template for web reader
│   │   ├── font/
│   │   │   └── google_sans_flex.ttf          # Default variable font
│   │   ├── values/strings.xml                # Base English strings
│   │   └── values-*/strings.xml              # 42 language localizations
│   └── kotlin/me/ash/reader/
│       ├── domain/
│       │   ├── data/                         # UseCases: GroupWithFeedsList, FilterState, ArticlePagingList, DiffMapHolder
│       │   └── service/                      # AccountService, LocalRssService, FeverRssService, GoogleReaderRssService, OpmlService, AppService
│       ├── infrastructure/
│       │   ├── image/                        # Coil 3 ImageLoaderFactory
│       │   ├── platform/                     # expect fun rememberWallpaperPrimaryColor(): Color?, expect interface SyncScheduler
│       │   └── preference/                   # All 67 Preferences & SettingsProvider
│       └── ui/
│           ├── component/                    # base/, menu/, scrollbar/, swipe/, dialogs, FilterBar, FeedIcon
│           ├── graphics/                     # MorphPolygonShape, RoundedPolygonShape
│           ├── motion/                       # ExpressiveTransitions, MaterialEasing, MaterialSharedAxis, MotionConstants
│           ├── page/                         # nav3/, adaptive/, startup/, home/, settings/
│           ├── svg/                          # SVGString, welcome, palette
│           └── theme/                        # Theme, Type, Shapes, DynamicTonalPalette, ZCAM/Oklab
│
├── androidMain/
│   ├── kotlin/me/ash/reader/
│   │   ├── infrastructure/
│   │   │   ├── android/                      # AndroidApp, MainActivity, CrashReportActivity, NotificationHelper, AndroidTTS
│   │   │   └── platform/                     # actual fun rememberWallpaperPrimaryColor(), actual class WorkManagerSyncScheduler
│   │   ├── domain/service/                   # SyncWorker, ReaderWorker, WidgetUpdateWorker
│   │   └── ui/widget/                        # Glance AppWidgets (ArticleCardWidget, ArticleListWidget)
│   └── res/                                  # Android AAPT resources (Launcher icons, XML configs, styles)
│
└── desktopMain/
    └── kotlin/me/ash/reader/
        ├── Main.kt                           # Window, Koin init, AppEntry
        └── infrastructure/platform/          # actual fun rememberWallpaperPrimaryColor(), actual class DesktopSyncScheduler
```

---

## 3. Detailed Component Architecture

### 3.1. Compose Multiplatform Resources
CMP 1.8.2 automatically generates a strongly-typed `Res` accessor:
- `Res.string.<id>`
- `Res.drawable.<id>`
- `Res.font.<id>`
- `Res.readBytes("files/reeder.html")`

In `app/build.gradle.kts`:
```kotlin
compose.resources {
    publicResClass = true
    packageOfResClass = "me.ash.reader"
    generateResClass = always
}
```

UI composables use `org.jetbrains.compose.resources.stringResource(Res.string.<id>)` and `org.jetbrains.compose.resources.painterResource(Res.drawable.<id>)`.

### 3.2. Coil 3 Image Loading Pipeline
Coil 3 replaces Coil 2:
- Version coordinates: `io.coil-kt.coil3:coil-compose`, `coil-network-okhttp`, `coil-svg`, `coil-gif`.
- Multiplatform `ImageLoader`:
```kotlin
fun newImageLoader(
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
                .maxSizePercent(0.25)
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
```

### 3.3. Preferences Decoupling
All 67 preference classes will be migrated to `commonMain`.
Instead of requiring an `android.content.Context` to access `dataStore`:
- `fun put(dataStore: DataStore<Preferences>, scope: CoroutineScope, value: T)`
- In `androidMain`, extension functions `fun Preference.put(context: Context, scope: CoroutineScope, value: T)` delegate to the Koin singleton `context.dataStore`.
- `SettingsProvider.kt` provides composition locals (`LocalThemeIndex`, `LocalDarkTheme`, etc.) using `DataStore<Preferences>`.

### 3.4. Background Sync Abstraction
```kotlin
// commonMain:
expect interface SyncScheduler {
    fun schedulePeriodicSync(intervalHours: Long, onlyOnWifi: Boolean, onlyWhenCharging: Boolean)
    fun cancelSync()
    fun triggerImmediateSync()
}

// androidMain:
actual class WorkManagerSyncScheduler(private val workManager: WorkManager) : SyncScheduler { ... }

// desktopMain:
actual class DesktopSyncScheduler(private val coroutineScope: CoroutineScope, private val syncService: AppService) : SyncScheduler { ... }
```

### 3.5. Dual-Target Desktop Entry Point
`Main.kt` initializes Koin, applies `AppTheme` and `ProvideCompositionLocals`, and renders `AppEntry`:
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
        ProvideCompositionLocals {
            AppTheme {
                AppEntry()
            }
        }
    }
}
```

---

## 4. Verification Plan

1. **Gradle Build Verification:**
   - Execute `./gradlew generateComposeResClass` — verify `Res` generates without errors.
   - Execute `./gradlew compileKotlinDesktop` — verify `commonMain` and `desktopMain` compile cleanly.
   - Execute `./gradlew compileGithubDebugKotlinAndroid` — verify `androidMain` compiles cleanly.
2. **Dual-Target Packaging:**
   - Assemble Android APK: `./gradlew assembleGithubDebug`.
   - Package Desktop Uber JAR: `./gradlew packageUberJarForCurrentOS`.
3. **Unit Tests:**
   - Common/Desktop unit tests: `./gradlew desktopTest`.
