package me.ash.reader.ui.component.base

import androidx.compose.runtime.Composable

@Composable
expect fun rememberPlatformFeedback(): (isHaptic: Boolean, isSound: Boolean) -> Unit
