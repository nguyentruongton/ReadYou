package me.ash.reader.ui.page.settings.tips

import androidx.compose.runtime.Composable
import org.koin.compose.viewmodel.koinViewModel

@Composable
expect fun UpdateDialog(
    updateViewModel: UpdateViewModel = koinViewModel(),
)
