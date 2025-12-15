package org.librevault.presentation.viewmodels

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.librevault.common.state.SelectState
import org.librevault.common.state.UiState
import org.librevault.common.vault_consts.VaultDirs
import org.librevault.domain.model.gallery.MediaId
import org.librevault.domain.model.vault.FolderName
import org.librevault.domain.use_case_bundle.GalleryUseCases
import org.librevault.presentation.activities.preview.PreviewActivity
import org.librevault.presentation.aliases.DeleteSelectionState
import org.librevault.presentation.aliases.EncryptListState
import org.librevault.presentation.aliases.MediaThumbnail
import org.librevault.presentation.events.GalleryEvent
import java.io.File

private const val TAG = "GalleryViewModel"

class GalleryViewModel(
    private val application: Application,
    private val galleryUseCases: GalleryUseCases,
) : AndroidViewModel(application) {
    private val _folderNameState = MutableStateFlow(FolderName.IMAGES)
    val folderNameState: StateFlow<FolderName> = _folderNameState

    private val _encryptState = MutableStateFlow<EncryptListState>(UiState.Idle)
    val encryptState: StateFlow<EncryptListState> = _encryptState

    private val _selectFiles = MutableStateFlow(false)
    val selectFiles: StateFlow<Boolean> = _selectFiles

    private val _deleteFilesSelectionState =
        MutableStateFlow<DeleteSelectionState>(SelectState.Idle)
    val deleteFilesSelectionState: StateFlow<DeleteSelectionState> = _deleteFilesSelectionState

    private val _allFolderNamesState = MutableStateFlow<List<FolderName>>(emptyList())
    val allFolderNamesState: StateFlow<List<FolderName>> = _allFolderNamesState

    private val _allFolderThumbsState =
        MutableStateFlow<Map<FolderName, List<MediaThumbnail>>>(mapOf())


    private val _currentFolderThumbsState =
        MutableStateFlow<UiState<List<MediaThumbnail>>>(UiState.Idle)

    val currentFolderThumbsState: StateFlow<UiState<List<MediaThumbnail>>> =
        _currentFolderThumbsState


    init {
        VaultDirs.initVaultDirs()
        _folderNameState
            .onEach { _ ->
                loadThumbnails(emptyList())
            }
            .launchIn(viewModelScope)
    }

    fun onEvent(galleryEvent: GalleryEvent) = when (galleryEvent) {
        is GalleryEvent.LoadFolder -> loadFolder(galleryEvent.folderName)

        GalleryEvent.SelectFiles -> selectFiles()
        GalleryEvent.UnselectFiles -> unselectFiles()

        is GalleryEvent.EncryptFiles -> encryptFiles(galleryEvent.files)

        is GalleryEvent.PreviewMedia -> previewMedia(galleryEvent.id)

        is GalleryEvent.LoadThumbnails -> loadThumbnails(galleryEvent.ids, galleryEvent.forceRefresh)

        is GalleryEvent.SetDeleteSelection -> setDeleteSelection(galleryEvent.id)
        GalleryEvent.ConfirmDeleteSelection -> confirmDeleteSelection()
        is GalleryEvent.DeleteSelectedFiles -> deleteSelectedFiles()
        GalleryEvent.ClearDeleteSelection -> clearDeleteSelection()
        GalleryEvent.CancelDeleteSelection -> cancelDeleteSelection()
    }

    private fun loadFolder(folderName: FolderName) {
        _folderNameState.value = folderName
    }

    private fun loadThumbnails(mediaIds: List<MediaId>, refresh: Boolean = false) {
        val ids = mediaIds.map { it.value }
        _currentFolderThumbsState.value = UiState.Loading
        Log.d(TAG, "loadThumbnails: Loading thumbnails")

        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {

            val currentFolder = _folderNameState.value

            if (ids.isEmpty() && !refresh) {
                // Use cached data if available
                val cached = _allFolderThumbsState.value
                if (cached.containsKey(currentFolder)) {
                    _currentFolderThumbsState.value =
                        UiState.Success(cached[currentFolder].orEmpty())
                    return@launch
                }
            }

            val mediaInfos = try {
                galleryUseCases.getAllMediaInfo()
            } catch (e: Exception) {
                _currentFolderThumbsState.value = UiState.Error(e)
                Log.e(TAG, "loadThumbnails: Error loading media info", e)
                return@launch
            }

            val mediaThumbs = try {
                if (ids.isEmpty()) {
                    galleryUseCases.getAllThumbnails()
                } else {
                    galleryUseCases.getAllThumbnailsById(ids)
                }
            } catch (e: Exception) {
                _currentFolderThumbsState.value = UiState.Error(e)
                return@launch
            }

            val mediaFolders = mediaInfos
                .flatMap { info -> info.folders.map { folder -> folder to info.id } }
                .groupBy({ it.first }, { it.second })

            val mediaInfoMap = mediaInfos.associateBy { it.id }

            val folderMediaIds = mediaFolders[currentFolder].orEmpty()

            val mediaThumbnails = mediaThumbs.getOrDefault(emptyList()).mapNotNull { thumb ->
                val info = mediaInfoMap[thumb.id] ?: return@mapNotNull null
                if (thumb.id !in folderMediaIds) return@mapNotNull null
                MediaThumbnail(info, thumb.data)
            }

            // Merge into existing map
            val existingThumbs =
                _allFolderThumbsState.value[currentFolder].orEmpty()

            val mergedThumbs =
                (existingThumbs + mediaThumbnails)
                    .associateBy { it.info.id }   // or thumb.id if exposed
                    .values
                    .toList()
            val updatedMap =
                _allFolderThumbsState.value + (currentFolder to mergedThumbs)

            withContext(Dispatchers.Main) {
                _allFolderNamesState.value = mediaFolders
                    .map { it.key }
                    .sortedBy { name ->
                        when (name) {
                            FolderName.IMAGES -> 0
                            FolderName.VIDEOS -> 1
                            else -> 2
                        }
                    }
                _allFolderThumbsState.value = updatedMap
                _currentFolderThumbsState.value = UiState.Success(mergedThumbs)
            }
        }
    }

    private fun selectFiles() {
        _selectFiles.value = true
    }

    private fun unselectFiles() {
        _selectFiles.value = false
    }

    private fun setDeleteSelection(id: String) {
        val currentSelection = _deleteFilesSelectionState.value.currentSelection

        val newSelection = if (id in currentSelection) {
            currentSelection - id
        } else {
            currentSelection + id
        }

        _deleteFilesSelectionState.value = SelectState.Selecting(newSelection)
    }

    private fun confirmDeleteSelection() {
        val currentSelection = _deleteFilesSelectionState.value.currentSelection
        _deleteFilesSelectionState.value = SelectState.Confirming(currentSelection)
    }

    private fun deleteSelectedFiles() {
        if (_deleteFilesSelectionState.value !is SelectState.Confirming) return

        val currentSelection = _deleteFilesSelectionState.value.currentSelection

        if (currentSelection.isEmpty()) return

        galleryUseCases.deleteMediaByIds(currentSelection) {
            _deleteFilesSelectionState.value = SelectState.Finished
        }
    }

    private fun clearDeleteSelection() {
        _deleteFilesSelectionState.value = SelectState.Finished
    }

    private fun cancelDeleteSelection() {
        _deleteFilesSelectionState.value = SelectState.Idle
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

    private fun previewMedia(mediaId: String) {
        Log.d(TAG, "previewMedia: Previewing media: $mediaId")
        PreviewActivity.startIntent(
            context = application,
            mediaId = mediaId
        )
    }

}