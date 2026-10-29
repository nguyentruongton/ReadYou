# KMP Phase 1: Multiplatform Build, Koin DI, Room KMP & DataStore KMP Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert `:app` to a Kotlin Multiplatform & Compose Multiplatform module (`androidTarget` + `jvm("desktop")`), replace Dagger Hilt with Koin 4.1.0 across the entire project, migrate Room Database (v1..v7) and DataStore Preferences to `commonMain`, and launch a working Compose Desktop bootstrap window alongside the existing Android app.

**Architecture:** Keep the single `:app` module structure (`src/commonMain`, `src/androidMain`, `src/desktopMain`). Clean `domain/model`, `domain/repository`, `infrastructure/db`, `infrastructure/datastore`, `infrastructure/platform`, and `infrastructure/net` of Android-specific APIs so they compile in `commonMain`, while keeping Android UI/Services in `src/androidMain` wired via Koin (`commonModule` + `platformModule`) until Phase 2 migrates `composeResources` and `ui/page`.

**Tech Stack:** Kotlin `2.2.0`, AGP `8.13.0`, Compose Multiplatform `1.8.2`, Room KMP `2.7.2` + `androidx.sqlite:sqlite-bundled:2.5.1`, DataStore Preferences Core `1.1.4`, Paging `3.3.6`, Koin `4.1.0`, OkHttp `5.0.0-alpha.12`, Retrofit `2.11.0`, Java 17 toolchain.

**Spec:** `docs/superpowers/specs/2026-10-29-kmp-phase1-foundation-di-db-design.md`

## Global Constraints

- Kotlin `2.2.0` (`ksp = "2.2.0-2.0.2"`), AGP `8.13.0`, Compose Multiplatform `1.8.2`, Room KMP `2.7.2`, SQLite Bundled `2.5.1`, DataStore `1.1.4`, Paging `3.3.6`, Koin `4.1.0`, `jvmTarget = 17`.
- Zero Dagger/Hilt dependencies, plugins, or `@Inject` / `@HiltViewModel` / `@AndroidEntryPoint` / `@HiltAndroidApp` / `@HiltWorker` / `@EntryPoint` annotations remain after Phase 1.
- Preserve package and class name `me.ash.reader.infrastructure.db.AndroidDatabase` (with `typealias RYDatabase = AndroidDatabase`) in `commonMain` so Room AutoMigrations (5 -> 6 -> 7) match `app/schemas/me.ash.reader.infrastructure.db.AndroidDatabase/1..7.json`.
- Preserve Android SQLite file name `"Reader"` (`context.getDatabasePath("Reader")`) and Android DataStore file path `context.filesDir.resolve("datastore/settings.preferences_pb")`.
- Do not create a fake `PlatformContext` class in `commonMain`.

## Review Focus

- Legacy Android `DESUtils` ciphertexts containing trailing newlines (`\n`) from `android.util.Base64.DEFAULT` must decrypt identically when using `java.util.Base64` in `commonMain`, and `DESUtils.empty` (`"CvJ1PKM8EW8="`) must decrypt to `""`.
- Accessing `Context.dataStore` from multiple Android call sites must return the exact same Koin singleton `DataStore<Preferences>` instance rather than instantiating a duplicate `preferencesDataStore` delegate (which throws `IllegalStateException: There are multiple DataStores active for the same file`).
- Room KMP migrations `MIGRATION_1_2` through `MIGRATION_4_5` must execute via `SQLiteConnection` (`androidx.sqlite.execSQL`) so they run identically under `BundledSQLiteDriver` on both Android and Desktop.
- `SyncBlockListPreference.toString(list)` and `SyncBlockListPreference.of(str)` round-trip conversion used by `SyncBlockListConverters` must preserve non-blank entries without Android dependencies.
- `WorkManager` background workers (`SyncWorker`, `ReaderWorker`, `WidgetUpdateWorker`) must resolve their constructor dependencies through Koin `workManagerFactory()` without `HiltWorkerFactory`.

---

