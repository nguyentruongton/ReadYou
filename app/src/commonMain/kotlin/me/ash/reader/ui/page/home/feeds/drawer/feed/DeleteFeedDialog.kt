package me.ash.reader.ui.page.home.feeds.drawer.feed

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DeleteForever
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
fun DeleteFeedDialog(
    feedName: String,
    feedOptionViewModel: FeedOptionViewModel = koinViewModel(),
    onConfirm: () -> Unit,
) {
        val feedOptionUiState = feedOptionViewModel.feedOptionUiState.collectAsStateValue()
    val scope = rememberCoroutineScope()
    val toastString = stringResource(Res.string.delete_toast, feedName)

    RYDialog(
        visible = feedOptionUiState.deleteDialogVisible,
        onDismissRequest = {
            feedOptionViewModel.hideDeleteDialog()
        },
        icon = {
            Icon(
                imageVector = Icons.Outlined.DeleteForever,
                contentDescription = stringResource(Res.string.unsubscribe),
            )
        },
        title = {
            Text(text = stringResource(Res.string.unsubscribe))
        },
        text = {
            Text(text = stringResource(Res.string.unsubscribe_tips, feedName))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    feedOptionViewModel.delete {
                        feedOptionViewModel.hideDeleteDialog()
                        onConfirm()
                        showToast(toastString)
                    }
                }
            ) {
                Text(
                    text = stringResource(Res.string.unsubscribe),
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    feedOptionViewModel.hideDeleteDialog()
                }
            ) {
                Text(
                    text = stringResource(Res.string.cancel),
                )
            }
        },
    )
}