package me.ash.reader.ui.page.home.feeds.drawer.group

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
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
fun AllMoveToGroupDialog(
    groupName: String,
    groupOptionViewModel: GroupOptionViewModel = koinViewModel(),
    onConfirm: () -> Unit,
) {
        val groupOptionUiState = groupOptionViewModel.groupOptionUiState.collectAsStateValue()
    val scope = rememberCoroutineScope()
    val toastString = stringResource(
        Res.string.all_move_to_group_toast,
        groupOptionUiState.targetGroup?.name ?: ""
    )

    RYDialog(
        visible = groupOptionUiState.allMoveToGroupDialogVisible,
        onDismissRequest = {
            groupOptionViewModel.hideAllMoveToGroupDialog()
        },
        icon = {
            Icon(
                imageVector = Icons.AutoMirrored.Outlined.DriveFileMove,
                contentDescription = stringResource(Res.string.move_to_group),
            )
        },
        title = {
            Text(text = stringResource(Res.string.move_to_group))
        },
        text = {
            Text(
                text = stringResource(
                    Res.string.all_move_to_group_tips,
                    groupName,
                    groupOptionUiState.targetGroup?.name ?: "",
                )
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    groupOptionViewModel.allMoveToGroup {
                        groupOptionViewModel.hideAllMoveToGroupDialog()
                        onConfirm()
                        showToast(toastString)
                    }
                }
            ) {
                Text(
                    text = stringResource(Res.string.confirm),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    groupOptionViewModel.hideAllMoveToGroupDialog()
                }
            ) {
                Text(
                    text = stringResource(Res.string.cancel),
                )
            }
        },
    )
}