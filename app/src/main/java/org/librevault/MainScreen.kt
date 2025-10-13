package org.librevault

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Card
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import coil3.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.kys0.unifile.UniFile
import java.io.File

private const val TAG = "MainScreen"

class MainScreen : Screen {


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val coroutine = rememberCoroutineScope()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val selectedFiles = remember { mutableStateListOf<File>() }
        val pickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia()
        ) { uris ->
            selectedFiles.clear()
            selectedFiles.addAll(
                uris.mapNotNull { uri ->
                    UniFile.fromUri(
                        context,
                        uri
                    )?.filePath?.let { File(it) }
                }
            )
        }

        val images = remember { mutableStateListOf<ByteArray>() }

        LaunchedEffect(key1 = Unit) {
            VaultFolder.init()
        }

        LaunchedEffect(key1 = selectedFiles.size) {
            if (selectedFiles.isNotEmpty()) {
                selectedFiles.forEach { file ->
                    FileCrypto.encryptFile(
                        inputFile = file,
                        outputFile = File(VaultFolder.path.absolutePath + File.separator + file.nameWithoutExtension + ".bin"),
                        onProgress = { progress ->
                            Log.d(TAG, "Content: Progress: $progress")
                        }
                    ) { success, e ->
                        if (success) Log.d(TAG, "Content: Success encrypting!")
                        else Log.e(TAG, "Content: ", e)
                    }
                }
            }
        }

        LaunchedEffect(key1 = Unit) {
            withContext(Dispatchers.IO) {
                images.clear()
                val files = VaultFolder.path.listFiles()?.filterNotNull() ?: emptyList()

                files.sortedBy { it.length() }.forEach { file ->
                    images += FileCrypto.decryptFileToBytes(file) {

                    }
                }
            }
        }

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Surface {
                    LazyColumn(
                        modifier = Modifier
                            .statusBarsPadding()
                            .fillMaxHeight()
                            .fillMaxWidth(0.85f)
                            .background(MaterialTheme.colorScheme.background),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            DrawerItem(
                                iconRes = R.drawable.baseline_image_24,
                                labelRes = R.string.photos,
                                selected = true,
                            ) {

                            }
                        }

                        item {
                            DrawerItem(
                                iconRes = R.drawable.baseline_play_circle_outline_24,
                                labelRes = R.string.videos,
                                selected = false,
                            ) {

                            }
                        }

                        item {
                            HorizontalDivider()
                        }

                        item {
                            Text(
                                text = stringResource(R.string.folders),
                                style = MaterialTheme.typography.labelLarge
                            )
                        }

                        items(items = emptyList<String>()) { folderName ->
                            // TODO
                        }
                    }
                }
            }
        ) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = stringResource(R.string.app_name))
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    coroutine.launch { drawerState.open() }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_menu_24),
                                    contentDescription = stringResource(R.string.navigate_up)
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            pickerLauncher.launch(
                                input = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        }
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.baseline_add_24),
                            contentDescription = stringResource(R.string.add)
                        )
                    }
                }
            ) { innerPadding ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        verticalItemSpacing = 8.dp
                    ) {
                        items(items = images) { image ->
                            Card(shape = MaterialTheme.shapes.medium) {
                                AsyncImage(
                                    model = image,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DrawerItem(
        modifier: Modifier = Modifier,
        @DrawableRes iconRes: Int,
        @StringRes labelRes: Int,
        selected: Boolean,
        onClick: () -> Unit,
    ) {
        NavigationDrawerItem(
            modifier = modifier,
            icon = {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = stringResource(id = labelRes)
                )
            },
            label = {
                Text(text = stringResource(id = labelRes))
            },
            selected = selected,
            onClick = onClick
        )
    }

}