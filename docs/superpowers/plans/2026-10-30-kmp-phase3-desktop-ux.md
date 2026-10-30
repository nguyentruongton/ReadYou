# KMP Phase 3: Desktop UX, Keyboard Shortcuts, System Tray/Menu Bar, Native TTS & Dual-Engine Reader Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Deliver a full Desktop UX for ReadYou featuring an upgraded Native Compose Reader (cross-element text selection, Bionic Reading, nested lists, styled blockquotes, tables), OS-native WebView via Rust Wry + UniFFI (`io.github.kdroidfilter:composewebview`), Desktop keyboard shortcuts, native Menu Bar & System Tray, and native OS Text-To-Speech (`say` / `System.Speech` / `spd-say`).

**Architecture:** Maintain the single `:app` module layout across `commonMain`, `androidMain`, and `desktopMain`. Upgrade `HtmlToComposable.kt` and `Content.kt` in `commonMain` to provide 100% native Skia/Metal/DirectX rendering with `BionicReadingHelper` and continuous `SelectionContainer` coverage. Integrate `io.github.kdroidfilter:composewebview:1.0.0-beta-02` (`wrywebview`) in `desktopMain` for OS-native WebView rendering (`WKWebView`/`WebView2`/`WebKitGTK`) inside a bounded viewport, wire `DesktopShortcutCoordinator` across `Main.kt`, `ArticleListReadingPage.kt`, and `ReadingPage.kt`, connect `DesktopSyncScheduler` to `RssService`, and implement process-backed `DesktopTextToSpeechManager`.

**Tech Stack:** Compose Multiplatform `1.8.2`, Kotlin `2.2.0` (`-Xskip-metadata-version-check`), `io.github.kdroidfilter:composewebview:1.0.0-beta-02`, Koin `4.1.0`, Jsoup, Java 17 toolchain.

**Spec:** `docs/superpowers/specs/2026-10-30-kmp-phase3-desktop-ux-design.md`

## Global Constraints

- Keep the single `:app` module layout (`commonMain`, `androidMain`, `desktopMain`).
- Do not replace JetBrains Compose Desktop's standard `androidx.compose.ui.window.Window`, `MenuBar`, or `Tray`.
- Both `./gradlew compileKotlinDesktop` and `./gradlew compileGithubDebugKotlinAndroid` must pass with zero errors.
- Unit and smoke tests (`./gradlew desktopTest`) must execute safely in headless environments without requiring a live native display handle for `RYWebView` or live audio hardware for `DesktopTextToSpeechManager`.
- Do not bundle JCEF / Chromium.

## Review Focus

- **Single quotes or special characters in Windows PowerShell TTS text:** An article containing `'` (e.g., `It's a test`) must escape `'` as `''` in `DesktopTextToSpeechManager.buildTtsCommand` for Windows so PowerShell does not fail with a syntax error.
- **Unicode & short words in Bionic Reading (`BionicReadingHelper`):** Words with Vietnamese diacritics (`Tiếng Việt`) or length `<= 3` (`an`, `đi`) and punctuation/numbers must preserve exact character offsets and bold the expected stem without throwing `IndexOutOfBoundsException`.
- **Empty or whitespace-only HTML in TTS (`DesktopTextToSpeechManager`):** Passing `"<p>   </p>"` or `""` to `readHtml` must leave `state` as `TextToSpeechManager.State.Idle` rather than starting a process with an empty argument.
- **Infinite height constraint crash in Desktop WebView (`Content.kt`):** Selecting `ReadingRendererPreference.WebView` on Desktop must render `RYWebView` in a bounded viewport container (`isEmbeddedNativeWebView == true`) instead of inside `Modifier.verticalScroll(scrollState)`.
- **Modifier key filtering in `DesktopShortcutCoordinator`:** Pressing `Cmd+C` or `Ctrl+C` (to copy selected text) or `Cmd+A` / `Ctrl+A` (to select all) must not trigger single-key shortcuts (`R`, `J`, `K`, `M`, `S`), while `Cmd+R` / `Ctrl+R` and plain `R` both trigger `DesktopShortcutEvent.Sync`.

---

### Task 1: Multiplatform Bionic Reading & Upgraded Native Compose Reader (`commonMain`)

**Files:**
- Create: `app/src/commonMain/kotlin/me/ash/reader/ui/component/reader/BionicReadingHelper.kt`
- Modify: `app/src/commonMain/kotlin/me/ash/reader/ui/component/reader/HtmlToComposable.kt`
- Modify: `app/src/commonMain/kotlin/me/ash/reader/ui/page/home/reading/Content.kt`
- Create: `app/src/desktopTest/kotlin/me/ash/reader/ui/component/reader/BionicReadingHelperTest.kt`

