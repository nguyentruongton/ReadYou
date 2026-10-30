# Technical Design Document: KMP Phase 3 — Desktop UX, Keyboard Shortcuts, System Tray/Menu Bar, Native TTS & Dual-Engine Reader

**Project:** ReadYou (`me.ash.reader`) Kotlin Multiplatform (KMP) & Compose Multiplatform (CMP) Migration
**Phase:** 3 — Desktop UX, Keyboard Shortcuts, Menu Bar, System Tray, Native OS TTS, Upgraded Native Compose Reader & Native OS WebView (`wrywebview`)
**Branch:** `kmp-phase3-desktop-ux`
**Date:** 2026-10-30

---

## 1. Intent & Why

Phase 1 and Phase 2 migrated ReadYou's database, dependency injection (Koin), preferences, resources, and UI screens into `commonMain`, allowing the application to run on Desktop (macOS, Windows, Linux). However, a great Desktop RSS reader requires more than a mobile UI in a window:
- Desktop users expect **keyboard-driven workflows** (`J`/`K` article navigation, `M` read/unread toggle, `S` star/unstar, `R` or `Cmd/Ctrl+R` sync, `Space`/`Shift+Space` page scrolling, `Esc` back navigation).
- Desktop users expect **native OS window integration**: a native **Menu Bar** (`File`, `Navigation`, `Article`, `Window`) and a **System Tray** icon that allows minimizing to tray, quick syncing, and background operation.
- Desktop users expect **working Text-To-Speech (TTS)** without cloud keys, leveraging native OS speech synthesizers (`say` on macOS, `System.Speech` via PowerShell on Windows, `spd-say`/`espeak` on Linux).
- Desktop users need a **first-class Dual-Engine Reader** combining:
  - **Engine A (Default on Desktop — Upgraded Native Compose Reader):** 100% Skia/Metal/DirectX native text & layout rendering via `HtmlToComposable.kt` with full cross-element text selection (`SelectionContainer`), Bionic Reading (`LocalReadingBoldCharacters`) in `AnnotatedString`, proper list indentation (`TextIndent`) & nested lists, styled blockquotes with a vertical accent bar, formatted tables, and `<pre>`/`<code>` blocks.
  - **Engine B (Optional OS-Native WebView via Rust Wry + UniFFI):** Lightweight OS-native WebView (`io.github.kdroidfilter:composewebview` / `wrywebview` using `WKWebView` on macOS via `SkikoInterop.HardwareLayer`, `WebView2` on Windows, `WebKitGTK` on Linux) that renders `WebViewHtml` + `WebViewStyle` + `WebViewScript` without bundling 120MB of Chromium (JCEF) and without replacing JetBrains Compose Desktop's standard `Window`/`MenuBar`/`Tray`.

---

## 2. Goals, Non-Goals & Anti-Goals

### Goals
1. **Upgraded Native Compose Reader (`HtmlToComposable.kt`, `Content.kt`, `ReadingRendererPreference.kt`):**
   - Set `ReadingRendererPreference.NativeComponent` as the default renderer on Desktop (while preserving `WebView` as an explicit user-selectable option).
   - Enable seamless text selection across the article headline (`Metadata`) and all body paragraphs, headers, lists, blockquotes, and tables inside `SelectionContainer`.
   - Implement multiplatform **Bionic Reading** (`BionicReadingTransformedText` / `AnnotatedString` word-stem bolding) directly inside `HtmlToComposable.kt` when `LocalReadingBoldCharacters.current.value` is enabled.
   - Upgrade HTML rendering for nested `<ul>`/`<ol>` lists with hanging `TextIndent`, `<blockquote>` with a themed left border bar, standalone `<pre>` blocks (even without nested `<code>`), and structured `<table>` rows.
2. **OS-Native Desktop WebView (`RYWebView.desktop.kt`):**
   - Integrate `io.github.kdroidfilter:composewebview:1.0.0-beta-02` (`wrywebview`) in `desktopMain` to embed OS-native `WKWebView` (macOS), `WebView2` (Windows), and `WebKitGTK` (Linux) inside standard Compose Desktop `SwingPanel`/`HardwareLayer`.
   - Render full `WebViewHtml.HTML` with `WebViewStyle.get(...)` and `WebViewScript.get(boldCharacters)` matching `RYWebView.android.kt`.
   - Fix the layout hierarchy in `Content.kt` so that on Desktop, `ReadingRendererPreference.WebView` gives `RYWebView` bounded viewport constraints (`Modifier.fillMaxSize()`) instead of nesting a native viewport/scroll container inside an unbounded `Modifier.verticalScroll(scrollState)`.
   - Intercept external link clicks in `RYWebView.desktop.kt` and open them via `LocalUriHandler` / `openPlatformUrl`, and gracefully fall back to the Native Compose Reader if the native WebView library fails to initialize (e.g. headless test environments).
