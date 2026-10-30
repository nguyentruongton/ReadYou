package me.ash.reader.ui.component

import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import me.ash.reader.*
import me.ash.reader.ui.component.base.TextFieldDialog
import org.jetbrains.compose.resources.stringResource

@Composable
fun RenameDialog(
    visible: Boolean = false,
    value: String = "",
    onValueChange: (String) -> Unit = {},
    onDismissRequest: () -> Unit = {},
    onConfirm: (String) -> Unit = {},
) {
    if (visible) {
        val textFieldState = rememberTextFieldState(value)
        LaunchedEffect(textFieldState) {
            snapshotFlow { textFieldState.text }.collect { onValueChange(it.toString()) }
        }
        TextFieldDialog(
            textFieldState = textFieldState,
            title = stringResource(Res.string.rename),
            icon = Icons.Outlined.Edit,
            placeholder = stringResource(Res.string.name),
            onDismissRequest = onDismissRequest,
            onConfirm = onConfirm,
        )
    }
}