**Interfaces:**
- Consumes: `androidx.compose.ui.text.AnnotatedString`, `androidx.compose.ui.text.SpanStyle`, `me.ash.reader.infrastructure.preference.LocalReadingBoldCharacters`
- Produces:
  - `object BionicReadingHelper { fun applyBionicReading(text: AnnotatedString, boldSpanStyle: SpanStyle): AnnotatedString; fun computeStemLength(wordLength: Int): Int }`

- [ ] **Step 1: Write the failing test in `app/src/desktopTest/kotlin/me/ash/reader/ui/component/reader/BionicReadingHelperTest.kt`**

```kotlin
package me.ash.reader.ui.component.reader

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BionicReadingHelperTest {

    private val boldStyle = SpanStyle(fontWeight = FontWeight.Bold)

    @Test
    fun `computeStemLength bolds full word for 1 to 3 chars and 70 percent rounded for longer words`() {
        assertEquals(1, BionicReadingHelper.computeStemLength(1))
        assertEquals(2, BionicReadingHelper.computeStemLength(2))
        assertEquals(3, BionicReadingHelper.computeStemLength(3))
        assertEquals(3, BionicReadingHelper.computeStemLength(4)) // round(4 * 0.7) = 3
        assertEquals(4, BionicReadingHelper.computeStemLength(5)) // round(5 * 0.7) = 4
        assertEquals(7, BionicReadingHelper.computeStemLength(10)) // round(10 * 0.7) = 7
    }

    @Test
    fun `applyBionicReading preserves text and adds bold spans to ASCII and Vietnamese Unicode words`() {
        val input = AnnotatedString("ReadYou đọc Tiếng Việt, 123!")
        val result = BionicReadingHelper.applyBionicReading(input, boldStyle)

        assertEquals(input.text, result.text)
        // "ReadYou" (0..7, len 7 -> stem 5: 0..5)
        // "đọc" (8..11, len 3 -> stem 3: 8..11)
        // "Tiếng" (12..17, len 5 -> stem 4: 12..16)
        // "Việt" (18..22, len 4 -> stem 3: 18..21)
        val ranges = result.spanStyles.map { it.start to it.end }
        assertTrue(ranges.contains(0 to 5))
        assertTrue(ranges.contains(8 to 11))
        assertTrue(ranges.contains(12 to 16))
        assertTrue(ranges.contains(18 to 21))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew desktopTest --tests "me.ash.reader.ui.component.reader.BionicReadingHelperTest"`
Expected: FAIL with `Unresolved reference 'BionicReadingHelper'`

- [ ] **Step 3: Implement `BionicReadingHelper.kt` and upgrade `HtmlToComposable.kt` & `Content.kt`**

In `app/src/commonMain/kotlin/me/ash/reader/ui/component/reader/BionicReadingHelper.kt`:
- Implement `BionicReadingHelper` with `private val WORD_REGEX = Regex("\\p{L}+")`, `fun computeStemLength(wordLength: Int): Int = if (wordLength <= 3) wordLength else kotlin.math.round(wordLength * 0.7f).toInt()`, and `fun applyBionicReading(text: AnnotatedString, boldSpanStyle: SpanStyle): AnnotatedString` that copies existing spans/annotations and adds `boldSpanStyle` over `[match.range.first, match.range.first + stemLength)` for every word match.

In `app/src/commonMain/kotlin/me/ash/reader/ui/component/reader/HtmlToComposable.kt`:
- In `formatBody`, read `val boldCharacters = LocalReadingBoldCharacters.current.value` and `val boldSpanStyle = SpanStyle(fontWeight = FontWeight.Bold, color = onSurfaceColor())`. When `boldCharacters` is true, pass `paragraph` through `BionicReadingHelper.applyBionicReading(paragraph, boldSpanStyle)`.
- Add `listDepth: Int = 0` parameter to `appendTextChildren`. For `<ul>` and `<ol>`, support nested lists and apply `ParagraphStyle(textIndent = TextIndent(firstLine = (listDepth * 12).sp, restLine = (listDepth * 12 + 16).sp))` using bullets `listOf("•", "◦", "▪")[listDepth.coerceAtMost(2)]` for `<ul>` and `"${i + 1}. "` for `<ol>`.
- For `"blockquote"`, terminate current text and emit a styled blockquote item with a `3.dp` left accent border (`MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)`), subtle background, and italic text.
- For `"pre"`, if `element.children().none { it.tagName() == "code" }`, call `terminateCurrentText()` and `lazyListScope.formatCodeBlock(...)` directly so standalone `<pre>` blocks are formatted as code blocks.
- For `"hr"`, terminate current text and emit a `HorizontalDivider` item with `16.dp` vertical padding.

