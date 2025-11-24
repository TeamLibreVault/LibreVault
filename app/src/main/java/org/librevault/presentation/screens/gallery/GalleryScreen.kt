package org.librevault.presentation.screens.gallery

import android.content.Context
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import cafe.adriel.voyager.core.screen.Screen
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.librevault.R
import org.librevault.common.state.SplashScreenConditionState
import org.librevault.common.state.UiState
import org.librevault.domain.model.gallery.FileType
import org.librevault.domain.model.vault.aliases.resolveVaultFiles
import org.librevault.presentation.activities.preview.PreviewActivity
import org.librevault.presentation.aliases.gallery.ThumbnailInfo
import org.librevault.presentation.aliases.gallery.ThumbnailsList
import org.librevault.presentation.events.gallery.GalleryEvent
import org.librevault.presentation.screens.components.FailureDisplay
import org.librevault.presentation.screens.components.LoadingIndicator
import org.librevault.presentation.screens.gallery.components.MediaPickerDialog
import org.librevault.presentation.viewmodels.gallery.GalleryViewModel

private const val TAG = "GalleryScreen"

class GalleryScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val viewModel = koinViewModel<GalleryViewModel>()
        val coroutine = rememberCoroutineScope()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

        val infoState by viewModel.infoState.collectAsState()
        val thumbnailsState by viewModel.thumbnailsState.collectAsState()
        val selectFiles by viewModel.selectFiles.collectAsState()
        val encryptState by viewModel.encryptState.collectAsState()

        LaunchedEffect(key1 = Unit) {
            viewModel.onEvent(GalleryEvent.LoadThumbnails)
        }

        LaunchedEffect(key1 = encryptState) {
            if (encryptState is UiState.Success) {
                Log.d(TAG, "LaunchedEffect: Refreshing gallery")
                Log.d(TAG, "LaunchedEffect: Thumbnails " + resolveVaultFiles().second)
                viewModel.onEvent(GalleryEvent.LoadThumbnails)
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
                                coroutine.launch {
                                    TODO()
                                    drawerState.close()
                                }
                            }
                        }

                        item {
                            DrawerItem(
                                iconRes = R.drawable.baseline_play_circle_outline_24,
                                labelRes = R.string.videos,
                                selected = false,
                            ) {
                                coroutine.launch {
                                    TODO()
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
                        title = { Text(text = stringResource(R.string.app_name)) },
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
                                        items(items = state.data, key = { it.id }) { thumb ->
                                            val context = LocalContext.current
                                            val thumbnailInfo: ThumbnailInfo =
                                                when (val info = infoState) {
                                                    is UiState.Error -> ThumbnailInfo.error()
                                                    is UiState.Success<ThumbnailInfo> -> {
                                                        info.data
                                                    }

                                                    else -> ThumbnailInfo.placeholder()
                                                }

                                            PreviewCard(
                                                context = context,
                                                thumb = thumb.data,
                                                info = thumbnailInfo,
                                            ) {
                                                PreviewActivity.startIntent(
                                                    context,
                                                    thumbnailInfo.id
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        else -> {}
                    }

                    LaunchedEffect(key1 = Unit) {
                        SplashScreenConditionState.isDecrypting = false
                    }
                }
            }
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

    @Composable
    private fun DrawerItem(
        modifier: Modifier = Modifier.Companion,
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
    private fun PreviewCard(
        context: Context,
        thumb: ByteArray,
        info: ThumbnailInfo,
        modifier: Modifier = Modifier,
        onClick: () -> Unit,
    ) {
        Card(shape = MaterialTheme.shapes.medium) {
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(context)
                    .data(thumb)
                    .diskCachePolicy(CachePolicy.DISABLED)
                    .memoryCachePolicy(CachePolicy.DISABLED)
                    .build()
            )

            Box {
                Image(
                    painter = painter,
                    contentDescription = info.fileName,
                    contentScale = ContentScale.Crop,
                    modifier = modifier
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onClick() }
                )

                if (info.fileType == FileType.VIDEO) {
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

    @Composable
    private fun EncryptingDialog(onDismissRequest: () -> Unit = {}) {
        Dialog(onDismissRequest = onDismissRequest) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.size(16.dp))
                    Text(text = stringResource(R.string.encrypting))
                }
            }
        }
    }

    @Composable
    private fun EmptyView() {
        val emptyMessages = listOf(
            "Nothing here. (Not even crickets) ¯\\_(ツ)_/¯",
            "Still empty. ¯\\_(ツ)_/¯",
            "Just vibes. (╯°□°）╯︵ ┻━┻",
            "Majestic void detected. ( ͡° ͜ʖ ͡°)",
            "Invisible content loading… (ಠ_ಠ)",
            "This space is totally blank. (•_•)",
            "Data went on vacation. (ᵔᴥᵔ)",
            "You discovered nothing. (ノಠ益ಠ)ノ彡┻━┻"
        )

        val randomMessage = remember { emptyMessages.random() }

        Box(modifier = Modifier
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
}