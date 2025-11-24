package org.librevault.presentation.viewmodels.gallery

import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.librevault.common.vault_consts.VaultDirs
import org.librevault.common.state.UiState
import org.librevault.data.use_case.gallery.GalleryUseCases
import org.librevault.presentation.aliases.gallery.EncryptListState
import org.librevault.presentation.aliases.gallery.InfoState
import org.librevault.presentation.aliases.gallery.ThumbnailsListState
import org.librevault.presentation.events.gallery.GalleryEvent
import java.io.File

private const val TAG = "GalleryViewModel"

class GalleryViewModel(private val galleryUseCases: GalleryUseCases) : ViewModel() {
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
        galleryUseCases.getInfoById(
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