In `app/src/commonMain/kotlin/me/ash/reader/ui/page/home/reading/Content.kt`:
- Allow `headline()` text to participate in `SelectionContainer` in `NativeComponent` mode (remove `DisableSelection` around `Metadata` in `NativeComponent` mode so users can drag-select across title + body).

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew desktopTest --tests "me.ash.reader.ui.component.reader.BionicReadingHelperTest"`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add app/src/commonMain/kotlin/me/ash/reader/ui/component/reader/BionicReadingHelper.kt \
        app/src/commonMain/kotlin/me/ash/reader/ui/component/reader/HtmlToComposable.kt \
        app/src/commonMain/kotlin/me/ash/reader/ui/page/home/reading/Content.kt \
        app/src/desktopTest/kotlin/me/ash/reader/ui/component/reader/BionicReadingHelperTest.kt
git commit -m "feat(reader): add multiplatform Bionic Reading and upgrade Native Compose HTML reader"
```

---

### Task 2: OS-Native Desktop WebView via Rust Wry + UniFFI (`io.github.kdroidfilter:composewebview`)

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `app/build.gradle.kts`
- Modify: `app/src/commonMain/kotlin/me/ash/reader/ui/component/webview/RYWebView.kt`
- Modify: `app/src/androidMain/kotlin/me/ash/reader/ui/component/webview/RYWebView.android.kt`
- Modify: `app/src/desktopMain/kotlin/me/ash/reader/ui/component/webview/RYWebView.desktop.kt`
- Modify: `app/src/commonMain/kotlin/me/ash/reader/ui/page/home/reading/Content.kt`
- Create: `app/src/desktopTest/kotlin/me/ash/reader/ui/component/webview/DesktopWebViewHtmlTest.kt`

**Interfaces:**
- Consumes: `WebViewHtml.HTML`, `WebViewStyle.get`, `WebViewScript.get`, `io.github.kdroidfilter.webview.web.WebView`, `io.github.kdroidfilter.webview.web.rememberWebViewStateWithHTMLData`, `io.github.kdroidfilter.webview.web.rememberWebViewNavigator`
- Produces:
  - `expect val isEmbeddedNativeWebView: Boolean` in `RYWebView.kt` (`false` in `RYWebView.android.kt`, `true` in `RYWebView.desktop.kt`)
  - `fun buildDesktopWebViewHtml(content: String, refererDomain: String?, fontSize: Int, lineHeight: Float, letterSpacing: Float, textMargin: Int, textColor: Int, textBold: Boolean, textAlign: String, boldTextColor: Int, subheadBold: Boolean, subheadUpperCase: Boolean, imgMargin: Int, imgBorderRadius: Int, linkTextColor: Int, codeTextColor: Int, codeBgColor: Int, selectionTextColor: Int, selectionBgColor: Int, boldCharacters: Boolean): String` in `RYWebView.desktop.kt`

- [ ] **Step 1: Write the failing test in `app/src/desktopTest/kotlin/me/ash/reader/ui/component/webview/DesktopWebViewHtmlTest.kt`**

```kotlin
package me.ash.reader.ui.component.webview

import org.junit.Assert.assertTrue
import org.junit.Test

class DesktopWebViewHtmlTest {

    @Test
    fun `isEmbeddedNativeWebView is true on Desktop`() {
        assertTrue(isEmbeddedNativeWebView)
    }

    @Test
    fun `buildDesktopWebViewHtml injects CSS variables, base href, article body, and Bionic Reading script`() {
        val html = buildDesktopWebViewHtml(
            content = "<p>Hello Desktop WebView</p>",
            refererDomain = "https://example.com",
            fontSize = 18,
            lineHeight = 1.4f,
            letterSpacing = 0.5f,
            textMargin = 24,
            textColor = 0xFF222222.toInt(),
            textBold = false,
            textAlign = "start",
            boldTextColor = 0xFF000000.toInt(),
            subheadBold = true,
            subheadUpperCase = false,
            imgMargin = 12,
            imgBorderRadius = 8,
            linkTextColor = 0xFF0066CC.toInt(),
            codeTextColor = 0xFF990055.toInt(),
            codeBgColor = 0xFFF5F5F5.toInt(),
            selectionTextColor = 0xFF000000.toInt(),
            selectionBgColor = 0xFFCCE5FF.toInt(),
            boldCharacters = true,
        )

        assertTrue(html.contains("--font-size: 18px;"))
        assertTrue(html.contains("<base href=\"https://example.com\" />"))
        assertTrue(html.contains("<p>Hello Desktop WebView</p>"))
        assertTrue(html.contains("setBold(true);"))
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew desktopTest --tests "me.ash.reader.ui.component.webview.DesktopWebViewHtmlTest"`
Expected: FAIL with `Unresolved reference 'isEmbeddedNativeWebView'`

