package org.librevault.presentation.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.librevault.common.state.UiState
import org.librevault.domain.model.vault.TempFile
import org.librevault.domain.use_case_bundle.PreviewUseCases
import org.librevault.presentation.aliases.MediaContentState
import org.librevault.presentation.aliases.MediaInfo
import org.librevault.presentation.aliases.MediaInfoState
import org.librevault.presentation.events.PreviewEvent
import java.io.File

private const val TAG = "PreviewViewModel"

class PreviewViewModel(
    private val useCases: PreviewUseCases,
) : ViewModel() {
    private val _mediaInfoState = MutableStateFlow<MediaInfoState>(UiState.Idle)
    val mediaInfoState: StateFlow<MediaInfoState> = _mediaInfoState

    private val _mediaContentState = MutableStateFlow<MediaContentState>(UiState.Idle)
    val mediaContentState: StateFlow<MediaContentState> = _mediaContentState

    private val _showDetailsDialogState = MutableStateFlow(false)
    val showDetailsDialogState: StateFlow<Boolean> = _showDetailsDialogState

    private val _showErrorInfoDialogState = MutableStateFlow(false)
    val showErrorInfoDialogState: StateFlow<Boolean> = _showErrorInfoDialogState

    fun onEvent(event: PreviewEvent) = when (event) {
        is PreviewEvent.RestoreImage -> restoreImage(event.mediaInfo, event.mediaContent)
        is PreviewEvent.DecryptMedia -> decryptMedia(event.id)
        is PreviewEvent.LoadMediaInfo -> loadMediaInfo(event.id)
        PreviewEvent.HideDetailsDialog -> hideDetailsDialog()
        is PreviewEvent.ShowDetailsDialog -> showDetailsDialog()
        PreviewEvent.HideErrorInfoDialog -> hideErrorInfoDialog()
        is PreviewEvent.ShowErrorInfoDialog -> showErrorInfoDialog()
    }

    private fun restoreImage(mediaInfo: MediaInfo, mediaContent: TempFile) {
        val originalFile = File(mediaInfo.filePath)
        mediaContent.copyTo(originalFile)
    }

    private fun loadMediaInfo(id: String?) {
        _mediaInfoState.value = UiState.Loading

        if (id == null) {
            _mediaInfoState.value = UiState.Error(IllegalStateException("No id provided"))
            return
        }

        useCases.getMediaInfoById(
            id = id,
            onFailure = { _mediaInfoState.value = UiState.Error(it) },
            onSuccess = { _mediaInfoState.value = UiState.Success(it) }
        )
    }

    private fun showDetailsDialog() {
        _showDetailsDialogState.value = true
    }

    private fun hideDetailsDialog() {
        _showDetailsDialogState.value = false
    }

    private fun showErrorInfoDialog() {
        _showErrorInfoDialogState.value = true
    }

    private fun hideErrorInfoDialog() {
        _showErrorInfoDialogState.value = false
    }

    private fun decryptMedia(id: String?) {
        if (id == null) {
            _mediaContentState.value = UiState.Error(IllegalStateException("No id provided"))
            return
        }

        useCases.decryptMediaById(
            id = id,
            onFailure = {
                Log.e(TAG, "decryptMedia: ", it)
                _mediaContentState.value = UiState.Error(it)
            },
            onSuccess = { _mediaContentState.value = UiState.Success(it) }
        )
    }

}