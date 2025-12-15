package org.librevault.presentation.screens.gallery

import android.util.Log
import androidx.activity.compose.BackHandler
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import org.librevault.common.state.SelectState
import org.librevault.common.state.SplashScreenConditionState
import org.librevault.common.state.UiState
import org.librevault.domain.model.gallery.MediaId
import org.librevault.domain.model.vault.FolderName
import org.librevault.presentation.aliases.MediaThumbnail
import org.librevault.presentation.aliases.ThumbnailInfoList
import org.librevault.presentation.events.GalleryEvent
import org.librevault.presentation.screens.components.FailureDisplay
import org.librevault.presentation.screens.components.LoadingIndicator
import org.librevault.presentation.screens.gallery.components.DeleteMediaConfirmationDialog
import org.librevault.presentation.screens.gallery.components.DrawerItem
import org.librevault.presentation.screens.gallery.components.EmptyView
import org.librevault.presentation.screens.gallery.components.EncryptingDialog
import org.librevault.presentation.screens.gallery.components.GalleryTopBar
import org.librevault.presentation.screens.gallery.components.PreviewCard
import org.librevault.presentation.screens.gallery.components.media_picker.MediaPickerDialog
import org.librevault.presentation.viewmodels.GalleryViewModel
import org.librevault.utils.ignore

private const val TAG = "GalleryScreen"

class GalleryScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<GalleryViewModel>()
        val coroutine = rememberCoroutineScope()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        val allFolderNamesState by viewModel.allFolderNamesState.collectAsState()
        val currentFolderThumbsState by viewModel.currentFolderThumbsState.collectAsState()
        val selectFiles by viewModel.selectFiles.collectAsState()
        val deleteFilesSelectionState by viewModel.deleteFilesSelectionState.collectAsState()
        val encryptState by viewModel.encryptState.collectAsState()
        val folderName by viewModel.folderNameState.collectAsState()

        BackHandler(enabled = deleteFilesSelectionState is SelectState.Selecting) {
            if (deleteFilesSelectionState is SelectState.Selecting) {
                viewModel.onEvent(GalleryEvent.CancelDeleteSelection)
            }
        }

        LaunchedEffect(key1 = Unit) {
            viewModel.onEvent(GalleryEvent.LoadFolder(FolderName.IMAGES))
        }

        LaunchedEffect(key1 = encryptState) {
            if (encryptState is UiState.Success) {
                Log.d(TAG, "LaunchedEffect: Refreshing gallery")
                val newFiles =
                    (encryptState as UiState.Success<ThumbnailInfoList>).data.map { MediaId(it.id) }
                viewModel.onEvent(GalleryEvent.LoadThumbnails(newFiles))
            }
        }

        LaunchedEffect(key1 = deleteFilesSelectionState) {
            val isFinished = deleteFilesSelectionState is SelectState.Finished

            if (isFinished) {
                viewModel.onEvent(GalleryEvent.ClearDeleteSelection)
                viewModel.onEvent(GalleryEvent.LoadThumbnails(forceRefresh = true))
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

                        items(items = allFolderNamesState.drop(2)) { folder ->
                            DrawerItem(
                                iconRes = R.drawable.baseline_folder_24,
                                label = folder(),
                                selected = folder == folderName,
                            ) {
                                coroutine.launch {
                                    viewModel.onEvent(GalleryEvent.LoadFolder(folder))
                                    drawerState.close()
                                }
                            }
                        }
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    GalleryTopBar(
                        deleteSelections = deleteFilesSelectionState.currentSelection,
                        drawerState = drawerState,
                        onSelectAllClicked = {

                        },
                        onDeselectAllClicked = {

                        },
                    ) {
                        viewModel.onEvent(GalleryEvent.ConfirmDeleteSelection)
                    }
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
                    when (val state = currentFolderThumbsState) {
                        is UiState.Error -> {
                            FailureDisplay(throwable = state.throwable)
                        }

                        UiState.Loading -> {
                            Log.d(TAG, "Content: Loading thumbnails")
                            LoadingIndicator()
                        }

                        is UiState.Success<List<MediaThumbnail>> -> {
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
                                        items(
                                            items = state.data,
                                            key = { it.info.id }
                                        ) { thumbnail ->
                                            val context = LocalContext.current
                                            val thumbnailInfo = thumbnail.info

                                            PreviewCard(
                                                context = context,
                                                thumb = thumbnail.content,
                                                info = thumbnailInfo,
                                                selected = thumbnailInfo.id in deleteFilesSelectionState.currentSelection,
                                                onLongClick = {
                                                    viewModel.onEvent(
                                                        GalleryEvent.SetDeleteSelection(
                                                            id = thumbnailInfo.id
                                                        )
                                                    )
                                                }
                                            ) {
                                                val isSelecting =
                                                    deleteFilesSelectionState is SelectState.Selecting

                                                if (isSelecting) {
                                                    Log.d(
                                                        TAG,
                                                        "Content: Delete media selection: ${thumbnail.info.id}"
                                                    )
                                                    viewModel.onEvent(
                                                        GalleryEvent.SetDeleteSelection(
                                                            id = thumbnail.info.id
                                                        )
                                                    )

                                                    return@PreviewCard
                                                }

                                                Log.d(
                                                    TAG,
                                                    "Content: Previewing media: ${thumbnail.info.id}"
                                                )
                                                viewModel.onEvent(
                                                    GalleryEvent.PreviewMedia(
                                                        id = thumbnailInfo.id
                                                    )
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> ignore()
                    }

                    LaunchedEffect(key1 = Unit) {
                        SplashScreenConditionState.isDecrypting = false
                    }
                }
            }
        }

        if (deleteFilesSelectionState is SelectState.Confirming) DeleteMediaConfirmationDialog(
            onDismissRequest = { viewModel.onEvent(GalleryEvent.CancelDeleteSelection) },
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