- [ ] **Step 3: Add `composewebview` dependency and implement `RYWebView.desktop.kt` and bounded viewport layout in `Content.kt`**

In `gradle/libs.versions.toml`:
- Add `composeWebview = "1.0.0-beta-02"` under `[versions]` and `compose-webview = { module = "io.github.kdroidfilter:composewebview", version.ref = "composeWebview" }` under `[libraries]`.

In `app/build.gradle.kts`:
- Add `freeCompilerArgs.add("-Xskip-metadata-version-check")` to `desktop` target `compilerOptions`.
- Add `implementation(libs.compose.webview)` to `desktopMain.dependencies`.

In `app/src/commonMain/kotlin/me/ash/reader/ui/component/webview/RYWebView.kt`:
- Add `expect val isEmbeddedNativeWebView: Boolean`.

In `app/src/androidMain/kotlin/me/ash/reader/ui/component/webview/RYWebView.android.kt`:
- Add `actual val isEmbeddedNativeWebView: Boolean = false`.

In `app/src/desktopMain/kotlin/me/ash/reader/ui/component/webview/RYWebView.desktop.kt`:
- Add `actual val isEmbeddedNativeWebView: Boolean = true`.
- Implement `buildDesktopWebViewHtml(...)` formatting `WebViewHtml.HTML` with `WebViewStyle.get(...)`, `refererDomain ?: ""`, `content`, and `WebViewScript.get(boldCharacters)`.
- Implement `@Composable actual fun RYWebView(...)`:
  - Read Material3 colors and reading style CompositionLocals (matching `RYWebView.android.kt`).
  - Build the `html` string via `buildDesktopWebViewHtml(...)`.
  - Use `rememberWebViewStateWithHTMLData(data = html)` and `rememberWebViewNavigator(requestInterceptor = ...)`.
  - In `requestInterceptor`, when `request.url` starts with `http://` or `https://` and is not the base `refererDomain`, open via `uriHandler.openUri(request.url)` and return `WebRequestInterceptResult.Reject`.
  - Configure `state.webSettings.backgroundColor = surfaceColor` and `state.webSettings.desktopWebSettings.transparent = false`.
  - Render `io.github.kdroidfilter.webview.web.WebView(state = state, modifier = modifier.fillMaxSize(), navigator = navigator)` when `!java.awt.GraphicsEnvironment.isHeadless()`, falling back to `NativeComponent` (`Reader`) in headless mode.

In `app/src/commonMain/kotlin/me/ash/reader/ui/page/home/reading/Content.kt`:
- In the `ReadingRendererPreference.WebView` branch, check `if (isEmbeddedNativeWebView)`:
  - Render a bounded `Column(modifier = modifier.padding(top = contentPadding.calculateTopPadding()).fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally)` containing `Spacer(Modifier.height(64.dp))`, `headline()`, and `RYWebView(modifier = Modifier.weight(1f).then(maxWidthModifier), content = content, refererDomain = link.extractDomain(), onImageClick = onImageClick)` without wrapping `RYWebView` inside `Modifier.verticalScroll(scrollState)`.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew desktopTest --tests "me.ash.reader.ui.component.webview.DesktopWebViewHtmlTest" && ./gradlew compileGithubDebugKotlinAndroid`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml \
        app/build.gradle.kts \
        app/src/commonMain/kotlin/me/ash/reader/ui/component/webview/RYWebView.kt \
        app/src/androidMain/kotlin/me/ash/reader/ui/component/webview/RYWebView.android.kt \
        app/src/desktopMain/kotlin/me/ash/reader/ui/component/webview/RYWebView.desktop.kt \
        app/src/commonMain/kotlin/me/ash/reader/ui/page/home/reading/Content.kt \
        app/src/desktopTest/kotlin/me/ash/reader/ui/component/webview/DesktopWebViewHtmlTest.kt
git commit -m "feat(desktop): integrate OS-native Wry WebView and fix Desktop WebView viewport constraints"
```

---

### Task 3: Native OS Text-To-Speech (`DesktopTextToSpeechManager`)

**Files:**
- Modify: `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/platform/DesktopTextToSpeechManager.kt`
- Create: `app/src/desktopTest/kotlin/me/ash/reader/infrastructure/platform/DesktopTextToSpeechManagerTest.kt`

