package org.librevault.presentation.screens.lock

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import org.librevault.R

class LockScreen : Screen {
    @Composable
    override fun Content() {
        Scaffold { innerPadding ->
            Surface(
                modifier = Modifier.Companion
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier.Companion
                        .fillMaxSize()
                        .padding(8.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.Companion.CenterHorizontally
                ) {
                    Icon(
                        modifier = Modifier.Companion.size(48.dp),
                        painter = painterResource(id = R.drawable.baseline_lock_24),
                        contentDescription = null
                    )
                    Text(
                        text = "The vault is locked.",
                        textAlign = TextAlign.Companion.Center,
                        style = MaterialTheme.typography.displayMedium
                    )
                }
            }
        }
    }
}