# Desktop RAM Optimization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Reduce real-world memory consumption of ReadYou on macOS Desktop from 1.2 GB to ~180MB-300MB by limiting the JVM heap ceiling, enabling G1GC periodic memory uncommit, and capping Coil bitmap memory cache.

**Architecture:** Add JVM execution arguments to Compose Multiplatform's desktop application block (`-Xmx384m`, G1GC with periodic uncommit, string deduplication). Extend `ImageLoaderFactory` to accept a byte cap on memory cache and configure desktop DI to use a 64MB cap instead of an unbounded percentage of total heap.

**Tech Stack:** Kotlin Multiplatform, JetBrains Compose Desktop, Coil 3, Gradle Kotlin DSL, JVM HotSpot G1GC.

**Spec:** Short Design Doc confirmed in session on 2026-11-01.

## Global Constraints

- Android functionality and memory behavior must remain unaffected (Android uses default Coil 25% cache sizing).
- Compose Desktop package and run tasks must successfully accept the new JVM options.
- No breaking changes to existing DI modules (`PlatformModule.android.kt` continues to work with default arguments).

## Review Focus

- Overflow when loading large feeds: Ensure 64MB Coil cache handles high-resolution images without OOM by evicting older bitmaps.
- Memory uncommit verification: Confirm `-XX:G1PeriodicGCInterval=10000` is supported on JVM 17+ and valid for Compose Desktop packaging.

---

### Task 1: Cap Coil Memory Cache on Desktop

**Files:**
- Modify: `app/src/commonMain/kotlin/me/ash/reader/infrastructure/image/ImageLoaderFactory.kt:15-46`
- Modify: `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/di/PlatformModule.desktop.kt:25-28`

**Interfaces:**
- Consumes: `ImageLoaderFactory.create(context, okHttpClient, cacheDir, extraComponents)`
- Produces: `ImageLoaderFactory.create(context, okHttpClient, cacheDir, maxMemoryCacheBytes: Long? = null, extraComponents = null)`

- [ ] **Step 1: Update `ImageLoaderFactory.kt` to accept `maxMemoryCacheBytes: Long? = null`**

Update `ImageLoaderFactory.create` signature and `memoryCache` configuration:
```kotlin
object ImageLoaderFactory {
    fun create(
        context: PlatformContext,
        okHttpClient: OkHttpClient,
        cacheDir: File? = null,
        maxMemoryCacheBytes: Long? = null,
        extraComponents: (ComponentRegistry.Builder.() -> Unit)? = null,
    ): ImageLoader {
        return ImageLoader.Builder(context)
            .coroutineContext(Dispatchers.Default)
            .components {
                add(OkHttpNetworkFetcherFactory(okHttpClient))
                add(SvgDecoder.Factory())
                extraComponents?.invoke(this)
            }
            .memoryCache {
                MemoryCache.Builder()
                    .apply {
                        if (maxMemoryCacheBytes != null) {
                            maxSizeBytes(maxMemoryCacheBytes)
                        } else {
                            maxSizePercent(context, 0.25)
                        }
                    }
                    .build()
            }
```

- [ ] **Step 2: Update `PlatformModule.desktop.kt` to pass a 64MB memory cache cap**

In `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/di/PlatformModule.desktop.kt`:
```kotlin
single<ImageLoader> {
    ImageLoaderFactory.create(
        context = PlatformContext.INSTANCE,
        okHttpClient = get(),
        cacheDir = get<AppPaths>().cacheDir,
        maxMemoryCacheBytes = 64L * 1024 * 1024,
    )
}
```

- [ ] **Step 3: Verify compilation of commonMain and desktopMain**

Run: `./gradlew :app:compileKotlinDesktop`
Expected: BUILD SUCCESSFUL

- [ ] **Step 4: Commit**

```bash
git add app/src/commonMain/kotlin/me/ash/reader/infrastructure/image/ImageLoaderFactory.kt app/src/desktopMain/kotlin/me/ash/reader/infrastructure/di/PlatformModule.desktop.kt
git commit -m "perf(desktop): cap Coil image memory cache to 64MB on desktop"
```

---

### Task 2: Configure Compose Desktop JVM Arguments and G1GC Uncommit

**Files:**
- Modify: `app/build.gradle.kts:194-205`

**Interfaces:**
- Consumes: `compose.desktop.application` block
- Produces: `jvmArgs` configured for runtime and distribution packaging

- [ ] **Step 1: Add `jvmArgs` to `compose.desktop.application` in `app/build.gradle.kts`**

In `app/build.gradle.kts`:
```kotlin
compose.desktop {
    application {
        mainClass = "me.ash.reader.MainKt"

        jvmArgs += listOf(
            "-Xms128m",
            "-Xmx384m",
            "-XX:+UseG1GC",
            "-XX:G1PeriodicGCInterval=10000",
            "-XX:+UseStringDeduplication"
        )

        buildTypes.release.proguard {
            isEnabled.set(false)
        }
        // ...
```

- [ ] **Step 2: Verify desktop build and task configuration**

Run: `./gradlew :app:compileKotlinDesktop :app:compileKotlinAndroid`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add app/build.gradle.kts
git commit -m "perf(desktop): configure JVM heap limit and G1GC uncommit arguments"
```