**Interfaces:**
- Consumes: `me.ash.reader.infrastructure.platform.TextToSpeechManager`, `org.jsoup.Jsoup`
- Produces:
  - `class DesktopTextToSpeechManager(private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO), private val processRunner: suspend (List<String>) -> Unit = defaultProcessRunner)`
  - `DesktopTextToSpeechManager.extractSentencesFromHtml(htmlContent: String): List<String>`
  - `DesktopTextToSpeechManager.buildTtsCommand(osName: String, text: String): List<String>`

- [ ] **Step 1: Write the failing test in `app/src/desktopTest/kotlin/me/ash/reader/infrastructure/platform/DesktopTextToSpeechManagerTest.kt`**

```kotlin
package me.ash.reader.infrastructure.platform

import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DesktopTextToSpeechManagerTest {

    @Test
    fun `extractSentencesFromHtml strips HTML tags and splits into non-empty sentences`() {
        val html = "<h1>Title!</h1><p>First sentence. Second sentence? Third sentence。</p>"
        val sentences = DesktopTextToSpeechManager.extractSentencesFromHtml(html)
        assertEquals(listOf("Title!", "First sentence.", "Second sentence?", "Third sentence。"), sentences)
    }

    @Test
    fun `extractSentencesFromHtml returns empty list for blank HTML`() {
        assertEquals(emptyList<String>(), DesktopTextToSpeechManager.extractSentencesFromHtml("<p>   </p>"))
    }

    @Test
    fun `buildTtsCommand builds OS specific commands and escapes Windows single quotes`() {
        val macCmd = DesktopTextToSpeechManager.buildTtsCommand("Mac OS X", "Hello world")
        assertEquals(listOf("/usr/bin/say", "Hello world"), macCmd)

        val winCmd = DesktopTextToSpeechManager.buildTtsCommand("Windows 11", "It's ReadYou")
        assertEquals("powershell", winCmd[0])
        assertTrue(winCmd.last().contains("It''s ReadYou"))

        val linuxCmd = DesktopTextToSpeechManager.buildTtsCommand("Linux", "Hello Linux")
        assertEquals(listOf("spd-say", "-w", "Hello Linux"), linuxCmd)
    }

    @Test
    fun `readHtml updates stateFlow through Reading chunks and returns to Idle or stops immediately`() = runTest {
        val dispatcher = StandardTestDispatcher(testScheduler)
        val scope = TestScope(dispatcher)
        val spokenCommands = mutableListOf<List<String>>()
        val gate = CompletableDeferred<Unit>()

        val manager = DesktopTextToSpeechManager(
            scope = scope,
            osNameProvider = { "Mac OS X" },
            processRunner = { cmd ->
                spokenCommands.add(cmd)
                gate.await()
            }
        )

        manager.readHtml("<p>Sentence one. Sentence two.</p>")
        testScheduler.runCurrent()

        assertEquals(TextToSpeechManager.State.Reading(1, 2), manager.state)
        assertEquals(1, spokenCommands.size)

        // Stop mid-speech
        manager.stop()
        assertEquals(TextToSpeechManager.State.Idle, manager.state)

        // Complete full run without stopping
        val completedCommands = mutableListOf<List<String>>()
        val manager2 = DesktopTextToSpeechManager(
            scope = scope,
            osNameProvider = { "Mac OS X" },
            processRunner = { cmd -> completedCommands.add(cmd) }
        )
        manager2.readHtml("<p>One. Two.</p>")
        advanceUntilIdle()
        assertEquals(2, completedCommands.size)
        assertEquals(TextToSpeechManager.State.Idle, manager2.state)
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew desktopTest --tests "me.ash.reader.infrastructure.platform.DesktopTextToSpeechManagerTest"`
Expected: FAIL with `Unresolved reference 'extractSentencesFromHtml'`

- [ ] **Step 3: Implement `DesktopTextToSpeechManager.kt`**

In `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/platform/DesktopTextToSpeechManager.kt`:
- Add companion object methods:
  - `fun extractSentencesFromHtml(htmlContent: String): List<String>`: parse with `Jsoup.parse(htmlContent).text().trim()`, return `emptyList()` if blank, otherwise split by `Regex("(?<=[.!?。！？\\n])\\s+")` and filter `isNotBlank()`.
  - `fun buildTtsCommand(osName: String, text: String): List<String>`:
    - When `osName.lowercase().contains("mac") || osName.lowercase().contains("darwin")`: `listOf("/usr/bin/say", text)`
    - When `osName.lowercase().contains("win")`: escape `'` with `''` (`val escaped = text.replace("'", "''")`) and return `listOf("powershell", "-NoProfile", "-NonInteractive", "-Command", "Add-Type -AssemblyName System.Speech; (New-Object System.Speech.Synthesis.SpeechSynthesizer).Speak('$escaped')")`
    - Otherwise (Linux): `listOf("spd-say", "-w", text)`