3. **Desktop Keyboard Shortcuts (`DesktopKeyboardShortcuts.kt`, `ReadingPage.kt`, `ArticleListReadingPage.kt`):**
   - Global & contextual keyboard handler ignoring shortcuts when focus is inside a text input field (`TextField` / search bar):
     - `J` / `Down` (with `Alt` or standalone `J`): Load next article.
     - `K` / `Up` (with `Alt` or standalone `K`): Load previous article.
     - `M`: Toggle Read / Unread status of the current article.
     - `S`: Toggle Starred status of the current article.
     - `R` or `Cmd+R` (macOS) / `Ctrl+R` (Windows/Linux): Trigger feed sync (`SyncScheduler.triggerImmediateSync` / `viewModel.sync()`).
     - `B`: Toggle Bionic Reading (`LocalReadingBoldCharacters`).
     - `V` or `O`: Open current article link in default system browser.
     - `Space` / `Shift+Space`: Page down / Page up in the active article reader (`LazyListState` or `ScrollState`).
     - `Escape`: Close current article / navigate back.
4. **Desktop Menu Bar & System Tray (`Main.kt`, `DesktopMenuBar.kt`, `DesktopTray.kt`):**
   - Native `MenuBar` inside `Window`:
     - **File / ReadYou:** Sync Now (`Cmd/Ctrl+R`), Settings (`Cmd/Ctrl+,`), Minimize to Tray (`Cmd/Ctrl+W`), Exit (`Cmd/Ctrl+Q`).
     - **View:** Toggle Dark Theme, Switch Renderer (Native Compose / OS WebView), Toggle Bionic Reading (`B`).
     - **Article:** Next Article (`J`), Previous Article (`K`), Toggle Read (`M`), Toggle Starred (`S`), Read Aloud / Stop TTS (`T`), Open in Browser (`O`).
   - System `Tray`:
     - Tray icon using ReadYou's app icon painter.
     - Tray menu: *Show / Hide ReadYou*, *Sync Now*, *Quit ReadYou*.
     - Closing the window when tray is active can hide to tray or exit cleanly via menu.
   - Connect `DesktopSyncScheduler` in `PlatformModule.desktop.kt` to `RssService` so `triggerImmediateSync` executes real RSS synchronization on Desktop.
5. **Native OS Text-To-Speech (`DesktopTextToSpeechManager.kt`):**
   - Strip HTML tags using `Jsoup.parse(htmlContent).text()` and split into sentences/chunks so progress (`State.Reading(current, total)`) updates live in the UI (`TtsButton`).
   - **macOS:** Execute `/usr/bin/say` per sentence chunk via `ProcessBuilder` on background coroutine, supporting `stop()` via `Process.destroyForcibly()`.
   - **Windows:** Execute PowerShell `System.Speech.Synthesis.SpeechSynthesizer` per sentence chunk, supporting `stop()` via `Process.destroyForcibly()`.
   - **Linux:** Execute `spd-say -w` or `espeak` fallback, supporting `stop()` via `Process.destroyForcibly()`.

### Non-Goals
- Native installer packaging (`.dmg`, `.msi`, `.deb`) and GitHub Actions release workflows — those belong to **Phase 4**.
- Replacing JetBrains Compose Desktop's standard `Window` with experimental window managers that break `MenuBar` or `Tray`.

### Anti-Goals
- Bundling JCEF / Chromium (~120MB–150MB bloat).
- Breaking Android compilation, Room KSP generation, or existing Android WebView behavior.

---

## 3. Constraints

1. **Kotlin & Compose Compatibility:**
   - The project compiles with Kotlin 2.2.x and Compose Multiplatform 1.8.2 (`material3-desktop:1.12.0-alpha03`).
   - `io.github.kdroidfilter:composewebview:1.0.0-beta-02` (`io.github.kdroidfilter:wrywebview:1.0.0-beta-02`) targets Kotlin 2.2 ABI (`2.2.21`) and embeds Wry + UniFFI directly into a standard Compose `SwingPanel` + `org.jetbrains.skiko.HardwareLayer`. To consume metadata 2.2.21 cleanly on Kotlin 2.2.0 (or by bumping `kotlin = "2.2.21"` / `-Xskip-metadata-version-check`), both `compileKotlinDesktop` and `compileGithubDebugKotlinAndroid` must pass with zero errors.
