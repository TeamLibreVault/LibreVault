package org.librevault.presentation.activities.preview

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.librevault.R
import org.librevault.common.activity.base.BaseLockActivity
import org.librevault.common.state.UiState
import org.librevault.domain.model.gallery.FileType
import org.librevault.presentation.activities.preview.components.ErrorFileType
import org.librevault.presentation.activities.preview.components.VideoPlayer
import org.librevault.presentation.activities.preview.components.ZoomableImage
import org.librevault.presentation.aliases.MediaContent
import org.librevault.presentation.aliases.MediaInfo
import org.librevault.presentation.events.PreviewEvent
import org.librevault.presentation.theme.LibreVaultTheme
import org.librevault.presentation.viewmodels.PreviewViewModel
import kotlin.time.Duration.Companion.minutes

private const val TAG = "PreviewActivity"

class PreviewActivity : BaseLockActivity() {

    private val viewModel by viewModel<PreviewViewModel>()

    override val autoLockEnabled: Boolean
        get() = true

    override val autoLockTimeout: Long
        get() = 1.minutes.inWholeMilliseconds

    override val lockOnCreateEnabled: Boolean
        get() = false

    override val isAnonymousMode: Boolean
        get() = true

    override fun getBiometricTitle(): String = getString(R.string.app_name)

    override fun getBiometricSubtitle(): String =
        getString(R.string.authenticate_to_unlock_the_vault)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val mediaId = intent.getStringExtra(MEDIA_ID)?.ifEmpty {
            throw IllegalStateException("No media id provided")
        }

        viewModel.onEvent(PreviewEvent.LoadMediaInfo(mediaId))
        viewModel.onEvent(PreviewEvent.DecryptMedia(mediaId))

        Log.d(TAG, "onCreate: Id = $mediaId")

