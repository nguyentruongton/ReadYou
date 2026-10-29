package me.ash.reader.ui.page.home.feeds.drawer.group

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope

import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import me.ash.reader.*
import me.ash.reader.ui.component.base.RYDialog
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.ext.showToast

@Composable
fun ClearGroupDialog(
    groupName: String,
    groupOptionViewModel: GroupOptionViewModel = koinViewModel(),
    onConfirm: () -> Unit,
) {
        val groupOptionUiState = groupOptionViewModel.groupOptionUiState.collectAsStateValue()
    val scope = rememberCoroutineScope()
    val toastString = stringResource(Res.string.clear_articles_in_group_toast, groupName)

    RYDialog(
        visible = groupOptionUiState.clearDialogVisible,
        onDismissRequest = {
            groupOptionViewModel.hideClearDialog()
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteSweep,
                contentDescription = stringResource(Res.string.clear_articles),
            )
        },
        title = {
            Text(text = stringResource(Res.string.clear_articles))
        },
        text = {
            Text(text = stringResource(Res.string.clear_articles_group_tips, groupName))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    groupOptionViewModel.clear {
                        groupOptionViewModel.hideClearDialog()
                        onConfirm()
                        showToast(toastString)
                    }
                }
            ) {
                Text(
                    text = stringResource(Res.string.clear),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    groupOptionViewModel.hideClearDialog()
                }
            ) {
                Text(
                    text = stringResource(Res.string.cancel),
                )
            }
        },
    )
}
