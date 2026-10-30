package me.ash.reader.ui.ext

import androidx.compose.runtime.Composable
import me.ash.reader.domain.model.general.Version
import me.ash.reader.infrastructure.preference.SharedContentPreference
import java.awt.Desktop
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.net.URI

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop BackHandler - backstack navigation handled via UI buttons and shortcuts
}

actual fun showToast(message: String?) {
    if (message.isNullOrBlank()) return
    println("[Toast] $message")
}

actual fun showToastLong(message: String?) {
    if (message.isNullOrBlank()) return
    println("[ToastLong] $message")
}

actual suspend fun showToastSuspend(message: String?) {
    if (message.isNullOrBlank()) return
    println("[ToastSuspend] $message")
}

actual fun getAppVersion(): Version = Version("0.16.2")

actual fun shareText(title: String?, link: String?, contentPreference: SharedContentPreference) {
    val textToShare = when (contentPreference) {
        SharedContentPreference.OnlyLink -> link.orEmpty()
        SharedContentPreference.TitleAndLink -> if (title.isNullOrEmpty()) link.orEmpty() else "$title\n${link.orEmpty()}"
    }
    runCatching {
        val selection = StringSelection(textToShare)
        Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, selection)
        println("[Share] Copied to clipboard: $textToShare")
    }
}

actual fun openPlatformUrl(url: String?) {
    if (url.isNullOrBlank()) return
    runCatching {
        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
            Desktop.getDesktop().browse(URI.create(url))
        }
    }
}

@Composable
actual fun rememberCertificateChooser(onAliasChosen: (String?) -> Unit): () -> Unit = {}

actual val isGitHub: Boolean = true
actual val isFDroid: Boolean = false
actual val isGooglePlay: Boolean = false

actual fun getPlatformBrowserList(): List<BrowserAppInfo> = emptyList()

@Composable
actual fun rememberBasicFontPicker(onLoaded: () -> Unit): () -> Unit {
    return {
        runCatching {
            val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Select Basic Font", java.awt.FileDialog.LOAD)
            dialog.setFilenameFilter { _, name ->
                val lower = name.lowercase()
                lower.endsWith(".ttf") || lower.endsWith(".otf")
            }
            dialog.isVisible = true
            val file = dialog.file?.let { java.io.File(dialog.directory, it) }
            if (file != null && file.exists()) {
                me.ash.reader.ui.theme.DesktopFontManager.installExternalFont(
                    bytes = file.readBytes(),
                    isReadingFont = false,
                )
                onLoaded()
            }
        }
    }
}

@Composable
actual fun rememberReadingFontPicker(onLoaded: () -> Unit): () -> Unit {
    return {
        runCatching {
            val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Select Reading Font", java.awt.FileDialog.LOAD)
            dialog.setFilenameFilter { _, name ->
                val lower = name.lowercase()
                lower.endsWith(".ttf") || lower.endsWith(".otf")
            }
            dialog.isVisible = true
            val file = dialog.file?.let { java.io.File(dialog.directory, it) }
            if (file != null && file.exists()) {
                me.ash.reader.ui.theme.DesktopFontManager.installExternalFont(
                    bytes = file.readBytes(),
                    isReadingFont = true,
                )
                onLoaded()
            }
        }
    }
}

@Composable
actual fun rememberFilePicker(onFileRead: (ByteArray) -> Unit): () -> Unit {
    return {
        runCatching {
            val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Select File", java.awt.FileDialog.LOAD)
            dialog.isVisible = true
            val file = dialog.file?.let { java.io.File(dialog.directory, it) }
            if (file != null && file.exists()) {
                onFileRead(file.readBytes())
            }
        }
    }
}

@Composable
actual fun rememberFileSaver(defaultName: String, onFileSave: () -> ByteArray?): () -> Unit {
    return {
        runCatching {
            val dialog = java.awt.FileDialog(null as java.awt.Frame?, "Save File", java.awt.FileDialog.SAVE)
            dialog.file = defaultName
            dialog.isVisible = true
            val file = dialog.file?.let { java.io.File(dialog.directory, it) }
            if (file != null) {
                val bytes = onFileSave()
                if (bytes != null) {
                    file.writeBytes(bytes)
                }
            }
        }
    }
}

actual fun openPlatformTtsSettings() {
    // Desktop does not have an Android TTS Settings screen
}