### Task 1: Multiplatform Gradle Configuration & Source Set Layout

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `build.gradle.kts`
- Modify: `app/build.gradle.kts`
- Move: `app/src/main/java/me/ash/reader/**` -> `app/src/androidMain/kotlin/me/ash/reader/**`
- Move: `app/src/test/java/**` -> `app/src/androidUnitTest/kotlin/**`
- Move: `app/src/androidTest/java/**` -> `app/src/androidInstrumentedTest/kotlin/**`

**Interfaces:**
- Consumes: none
- Produces:
  - KMP source sets `commonMain`, `commonTest`, `androidMain`, `androidUnitTest`, `desktopMain`, `desktopTest` in `:app`
  - Version catalog entries: `kotlin-multiplatform`, `compose-multiplatform`, `sqlite-bundled`, `datastore-preferences-core`, `okio`, `koin-core`, `koin-android`, `koin-androidx-workmanager`, `koin-compose`, `koin-compose-viewmodel`

- [ ] **Step 1: Update `gradle/libs.versions.toml` with KMP, CMP, SQLite Bundled, Okio, and Koin 4.1.0 entries and remove Hilt/Dagger entries**

Add to `[versions]`:
```toml
composeMultiplatform = "1.8.2"
sqlite = "2.5.1"
koin = "4.1.0"
okio = "3.10.2"
```
Add to `[libraries]`:
```toml
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
sqlite-bundled = { group = "androidx.sqlite", name = "sqlite-bundled", version.ref = "sqlite" }
datastore-preferences-core = { group = "androidx.datastore", name = "datastore-preferences-core", version.ref = "datastore" }
okio = { group = "com.squareup.okio", name = "okio", version.ref = "okio" }
koin-core = { group = "io.insert-koin", name = "koin-core", version.ref = "koin" }
koin-android = { group = "io.insert-koin", name = "koin-android", version.ref = "koin" }
koin-androidx-workmanager = { group = "io.insert-koin", name = "koin-androidx-workmanager", version.ref = "koin" }
koin-compose = { group = "io.insert-koin", name = "koin-compose", version.ref = "koin" }
koin-compose-viewmodel = { group = "io.insert-koin", name = "koin-compose-viewmodel", version.ref = "koin" }
```
Add to `[plugins]`:
```toml
kotlin-multiplatform = { id = "org.jetbrains.kotlin.multiplatform", version.ref = "kotlin" }
compose-multiplatform = { id = "org.jetbrains.compose", version.ref = "composeMultiplatform" }
```
Remove `dagger`, `hilt`, `hilt-*` libraries and `hilt` plugin from `gradle/libs.versions.toml` and root `build.gradle.kts`.

- [ ] **Step 2: Move existing Java/Kotlin source directories into KMP source sets**

Move `app/src/main/java` to `app/src/androidMain/kotlin`, `app/src/test/java` to `app/src/androidUnitTest/kotlin`, and `app/src/androidTest/java` to `app/src/androidInstrumentedTest/kotlin`, while keeping `app/src/main/AndroidManifest.xml`, `app/src/main/res`, and `app/src/main/assets` in place. Create `app/src/commonMain/kotlin`, `app/src/commonTest/kotlin`, `app/src/desktopMain/kotlin`, and `app/src/desktopTest/kotlin`.

- [ ] **Step 3: Configure `app/build.gradle.kts` for Kotlin Multiplatform (`androidTarget` + `jvm("desktop")`)**

Configure `plugins` with `kotlin.multiplatform`, `compose.multiplatform`, `android.application`, `ksp`, `room`, `compose.compiler`, `kotlinx.serialization`, `kotlin.parcelize`, `aboutlibraries`. Set `jvmTarget = JvmTarget.JVM_17` on both `androidTarget()` and `jvm("desktop")`, configure `sourceSets` (`commonMain`, `commonTest`, `androidMain`, `desktopMain`), configure top-level `room { schemaDirectory("$projectDir/schemas") }`, configure `compose.desktop { application { mainClass = "me.ash.reader.MainKt" } }`, and wire Room KSP for `kspCommonMainMetadata`, `kspAndroid`, and `kspDesktop`.

---

### Task 2: Multiplatform Domain Models, Security (`DESUtils`), and Account Preferences in `commonMain`