2. **Headless Test Safety:**
   - Unit and smoke tests (`./gradlew desktopTest`) run in headless or non-GUI environments. Both `DesktopTextToSpeechManager` and `RYWebView.desktop.kt` must be testable without crashing when native display handles are absent.

---

## 4. Approaches Considered

| Approach | Description | Trade-offs | Decision |
|---|---|---|---|
| **Approach 1: Native Compose Reader Only (Hướng A)** | Upgrade `HtmlToComposable.kt` and keep `RYWebView.desktop.kt` as a Compose Reader fallback. | Zero native binary dependencies, but cannot render complex CSS/JS layout via `reeder.html` / `WebViewStyle` on Desktop. | Rejected in favor of Approach 3 |
| **Approach 2: Full Upgrade to Kotlin 2.4.20 + CMP 1.12.2** | Upgrade entire Gradle stack to Kotlin 2.4.20 + CMP 1.12.2 before reader work. | High risk of breaking Room 2.7.2 KSP, Glance widgets, and Android dependencies mid-flight. | Rejected |
| **Approach 3 (Selected): Dual-Engine Reader — Upgraded Native Compose Reader (A) + OS-Native Wry WebView (B)** | Make upgraded `HtmlToComposable.kt` (with full selection, Bionic Reading, lists, blockquotes, tables) the primary Desktop reader AND integrate Wry + UniFFI OS-native WebView (`WKWebView`/`WebView2`/`WebKitGTK`) for WebView mode, alongside Desktop Shortcuts, MenuBar, Tray, and OS TTS. | Adds ~1.6MB of Rust Wry native libraries (`libcomposewebview_wry.dylib`/`.dll`/`.so`) for Desktop while preserving 100% standard Compose `Window`/`MenuBar`/`Tray` and Android compatibility. | **Selected by User** |

---

## 5. Detailed Technical Design

### 5.1. Engine A: Upgraded Native Compose Reader (`commonMain`)

