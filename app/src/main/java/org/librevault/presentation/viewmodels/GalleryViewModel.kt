package org.librevault.presentation.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.librevault.common.state.UiState
import org.librevault.common.vault_consts.VaultDirs
import org.librevault.domain.model.vault.FolderName
import org.librevault.domain.use_case_bundle.GalleryUseCases
import org.librevault.presentation.activities.preview.PreviewActivity
import org.librevault.presentation.aliases.DeleteSelectionList
import org.librevault.presentation.aliases.EncryptListState
import org.librevault.presentation.aliases.MutableDeleteSelectionList
import org.librevault.presentation.aliases.ThumbnailInfoListState
import org.librevault.presentation.aliases.ThumbnailsListState
import org.librevault.presentation.events.GalleryEvent
import java.io.File

private const val TAG = "GalleryViewModel"

class GalleryViewModel(
    private val application: Application,
    private val galleryUseCases: GalleryUseCases,
) : AndroidViewModel(application) {
    private val _folderNameState = MutableStateFlow(FolderName.IMAGES)
    val folderNameState: StateFlow<FolderName> = _folderNameState

    private val _thumbnailsState = MutableStateFlow<ThumbnailsListState>(UiState.Idle)
    val thumbnailsState: StateFlow<ThumbnailsListState> = _thumbnailsState

    private val _encryptState = MutableStateFlow<EncryptListState>(UiState.Idle)
    val encryptState: StateFlow<EncryptListState> = _encryptState

    private val _selectFiles = MutableStateFlow(false)
    val selectFiles: StateFlow<Boolean> = _selectFiles

    private val _deleteFilesSelection =
        MutableStateFlow<MutableDeleteSelectionList>(mutableListOf())
    val deleteFilesSelection: StateFlow<DeleteSelectionList> = _deleteFilesSelection

    private val _deleteSelectedFiles = MutableStateFlow(false)
    val deleteSelectedFiles: StateFlow<Boolean> = _deleteSelectedFiles

    private val _thumbnailInfoListState = MutableStateFlow<ThumbnailInfoListState>(UiState.Idle)
    val thumbnailInfoListState: StateFlow<ThumbnailInfoListState> = _thumbnailInfoListState

    init {
        VaultDirs.initVaultDirs()
    }

    fun onEvent(galleryEvent: GalleryEvent) = when (galleryEvent) {
        is GalleryEvent.LoadFolder -> loadFolder(galleryEvent.folderName)

        GalleryEvent.ClearGallery -> clearGallery()
        GalleryEvent.SelectFiles -> selectFiles()
        GalleryEvent.UnselectFiles -> unselectFiles()

        is GalleryEvent.EncryptFiles -> encryptFiles(galleryEvent.files)
        is GalleryEvent.LoadMediaInfos -> loadMediaInfos(galleryEvent.ids)
        is GalleryEvent.PreviewMedia -> previewMedia(galleryEvent.id)

        is GalleryEvent.LoadThumbnails -> loadThumbnails(galleryEvent.ids)
        GalleryEvent.RefreshGallery -> refreshGallery()

        is GalleryEvent.SetDeleteSelection -> setDeleteSelection(galleryEvent.id)
        is GalleryEvent.DeleteSelectedFiles -> deleteSelectedFiles()
        GalleryEvent.ClearDeleteSelection -> clearDeleteSelection()
    }

    private fun loadFolder(folderName: FolderName) {
        _folderNameState.value = folderName
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

    private fun setDeleteSelection(id: String) {
        val current = _deleteFilesSelection.value
        _deleteFilesSelection.value = if (id in current) {
            current - id
        } else {
            current + id
        } as MutableDeleteSelectionList
    }

    private fun deleteSelectedFiles() {
        if (_deleteSelectedFiles.value) {
            val ids = _deleteFilesSelection.value
            galleryUseCases.deleteMediaByIds(ids) {
                _deleteFilesSelection.value = mutableListOf()
            }
        } else _deleteSelectedFiles.value = true
    }

    private fun clearDeleteSelection() {
        _deleteFilesSelection.value = mutableListOf()
        _deleteSelectedFiles.value = false
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

    private fun loadThumbnails(ids: List<String>) {
        val currentThumbnails = when (val currentState = _thumbnailsState.value) {
            is UiState.Success -> currentState.data
            else -> emptyList()
        }

        _thumbnailsState.value = UiState.Loading
        Log.d(TAG, "loadThumbnails: Loading thumbnails")

        if (ids.isNotEmpty()) {
            galleryUseCases.getAllThumbnailsById(
                ids = ids,
                onThumbsDecrypted = { newThumbnails ->
                    // Append new thumbnails
                    val updatedThumbnails = currentThumbnails + newThumbnails
                    _thumbnailsState.value = UiState.Success(updatedThumbnails)
                    Log.d(
                        TAG,
                        "loadThumbnails: Thumbs decrypted: ${newThumbnails.joinToString { it.id }}"
                    )
                },
                onError = {
                    _thumbnailsState.value = UiState.Error(it)
                    Log.e(TAG, "loadThumbnails: Error loading thumbnails", it)
                }
            )
        } else {
            galleryUseCases.getAllThumbnails(
                onThumbsDecrypted = { value ->
                    Log.d(TAG, "loadThumbnails: Thumbs decrypted: ${value.size}")
                    _thumbnailsState.value = UiState.Success(value)
                },
                onError = {
                    Log.e(TAG, "loadThumbnails: Error loading thumbnails", it)
                    _thumbnailsState.value = UiState.Error(it)
                }
            )
        }
    }

    private fun loadMediaInfos(ids: List<String>) {
        val currentInfos = when (val currentState = _thumbnailInfoListState.value) {
            is UiState.Success -> currentState.data
            else -> emptyList()
        }

        _thumbnailInfoListState.value = UiState.Loading
        Log.d(TAG, "loadMediaInfos: Loading info")

        if (ids.isNotEmpty()) {
            galleryUseCases.getMediaInfoByIds(
                ids = ids,
                onSuccess = {
                    Log.d(TAG, "loadMediaInfos: Infos decrypted: ${it.joinToString()}")
                    val updatedInfos = currentInfos + it
                    _thumbnailInfoListState.value = UiState.Success(updatedInfos)
                },
                onError = {
                    Log.e(TAG, "loadMediaInfos: Error decrypting info", it)
                    _thumbnailInfoListState.value = UiState.Error(it)
                }
            )
        } else {
            galleryUseCases.getAllMediaInfo(
                onSuccess = {
                    Log.d(TAG, "loadMediaInfos: Info decrypted: ${it.size}")
                    _thumbnailInfoListState.value = UiState.Success(it)
                },
                onError = {
                    Log.e(TAG, "loadMediaInfos: Error decrypting info", it)
                    _thumbnailInfoListState.value = UiState.Error(it)
                }
            )
        }
    }

    private fun previewMedia(mediaId: String) {
        Log.d(TAG, "previewMedia: Previewing media: $mediaId")
        PreviewActivity.startIntent(
            context = application,
            mediaId = mediaId
        )
    }

    private fun refreshGallery() {

    }
}