**Files:**
- Move & Modify: `app/src/androidMain/kotlin/me/ash/reader/ui/ext/BooleanExt.kt` -> `app/src/commonMain/kotlin/me/ash/reader/ui/ext/BooleanExt.kt`
- Move & Modify: `app/src/androidMain/kotlin/me/ash/reader/domain/model/account/security/DESUtils.kt` -> `app/src/commonMain/kotlin/me/ash/reader/domain/model/account/security/DESUtils.kt`
- Move: `app/src/androidMain/kotlin/me/ash/reader/domain/model/account/security/SecurityKey.kt` (and `FeverSecurityKey.kt`, `FreshRSSSecurityKey.kt`, `GoogleReaderSecurityKey.kt`, `LocalSecurityKey.kt`) -> `app/src/commonMain/kotlin/me/ash/reader/domain/model/account/security/`
- Move & Split: `app/src/androidMain/kotlin/me/ash/reader/domain/model/account/AccountType.kt` -> `app/src/commonMain/kotlin/me/ash/reader/domain/model/account/AccountType.kt` + `app/src/androidMain/kotlin/me/ash/reader/domain/model/account/AccountTypeExt.android.kt`
- Move & Split: `SyncIntervalPreference.kt`, `SyncOnStartPreference.kt`, `SyncOnlyOnWiFiPreference.kt`, `SyncOnlyWhenChargingPreference.kt`, `KeepArchivedPreference.kt`, `SyncBlockListPreference.kt` -> `app/src/commonMain/kotlin/me/ash/reader/infrastructure/preference/` + `app/src/androidMain/kotlin/me/ash/reader/infrastructure/preference/AccountPreferenceExt.android.kt`
- Move: `Account.kt`, `SyncIntervalConverters.kt`, `SyncOnStartConverters.kt`, `SyncOnlyOnWiFiConverters.kt`, `SyncOnlyWhenChargingConverters.kt`, `KeepArchivedConverters.kt`, `SyncBlockListConverters.kt` -> `app/src/commonMain/kotlin/me/ash/reader/domain/model/account/`
- Move: `Group.kt`, `GroupWithFeed.kt` -> `app/src/commonMain/kotlin/me/ash/reader/domain/model/group/`
- Move: `Feed.kt`, `FeedWithArticle.kt`, `FeedWithGroup.kt`, `ImportantNum.kt` -> `app/src/commonMain/kotlin/me/ash/reader/domain/model/feed/`
- Move: `Article.kt`, `ArchivedArticle.kt`, `ArticleMeta.kt`, `ArticleWithFeed.kt` -> `app/src/commonMain/kotlin/me/ash/reader/domain/model/article/`
- Move: `Version.kt`, `MarkAsReadConditions.kt`, `ElevationTokens.kt` -> `app/src/commonMain/kotlin/me/ash/reader/domain/model/`
- Create: `app/src/commonTest/kotlin/me/ash/reader/domain/model/account/AccountModelCommonTest.kt`

**Interfaces:**
- Consumes: Task 1 source sets
- Produces:
  - `object DESUtils { const val empty = "CvJ1PKM8EW8="; fun encrypt(cleartext: String): String; fun decrypt(ciphertext: String): String }` in `commonMain`
  - All 5 Room `@Entity` classes (`Account`, `Feed`, `Article`, `Group`, `ArchivedArticle`) and all `@TypeConverter` classes in `commonMain`
  - Android UI/WorkManager extensions `AccountType.toDesc(Context)`, `AccountType.toIcon()`, `SyncIntervalPreference.toDesc(Context)`, `SyncIntervalPreference.put(Int, AccountViewModel)`, `SyncIntervalPreference.toPeriodicWorkRequestBuilder()`, etc. in `androidMain`

- [ ] **Step 1: Write the failing test in `app/src/commonTest/kotlin/me/ash/reader/domain/model/account/AccountModelCommonTest.kt`**

