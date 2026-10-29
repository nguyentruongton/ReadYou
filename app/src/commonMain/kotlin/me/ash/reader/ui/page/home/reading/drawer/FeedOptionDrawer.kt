package me.ash.reader.ui.page.home.reading.drawer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetState
import androidx.compose.material3.Tab
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import kotlinx.coroutines.launch
import me.ash.reader.*
import me.ash.reader.ui.component.base.BottomDrawer
import me.ash.reader.ui.ext.BackHandler
import me.ash.reader.ui.ext.collectAsStateValue
import me.ash.reader.ui.page.home.feeds.drawer.feed.FeedOptionViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun StyleOptionDrawer(
    drawerState: ModalBottomSheetState,
    feedOptionViewModel: FeedOptionViewModel = koinViewModel(),
    content: @Composable () -> Unit = {},
) {
    val scope = rememberCoroutineScope()
    val feedOptionUiState = feedOptionViewModel.feedOptionUiState.collectAsStateValue()
    val feed = feedOptionUiState.feed
    val toastString = stringResource(Res.string.rename_toast, feedOptionUiState.newName)

    BackHandler(drawerState.isVisible) {
        scope.launch {
            drawerState.hide()
        }
    }

    BottomDrawer(
        drawerState = drawerState,
        sheetContent = {
            Info()
        }
    ) {
        content()
    }
}

@Composable
fun Info() {
    Column(modifier = Modifier.navigationBarsPadding()) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Tab(selected = true, onClick = { /*TODO*/ })
        }
    }
}
