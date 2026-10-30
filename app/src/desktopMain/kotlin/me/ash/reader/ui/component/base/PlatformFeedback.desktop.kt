package me.ash.reader.ui.component.base

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberPlatformFeedback(): (isHaptic: Boolean, isSound: Boolean) -> Unit {
    return remember { { _, _ -> } }
}
