# Thiết Kế Kỹ Thuật — Chặng 1: Nền Tảng KMP/CMP, DI (Hilt sang Koin), Room KMP & DataStore KMP

**Ngày:** 2026-10-29  
**Dự án:** Nâng cấp ReadYou (`me.ash.reader`) lên Kotlin Multiplatform (KMP) & Compose Multiplatform (CMP)  
**Phạm vi:** Chặng 1 trong lộ trình 4 chặng (Nền tảng Build đa mục tiêu, Chuyển đổi Dependency Injection từ Hilt sang Koin, Đa nền tảng hóa Room Database & DataStore Preferences, Khởi tạo Desktop Target).

---

## 1. Mục Đích & Lý Do (Intent & Why)

ReadYou là ứng dụng đọc RSS mã nguồn mở hiện được viết độc quyền cho Android (`com.android.application` + `org.jetbrains.kotlin.android`), nhưng khoảng 90% mã nguồn thuộc về tầng nghiệp vụ JVM (`OkHttp`, `Retrofit`, `Rome`, `Jsoup`, `Readability4J`), cơ sở dữ liệu (`Room`), cấu hình (`DataStore`) và giao diện (`Jetpack Compose` + `Material 3 Expressive` + `Navigation 3`).

Mục tiêu tổng thể là mở rộng ReadYou sang **Windows (`x64`/`arm64`)** và **macOS Universal (`arm64` + `x86_64`)** với triết lý **100% Native** (Skia GPU + OS Native integration), giữ nguyên trải nghiệm của bản Android.

Vì dự án có **373 file Kotlin** (trong đó **155 file** import `android.*` và **110 file** tham chiếu `me.ash.reader.R`), việc chuyển đổi toàn bộ trong một bước duy nhất sẽ làm gãy khả năng biên dịch trong thời gian dài. Do đó, dự án được chia thành **4 chặng độc lập, có thể biên dịch và kiểm thử ngay sau mỗi chặng**:

1. **Chặng 1 (Tài liệu này):** Thiết lập KMP/CMP (`androidTarget` + `jvm("desktop")`), thay thế hoàn toàn Dagger Hilt bằng **Koin Multiplatform**, chuyển **Room Database (schema v1..v7)** và **DataStore Core** sang `commonMain`, tách `android.content.Context` khỏi tầng dữ liệu/cache, và dựng điểm vào `desktopMain` khởi chạy cửa sổ Compose Desktop kết nối thành công với DB/DataStore.
2. **Chặng 2:** Đa nền tảng hóa tài nguyên (`composeResources`), Coil 3 và chuyển toàn bộ giao diện `ui/page/` sang `commonMain`.
3. **Chặng 3:** Hệ thống Dual-Mode Reader (`HtmlToComposable` + OS Native WebView qua `WKWebView`/`WebView2`).
4. **Chặng 4:** Tích hợp hệ thống Desktop (System Tray, TTS, FileDialog, Keyboard Shortcuts) & CI/CD đóng gói `.dmg`/`.msi`/`.exe`.

---

## 2. Mục Tiêu, Phi Mục Tiêu & Chống Mục Tiêu (Goals, Non-Goals & Anti-Goals)