```kotlin
package me.ash.reader.domain.model.account

import me.ash.reader.domain.model.account.security.DESUtils
import me.ash.reader.infrastructure.preference.SyncBlockListPreference
import org.junit.Assert.assertEquals
import org.junit.Test

class AccountModelCommonTest {
    @Test
    fun desUtilsDecryptsLegacyEmptyAndTrailingNewlineCiphertexts() {
        assertEquals("", DESUtils.decrypt(DESUtils.empty))
        assertEquals("", DESUtils.decrypt("CvJ1PKM8EW8=\n"))
        val encrypted = DESUtils.encrypt("secret-token-123")
        assertEquals("secret-token-123", DESUtils.decrypt(encrypted))
    }

    @Test
    fun syncBlockListConvertersRoundTrip() {
        val converters = SyncBlockListConverters()
        val raw = converters.fromSyncBlockList(listOf("ads", "  sponsor  ", ""))
        val parsed = converters.toSyncBlockList(raw).map { it.trim().trimEnd(',') }.filter { it.isNotEmpty() }
        assertEquals(listOf("ads", "sponsor"), parsed)
    }
}
```

- [ ] **Step 2: Move `DESUtils.kt`, `AccountType.kt`, Account preferences, Converters, and Entities to `commonMain` and split Android-only extensions into `androidMain`**

In `DESUtils.kt`, replace `android.util.Base64` with `java.util.Base64.getMimeEncoder()` / `java.util.Base64.getMimeDecoder()` so both standard and newline-terminated Base64 strings from Android decrypt identically. Keep `AccountType.toDesc(Context)` and `@Composable AccountType.toIcon()` in `AccountTypeExt.android.kt` (`package me.ash.reader.domain.model.account`), and keep `put(accountId, viewModel)`, `toDesc(Context)`, and `toPeriodicWorkRequestBuilder()` in `AccountPreferenceExt.android.kt` (`package me.ash.reader.infrastructure.preference`).

- [ ] **Step 3: Run Desktop unit test to verify `commonMain` models compile and pass tests**

Run: `./gradlew :app:desktopTest --tests "me.ash.reader.domain.model.account.AccountModelCommonTest"`
Expected: PASS (2 tests passed)

---

### Task 3: Room KMP Database (`AndroidDatabase` / `RYDatabase`) & DAOs in `commonMain`

**Files:**
- Move & Modify: `AccountDao.kt`, `FeedDao.kt`, `ArticleDao.kt`, `GroupDao.kt` -> `app/src/commonMain/kotlin/me/ash/reader/domain/repository/`
- Move & Modify: `app/src/androidMain/kotlin/me/ash/reader/infrastructure/db/AndroidDatabase.kt` -> `app/src/commonMain/kotlin/me/ash/reader/infrastructure/db/AndroidDatabase.kt`
- Create: `app/src/androidMain/kotlin/me/ash/reader/infrastructure/db/DatabaseFactory.android.kt`
- Create: `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/db/DatabaseFactory.desktop.kt`
- Create: `app/src/desktopTest/kotlin/me/ash/reader/infrastructure/db/DesktopDatabaseTest.kt`

**Interfaces:**
- Consumes: Task 2 Entities and TypeConverters
- Produces:
  - `abstract class AndroidDatabase : RoomDatabase()` and `typealias RYDatabase = AndroidDatabase` in `me.ash.reader.infrastructure.db` (`commonMain`)
  - `expect object AndroidDatabaseConstructor : RoomDatabaseConstructor<AndroidDatabase>`
  - `val allMigrations: Array<Migration>` using `SQLiteConnection` (`androidx.sqlite.execSQL`)
  - `fun createAndroidDatabase(context: Context): AndroidDatabase` in `androidMain`
  - `fun createDesktopDatabase(dbFile: File): AndroidDatabase` in `desktopMain`

- [ ] **Step 1: Write the failing test in `app/src/desktopTest/kotlin/me/ash/reader/infrastructure/db/DesktopDatabaseTest.kt`**

