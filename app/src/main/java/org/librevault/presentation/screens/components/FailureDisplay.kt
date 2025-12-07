package org.librevault.presentation.screens.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import org.librevault.R
import org.librevault.presentation.theme.LibreVaultTheme
import javax.crypto.AEADBadTagException

@Composable
fun FailureDisplay(modifier: Modifier = Modifier, throwable: Throwable) {
    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        TopMessage(
            message = stringResource(R.string.decryption_failed),
            type = MessageType.ERROR,
            visible = true
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                painter = painterResource(R.drawable.baseline_error_outline_24),
                contentDescription = null,
            )
            Spacer(Modifier.size(8.dp))
            when (throwable) {
                is AEADBadTagException -> {
                    Text(
                        text = stringResource(R.string.aeadbt_err_msg).trimIndent(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                else -> {
                    Text(stringResource(R.string.unknown_error))
                }
            }
        }
    }
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE,
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun FailureDisplayPreview() {
    LibreVaultTheme(darkTheme = true) {
        Scaffold {
            FailureDisplay(throwable = AEADBadTagException("dump"))
        }
    }
}