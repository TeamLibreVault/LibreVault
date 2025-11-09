package org.librevault

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cafe.adriel.voyager.core.screen.Screen
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import kotlinx.coroutines.launch
import me.kys0.unifile.UniFile
import org.librevault.Constants.Vault.InfoKeys
import java.io.File
import java.util.Properties

class GalleryScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current
        //val navigator = LocalNavigator.currentOrThrow
        val viewModel = viewModel<GalleryViewModel>()
        val coroutine = rememberCoroutineScope()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val selectedFiles = viewModel.selectedFiles
        val pickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia()
        ) { uris ->
            val files = uris.mapNotNull { uri ->
                UniFile.fromUri(
                    context,
                    uri
                )?.filePath?.let {
                    Log.d("eerer", "Path: $it")
                    File(it)
                }
            }
            viewModel.setSelectedFiles(files)
        }
        val filterType by viewModel.filterType

        var showMediaPicker by remember { mutableStateOf(false) }

        LaunchedEffect(key1 = Unit) {
            viewModel.initDirs()
        }

        var encrypted by remember { mutableIntStateOf(0) }

        LaunchedEffect(selectedFiles.size) {
            viewModel.encryptFiles(
                files = selectedFiles,
            ) {
                encrypted++
            }
        }

        LaunchedEffect(key1 = encrypted) {
            viewModel.decryptFiles {
                SplashScreenCondition.isDecrypting = false
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
                                selected = filterType() == FileType.IMAGE(),
                            ) {
                                coroutine.launch {
                                    viewModel.setThumbnailsFolder(FileType.IMAGE.toVaultFolder())
                                    drawerState.close()
                                }
                            }
                        }

                        item {
                            DrawerItem(
                                iconRes = R.drawable.baseline_play_circle_outline_24,
                                labelRes = R.string.videos,
                                selected = filterType() == FileType.VIDEO(),
                            ) {
                                coroutine.launch {
                                    viewModel.setThumbnailsFolder(FileType.VIDEO.toVaultFolder())
                                    drawerState.close()
                                }
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
                                    painter = painterResource(id = R.drawable.baseline_menu_24),
                                    contentDescription = stringResource(id = R.string.navigate_up)
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            showMediaPicker = true
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
                    val context = LocalContext.current

                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        verticalItemSpacing = 8.dp,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val folderImages by mutableStateOf(viewModel.getThumbnails(filterType))

                        items(items = folderImages) { (thumb, info) ->
                            val fileName = info.getProperty(InfoKeys.VAULT_FILE_NAME)

                            PreviewCard(
                                thumb = thumb,
                                info = info,
                            ) {
                                PreviewActivity.startIntent(context, fileName)
                            }
                        }
                    }
                }
            }
        }

        if (showMediaPicker) MediaPickerDialog(
            onDismissRequest = {
                showMediaPicker = false
            }
        ) {
            viewModel.setSelectedFiles(it)
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

    @Composable
    fun PreviewCard(
        thumb: ByteArray,
        info: Properties,
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
    ) {
        val context = LocalContext.current

        Card(
            shape = MaterialTheme.shapes.medium,
            modifier = modifier.fillMaxWidth()
        ) {
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(thumb)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .build()
            )

            Box {
                val fileType = FileType.parse(info.getProperty(InfoKeys.FILE_TYPE))

                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClick() },
                    contentScale = ContentScale.Crop
                )

                if (fileType == FileType.VIDEO) {
                    Image(
                        modifier = Modifier
                            .size(76.dp)
                            .align(Alignment.Center),
                        painter = painterResource(R.drawable.baseline_play_arrow_24),
                        contentScale = ContentScale.Crop,
                        colorFilter = ColorFilter.tint(Color.White),
                        contentDescription = null
                    )
                }
            }
        }
    }
}