```kotlin
package me.ash.reader.infrastructure.db

import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import me.ash.reader.domain.model.account.Account
import me.ash.reader.domain.model.account.AccountType
import org.junit.Assert.assertEquals
import org.junit.Test

class DesktopDatabaseTest {
    @Test
    fun roomInMemoryDatabaseInsertsAndQueriesAccount() = runBlocking {
        val db = Room.inMemoryDatabaseBuilder<AndroidDatabase>()
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO)
            .addMigrations(*allMigrations)
            .build()
        try {
            val id = db.accountDao().insert(Account(name = "Desktop Local", type = AccountType.Local)).toInt()
            val loaded = db.accountDao().queryById(id)
            assertEquals("Desktop Local", loaded?.name)
            assertEquals(AccountType.Local.id, loaded?.type?.id)
        } finally {
            db.close()
        }
    }
}
```

- [ ] **Step 2: Move the 4 DAOs and `AndroidDatabase.kt` to `commonMain` and create platform database factories**

In `FeedDao.kt`, remove `import timber.log.Timber` and `Timber.d("Update ${new.name}")`.
In `AndroidDatabase.kt`:
- Annotate `@Database(...)` and `@TypeConverters(...)` and `@ConstructedBy(AndroidDatabaseConstructor::class)` on `abstract class AndroidDatabase : RoomDatabase()`.
- Declare `@Suppress("NO_ACTUAL_FOR_EXPECT") expect object AndroidDatabaseConstructor : RoomDatabaseConstructor<AndroidDatabase> { override fun initialize(): AndroidDatabase }`.
- Declare `typealias RYDatabase = AndroidDatabase`.
- Update `MIGRATION_1_2`, `MIGRATION_2_3`, `MIGRATION_3_4`, `MIGRATION_4_5` to override `fun migrate(connection: SQLiteConnection)` using `import androidx.sqlite.SQLiteConnection` and `import androidx.sqlite.execSQL`.
- Implement `createAndroidDatabase(context: Context): AndroidDatabase` in `DatabaseFactory.android.kt` (using `context.getDatabasePath("Reader")` and `BundledSQLiteDriver()`) and `createDesktopDatabase(dbFile: File): AndroidDatabase` in `DatabaseFactory.desktop.kt`.

- [ ] **Step 3: Run Desktop database test to verify Room KMP KSP code generation and `BundledSQLiteDriver`**

Run: `./gradlew :app:desktopTest --tests "me.ash.reader.infrastructure.db.DesktopDatabaseTest"`
Expected: PASS (1 test passed)

---

### Task 4: Multiplatform `AppPaths`, `DataStore<Preferences>`, and Network (`OkHttp` & `Retrofit`) in `commonMain`

**Files:**
- Create: `app/src/commonMain/kotlin/me/ash/reader/infrastructure/platform/AppPaths.kt`
- Create: `app/src/androidMain/kotlin/me/ash/reader/infrastructure/platform/AppPaths.android.kt`
- Create: `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/platform/AppPaths.desktop.kt`
- Create: `app/src/commonMain/kotlin/me/ash/reader/infrastructure/datastore/DataStoreFactory.kt`
- Create: `app/src/commonMain/kotlin/me/ash/reader/infrastructure/datastore/PreferencesKey.kt`
- Modify: `app/src/androidMain/kotlin/me/ash/reader/ui/ext/DataStoreExt.kt`
- Move: `app/src/androidMain/kotlin/me/ash/reader/infrastructure/net/NetworkDataSource.kt` -> `app/src/commonMain/kotlin/me/ash/reader/infrastructure/net/NetworkDataSource.kt`
- Move: `app/src/androidMain/kotlin/me/ash/reader/infrastructure/net/RetryableTask.kt` -> `app/src/commonMain/kotlin/me/ash/reader/infrastructure/net/RetryableTask.kt`
- Create: `app/src/commonMain/kotlin/me/ash/reader/infrastructure/net/HttpClientFactory.kt`
- Create: `app/src/androidMain/kotlin/me/ash/reader/infrastructure/net/PlatformSsl.android.kt`
- Create: `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/net/PlatformSsl.desktop.kt`
- Create: `app/src/desktopTest/kotlin/me/ash/reader/infrastructure/datastore/DesktopDataStoreTest.kt`

