package org.librevault.presentation.screens.gallery

import android.util.Log
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.librevault.R
import org.librevault.common.state.SplashScreenConditionState
import org.librevault.common.state.UiState
import org.librevault.domain.model.vault.FolderName
import org.librevault.presentation.aliases.ThumbnailInfo
import org.librevault.presentation.aliases.ThumbnailInfoList
import org.librevault.presentation.aliases.ThumbnailsList
import org.librevault.presentation.events.GalleryEvent
import org.librevault.presentation.screens.components.FailureDisplay
import org.librevault.presentation.screens.components.LoadingIndicator
import org.librevault.presentation.screens.gallery.components.DeleteMediaConfirmationDialog
import org.librevault.presentation.screens.gallery.components.DrawerItem
import org.librevault.presentation.screens.gallery.components.EmptyView
import org.librevault.presentation.screens.gallery.components.EncryptingDialog
import org.librevault.presentation.screens.gallery.components.PreviewCard
import org.librevault.presentation.screens.gallery.components.media_picker.MediaPickerDialog
import org.librevault.presentation.viewmodels.GalleryViewModel

private const val TAG = "GalleryScreen"

class GalleryScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<GalleryViewModel>()
        val coroutine = rememberCoroutineScope()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        val thumbnailInfoListState by viewModel.thumbnailInfoListState.collectAsState()
        val thumbnailsState by viewModel.thumbnailsState.collectAsState()
        val selectFiles by viewModel.selectFiles.collectAsState()
        val deleteFilesSelection by viewModel.deleteFilesSelection.collectAsState()
        val deleteSelectedFiles by viewModel.deleteSelectedFiles.collectAsState()
        val encryptState by viewModel.encryptState.collectAsState()
        val folderName by viewModel.folderNameState.collectAsState()

        LaunchedEffect(key1 = Unit) {
            viewModel.onEvent(GalleryEvent.LoadThumbnails())
            viewModel.onEvent(GalleryEvent.LoadMediaInfos())
        }

        LaunchedEffect(key1 = encryptState) {
            if (encryptState is UiState.Success) {
                Log.d(TAG, "LaunchedEffect: Refreshing gallery")
                val newFiles =
                    (encryptState as UiState.Success<ThumbnailInfoList>).data.map { it.id }
                viewModel.onEvent(GalleryEvent.LoadThumbnails(newFiles))
                viewModel.onEvent(GalleryEvent.LoadMediaInfos(newFiles))
            }
        }
        // Use UiState instead
        LaunchedEffect(key1 = deleteFilesSelection, key2 = deleteSelectedFiles) {
            if (deleteSelectedFiles && deleteFilesSelection.isEmpty()) {
                viewModel.onEvent(GalleryEvent.ClearDeleteSelection)
                viewModel.onEvent(GalleryEvent.LoadThumbnails())
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
                                selected = folderName == FolderName.IMAGES,
                            ) {
                                coroutine.launch {
                                    viewModel.onEvent(GalleryEvent.LoadFolder(FolderName.IMAGES))
                                    drawerState.close()
                                }
                            }
                        }

                        item {
                            DrawerItem(
                                iconRes = R.drawable.baseline_play_circle_outline_24,
                                labelRes = R.string.videos,
                                selected = folderName == FolderName.VIDEOS,
                            ) {
                                coroutine.launch {
                                    viewModel.onEvent(GalleryEvent.LoadFolder(FolderName.VIDEOS))
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

                        items(items = emptyList<String>()) {
                            // TODO
                        }
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text(text = stringResource(R.string.app_name)) },
                        actions = {
                            if (deleteFilesSelection.isNotEmpty()) IconButton(
                                onClick = {
                                    viewModel.onEvent(GalleryEvent.DeleteSelectedFiles)
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.baseline_delete_24),
                                    contentDescription = stringResource(id = R.string.delete)
                                )
                            }
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
                        onClick = { viewModel.onEvent(GalleryEvent.SelectFiles) }
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
                    when (val state = thumbnailsState) {
                        is UiState.Error -> {
                            FailureDisplay(throwable = state.throwable)
                        }

                        UiState.Loading -> {
                            Log.d(TAG, "Content: Loading thumbnails")
                            LoadingIndicator()
                        }

                        is UiState.Success<ThumbnailsList> -> {
                            Log.d(TAG, "Content: Thumbnails loaded: ${state.data.size}")

                            when (state.data.size) {
                                0 -> EmptyView()

                                else -> {
                                    LazyVerticalGrid(
                                        columns = GridCells.Fixed(3),
                                        contentPadding = PaddingValues(8.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        val thumbnailsInfo =
                                            when (val info = thumbnailInfoListState) {
                                                is UiState.Success -> info.data
                                                is UiState.Error -> {
                                                    Log.e(
                                                        TAG,
                                                        "Content: Error loading info",
                                                        info.throwable
                                                    )
                                                    emptyList()
                                                }

                                                else -> emptyList()
                                            }

                                        val infoMap = thumbnailsInfo.associateBy { it.id }

                                        val currentFolderThumbs = state.data.filter { thumb ->
                                            folderName in (infoMap[thumb.id]?.folders
                                                ?: emptyList())
                                        }


                                        items(
                                            items = currentFolderThumbs,
                                            key = { it.id }
                                        ) { thumbnail ->
                                            val context = LocalContext.current
                                            val thumbnailInfo =
                                                thumbnailsInfo.firstOrNull { it.id == thumbnail.id }
                                                    ?: ThumbnailInfo.placeholder()

                                            PreviewCard(
                                                context = context,
                                                thumb = thumbnail.data,
                                                info = thumbnailInfo,
                                                selected = thumbnail.id in deleteFilesSelection,
                                                onLongClick = {
                                                    viewModel.onEvent(
                                                        GalleryEvent.SetDeleteSelection(
                                                            id = thumbnail.id
                                                        )
                                                    )
                                                }
                                            ) {
                                                Log.d(
                                                    TAG,
                                                    "Content: Previewing media: ${thumbnail.id}"
                                                )
                                                viewModel.onEvent(
                                                    GalleryEvent.PreviewMedia(
                                                        thumbnail.id
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> Unit
                    }

                    LaunchedEffect(key1 = Unit) {
                        SplashScreenConditionState.isDecrypting = false
                    }
                }
            }
        }

        if (deleteSelectedFiles) DeleteMediaConfirmationDialog(
            onDismissRequest = { viewModel.onEvent(GalleryEvent.ClearDeleteSelection) },
        ) {
            viewModel.onEvent(GalleryEvent.DeleteSelectedFiles)
        }

        if (selectFiles) MediaPickerDialog(
            onDismissRequest = { viewModel.onEvent(GalleryEvent.UnselectFiles) }
        ) { files ->
            viewModel.onEvent(GalleryEvent.EncryptFiles(files))
        }

        when (val state = encryptState) {
            is UiState.Error -> {
                Log.e(TAG, "Content: Error encrypting files", state.throwable)
            }

            UiState.Loading -> {
                Log.d(TAG, "Content: Encrypting files")
                EncryptingDialog()
            }

            is UiState.Success<*> -> {
                Log.d(TAG, "Content: Files encrypted: ${state.data}")
            }

            else -> {}
        }
    }
}