        setContent {
            LibreVaultTheme {
                val mediaInfoState by viewModel.mediaInfoState.collectAsState()
                val mediaContentState by viewModel.mediaContentState.collectAsState()
                val errorInfoDialog by viewModel.showErrorInfoDialogState.collectAsState()

                var mediaInfo by remember { mutableStateOf(MediaInfo.placeholder()) }

                when (val state = mediaInfoState) {
                    is UiState.Error -> {
                        viewModel.onEvent(PreviewEvent.ShowErrorInfoDialog)
                        mediaInfo = MediaInfo.error()
                        Log.e(TAG, "onCreate: Error loading media info", state.throwable)
                    }

                    is UiState.Success<MediaInfo> -> {
                        mediaInfo = state.data
                        Log.d(TAG, "onCreate: Media info loaded: $mediaInfo")
                    }

                    else -> Unit
                }

                when (val state = mediaContentState) {
                    is UiState.Error -> ErrorLoadingImage(state.throwable)
                    UiState.Idle -> LoadingContent()
                    UiState.Loading -> LoadingContent()
                    is UiState.Success<MediaContent> -> {
                        ContentPreview(
                            mediaInfo = mediaInfo,
                            mediaContent = state.data,
                            onBackClick = ::finish
                        )
                    }
                }

                if (errorInfoDialog) ErrorLoadingInfoDialog { viewModel.onEvent(PreviewEvent.HideErrorInfoDialog) }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Suppress("DEPRECATION")
    @Composable
    private fun ContentPreview(
        mediaInfo: MediaInfo,
        mediaContent: MediaContent,
        onBackClick: () -> Unit,
    ) {
        val detailsDialog by viewModel.showDetailsDialogState.collectAsState()
        var isUiVisible by remember { mutableStateOf(true) }

        // Handles system UI visibility (status + nav bars)
        val systemUiController = rememberSystemUiController()
        LaunchedEffect(key1 = isUiVisible) {
            systemUiController.isSystemBarsVisible = isUiVisible
        }

        Scaffold(
            topBar = {
                AnimatedVisibility(
                    visible = isUiVisible,
                    enter = slideInVertically(
                        initialOffsetY = { fullHeight -> -fullHeight }
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { fullHeight -> -fullHeight }
                    )
                ) {
                    TopAppBar(
                        title = {
                            Text(
                                text = mediaInfo.fileName,
                                maxLines = 1,
                            )
                        },
                        navigationIcon = {
                            IconButton(onClick = onBackClick) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_arrow_back_24),
                                    contentDescription = stringResource(R.string.navigate_up)
                                )
                            }
                        },
                        actions = {
                            IconButton(onClick = {
                                viewModel.onEvent(
                                    event = PreviewEvent.ShowDetailsDialog(
                                        mediaInfo = mediaInfo
                                    )
                                )
                            }) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_info_24),
                                    contentDescription = stringResource(R.string.details)
                                )
                            }

                            IconButton(
                                onClick = {
                                    viewModel.onEvent(
                                        event = PreviewEvent.RestoreImage(
                                            mediaInfo = mediaInfo,
                                            mediaContent = mediaContent
                                        )
                                    )
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_settings_backup_restore_24),
                                    contentDescription = stringResource(R.string.restore_file)
                                )
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
                    )
                }
            }
        ) { innerPadding ->
            when (mediaInfo.fileType) {
                FileType.IMAGE -> ZoomableImage(
                    mediaContent = mediaContent,
                    mediaInfo = mediaInfo,
                    innerPadding = innerPadding
                ) {
                    isUiVisible = !isUiVisible
                }

                FileType.VIDEO -> VideoPlayer(
                    byteArray = mediaContent.data,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )

                FileType.ERROR -> ErrorFileType()
            }
        }

        if (detailsDialog) ImageDetailsDialog(
            onDismissRequest = { viewModel.onEvent(PreviewEvent.HideDetailsDialog) },
            mediaInfo = mediaInfo
        )
    }

    @Composable
    fun ErrorLoadingInfoDialog(throwable: Throwable? = null, onDismiss: () -> Unit) {
        val missingInfo = stringResource(R.string.media_info_missing)

        AlertDialog(
            onDismissRequest = { onDismiss() },
            title = { Text(stringResource(R.string.media_info_missing_title)) },
            text = { Text(throwable?.localizedMessage?.ifEmpty { missingInfo } ?: missingInfo) },
            confirmButton = {
                TextButton(onClick = { onDismiss() }) {
                    Text(stringResource(R.string.i_understand))
                }
            }
        )
    }

    @Composable
    private fun ErrorLoadingImage(
        throwable: Throwable,
        modifier: Modifier = Modifier,
    ) {
        Box(
            modifier = modifier
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(R.drawable.baseline_error_outline_24),
                    contentDescription = stringResource(R.string.error_loading_image),
                    tint = Color.Red,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(
                        R.string.failed_to_load_image,
                        throwable.localizedMessage
                    ),
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )
            }
        }
    }

    @Composable
    private fun LoadingContent(modifier: Modifier = Modifier) {
        Box(
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .fillMaxSize()
        ) {
            CircularProgressIndicator(modifier = modifier.align(Alignment.Center))
        }
    }

    @Composable
    fun ImageDetailsDialog(
        onDismissRequest: () -> Unit,
        mediaInfo: MediaInfo,
    ) {
        AlertDialog(
            onDismissRequest = onDismissRequest,
            title = { Text(text = stringResource(R.string.image_details)) },
            text = {
                val info = buildString {
                    appendLine(stringResource(R.string.original_path, mediaInfo.filePath))
                    appendLine(stringResource(R.string.original_file_name, mediaInfo.fileName))
                    appendLine(stringResource(R.string.file_size, mediaInfo.formattedFileSize))
                    appendLine(stringResource(R.string.file_type, mediaInfo.fileType))
                    appendLine(stringResource(R.string.file_extension, mediaInfo.fileExtension))
                }
                Text(text = info)
            },
            confirmButton = {
                TextButton(onClick = onDismissRequest) {
                    Text(text = stringResource(R.string.close))
                }
            }
        )
    }


    companion object {
        private const val MEDIA_ID = "media_id"
        fun startIntent(context: Context, mediaId: String) {
            context.startActivity(
                Intent(context, PreviewActivity::class.java).apply {
                    putExtra(MEDIA_ID, mediaId)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
    }

}