**Interfaces:**
- Consumes: Task 1 Okio & DataStore Core dependencies
- Produces:
  - `data class AppPaths(val dataDir: File, val cacheDir: File, val dataStoreFile: File)` in `me.ash.reader.infrastructure.platform`
  - `fun createAndroidAppPaths(context: Context): AppPaths` (`dataStoreFile = context.filesDir.resolve("datastore/settings.preferences_pb")`)
  - `fun createDesktopAppPaths(): AppPaths` (macOS `~/Library/Application Support/me.ash.reader/`, Windows `%APPDATA%\ReadYou\`)
  - `fun createPreferenceDataStore(appPaths: AppPaths, scope: CoroutineScope): DataStore<Preferences>`
  - `fun createOkHttpClient(appPaths: AppPaths, userAgent: String): OkHttpClient`

- [ ] **Step 1: Write the failing test in `app/src/desktopTest/kotlin/me/ash/reader/infrastructure/datastore/DesktopDataStoreTest.kt`**

```kotlin
package me.ash.reader.infrastructure.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import java.nio.file.Files
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import me.ash.reader.infrastructure.platform.AppPaths
import org.junit.Assert.assertEquals
import org.junit.Test

class DesktopDataStoreTest {
    @Test
    fun preferenceDataStoreWritesAndReadsKeysOnDesktop() = runBlocking {
        val tempDir = Files.createTempDirectory("readyou-test").toFile()
        val appPaths = AppPaths(
            dataDir = tempDir,
            cacheDir = tempDir.resolve("cache"),
            dataStoreFile = tempDir.resolve("datastore/settings.preferences_pb"),
        )
        val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        try {
            val dataStore = createPreferenceDataStore(appPaths, scope)
            val key = intPreferencesKey(PreferencesKey.currentAccountId)
            dataStore.edit { it[key] = 42 }
            assertEquals(42, dataStore.data.first()[key])
        } finally {
            scope.cancel()
            tempDir.deleteRecursively()
        }
    }
}
```

- [ ] **Step 2: Implement `AppPaths`, `DataStoreFactory.kt`, `PreferencesKey.kt`, and `HttpClientFactory.kt`**

- Implement `AppPaths` in `commonMain`, `createAndroidAppPaths(context: Context)` in `AppPaths.android.kt`, and `createDesktopAppPaths()` in `AppPaths.desktop.kt`.
- Implement `createPreferenceDataStore(appPaths: AppPaths, scope: CoroutineScope): DataStore<Preferences>` using `PreferenceDataStoreFactory.createWithPath(scope = scope) { appPaths.dataStoreFile.absolutePath.toPath() }`.
- Move `PreferencesKey` and `DataStoreKey` definitions into `commonMain`, and update `val Context.dataStore: DataStore<Preferences>` in `DataStoreExt.kt` (`androidMain`) to retrieve the singleton `GlobalContext.get().get<DataStore<Preferences>>()` so no duplicate DataStore instance is ever opened for `settings.preferences_pb`.
- Implement `createOkHttpClient(appPaths: AppPaths, userAgent: String): OkHttpClient` in `HttpClientFactory.kt` with `expect fun OkHttpClient.Builder.configurePlatformSsl(clientCertificateAlias: String?, trustAllCerts: Boolean)` (`androidMain` uses `KeyChain`, `desktopMain` uses JVM `X509TrustManager`).

- [ ] **Step 3: Run Desktop DataStore unit test**

Run: `./gradlew :app:desktopTest --tests "me.ash.reader.infrastructure.datastore.DesktopDataStoreTest"`
Expected: PASS (1 test passed)

---

### Task 5: Replace Dagger Hilt with Koin Multiplatform Across All Modules, ViewModels, Activities, Workers & Widgets

**Files:**
- Delete: all 13 Hilt files in `app/src/androidMain/kotlin/me/ash/reader/infrastructure/di/` (`AccountServiceModule.kt`, `ApplicationScope.kt`, `CacheHolderModule.kt`, `CoroutineDispatcherModule.kt`, `CoroutineQualifier.kt`, `CoroutineScopeModule.kt`, `DatabaseModule.kt`, `ImageLoaderModule.kt`, `OkHttpClientModule.kt`, `RetrofitModule.kt`, `SettingsProviderModule.kt`, `UseCaseModule.kt`, `WorkerModule.kt`)
- Create: `app/src/commonMain/kotlin/me/ash/reader/infrastructure/di/Qualifiers.kt`
- Create: `app/src/commonMain/kotlin/me/ash/reader/infrastructure/di/AppModules.kt`
- Create: `app/src/androidMain/kotlin/me/ash/reader/infrastructure/di/PlatformModule.android.kt`
- Create: `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/di/PlatformModule.desktop.kt`
- Modify: all `@Inject` / `@HiltViewModel` / `@AndroidEntryPoint` / `@HiltAndroidApp` / `@HiltWorker` / `hiltViewModel()` files in `app/src/androidMain/kotlin/me/ash/reader/`

**Interfaces:**
- Consumes: `AndroidDatabase`, `AppPaths`, `createPreferenceDataStore`, `createOkHttpClient`, `NetworkDataSource`
- Produces:
  - `val commonModule: Module` and `expect val platformModule: Module` in `me.ash.reader.infrastructure.di`
  - Named qualifiers `APPLICATION_SCOPE`, `IO_DISPATCHER`, `DEFAULT_DISPATCHER`, `MAIN_DISPATCHER`, `MAIN_IMMEDIATE_DISPATCHER`

- [ ] **Step 1: Create Koin `Qualifiers.kt`, `AppModules.kt` (`commonMain`), `PlatformModule.android.kt` (`androidMain`), and `PlatformModule.desktop.kt` (`desktopMain`)**

In `AppModules.kt`:
```kotlin
package me.ash.reader.infrastructure.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.domain.repository.ArticleDao
import me.ash.reader.domain.repository.FeedDao
import me.ash.reader.domain.repository.GroupDao
import me.ash.reader.infrastructure.datastore.createPreferenceDataStore
import me.ash.reader.infrastructure.db.AndroidDatabase
import me.ash.reader.infrastructure.net.NetworkDataSource
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

expect val platformModule: Module

val commonModule = module {
    single<CoroutineDispatcher>(named(DEFAULT_DISPATCHER)) { Dispatchers.Default }
    single<CoroutineDispatcher>(named(IO_DISPATCHER)) { Dispatchers.IO }
    single<CoroutineDispatcher>(named(MAIN_DISPATCHER)) { Dispatchers.Main }
    single<CoroutineDispatcher>(named(MAIN_IMMEDIATE_DISPATCHER)) { Dispatchers.Main.immediate }
    single<CoroutineScope>(named(APPLICATION_SCOPE)) {
        CoroutineScope(SupervisorJob() + get<CoroutineDispatcher>(named(DEFAULT_DISPATCHER)))
    }
    single<AccountDao> { get<AndroidDatabase>().accountDao() }
    single<FeedDao> { get<AndroidDatabase>().feedDao() }
    single<ArticleDao> { get<AndroidDatabase>().articleDao() }
    single<GroupDao> { get<AndroidDatabase>().groupDao() }
    single<DataStore<Preferences>> {
        createPreferenceDataStore(get(), get(named(APPLICATION_SCOPE)))
    }
    single<NetworkDataSource> { NetworkDataSource.getInstance() }
}
```
In `PlatformModule.android.kt`, register `AppPaths`, `AndroidDatabase`, `OkHttpClient`, `ImageLoader`, `WorkManager`, all singletons (`SettingsProvider`, `AccountService`, `AndroidStringsHelper`, `NotificationHelper`, `AndroidImageDownloader`, `TextToSpeechManager`, `OPMLDataSource`, `RssHelper`, `ReaderCacheHelper`, `DiffMapHolder`, `SyncLogger`, `LocalRssService`, `FeverRssService`, `GoogleReaderRssService`, `RssService`, `OpmlService`, `AppService`, `FilterStateUseCase`, `ArticlePagingListUseCase`, `GroupWithFeedsListUseCase`, `WidgetRepository`), all 9 ViewModels via `viewModelOf(...)`, and all 3 Workers (`SyncWorker`, `ReaderWorker`, `WidgetUpdateWorker`) via `workerOf(...)`.

- [ ] **Step 2: Remove all Hilt/Dagger annotations and replace `hiltViewModel()` with `koinViewModel()`**

- Remove `@Inject`, `@ApplicationContext`, `@ApplicationScope`, `@IODispatcher`, `@DefaultDispatcher`, `@MainDispatcher`, `@MainImmediateDispatcher`, and `@HiltViewModel` from all services, use cases, helpers, and the 9 ViewModels.
- Update `SyncWorker`, `ReaderWorker`, and `WidgetUpdateWorker` to standard constructors `(context: Context, workerParams: WorkerParameters, ...)` registered with Koin's `workerOf(::SyncWorker)`, `workerOf(::ReaderWorker)`, `workerOf(::WidgetUpdateWorker)`.
- Update `AndroidApp` to call `startKoin { androidContext(this@AndroidApp); workManagerFactory(); modules(commonModule, platformModule) }` in `onCreate()` and resolve properties via `by inject()`.
- Update `MainActivity`, `CrashReportActivity`, `WidgetConfigActivity`, `ArticleCardWidgetReceiver`, and `ArticleListWidgetReceiver` to use `by inject()` (implementing `KoinComponent` on `BroadcastReceiver` subclasses) and update `WidgetRepository.Companion.get(context)` to `GlobalContext.get().get()`.
- Replace `androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel` with `org.koin.compose.viewmodel.koinViewModel` across all Composable call sites.

- [ ] **Step 3: Verify zero Hilt/Dagger references remain and compile Android debug APK**

Run: `./gradlew :app:assembleGithubDebug`
Expected: `BUILD SUCCESSFUL` with zero references to `dagger.hilt` or `javax.inject`.

---

### Task 6: Desktop Entry Point (`Main.kt`) & End-to-End Phase 1 Verification

**Files:**
- Create: `app/src/desktopMain/kotlin/me/ash/reader/Main.kt`
- Create: `app/src/desktopTest/kotlin/me/ash/reader/infrastructure/di/DesktopKoinSmokeTest.kt`

**Interfaces:**
- Consumes: `commonModule`, `platformModule` (`desktopMain`), `AndroidDatabase`, `DataStore<Preferences>`, `AppPaths`
- Produces:
  - `fun main()` in `me.ash.reader.MainKt` launching the Compose Multiplatform Desktop window with Koin + Room KMP + DataStore KMP initialized

- [ ] **Step 1: Write the failing Koin Desktop smoke test in `app/src/desktopTest/kotlin/me/ash/reader/infrastructure/di/DesktopKoinSmokeTest.kt`**

```kotlin
package me.ash.reader.infrastructure.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import me.ash.reader.domain.repository.AccountDao
import me.ash.reader.infrastructure.db.AndroidDatabase
import me.ash.reader.infrastructure.platform.AppPaths
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

class DesktopKoinSmokeTest {
    @Test
    fun desktopKoinGraphResolvesDatabaseDaosAndDataStore() {
        val app = startKoin {
            modules(commonModule, platformModule)
        }
        try {
            val koin = app.koin
            assertNotNull(koin.get<AppPaths>())
            assertNotNull(koin.get<AndroidDatabase>())
            assertNotNull(koin.get<AccountDao>())
            assertNotNull(koin.get<DataStore<Preferences>>())
        } finally {
            stopKoin()
        }
    }
}
```

- [ ] **Step 2: Implement `PlatformModule.desktop.kt` and `app/src/desktopMain/kotlin/me/ash/reader/Main.kt`**

In `PlatformModule.desktop.kt`, provide `AppPaths`, `AndroidDatabase` (`createDesktopDatabase(File(get<AppPaths>().dataDir, "ReadYou.db"))`), and `OkHttpClient`.
In `Main.kt`, initialize `startKoin { modules(commonModule, platformModule) }` and open a Compose Desktop `Window(onCloseRequest = ::exitApplication, title = "ReadYou")` displaying the database path, account count from `AccountDao.queryAllAsFlow()`, and DataStore status.

- [ ] **Step 3: Run Desktop tests and compile both Desktop and Android targets**

Run: `./gradlew :app:desktopTest :app:compileKotlinDesktop :app:assembleGithubDebug`
Expected: `BUILD SUCCESSFUL` with all Desktop tests and Android debug build passing.
