package me.ash.reader.ui.ext

import android.content.Intent
import android.widget.Toast
import androidx.compose.runtime.Composable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import me.ash.reader.BuildConfig
import me.ash.reader.domain.model.general.Version
import me.ash.reader.domain.model.general.toVersion
import me.ash.reader.infrastructure.android.AndroidApp
import me.ash.reader.infrastructure.preference.SharedContentPreference

@Composable
actual fun BackHandler(enabled: Boolean, onBack: () -> Unit) {
    androidx.activity.compose.BackHandler(enabled = enabled, onBack = onBack)
}

actual fun showToast(message: String?) {
    if (message.isNullOrBlank()) return
    runCatching {
        Toast.makeText(AndroidApp.instance, message, Toast.LENGTH_SHORT).show()
    }
}

actual fun showToastLong(message: String?) {
    if (message.isNullOrBlank()) return
    runCatching {
        Toast.makeText(AndroidApp.instance, message, Toast.LENGTH_LONG).show()
    }
}

actual suspend fun showToastSuspend(message: String?) {
    if (message.isNullOrBlank()) return
    withContext(Dispatchers.Main) {
        showToast(message)
    }
}

actual fun getAppVersion(): Version {
    return runCatching {
        val app = AndroidApp.instance
        app.packageManager.getPackageInfo(app.packageName, 0).versionName.toVersion()
    }.getOrDefault(Version("0.7.2"))
}

actual fun shareText(title: String?, link: String?, contentPreference: SharedContentPreference) {
    val textToShare = when (contentPreference) {
        SharedContentPreference.OnlyLink -> link.orEmpty()
        SharedContentPreference.TitleAndLink -> if (title.isNullOrEmpty()) link.orEmpty() else "$title\n${link.orEmpty()}"
    }
    val app = AndroidApp.instance
    val intent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_TEXT, textToShare)
        type = "text/plain"
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    app.startActivity(Intent.createChooser(intent, "Share").apply {
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    })
}

actual fun openPlatformUrl(url: String?) {
    if (url.isNullOrBlank()) return
    runCatching {
        val app = AndroidApp.instance
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(url)).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        app.startActivity(intent)
    }
}

actual val isGitHub: Boolean = BuildConfig.FLAVOR == "github"
actual val isFDroid: Boolean = BuildConfig.FLAVOR == "fdroid"
actual val isGooglePlay: Boolean = BuildConfig.FLAVOR == "googlePlay"

actual fun getPlatformBrowserList(): List<BrowserAppInfo> {
    return runCatching {
        val app = AndroidApp.instance
        app.getBrowserAppList().map {
            BrowserAppInfo(it.loadLabel(app.packageManager).toString(), it.activityInfo.packageName)
        }
    }.getOrDefault(emptyList())
}

@Composable
actual fun rememberBasicFontPicker(onLoaded: () -> Unit): () -> Unit {
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            ExternalFonts(context, it, ExternalFonts.FontType.BasicFont).copyToInternalStorage()
            onLoaded()
        }
    }
    return { launcher.launch(arrayOf("*/*")) }
}

@Composable
actual fun rememberReadingFontPicker(onLoaded: () -> Unit): () -> Unit {
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            ExternalFonts(context, it, ExternalFonts.FontType.ReadingFont).copyToInternalStorage()
            onLoaded()
        }
    }
    return { launcher.launch(arrayOf("*/*")) }
}

@Composable
actual fun rememberFilePicker(onFileRead: (ByteArray) -> Unit): () -> Unit {
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            runCatching {
                context.contentResolver.openInputStream(it)?.use { input ->
                    onFileRead(input.readBytes())
                }
            }
        }
    }
    return { launcher.launch(arrayOf("*/*")) }
}

@Composable
actual fun rememberFileSaver(defaultName: String, onFileSave: () -> ByteArray?): () -> Unit {
    val context = androidx.compose.ui.platform.LocalContext.current
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        uri?.let {
            runCatching {
                val bytes = onFileSave() ?: return@runCatching
                context.contentResolver.openOutputStream(it)?.use { output ->
                    output.write(bytes)
                }
            }
        }
    }
    return { launcher.launch(defaultName) }
}

@Composable
actual fun rememberCertificateChooser(onAliasChosen: (String?) -> Unit): () -> Unit {
    val context = androidx.compose.ui.platform.LocalContext.current
    return {
        runCatching {
            val act = context.findActivity() ?: (context as? android.app.Activity)
            if (act != null) {
                android.security.KeyChain.choosePrivateKeyAlias(
                    act,
                    { alias -> onAliasChosen(alias) },
                    null, null, null, null
                )
            }
        }
    }
}

actual fun openPlatformTtsSettings() {
    val ctx = AndroidApp.instance
    runCatching {
        val intent = android.content.Intent().apply {
            action = "com.android.settings.TTS_SETTINGS"
            flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        }
        ctx.startActivity(intent)
    }.onFailure {
        showToast("TTS settings screen not found.")
    }
}