### Mục tiêu (Goals)
- Chuyển đổi module `:app` sang cấu trúc **Kotlin Multiplatform (`org.jetbrains.kotlin.multiplatform`)** kết hợp **Compose Multiplatform (`org.jetbrains.compose`)** với 2 target: `androidTarget()` và `jvm("desktop")`.
- Giữ nguyên cấu hình Android (`flavors`: `github`, `fdroid`, `googlePlay`; `signingConfigs`; `room` schema `1..7.json`; `buildConfigField("USER_AGENT_STRING", ...)`).
- **Xóa bỏ hoàn toàn Dagger Hilt** (`com.google.dagger.hilt.android`, `@HiltAndroidApp`, `@AndroidEntryPoint`, `@HiltViewModel`, `@HiltWorker`, `@EntryPoint`, `@Inject`) và thay thế bằng **Koin 4.x** (`koin-core`, `koin-android`, `koin-androidx-workmanager`, `koin-compose`, `koin-compose-viewmodel`) trên toàn bộ dự án.
- Chuyển đổi cơ sở dữ liệu [`AndroidDatabase`](file:///Users/stark/Documents/GitHub/ReadYou/app/src/main/java/me/ash/reader/infrastructure/db/AndroidDatabase.kt) sang **Room KMP (`2.7.2`)** + **`androidx.sqlite:sqlite-bundled`** trong `src/commonMain` (giữ nguyên tên class hoặc alias `AndroidDatabase` / `RYDatabase` để tương thích schema `1..7.json`, giữ nguyên 5 Entity, 4 DAO, 8 TypeConverter và toàn bộ `MIGRATION_1_2` .. `MIGRATION_4_5` + `AutoMigration` 5 $\rightarrow$ 7).
- Chuyển đổi khởi tạo `DataStore<Preferences>` sang `PreferenceDataStoreFactory.createWithPath` (Okio `Path`) dùng chung cho cả Android và Desktop, đảm bảo **tương thích ngược 100%** với file `datastore/settings.preferences_pb` hiện có trên thiết bị Android của người dùng.
- Cung cấp lớp quản lý đường dẫn đa nền tảng `AppPaths` (`dataDir`, `cacheDir`) và mạng (`OkHttpClient`, `NetworkDataSource`) trong `commonMain`.
- Tạo entry point `src/desktopMain/kotlin/me/ash/reader/Main.kt` khởi tạo Koin + Room KMP + DataStore KMP trên Desktop và mở cửa sổ Compose Desktop hiển thị trạng thái kết nối Database/Account & Settings.
- Đảm bảo cả `:app:assembleGithubDebug` (Android) và `:app:compileKotlinDesktop` / `:app:run` (Desktop) đều biên dịch và chạy thành công.

### Phi mục tiêu trong Chặng 1 (Non-Goals)
- Chưa di chuyển 42 thư mục `values-*/strings.xml` sang `composeResources` và chưa sửa 110 file đang gọi `me.ash.reader.R` (thuộc Chặng 2).
- Chưa di chuyển các màn hình `ui/page/` đang phụ thuộc `android.content.Context` và `R.*` sang `commonMain` (trong Chặng 1 các file này nằm tại `src/androidMain`, sẽ được nhấc sang `commonMain` ở Chặng 2).
- Chưa triển khai WebView Desktop hoặc nâng cấp Coil 2 lên Coil 3 (thuộc Chặng 2 & 3).

### Chống mục tiêu (Anti-Goals)
- **Không** duy trì song song Hilt và Koin.
- **Không** tạo đối tượng `PlatformContext` giả lập trong `commonMain` chỉ để lách chữ ký hàm Android.
- **Không** làm thay đổi cấu trúc bảng SQLite hay đường dẫn file `.db` / `.preferences_pb` trên Android khiến người dùng hiện tại mất dữ liệu khi cập nhật ứng dụng.
- **Không** sử dụng các phiên bản thư viện giả định/chưa phát hành trên Maven Central / Google Maven.

---

## 3. Ràng Buộc Kỹ Thuật (Constraints)

1. **Phiên bản thư viện thực tế trên Maven:**
   - Kotlin: `2.2.0` (tương thích KSP `2.2.0-2.0.2`).
   - Android Gradle Plugin (AGP): `8.13.0`.
   - Compose Multiplatform plugin: `1.8.2` (hoặc bản ổn định tương thích Kotlin `2.2.0`).
   - Room KMP: `2.7.2` (`androidx.room:room-runtime`, `androidx.room:room-compiler`, `androidx.room:room-paging`) + `androidx.sqlite:sqlite-bundled:2.5.1`.
   - DataStore: `1.1.4` (`androidx.datastore:datastore-preferences-core`, `androidx.datastore:datastore-preferences`).
   - Paging: `3.3.6` (`androidx.paging:paging-common`, `androidx.paging:paging-compose`).
   - Koin: `4.1.0` (`io.insert-koin:koin-core`, `koin-android`, `koin-androidx-workmanager`, `koin-compose`, `koin-compose-viewmodel`).
2. **Tương thích Schema Room (`schemas/me.ash.reader.infrastructure.db.AndroidDatabase/1..7.json`):**
   - Thư mục `app/schemas/` hiện lưu schema dưới tên đầy đủ `me.ash.reader.infrastructure.db.AndroidDatabase`. Nếu đổi tên class `@Database` mà không di chuyển thư mục schema, Room AutoMigration (từ v5 $\rightarrow$ v6 $\rightarrow$ v7) sẽ báo lỗi không tìm thấy schema cũ. Do đó, giữ nguyên package + tên class `me.ash.reader.infrastructure.db.AndroidDatabase` (và khai báo `typealias RYDatabase = AndroidDatabase`) trong `commonMain`, kết hợp `@ConstructedBy(AndroidDatabaseConstructor::class)` theo chuẩn Room KMP.
3. **Tương thích File Database & DataStore trên Android:**
   - Trên Android, tên file Room DB phải giữ nguyên là `"Reader"` (`context.getDatabasePath("Reader").absolutePath`), và file DataStore phải giữ nguyên tại `context.filesDir.resolve("datastore/settings.preferences_pb")` (trùng khớp với `preferencesDataStore(name = "settings")` cũ).
4. **Target JVM & Toolchain:**
   - Nâng `jvmTarget` từ Java 11 lên **Java 17** (yêu cầu tối thiểu của Compose Multiplatform Desktop & đóng gói `jpackage`).

---

## 4. Các Phương Án Kiến Trúc Đã Cân Nhắc (Approaches)

### Phương án 1: Giữ nguyên 1 module `:app` đa target (`commonMain` + `androidMain` + `desktopMain`) và dịch chuyển từng tầng (Đã chọn)
- **Mô tả:** Chuyển `:app` sang plugin `org.jetbrains.kotlin.multiplatform` + `com.android.application` + `org.jetbrains.compose`. Đưa toàn bộ Entity, DAO, Converter, Room Database (`AndroidDatabase` / `RYDatabase`), DataStore Core, Network (`OkHttp`, `Retrofit`), `AppPaths`, và cấu hình Koin gốc vào `src/commonMain`. Toàn bộ code Android UI/Service hiện còn phụ thuộc `R.*` và `Context` được đặt trong `src/androidMain` (hoặc trỏ source set `androidMain` vào code hiện hữu sau khi đã tách phần `commonMain`), đồng thời thay thế 100% Hilt bằng Koin. Tạo `src/desktopMain` với cửa sổ Compose Desktop khởi chạy cùng Koin + Room + DataStore.
- **Ưu điểm:** Bám sát cấu trúc thư mục 1 module `:app` trong định hướng của dự án; loại bỏ hoàn toàn Hilt ngay trong Chặng 1; đảm bảo cả Android và Desktop đều biên dịch và chạy được ngay khi kết thúc Chặng 1 mà không bị chặn bởi 110 file `R.*`.
- **Nhược điểm:** Cần tách khéo léo các hàm mở rộng phụ thuộc Android (`toDesc(context)`, `toIcon()`, `put(accountId, viewModel)`, `toPeriodicWorkRequestBuilder()`) ra khỏi các class model/preference dùng trong Room Entity (`AccountType`, `SyncIntervalPreference`, v.v.).

### Phương án 2: Tách thành 2 module (`:shared` KMP + `:app` Android)
- **Mô tả:** Tạo module `:shared` chứa `commonMain`, `androidMain`, `desktopMain` và giữ `:app` thuần Android.
- **Ưu điểm:** Tách biệt cấu hình đóng gói Android App khỏi KMP library.
- **Nhược điểm:** Khác với cấu trúc 1 module `:app` mong muốn, buộc phải mở visibility `public` cho hàng loạt thành phần nội bộ và cấu hình lại đường dẫn Room schema.

### Phương án 3: Đưa toàn bộ `Preference` và `domain/service` vào `commonMain` ngay ở Chặng 1 bằng `expect class PlatformContext`
- **Mô tả:** Tạo lớp `PlatformContext` giả lập `android.content.Context` trên Desktop.
- **Ưu điểm:** Ít phải sửa chữ ký hàm `put(context, scope)`.
- **Nhược điểm:** Mang anti-pattern `Context` của Android sang Desktop và vẫn không biên dịch được `commonMain` nếu chưa chuyển xong 110 file `R.string.*` của Chặng 2.

**Quyết định:** Chọn **Phương án 1**.

---

## 5. Thiết Kế Chi Tiết Chặng 1 (Detailed Design)

### 5.1. Cấu hình Gradle (`gradle/libs.versions.toml` & `app/build.gradle.kts`)

1. **Plugins áp dụng trong `app/build.gradle.kts`:**
   - Thay `alias(libs.plugins.kotlin.android)` và `alias(libs.plugins.hilt)` bằng:
     - `alias(libs.plugins.kotlin.multiplatform)` (`org.jetbrains.kotlin.multiplatform`)
     - `alias(libs.plugins.compose.multiplatform)` (`org.jetbrains.compose`)
     - Giữ `alias(libs.plugins.android.application)`, `alias(libs.plugins.ksp)`, `alias(libs.plugins.room)`, `alias(libs.plugins.compose.compiler)`, `alias(libs.plugins.kotlinx.serialization)`, `alias(libs.plugins.kotlin.parcelize)`, `alias(libs.plugins.aboutlibraries)`.
2. **Targets trong khối `kotlin { ... }`:**
   - `androidTarget()` với `compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }`.
   - `jvm("desktop")` với `compilerOptions { jvmTarget.set(JvmTarget.JVM_17) }`.
3. **KSP cho Room Multiplatform:**
   - Thay `ksp(libs.room.compiler)` đơn lẻ bằng:
     ```kotlin
     dependencies {
         add("kspCommonMainMetadata", libs.room.compiler)
         add("kspAndroid", libs.room.compiler)
         add("kspDesktop", libs.room.compiler)
     }
     ```
   - Cấu hình `room { schemaDirectory("$projectDir/schemas") }` ở cấp cao nhất.
4. **Cấu trúc Source Sets:**
   - `src/commonMain/kotlin`: Chứa toàn bộ code đa nền tảng (Database, Entities, DAOs, TypeConverters, DataStore Core, AppPaths, Network, Koin `commonModule` + `expect val platformModule`, Multiplatform Logger).
   - `src/androidMain/kotlin` (cùng `src/main/res`, `src/main/assets`, `src/main/AndroidManifest.xml`): Chứa phần code Android hiện tại đã được chuyển sang Koin (`AndroidApp`, `MainActivity`, `CrashReportActivity`, `SyncWorker`, `ReaderWorker`, `WidgetUpdateWorker`, Glance Widgets, Services, ViewModels, UI pages chưa chuyển sang `commonMain`).
   - `src/desktopMain/kotlin`: Chứa `Main.kt`, `platformModule.desktop.kt`, `AppPaths.desktop.kt`, `DatabaseFactory.desktop.kt`, `SSLHelper.desktop.kt`.

### 5.2. Đa Nền Tảng Hóa Tầng Model & Room Database (`src/commonMain`)

Để toàn bộ 5 `@Entity` (`Account`, `Feed`, `Article`, `Group`, `ArchivedArticle`), 4 `@Dao` (`AccountDao`, `FeedDao`, `ArticleDao`, `GroupDao`) và `AndroidDatabase` (`RYDatabase`) nằm hoàn toàn trong `src/commonMain`:

1. **Tách các hàm phụ thuộc Android/UI khỏi các kiểu dữ liệu của `Account`:**
   - [`AccountType.kt`](file:///Users/stark/Documents/GitHub/ReadYou/app/src/main/java/me/ash/reader/domain/model/account/AccountType.kt):
     - Trong `commonMain`: Giữ nguyên `class AccountType(val id: Int)` và `class AccountTypeConverters`.
     - Trong `androidMain` (`AccountTypeExt.android.kt`): Đặt extension `fun AccountType.toDesc(context: Context): String` và `@Composable fun AccountType.toIcon(): Any` (cùng package `me.ash.reader.domain.model.account` để mọi nơi gọi hiện tại không cần đổi import).
   - 6 lớp cấu hình gắn với `Account` (`SyncIntervalPreference`, `SyncOnStartPreference`, `SyncOnlyOnWiFiPreference`, `SyncOnlyWhenChargingPreference`, `KeepArchivedPreference`, `SyncBlockListPreference`):
     - Trong `commonMain`: Giữ nguyên `sealed class` + `value` + `companion object` (`default`, `values`) + toán tử `operator fun not()` + các `*Converters` (`SyncIntervalConverters`, v.v.).
     - Trong `androidMain` (`AccountPreferenceExt.android.kt`): Đặt các hàm mở rộng `fun *.put(accountId: Int, viewModel: AccountViewModel)`, `fun *.toDesc(context: Context): String`, và `fun SyncIntervalPreference.toPeriodicWorkRequestBuilder()` trong cùng package `me.ash.reader.infrastructure.preference`.
2. **Thay thế `android.util.Base64` trong [`DESUtils.kt`](file:///Users/stark/Documents/GitHub/ReadYou/app/src/main/java/me/ash/reader/domain/model/account/security/DESUtils.kt):**
   - Dùng `java.util.Base64.getMimeEncoder().encodeToString(...)` và `java.util.Base64.getMimeDecoder().decode(...)` (hoặc `java.util.Base64.getEncoder()`), đảm bảo giải mã tương thích 100% với chuỗi có/không có ký tự xuống dòng `\n` của `android.util.Base64.DEFAULT` và hằng số `DESUtils.empty = "CvJ1PKM8EW8="`.
3. **Thay thế `timber.log.Timber` trong [`FeedDao.kt`](file:///Users/stark/Documents/GitHub/ReadYou/app/src/main/java/me/ash/reader/domain/repository/FeedDao.kt) và [`GoogleReaderAPI.kt`](file:///Users/stark/Documents/GitHub/ReadYou/app/src/main/java/me/ash/reader/infrastructure/rss/provider/greader/GoogleReaderAPI.kt):**
   - Tạo `MultiplatformLogger` (hoặc `expect object RLog`) trong `commonMain`, hoặc trong `FeedDao.insertOrUpdate` bỏ lời gọi `Timber.d(...)` không cần thiết để `FeedDao` thuần Room KMP.
4. **Khởi tạo Room KMP (`AndroidDatabase.kt` trong `commonMain`):**
   - Sử dụng `@ConstructedBy(AndroidDatabaseConstructor::class)` và `expect object AndroidDatabaseConstructor : RoomDatabaseConstructor<AndroidDatabase>`.
   - Chuyển `MIGRATION_1_2`, `MIGRATION_2_3`, `MIGRATION_3_4`, `MIGRATION_4_5` sang sử dụng `override fun migrate(connection: SQLiteConnection)` với `connection.execSQL(...)` của `androidx.sqlite.SQLiteConnection` (API chuẩn của Room KMP 2.7.x dùng chung cho cả Android và Desktop).
   - Khai báo `typealias RYDatabase = AndroidDatabase`.
   - Trên **Android (`androidMain`)**:
     ```kotlin
     fun createAndroidDatabase(context: Context): AndroidDatabase {
         val dbFile = context.getDatabasePath("Reader")
         return Room.databaseBuilder<AndroidDatabase>(
             context = context.applicationContext,
             name = dbFile.absolutePath,
         )
             .setDriver(BundledSQLiteDriver())
             .setQueryCoroutineContext(Dispatchers.IO)
             .addMigrations(*allMigrations)
             .build()
     }
     ```
   - Trên **Desktop (`desktopMain`)**:
     ```kotlin
     fun createDesktopDatabase(appPaths: AppPaths): AndroidDatabase {
         val dbFile = File(appPaths.dataDir, "ReadYou.db")
         return Room.databaseBuilder<AndroidDatabase>(
             name = dbFile.absolutePath,
         )
             .setDriver(BundledSQLiteDriver())
             .setQueryCoroutineContext(Dispatchers.IO)
             .addMigrations(*allMigrations)
             .build()
     }
     ```

### 5.3. Quản Lý Đường Dẫn Hệ Điều Hành (`AppPaths`) & `DataStore<Preferences>`

1. **Lớp `AppPaths` (`src/commonMain/kotlin/me/ash/reader/infrastructure/platform/AppPaths.kt`):**
   ```kotlin
   data class AppPaths(
       val dataDir: File,
       val cacheDir: File,
       val dataStoreFile: File,
   )
   ```
   - **Android (`androidMain`):**
     - `dataDir = context.filesDir`
     - `cacheDir = context.cacheDir`
     - `dataStoreFile = context.filesDir.resolve("datastore/settings.preferences_pb")` (trùng khớp tuyệt đối với đường dẫn của `preferencesDataStore(name = "settings")` để giữ nguyên cài đặt cũ của người dùng Android).
   - **Desktop (`desktopMain`):**
     - **macOS:** `~/Library/Application Support/me.ash.reader/` (`dataDir`), `~/Library/Caches/me.ash.reader/` (`cacheDir`).
     - **Windows:** `%APPDATA%\ReadYou\` (`dataDir`), `%LOCALAPPDATA%\ReadYou\Cache\` (`cacheDir`).
     - **Linux/Fallback:** `~/.local/share/me.ash.reader/` (`dataDir`), `~/.cache/me.ash.reader/` (`cacheDir`).
     - `dataStoreFile = dataDir.resolve("datastore/settings.preferences_pb")`.
2. **Khởi tạo `DataStore<Preferences>` (`src/commonMain/kotlin/me/ash/reader/infrastructure/datastore/DataStoreFactory.kt`):**
   ```kotlin
   fun createDataStore(appPaths: AppPaths, scope: CoroutineScope): DataStore<Preferences> {
       return PreferenceDataStoreFactory.createWithPath(
           scope = scope,
           produceFile = { appPaths.dataStoreFile.absolutePath.toPath() }
       )
   }
   ```
   - Trong `androidMain`, `val Context.dataStore: DataStore<Preferences>` trong [`DataStoreExt.kt`](file:///Users/stark/Documents/GitHub/ReadYou/app/src/main/java/me/ash/reader/ui/ext/DataStoreExt.kt) sẽ trỏ trực tiếp về singleton `DataStore<Preferences>` từ Koin (`GlobalContext.get().get<DataStore<Preferences>>()`), giúp tránh lỗi `IllegalStateException: There are multiple DataStores active for the same file` và giữ nguyên mọi hàm mở rộng `Context.dataStore` hiện tại cho tới khi hoàn tất Chặng 2.
   - Chuyển `PreferencesKey` và `DataStoreKey` sang `commonMain` (thay `Log.e` bằng `println`/logger đa nền tảng).

### 5.4. Đa Nền Tảng Hóa Mạng (`OkHttpClient` & `Retrofit`)

- Chuyển `NetworkDataSource`, `UserAgentInterceptor` và `cachingHttpClient` sang `src/commonMain/kotlin/me/ash/reader/infrastructure/net/`.
- Tách hàm `OkHttpClient.Builder.setupClientCertSsl(...)` dùng `android.security.KeyChain` thành hàm `expect/actual` (trên Android dùng `KeyChain`, trên Desktop dùng TrustManager/KeyStore chuẩn của JVM).
- Cung cấp hằng số `USER_AGENT_STRING` qua `AppConfig` (trên Android lấy từ `BuildConfig.USER_AGENT_STRING`, trên Desktop là `"ReadYou/0.16.2(47)"`).

### 5.5. Chuyển Đổi Dependency Injection: Dagger Hilt $\rightarrow$ Koin Multiplatform

Xóa toàn bộ 13 file Hilt module trong `infrastructure/di/` và thay bằng hệ thống module Koin tại `me.ash.reader.infrastructure.di`:

1. **Qualifiers trong Koin:**
   - `named("ApplicationScope")`, `named("IODispatcher")`, `named("DefaultDispatcher")`, `named("MainDispatcher")`.
2. **`commonModule` (`src/commonMain`):**
   - Coroutine Dispatchers & `CoroutineScope(SupervisorJob() + Dispatchers.Default)`.
   - `single<AndroidDatabase> { ... }` (cung cấp từ `platformModule`).
   - `single<AccountDao> { get<AndroidDatabase>().accountDao() }`
   - `single<FeedDao> { get<AndroidDatabase>().feedDao() }`
   - `single<ArticleDao> { get<AndroidDatabase>().articleDao() }`
   - `single<GroupDao> { get<AndroidDatabase>().groupDao() }`
   - `single<DataStore<Preferences>> { createDataStore(get(), get(named("ApplicationScope"))) }`
   - `single<OkHttpClient> { provideOkHttpClient(get()) }`
   - `single<NetworkDataSource> { NetworkDataSource.getInstance() }`
3. **`platformModule` trên Android (`src/androidMain`):**
   - `single { AppPaths.fromContext(androidContext()) }`
   - `single<AndroidDatabase> { createAndroidDatabase(androidContext()) }`
   - `single { WorkManager.getInstance(androidContext()) }`
   - `single { provideImageLoader(androidContext(), get()) }`
   - Các Singleton Service & Helper hiện tại: `SettingsProvider`, `AccountService`, `AndroidStringsHelper`, `NotificationHelper`, `AndroidImageDownloader`, `TextToSpeechManager`, `OPMLDataSource`, `RssHelper`, `ReaderCacheHelper`, `DiffMapHolder`, `SyncLogger`, `LocalRssService`, `FeverRssService`, `GoogleReaderRssService`, `RssService`, `OpmlService`, `AppService`, `FilterStateUseCase`, `ArticlePagingListUseCase`, `GroupWithFeedsListUseCase`, `WidgetRepository`.
   - 9 ViewModels đăng ký bằng `viewModelOf(...)`:
     - `FeedsViewModel`, `ArticleListReaderViewModel`, `SubscribeViewModel`, `AccountViewModel`, `AdditionViewModel`, `GroupOptionViewModel`, `FeedOptionViewModel`, `TroubleshootingViewModel`, `UpdateViewModel`.
   - 3 WorkManager Workers đăng ký bằng `workerOf(...)` (`io.insert-koin:koin-androidx-workmanager`):
     - `SyncWorker`, `ReaderWorker`, `WidgetUpdateWorker`.
4. **Cập nhật các điểm tiêm phụ thuộc trên Android:**
   - Trong `AndroidApp`: Khởi tạo `startKoin { androidContext(this@AndroidApp); workManagerFactory(); modules(commonModule, platformModule) }` và lấy các dependency qua `by inject()`.
   - Trong `MainActivity`, `CrashReportActivity`, `WidgetConfigActivity`, `ArticleCardWidgetReceiver`, `ArticleListWidgetReceiver`: Xóa `@AndroidEntryPoint` + `@Inject lateinit var`, chuyển sang `by inject()` của `android.content.ComponentCallbacks` / `KoinComponent`.
   - Trong `WidgetRepository.Companion.get(context)`: Lấy `GlobalContext.get().get<WidgetRepository>()` thay cho `EntryPointAccessors`.
   - Trong 25 file Composable gọi `hiltViewModel()`: Thay `import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel` bằng `import org.koin.compose.viewmodel.koinViewModel` và gọi `koinViewModel()`.
5. **`platformModule` & `Main.kt` trên Desktop (`src/desktopMain`):**
   - `single { AppPaths.forDesktop() }`
   - `single<AndroidDatabase> { createDesktopDatabase(get()) }`
   - `Main.kt`: Khởi tạo `startKoin { modules(commonModule, platformModule) }` và mở `Window(onCloseRequest = ::exitApplication, title = "ReadYou")` hiển thị giao diện kiểm chứng kết nối Room KMP + DataStore KMP (sẵn sàng để gắn `AppEntry` ở Chặng 2).

---

## 6. Những Chi Tiết Dành Cho Người Triển Khai (Left to the Builder)

- Cách tổ chức các hàm helper nội bộ trong `AppModules.kt` (tách thành nhiều biến module nhỏ như `databaseModule`, `networkModule`, `viewModelModule` rồi gộp vào `commonModule`/`platformModule` hay để chung một file).
- Giao diện cụ thể của màn hình khung sườn (Bootstrap Screen) trên Desktop ở cuối Chặng 1 trước khi toàn bộ `AppEntry` được chuyển sang `commonMain` ở Chặng 2.
- Thứ tự các bước commit trung gian khi xóa annotation `@Inject` và `@HiltViewModel` trên 9 ViewModel và các Service.
