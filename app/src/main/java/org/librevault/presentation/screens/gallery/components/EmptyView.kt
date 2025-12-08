package org.librevault.presentation.screens.gallery.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.librevault.R

@Composable
fun EmptyView() {
    val emptyMessages = listOf(
        stringResource(R.string.nothing_here_not_even_crickets),
        stringResource(R.string.still_empty),
        stringResource(R.string.just_vibes),
        stringResource(R.string.majestic_void_detected),
        stringResource(R.string.invisible_content_loading),
        stringResource(R.string.this_space_is_totally_blank),
        stringResource(R.string.data_went_on_vacation),
        stringResource(R.string.you_discovered_nothing)
    )

    val randomMessage = remember { emptyMessages.random() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        Text(
            modifier = Modifier.align(Alignment.Center),
            text = randomMessage,
            textAlign = TextAlign.Center,
            fontSize = 18.sp
        )
    }
}