- In `DesktopTextToSpeechManager`:
  - Accept constructor parameters `private val scope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)`, `private val osNameProvider: () -> String = { System.getProperty("os.name") ?: "" }`, and optional `private val processRunner: (suspend (List<String>) -> Unit)? = null`.
  - Track `private var readingJob: Job? = null` and `@Volatile private var currentProcess: Process? = null`.
  - In `readHtml(htmlContent: String)`: cancel any previous `readingJob` and destroy `currentProcess`; extract sentences; if empty, set `state = State.Idle` and return; otherwise launch a coroutine that iterates `sentences.forEachIndexed { index, sentence -> state = State.Reading(index + 1, sentences.size); ... }` and resets `state = State.Idle` in `finally` (if still active).
  - In `stop()`: cancel `readingJob`, `currentProcess?.destroyForcibly()`, `currentProcess = null`, `state = TextToSpeechManager.State.Idle`.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew desktopTest --tests "me.ash.reader.infrastructure.platform.DesktopTextToSpeechManagerTest"`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add app/src/desktopMain/kotlin/me/ash/reader/infrastructure/platform/DesktopTextToSpeechManager.kt \
        app/src/desktopTest/kotlin/me/ash/reader/infrastructure/platform/DesktopTextToSpeechManagerTest.kt
git commit -m "feat(desktop): implement native OS TextToSpeechManager for macOS, Windows, and Linux"
```

---

### Task 4: Desktop Keyboard Shortcuts, Menu Bar, System Tray & Real SyncScheduler Wiring

**Files:**
- Create: `app/src/commonMain/kotlin/me/ash/reader/infrastructure/platform/DesktopShortcutCoordinator.kt`
- Modify: `app/src/commonMain/kotlin/me/ash/reader/ui/page/home/reading/ReadingPage.kt`
- Modify: `app/src/commonMain/kotlin/me/ash/reader/ui/page/adaptive/ArticleListReadingPage.kt`
- Modify: `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/di/PlatformModule.desktop.kt`
- Modify: `app/src/desktopMain/kotlin/me/ash/reader/Main.kt`
- Create: `app/src/desktopTest/kotlin/me/ash/reader/infrastructure/platform/DesktopShortcutCoordinatorTest.kt`
- Modify: `app/src/desktopTest/kotlin/me/ash/reader/DesktopUiSmokeTest.kt`

**Interfaces:**
- Consumes: `androidx.compose.ui.input.key.*`, `me.ash.reader.domain.service.RssService`, `me.ash.reader.infrastructure.platform.SyncScheduler`
- Produces:
  - `enum class DesktopShortcutEvent { NextArticle, PreviousArticle, ToggleRead, ToggleStarred, Sync, ToggleBionicReading, OpenInBrowser, ToggleTts, PageDown, PageUp, EscapeBack, OpenSettings }`
  - `class DesktopShortcutCoordinator { val events: SharedFlow<DesktopShortcutEvent>; fun tryEmit(event: DesktopShortcutEvent): Boolean; fun mapKeyEvent(key: Key, type: KeyEventType, isMetaPressed: Boolean = false, isCtrlPressed: Boolean = false, isAltPressed: Boolean = false, isShiftPressed: Boolean = false): DesktopShortcutEvent? }`
  - `val LocalDesktopShortcutCoordinator = staticCompositionLocalOf { DesktopShortcutCoordinator() }`

- [ ] **Step 1: Write the failing test in `app/src/desktopTest/kotlin/me/ash/reader/infrastructure/platform/DesktopShortcutCoordinatorTest.kt`**

