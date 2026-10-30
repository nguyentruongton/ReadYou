package me.ash.reader.ui.page.home.feeds.drawer.feed

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
fun ClearFeedDialog(
    feedName: String,
    feedOptionViewModel: FeedOptionViewModel = koinViewModel(),
    onConfirm: () -> Unit = {}
) {
        val feedOptionUiState = feedOptionViewModel.feedOptionUiState.collectAsStateValue()
    val scope = rememberCoroutineScope()
    val toastString = stringResource(Res.string.clear_articles_in_feed_toast, feedName)

    RYDialog(
        visible = feedOptionUiState.clearDialogVisible,
        onDismissRequest = {
            feedOptionViewModel.hideClearDialog()
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
            Text(text = stringResource(Res.string.clear_articles_feed_tips, feedName))
        },
        confirmButton = {
            TextButton(
                onClick = {
                    feedOptionViewModel.clearFeed {
                        feedOptionViewModel.hideClearDialog()
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
                    feedOptionViewModel.hideClearDialog()
                }
            ) {
                Text(
                    text = stringResource(Res.string.cancel),
                )
            }
        },
    )
}
