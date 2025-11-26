package org.librevault.presentation.viewmodels

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.librevault.common.state.UiState
import org.librevault.common.vault_consts.VaultDirs
import org.librevault.domain.use_case_bundle.GalleryUseCases
import org.librevault.presentation.activities.preview.PreviewActivity
import org.librevault.presentation.aliases.EncryptListState
import org.librevault.presentation.aliases.InfoState
import org.librevault.presentation.aliases.ThumbnailsListState
import org.librevault.presentation.events.GalleryEvent
import java.io.File

private const val TAG = "GalleryViewModel"

class GalleryViewModel(
    private val context: Context,
    private val galleryUseCases: GalleryUseCases
) : AndroidViewModel(context as Application) {
    private val _thumbnailsState = MutableStateFlow<ThumbnailsListState>(UiState.Idle)
    val thumbnailsState: StateFlow<ThumbnailsListState> = _thumbnailsState

    private val _encryptState = MutableStateFlow<EncryptListState>(UiState.Idle)
    val encryptState: StateFlow<EncryptListState> = _encryptState

    private val _selectFiles = MutableStateFlow(false)
    val selectFiles: StateFlow<Boolean> = _selectFiles

    private val _infoState = MutableStateFlow<InfoState>(UiState.Idle)
    val infoState: StateFlow<InfoState> = _infoState

    init {
        initDirs()
    }

    fun onEvent(galleryEvent: GalleryEvent) {
        when (galleryEvent) {
            GalleryEvent.ClearGallery -> clearGallery()
            GalleryEvent.SelectFiles -> selectFiles()
            GalleryEvent.UnselectFiles -> unselectFiles()

            is GalleryEvent.EncryptFiles -> encryptFiles(galleryEvent.files)
            is GalleryEvent.DecryptInfo -> decryptInfo(galleryEvent.id)
            is GalleryEvent.PreviewMedia -> previewMedia(galleryEvent.id)

            GalleryEvent.LoadThumbnails -> loadThumbnails()
            GalleryEvent.RefreshGallery -> refreshGallery()
        }
    }

    private fun initDirs() {
        VaultDirs.apply {
            if (ROOT.exists().not()) ROOT.mkdirs()
            if (THUMBS.exists().not()) THUMBS.mkdirs()
            if (DATA.exists().not()) DATA.mkdirs()
            if (INFO.exists().not()) INFO.mkdirs()
        }
    }

    private fun clearGallery() {
        TODO("I don't know what to do here yet.")
    }

    private fun selectFiles() {
        _selectFiles.value = true
    }

    private fun unselectFiles() {
        _selectFiles.value = false
    }

    private fun encryptFiles(files: List<File>) {
        _encryptState.value = UiState.Loading

        if (files.isNotEmpty()) {
            galleryUseCases.addItems(
                files = files,
                onFailure = { _encryptState.value = UiState.Error(it) },
                onSuccess = { _encryptState.value = UiState.Success(it) }
            )
        }
    }

    private fun decryptInfo(id: String) {
        _infoState.value = UiState.Loading
        galleryUseCases.getMediaInfoById(
            id = id,
            onFailure = {
                Log.e(TAG, "decryptInfo: Error decrypting info", it)
                _infoState.value = UiState.Error(it)
            },
            onSuccess = {
                Log.d(TAG, "decryptInfo: Info decrypted: $it")
                _infoState.value = UiState.Success(it)
            }
        )
    }

    private fun previewMedia(mediaId: String) {
        Log.d(TAG, "previewMedia: Previewing media: $mediaId")
        PreviewActivity.startIntent(
            context = context,
            mediaId = mediaId
        )
    }

    private fun loadThumbnails() {
        _thumbnailsState.value = UiState.Loading
        galleryUseCases.getAllThumbnails(
            onThumbsDecrypted = { value -> _thumbnailsState.value = UiState.Success(value) },
            onError = {
                Log.e(TAG, "loadThumbnails: Error loading thumbnails", it)
                _thumbnailsState.value = UiState.Error(it)
            }
        )
    }

    private fun refreshGallery() {

    }
}