```kotlin
package me.ash.reader.infrastructure.platform

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DesktopShortcutCoordinatorTest {

    private val coordinator = DesktopShortcutCoordinator()

    @Test
    fun `mapKeyEvent ignores KeyUp and maps standard single keys on KeyDown`() {
        assertNull(coordinator.mapKeyEvent(Key.J, KeyEventType.KeyUp))
        assertEquals(DesktopShortcutEvent.NextArticle, coordinator.mapKeyEvent(Key.J, KeyEventType.KeyDown))
        assertEquals(DesktopShortcutEvent.PreviousArticle, coordinator.mapKeyEvent(Key.K, KeyEventType.KeyDown))
        assertEquals(DesktopShortcutEvent.ToggleRead, coordinator.mapKeyEvent(Key.M, KeyEventType.KeyDown))
        assertEquals(DesktopShortcutEvent.ToggleStarred, coordinator.mapKeyEvent(Key.S, KeyEventType.KeyDown))
        assertEquals(DesktopShortcutEvent.Sync, coordinator.mapKeyEvent(Key.R, KeyEventType.KeyDown))
        assertEquals(DesktopShortcutEvent.ToggleBionicReading, coordinator.mapKeyEvent(Key.B, KeyEventType.KeyDown))
        assertEquals(DesktopShortcutEvent.OpenInBrowser, coordinator.mapKeyEvent(Key.O, KeyEventType.KeyDown))
        assertEquals(DesktopShortcutEvent.ToggleTts, coordinator.mapKeyEvent(Key.T, KeyEventType.KeyDown))
        assertEquals(DesktopShortcutEvent.EscapeBack, coordinator.mapKeyEvent(Key.Escape, KeyEventType.KeyDown))
    }

    @Test
    fun `mapKeyEvent maps Space to PageDown and Shift+Space to PageUp`() {
        assertEquals(DesktopShortcutEvent.PageDown, coordinator.mapKeyEvent(Key.Spacebar, KeyEventType.KeyDown))
        assertEquals(
            DesktopShortcutEvent.PageUp,
            coordinator.mapKeyEvent(Key.Spacebar, KeyEventType.KeyDown, isShiftPressed = true)
        )
    }

    @Test
    fun `mapKeyEvent maps Cmd+R and Ctrl+R to Sync and Cmd+Comma to OpenSettings while ignoring Cmd+C and Cmd+A`() {
        assertEquals(
            DesktopShortcutEvent.Sync,
            coordinator.mapKeyEvent(Key.R, KeyEventType.KeyDown, isMetaPressed = true)
        )
        assertEquals(
            DesktopShortcutEvent.Sync,
            coordinator.mapKeyEvent(Key.R, KeyEventType.KeyDown, isCtrlPressed = true)
        )
        assertEquals(
            DesktopShortcutEvent.OpenSettings,
            coordinator.mapKeyEvent(Key.Comma, KeyEventType.KeyDown, isMetaPressed = true)
        )
        assertNull(coordinator.mapKeyEvent(Key.C, KeyEventType.KeyDown, isMetaPressed = true))
        assertNull(coordinator.mapKeyEvent(Key.A, KeyEventType.KeyDown, isCtrlPressed = true))
    }

    @Test
    fun `tryEmit delivers event to subscribers`() = runTest {
        var received: DesktopShortcutEvent? = null
        val job = launch(UnconfinedTestDispatcher(testScheduler)) {
            received = coordinator.events.first()
        }
        coordinator.tryEmit(DesktopShortcutEvent.NextArticle)
        assertEquals(DesktopShortcutEvent.NextArticle, received)
        job.cancel()
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew desktopTest --tests "me.ash.reader.infrastructure.platform.DesktopShortcutCoordinatorTest"`
Expected: FAIL with `Unresolved reference 'DesktopShortcutCoordinator'`

- [ ] **Step 3: Implement `DesktopShortcutCoordinator.kt`, wire `ReadingPage.kt`, `PlatformModule.desktop.kt`, and `Main.kt`**

In `app/src/commonMain/kotlin/me/ash/reader/infrastructure/platform/DesktopShortcutCoordinator.kt`:
- Define `DesktopShortcutEvent`, `DesktopShortcutCoordinator` (using `MutableSharedFlow<DesktopShortcutEvent>(extraBufferCapacity = 16)`), and `LocalDesktopShortcutCoordinator`.
- Implement `mapKeyEvent(...)` handling:
  - Only `KeyEventType.KeyDown`.
  - When `isMetaPressed || isCtrlPressed`: `Key.R -> Sync`, `Key.Comma -> OpenSettings`, else `null` (preserving `Cmd/Ctrl+C`, `Cmd/Ctrl+A`, `Cmd/Ctrl+V`).
  - When `isAltPressed`: `null`.
  - Plain keys: `Key.J -> NextArticle`, `Key.K -> PreviousArticle`, `Key.M -> ToggleRead`, `Key.S -> ToggleStarred`, `Key.R -> Sync`, `Key.B -> ToggleBionicReading`, `Key.O` or `Key.V -> OpenInBrowser`, `Key.T -> ToggleTts`, `Key.Escape -> EscapeBack`, `Key.Spacebar -> if (isShiftPressed) PageUp else PageDown`.

