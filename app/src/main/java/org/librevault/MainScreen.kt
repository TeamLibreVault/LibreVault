package org.librevault

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen

class MainScreen: Screen {

    @Composable
    override fun Content() {
        val drawerState = rememberDrawerState(DrawerValue.Open)

        ModalNavigationDrawer(
            modifier = Modifier.Companion.statusBarsPadding(),
            drawerState = drawerState,
            drawerContent = {
                LazyColumn(
                    modifier = Modifier.Companion
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background),
                    contentPadding = PaddingValues(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_image_24),
                                    contentDescription = "Photos"
                                )
                            },
                            label = {
                                Text(text = "Photos")
                            },
                            selected = true,
                            onClick = {

                            }
                        )
                    }

                    item {
                        NavigationDrawerItem(
                            icon = {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_play_circle_outline_24),
                                    contentDescription = "Videos"
                                )
                            },
                            label = {
                                Text(text = "Videos")
                            },
                            selected = false,
                            onClick = {

                            }
                        )
                    }

                    item {
                        HorizontalDivider()
                    }

                    item {
                        Text(
                            text = "Folders",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    items(items = emptyList<String>()) { folderName ->
                        // TODO
                    }
                }
            }
        ) {
            Scaffold(modifier = Modifier.Companion.fillMaxSize()) { innerPadding ->
                Box(modifier = Modifier.Companion.padding(innerPadding))
            }
        }
    }

    @Preview(
        showBackground = true,
        showSystemUi = true,
        wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
    )
    @Composable
    private fun MainScreenPrev() {
        MaterialTheme {
            MainScreen().Content()
        }
    }
}