package me.ash.reader.ui.page.settings.tips

import androidx.compose.runtime.Composable

@Composable
expect fun TipsAndSupportPage(
    onBack: () -> Unit,
    navigateToLicenseList: () -> Unit,
)
