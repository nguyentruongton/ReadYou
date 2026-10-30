package me.ash.reader.ui.page.settings.tips

import androidx.compose.runtime.Composable
import me.ash.reader.ui.component.base.RYDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.openPlatformUrl

@Composable
actual fun UpdateDialog(
    updateViewModel: UpdateViewModel,
) {
    val uiState = updateViewModel.updateUiState.collectAsStateValue()
    if (!uiState.updateDialogVisible) return

    RYDialog(
        visible = uiState.updateDialogVisible,
        onDismissRequest = { updateViewModel.hideDialog() },
        title = { Text("Update Available") },
        text = { Text("A new version of ReadYou is available on GitHub Releases.") },
        confirmButton = {
            TextButton(onClick = {
                openPlatformUrl("https://github.com/Ashinch/ReadYou/releases")
                updateViewModel.hideDialog()
            }) {
                Text("View Release")
            }
        },
        dismissButton = {
            TextButton(onClick = { updateViewModel.hideDialog() }) {
                Text("Dismiss")
            }
        }
    )
}
