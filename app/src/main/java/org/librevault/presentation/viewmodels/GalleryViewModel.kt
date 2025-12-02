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
    private val galleryUseCases: GalleryUseCases,
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
        VaultDirs.initVaultDirs()
    }

    fun onEvent(galleryEvent: GalleryEvent) = when (galleryEvent) {
        GalleryEvent.ClearGallery -> clearGallery()
        GalleryEvent.SelectFiles -> selectFiles()
        GalleryEvent.UnselectFiles -> unselectFiles()

        is GalleryEvent.EncryptFiles -> encryptFiles(galleryEvent.files)
        is GalleryEvent.DecryptInfo -> decryptInfo(galleryEvent.id)
        is GalleryEvent.PreviewMedia -> previewMedia(galleryEvent.id)

        is GalleryEvent.LoadThumbnails -> loadThumbnails(galleryEvent.items)
        GalleryEvent.RefreshGallery -> refreshGallery()
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

    private fun loadThumbnails(items: List<String>) {
        val currentThumbnails = when (val currentState = _thumbnailsState.value) {
            is UiState.Success -> currentState.data
            else -> emptyList()
        }

        _thumbnailsState.value = UiState.Loading
        Log.d(TAG, "loadThumbnails: Loading thumbnails")

        if (items.isNotEmpty()) {
            galleryUseCases.getAllThumbnailsById(
                ids = items,
                onThumbsDecrypted = { newThumbnails ->
                    // Append new thumbnails
                    val updatedThumbnails = currentThumbnails + newThumbnails
                    _thumbnailsState.value = UiState.Success(updatedThumbnails)
                    Log.d(TAG, "loadThumbnails: Thumbs decrypted: ${newThumbnails.joinToString { it.id }}")
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

    private fun refreshGallery() {

    }
}