package org.librevault.presentation.screens.gallery.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.librevault.BuildConfig
import org.librevault.R
import org.librevault.domain.model.vault.FolderName
import org.librevault.presentation.theme.LibreVaultTheme

@Composable
fun GalleryDrawer(
    drawerState: DrawerState,
    folderName: FolderName,
    allFolderNames: List<FolderName>,
    onDrawerClick: (FolderName) -> Unit
) {
    val coroutine = rememberCoroutineScope()

    Surface {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .navigationBarsPadding()
                .fillMaxHeight(1f)
                .fillMaxWidth(0.85f)
                .background(MaterialTheme.colorScheme.background),
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Photos
                item {
                    DrawerItem(
                        iconRes = R.drawable.baseline_image_24,
                        labelRes = R.string.photos,
                        selected = folderName == FolderName.IMAGES,
                    ) {
                        coroutine.launch {
                            onDrawerClick(FolderName.IMAGES)
                            drawerState.close()
                        }
                    }
                }

                // Videos
                item {
                    DrawerItem(
                        iconRes = R.drawable.baseline_play_circle_outline_24,
                        labelRes = R.string.videos,
                        selected = folderName == FolderName.VIDEOS,
                    ) {
                        coroutine.launch {
                            onDrawerClick(FolderName.VIDEOS)
                            drawerState.close()
                        }
                    }
                }

                // Divider
                item { HorizontalDivider() }

                // Folder header
                item {
                    Text(
                        text = stringResource(R.string.folders),
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                // Dynamic folders
                items(items = allFolderNames.drop(2)) { folder ->
                    DrawerItem(
                        iconRes = R.drawable.baseline_folder_24,
                        label = folder(),
                        selected = folder() == folderName(),
                    ) {
                        coroutine.launch {
                            onDrawerClick(folder)
                            drawerState.close()
                        }
                    }
                }
            }

            // Build info
            Text(
                text = "Build id: ${BuildConfig.BUILD_ID}",
                style = MaterialTheme.typography.labelSmall
            )
            Text(
                text = "Version: ${BuildConfig.VERSION_NAME}, Code: ${BuildConfig.VERSION_CODE}",
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    wallpaper = Wallpapers.BLUE_DOMINATED_EXAMPLE
)
@Composable
private fun GalleryDrawerPreview() {
    LibreVaultTheme {
        val drawerState = rememberDrawerState(DrawerValue.Open)

        GalleryDrawer(
            drawerState = drawerState,
            folderName = FolderName.IMAGES,
            allFolderNames = listOf(FolderName("Family"), FolderName("Work"))
        ) {
        }
    }
}