package me.ash.reader.ui.ext

import androidx.compose.runtime.Composable
import me.ash.reader.domain.model.general.Version
import me.ash.reader.infrastructure.preference.SharedContentPreference

@Composable
expect fun BackHandler(enabled: Boolean = true, onBack: () -> Unit)

expect fun showToast(message: String?)
expect fun showToastLong(message: String?)
expect suspend fun showToastSuspend(message: String?)

expect fun getAppVersion(): Version

expect fun shareText(title: String?, link: String?, contentPreference: SharedContentPreference)

expect fun openPlatformUrl(url: String?)

expect val isGitHub: Boolean
expect val isFDroid: Boolean
expect val isGooglePlay: Boolean

data class BrowserAppInfo(val name: String, val packageName: String)
expect fun getPlatformBrowserList(): List<BrowserAppInfo>

@Composable
expect fun rememberBasicFontPicker(onLoaded: () -> Unit): () -> Unit

@Composable
expect fun rememberReadingFontPicker(onLoaded: () -> Unit): () -> Unit

@Composable
expect fun rememberFilePicker(onFileRead: (ByteArray) -> Unit): () -> Unit

@Composable
expect fun rememberFileSaver(defaultName: String, onFileSave: () -> ByteArray?): () -> Unit

@Composable
expect fun rememberCertificateChooser(onAliasChosen: (String?) -> Unit): () -> Unit

expect fun openPlatformTtsSettings()
