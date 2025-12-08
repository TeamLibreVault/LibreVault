package org.librevault.presentation.screens.gallery.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.librevault.R

@Composable
fun DeleteMediaConfirmationDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = { onDismissRequest() },
        title = { Text(text = stringResource(R.string.delete_media)) },
        text = { Text(stringResource(R.string.are_you_sure_you_want_to_delete_this_media_this_action_cannot_be_undone)) },
        confirmButton = {
            TextButton(onClick = { onConfirm() }) {
                Text(stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = { onDismissRequest() }) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}