In `app/src/commonMain/kotlin/me/ash/reader/ui/page/home/reading/ReadingPage.kt`:
- Observe `LocalDesktopShortcutCoordinator.current.events` via `LaunchedEffect(shortcutCoordinator, readerState.articleId)`:
  - `NextArticle`: load `readerState.nextArticle` via `onLoadArticle(id, index)`
  - `PreviousArticle`: load `readerState.previousArticle` via `onLoadArticle(id, index)`
  - `ToggleRead`: `if (readerState.articleId != null) viewModel.updateReadStatus(!readingUiState.isUnread)`
  - `ToggleStarred`: `if (readerState.articleId != null) viewModel.updateStarredStatus(!readingUiState.isStarred)`
  - `Sync`: `viewModel.sync()`
  - `ToggleBionicReading`: `(!boldCharacters).put(coroutineScope)`
  - `OpenInBrowser`: `readerState.link?.takeIf { it.isNotBlank() }?.let { uriHandler.openUri(it) }`
  - `ToggleTts`: if `viewModel.textToSpeechManager.state is TextToSpeechManager.State.Idle`, call `readHtml(readerState.content.text ?: "")`, else `viewModel.textToSpeechManager.stop()`
  - `EscapeBack`: `if (readerState.articleId != null) onNavAction(NavigationAction.Close)`
- Inside the `AnimatedContent` block in `ReadingPage.kt` where `listState` and `scrollState` live, observe `PageDown` and `PageUp` events to scroll `listState` (`val delta = (listState.layoutInfo.viewportSize.height * 0.8f).coerceAtLeast(300f)`) or `scrollState` (`animateScrollBy(if (event == PageDown) delta else -delta)`).

In `app/src/desktopMain/kotlin/me/ash/reader/infrastructure/di/PlatformModule.desktop.kt`:
- Register `single { DesktopShortcutCoordinator() }`.
- Update `single<SyncScheduler>` so `DesktopSyncScheduler` passes `onSyncRequest = { accountId, feedId, groupId -> val rssService = get<me.ash.reader.domain.service.RssService>(); when (val svc = if (accountId != null) rssService.get(accountId) else rssService.get()) { is me.ash.reader.domain.service.LocalRssService -> svc.doSyncOneTime(feedId, groupId); is me.ash.reader.domain.service.GoogleReaderRssService -> svc.doSyncOneTime(feedId, groupId); else -> svc.doSyncOneTime() } }`.

In `app/src/desktopMain/kotlin/me/ash/reader/Main.kt`:
- Provide `LocalDesktopShortcutCoordinator provides shortcutCoordinator`.
- Manage `var isWindowVisible by remember { mutableStateOf(true) }`.
- Configure `Tray` (when `isTraySupported`) with ReadYou vector icon painter, tooltip `"ReadYou"`, and menu items:
  - *Show / Hide ReadYou* (`isWindowVisible = !isWindowVisible`)
  - *Sync Now* (`syncScheduler.triggerImmediateSync()`)
  - *Settings* (`isWindowVisible = true`; navigate to `Route.Settings`)
  - *Quit ReadYou* (`exitApplication()`)
- Configure `Window(visible = isWindowVisible, onCloseRequest = { if (isTraySupported) isWindowVisible = false else exitApplication() }, onKeyEvent = { event -> ... })` and `MenuBar`:
  - **File:** *Sync Now* (`Cmd/Ctrl+R`), *Settings* (`Cmd/Ctrl+,`), *Hide Window* (`Cmd/Ctrl+W`), *Quit* (`Cmd/Ctrl+Q`).
  - **View:** *Native Compose Reader*, *OS WebView Reader*, *Toggle Bionic Reading* (`B`).
  - **Article:** *Next Article* (`J`), *Previous Article* (`K`), *Toggle Read/Unread* (`M`), *Toggle Starred* (`S`), *Read Aloud / Stop TTS* (`T`), *Open in Browser* (`O`).

In `app/src/desktopTest/kotlin/me/ash/reader/DesktopUiSmokeTest.kt`:
- Add assertion that `DesktopShortcutCoordinator` is registered in Koin and works alongside `DesktopSyncScheduler` and `DesktopTextToSpeechManager`.

- [ ] **Step 4: Run all tests and dual-target builds to verify everything passes**

Run: `./gradlew desktopTest testGithubDebugUnitTest compileKotlinDesktop compileGithubDebugKotlinAndroid`
Expected: `BUILD SUCCESSFUL`

- [ ] **Step 5: Commit**

```bash
git add app/src/commonMain/kotlin/me/ash/reader/infrastructure/platform/DesktopShortcutCoordinator.kt \
        app/src/commonMain/kotlin/me/ash/reader/ui/page/home/reading/ReadingPage.kt \
        app/src/desktopMain/kotlin/me/ash/reader/infrastructure/di/PlatformModule.desktop.kt \
        app/src/desktopMain/kotlin/me/ash/reader/Main.kt \
        app/src/desktopTest/kotlin/me/ash/reader/infrastructure/platform/DesktopShortcutCoordinatorTest.kt \
        app/src/desktopTest/kotlin/me/ash/reader/DesktopUiSmokeTest.kt
git commit -m "feat(desktop): add Desktop keyboard shortcuts, MenuBar, System Tray, and SyncScheduler wiring"
```