1. **Seamless Cross-Element Selection (`Content.kt` & `HtmlToComposable.kt`):**
   - In [`Content.kt`](file:///Users/stark/Documents/GitHub/ReadYou/app/src/commonMain/kotlin/me/ash/reader/ui/page/home/reading/Content.kt), remove `DisableSelection` around `Metadata` in `NativeComponent` mode so users can drag-select from the article title, author, and date straight down into the body text, lists, blockquotes, and code blocks.
   - Keep `DisableSelection` only on non-text media overlays (`ArticleImage` and video play buttons).
2. **Multiplatform Bionic Reading in Native Compose (`BionicReadingHelper.kt` & `HtmlToComposable.kt`):**
   - Create `me.ash.reader.ui.component.reader.BionicReadingHelper` in `commonMain`:
     - Given a word stem percentage of `0.7` (matching `WebViewScript.kt`: for words with length > 3, `round(length * 0.7f)`, otherwise full word length), applies `SpanStyle(fontWeight = FontWeight.Bold, color = onSurfaceColor)` to the stem of each word (`\p{L}+`) when `LocalReadingBoldCharacters.current.value == true` (excluding `preFormatted` code blocks).
3. **Enhanced HTML Elements (`HtmlToComposable.kt`):**
   - **Nested & Indented Lists (`<ul>`, `<ol>`, `<li>`):**
     - Support arbitrary nesting depth (`listDepth`).
     - Apply `ParagraphStyle(textIndent = TextIndent(firstLine = (depth * 12).sp, restLine = (depth * 12 + 16).sp))` with bullets (`•`, `◦`, `▪` by depth) for `<ul>` and numbered indices (`1.`, `2.`) for `<ol>`.
   - **Blockquotes (`<blockquote>`):**
     - Terminate current paragraph and emit a styled blockquote block with a `3.dp` left vertical accent bar (`MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)`), subtle surface container background, and `12.dp` start padding so quotes stand out visually.
   - **Code Blocks (`<pre>` and `<pre><code>`):**
     - Handle both `<pre><code>...</code></pre>` and standalone `<pre>...</pre>` via `formatCodeBlock(...)`.
   - **Horizontal Rule (`<hr>`):**
     - Emit a subtle `HorizontalDivider` item with vertical padding.
4. **Default Renderer Preference:**
   - Keep user preference persistence intact in `ReadingRendererPreference`, and ensure both `NativeComponent` and `WebView` work smoothly on Desktop.

### 5.2. Engine B: OS-Native Wry WebView (`desktopMain` & `commonMain`)

1. **Gradle Configuration (`gradle/libs.versions.toml` & `app/build.gradle.kts`):**
   - Add `io.github.kdroidfilter:composewebview:1.0.0-beta-02` (`io.github.kdroidfilter:wrywebview:1.0.0-beta-02`) to `desktopMain.dependencies`.
   - Add `-Xskip-metadata-version-check` to `compilerOptions.freeCompilerArgs` in `app/build.gradle.kts` so Kotlin 2.2.0 consumes `2.2.21` metadata seamlessly without touching Android KSP/Room plugins.
2. **Viewport Layout Fix in [`Content.kt`](file:///Users/stark/Documents/GitHub/ReadYou/app/src/commonMain/kotlin/me/ash/reader/ui/page/home/reading/Content.kt):**
   - Define `expect val isEmbeddedNativeWebView: Boolean` (`false` on Android where `AndroidView(WebView)` scrolls inside Compose's `verticalScroll`, `true` on Desktop where `WKWebView`/`WebView2` manages its own native viewport scrolling).
   - When `isEmbeddedNativeWebView == true` and `renderer == ReadingRendererPreference.WebView`:
     - Render the `headline()` and `RYWebView(modifier = Modifier.weight(1f).fillMaxWidth(), ...)` inside a bounded `Column(Modifier.fillMaxSize())` (or include the headline inside the HTML document) so `SwingPanel` receives finite constraints and scrolls at native 90Hz/120Hz.
3. **Implementation of [`RYWebView.desktop.kt`](file:///Users/stark/Documents/GitHub/ReadYou/app/src/desktopMain/kotlin/me/ash/reader/ui/component/webview/RYWebView.desktop.kt):**
   - Generate the styled HTML string using `WebViewHtml.HTML.format(WebViewStyle.get(...), refererDomain ?: "", content, WebViewScript.get(boldCharacters.value))` using all active theme and typography composition locals (`LocalReadingTextFontSize`, `LocalReadingTextLineHeight`, `LocalReadingTextLetterSpacing`, `LocalReadingTextHorizontalPadding`, `LocalReadingTextBold`, `LocalReadingTextAlign`, `LocalReadingSubheadBold`, `LocalReadingSubheadUpperCase`, `LocalReadingImageHorizontalPadding`, `LocalReadingImageRoundedCorners`, `LocalReadingBoldCharacters`, and Material3 colors).
   - Render `io.github.kdroidfilter.webview.web.WebView` with `rememberWebViewStateWithHTMLData(data = html)` and `rememberWebViewNavigator()`.
   - Configure a `RequestInterceptor` on the `WebViewNavigator` so clicking any external `http`/`https` link opens it in the system browser via `LocalUriHandler.current.openUri(url)` and returns `WebRequestInterceptResult.Reject` so the reader view doesn't navigate away from the article.
   - Wrap native WebView initialization with a safe fallback to `NativeComponent` (`Reader`) if native display initialization is unavailable (e.g., in headless tests).

### 5.3. Desktop Keyboard Shortcuts & Reader Scrolling (`commonMain` & `desktopMain`)

1. **Action Bus / State Coordinator (`DesktopShortcutCoordinator.kt`):**
   - Create `DesktopShortcutEvent` & `LocalDesktopShortcutEvents` (`SharedFlow<DesktopShortcutEvent>`) in `commonMain`:
     - `NextArticle`, `PreviousArticle`, `ToggleRead`, `ToggleStarred`, `Sync`, `ToggleBionicReading`, `OpenInBrowser`, `ToggleTts`, `PageDown`, `PageUp`, `EscapeBack`, `OpenSettings`.
   - In [`ReadingPage.kt`](file:///Users/stark/Documents/GitHub/ReadYou/app/src/commonMain/kotlin/me/ash/reader/ui/page/home/reading/ReadingPage.kt) and [`ArticleListReadingPage.kt`](file:///Users/stark/Documents/GitHub/ReadYou/app/src/commonMain/kotlin/me/ash/reader/ui/page/adaptive/ArticleListReadingPage.kt):
     - Observe `LocalDesktopShortcutEvents` to execute:
       - `NextArticle` / `PreviousArticle`: load `readerState.nextArticle` / `readerState.previousArticle` (or select the first article in the list if none is open).
       - `ToggleRead`: `viewModel.updateReadStatus(!readingUiState.isUnread)`
       - `ToggleStarred`: `viewModel.updateStarredStatus(!readingUiState.isStarred)`
       - `Sync`: `viewModel.sync()`
       - `PageDown` / `PageUp`: animate scroll by `+0.85 * viewportHeight` / `-0.85 * viewportHeight` on `listState` (`scrollBy` / `animateScrollBy`) or `scrollState`.
       - `ToggleTts`: start or stop `viewModel.textToSpeechManager`.
       - `OpenInBrowser`: open `readerState.link` via `uriHandler.openUri(...)`.
       - `EscapeBack`: trigger `onNavAction(NavigationAction.Close)`.
2. **Window Key Event Handler (`Main.kt`):**
   - Attach `onPreviewKeyEvent` on `Window` in `Main.kt` (translating key presses when no text input is actively consuming typing, or when `Meta`/`Ctrl` modifiers are used) and emit into `DesktopShortcutCoordinator`.

### 5.4. System Tray, Menu Bar & Desktop Sync (`desktopMain`)

1. **Real `DesktopSyncScheduler` Wiring (`PlatformModule.desktop.kt`):**
   - Update `PlatformModule.desktop.kt` so `DesktopSyncScheduler`'s `onSyncRequest` resolves `RssService` from Koin and invokes `rssService.get(accountId).doSyncOneTime(feedId, groupId)` on `IO_DISPATCHER`.
2. **Native Menu Bar & System Tray (`Main.kt`):**
   - Use `androidx.compose.ui.window.MenuBar` inside `Window` to provide native macOS top menu bar / Windows & Linux window menu bar with standard shortcuts (`KeyShortcut(Key.R, meta = isMac, ctrl = !isMac)`, etc.).
   - Use `androidx.compose.ui.window.Tray` (guarded by `isTraySupported`) with a custom vector/canvas icon painter, tooltip `"ReadYou"`, and menu items:
     - *Show / Hide Window* (toggles `isWindowVisible`)
     - *Sync Now* (triggers `syncScheduler.triggerImmediateSync()`)
     - *Settings* (shows window and navigates to `Route.Settings`)
     - *Quit ReadYou* (`exitApplication()`)

### 5.5. Native OS Text-To-Speech (`DesktopTextToSpeechManager.kt`)

1. **Sentence Chunking & HTML Stripping:**
   - Extract clean text from `htmlContent` via `Jsoup.parse(htmlContent).text()`.
   - Split text into readable sentence chunks (`Regex("(?<=[.!?。！？\\n])\\s+")`) filtering out blank strings.
2. **OS Process Execution & Lifecycle:**
   - Manage a single active `Job` and `@Volatile private var currentProcess: Process? = null` on a dedicated `CoroutineScope(SupervisorJob() + Dispatchers.IO)`.
   - Iterate through chunks `index` from `0` to `chunks.size - 1`:
     - Update `_stateFlow.value = TextToSpeechManager.State.Reading(index + 1, chunks.size)`.
     - Build OS-specific command via `buildTtsCommand(osName, chunk)`:
       - **macOS:** `listOf("/usr/bin/say", chunk)`
       - **Windows:** `listOf("powershell", "-NoProfile", "-NonInteractive", "-Command", "Add-Type -AssemblyName System.Speech; (New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('${escapePowerShell(chunk)}')")`
       - **Linux:** `listOf("spd-say", "-w", chunk)` (with fallback to `listOf("espeak", chunk)`).
     - Wait for process completion (`process.waitFor()`). If cancelled or `stop()` is called, `currentProcess?.destroyForcibly()` terminates speech immediately and sets state back to `State.Idle`.
     - When all chunks complete, reset state to `State.Idle`.

---

## 6. Verification Plan

1. **Unit Tests (`./gradlew desktopTest`):**
   - `BionicReadingHelperTest`: Verify word-stem bolding on ASCII, Vietnamese/Unicode words, short words (<=3 chars), and punctuation preservation.
   - `DesktopTextToSpeechManagerTest`: Verify HTML stripping, sentence chunking, OS command construction (`macOS`, `Windows`, `Linux`), state transitions (`Idle` -> `Reading(i, total)` -> `Idle`), and immediate cancellation via `stop()`.
   - `DesktopShortcutCoordinatorTest`: Verify key event mapping (`J`, `K`, `M`, `S`, `R`, `Space`, `Shift+Space`, `Esc`) and event emission.
   - `DesktopUiSmokeTest`: Verify Koin graph, `DesktopSyncScheduler`, `DesktopTextToSpeechManager`, and UI preferences resolve cleanly.
2. **Android & Desktop Compilation & Packaging:**
   - `./gradlew compileKotlinDesktop`
   - `./gradlew compileGithubDebugKotlinAndroid`
   - `./gradlew desktopTest testGithubDebugUnitTest`
   - `./gradlew packageUberJarForCurrentOS`
