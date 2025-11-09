package org.librevault

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class GalleryViewModel : ViewModel() {
    private val _vaultData = mutableStateListOf<VaultData>()
    val vaultData: List<VaultData> = _vaultData

    private val _selectedFiles = mutableStateListOf<File>()
    val selectedFiles: List<File> = _selectedFiles

    private var _filterType = mutableStateOf(FileType.IMAGE.toVaultFolder())
    var filterType: State<VaultFolder> = _filterType

    fun initDirs() {
        Constants.Vault.apply {
            if (ROOT.exists().not()) ROOT.mkdirs()
            if (THUMBS.exists().not()) THUMBS.mkdirs()
            if (DATA.exists().not()) DATA.mkdirs()
            if (INFO.exists().not()) INFO.mkdirs()
        }
    }

    fun setSelectedFiles(files: List<File>) {
        _selectedFiles.clear()
        _selectedFiles.addAll(files)
    }

    fun setThumbnailsFolder(folder: VaultFolder) {
        _filterType.value = folder
    }

    fun getThumbnails(folder: VaultFolder): List<VaultData> = _vaultData.filter { it.second.getProperty(Constants.Vault.InfoKeys.FILE_TYPE) == folder() }

    fun encryptFiles(
        files: List<File>,
        onFileEncrypted: (File) -> Unit = {},
        onCompletion: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            val baseKey = getBaseKey()

            if (files.isNotEmpty()) {
                files.forEachIndexed { idx, file ->
                    val name = RandomNameGenerator.generate()
                    val originalOutput = Constants.Vault.DATA.resolve(name)
                    val infoOutput = Constants.Vault.INFO.resolve(name)
                    val thumbOutput = Constants.Vault.THUMBS.resolve(name)
                    val infoBytes = buildProperties(" Info") {
                        setProperty(
                            Constants.Vault.InfoKeys.FILE_TYPE,
                            FileType.parse(file)?.name
                        )
                        setProperty(
                            Constants.Vault.InfoKeys.ORIGINAL_PATH,
                            file.absolutePath
                        )
                        setProperty(Constants.Vault.InfoKeys.PARENT_FOLDER, file.parent)
                        setProperty(
                            Constants.Vault.InfoKeys.ORIGINAL_FILE_NAME,
                            file.nameWithoutExtension
                        )
                        setProperty(Constants.Vault.InfoKeys.VAULT_FILE_NAME, name)
                        setProperty(
                            Constants.Vault.InfoKeys.FILE_EXTENSION,
                            file.extension
                        )
                    }.encodeToByteArray()
                    val thumbBytes = MediaThumbnailer.generate(file) ?: byteArrayOf()

                    SecureFileCipher.encryptBytes(
                        inputBytes = infoBytes,
                        outputFile = infoOutput,
                        key = baseKey
                    )
                    SecureFileCipher.encryptBytes(
                        inputBytes = thumbBytes,
                        outputFile = thumbOutput,
                        key = baseKey
                    )
                    SecureFileCipher.encryptFile(
                        inputFile = file,
                        outputFile = originalOutput,
                        key = baseKey,
                        onComplete = {
                            onFileEncrypted(file)
                        }
                    )

                }

                baseKey.fill(0)
                withContext(Dispatchers.Main) { onCompletion() }
            }
        }
    }

    fun decryptFiles(
        onFileDecrypted: (VaultData) -> Unit = {},
        onCompletion: () -> Unit,
    ) {
        viewModelScope.launch(Dispatchers.IO + SupervisorJob()) {
            val baseKey = getBaseKey()

            val files = Constants.Vault.THUMBS.listFiles()
                ?.filterNotNull()
                ?.filter { it.extension.isEmpty() }
                ?: emptyList()

            // Keep track of which files you've already decrypted
            val existingNames = _vaultData.mapIndexedNotNull { index, _ ->
                Constants.Vault.THUMBS.listFiles()?.getOrNull(index)?.nameWithoutExtension
            }.toSet()

            // Only decrypt new files
            val newFiles = files.filterNot { it.nameWithoutExtension in existingNames }

            newFiles.forEachIndexed { idx, file ->
                val decryptedBytes = SecureFileCipher.decryptToBytes(
                    inputFile = file,
                    key = baseKey
                )
                val decryptedInfo = SecureFileCipher.decryptToBytes(
                    inputFile = Constants.Vault.INFO.resolve(file.nameWithoutExtension),
                    key = baseKey
                ).decodeToString().toProperties()

                val vd = decryptedBytes to decryptedInfo
                _vaultData.add(vd)
                onFileDecrypted(vd)
            }

            baseKey.fill(0)
            withContext(Dispatchers.Main) { onCompletion() }
        }

    }
}