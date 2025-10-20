package org.librevault

import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.material3.Card
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.kys0.unifile.UniFile
import org.librevault.Constants.Vault.InfoKeys
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Properties
import kotlin.time.measureTime

private const val TAG = "MainScreen"

class MainScreen : Screen {

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val coroutine = rememberCoroutineScope()
        val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
        val selectedFiles = remember { mutableStateListOf<File>() }
        val pickerLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickMultipleVisualMedia()
        ) { uris ->
            selectedFiles.clear()
            selectedFiles.addAll(
                uris.mapNotNull { uri ->
                    UniFile.fromUri(
                        context,
                        uri
                    )?.filePath?.let { File(it) }
                }
            )
        }

        val images = remember { mutableStateListOf<ByteArray>() }

        LaunchedEffect(key1 = Unit) {
            Constants.Vault.apply {
                if (ROOT.exists().not()) ROOT.mkdirs()
                if (THUMBS.exists().not()) THUMBS.mkdirs()
                if (DATA.exists().not()) DATA.mkdirs()
                if (INFO.exists().not()) INFO.mkdirs()
            }
        }

        fun getBaseKey(): ByteArray {
            val baseKeyFile = Constants.Vault.ROOT.resolve("base")
            val baseKey: ByteArray = if (baseKeyFile.exists()) {
                BaseKeyCrypto.decrypt(baseKeyFile)
            } else {
                val key = SecureFileCipher.generateBaseKey() // should return ByteArray
                BaseKeyCrypto.encrypt(key, baseKeyFile)
                key
            }
            return baseKey
        }

        var encrypted by remember { mutableIntStateOf(0) }

        LaunchedEffect(selectedFiles.size) {
            withContext(Dispatchers.IO) {
                val baseKey = getBaseKey()

                if (selectedFiles.isNotEmpty()) {
                    selectedFiles.forEachIndexed { idx, file ->
                        val duration = measureTime {
                            val name = RandomNameGenerator.generate()

                            val originalOutput = Constants.Vault.DATA.resolve(name)
                            val infoOutput = Constants.Vault.INFO.resolve(name)
                            val thumbOutput = Constants.Vault.THUMBS.resolve(name)

                            val infoBytes = buildProperties(" Info") {
                                setProperty(InfoKeys.FILE_TYPE, FileType.of(file.extension)?.name)
                                setProperty(InfoKeys.ORIGINAL_PATH, file.absolutePath)
                                setProperty(InfoKeys.PARENT_FOLDER, file.parent)
                                setProperty(InfoKeys.FILE_NAME, file.nameWithoutExtension)
                                setProperty(InfoKeys.FILE_EXTENSION, file.extension)
                            }.encodeToByteArray()
                            val thumbBytes = MediaThumbnailer.generate(file) ?: byteArrayOf()

                            Log.d(TAG, "Your Info: $infoBytes")

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
                                key = baseKey
                            ) {
                                encrypted++
                            }
                        }

                        Log.d(
                            TAG,
                            "[${idx + 1}/${selectedFiles.size}] Encryption for: ${file.nameWithoutExtension} took $duration!"
                        )
                    }

                    Log.d(TAG, "Encryption is done!")

                    selectedFiles.clear()
                    baseKey.fill(0)
                }
            }
        }

        LaunchedEffect(key1 = encrypted) {
            withContext(Dispatchers.IO + SupervisorJob()) {
                val baseKey = getBaseKey()

                val files = Constants.Vault.THUMBS.listFiles()
                    ?.filterNotNull()
                    ?.filter { it.extension.isEmpty() }
                    ?: emptyList()

                // Keep track of which files you've already decrypted
                val existingNames = images.mapIndexedNotNull { index, _ ->
                    Constants.Vault.THUMBS.listFiles()?.getOrNull(index)?.nameWithoutExtension
                }.toSet()

                // Only decrypt new files
                val newFiles = files.filterNot { it.nameWithoutExtension in existingNames }

                newFiles.forEachIndexed { idx, file ->
                    val time = measureTime {
                        val decryptedBytes = SecureFileCipher.decryptToBytes(
                            inputFile = file,
                            key = baseKey
                        )
                        // Add dynamically on the main thread
                        withContext(Dispatchers.Main) {
                            images.add(decryptedBytes)
                        }
                    }

                    Log.d(
                        TAG,
                        "[${idx + 1}/${newFiles.size}] Decryption for: ${file.nameWithoutExtension} took $time!"
                    )
                }

                baseKey.fill(0)
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

                            }
                        }

                        item {
                            DrawerItem(
                                iconRes = R.drawable.baseline_play_circle_outline_24,
                                labelRes = R.string.videos,
                                selected = false,
                            ) {

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
                modifier = Modifier.fillMaxSize(),
                topBar = {
                    TopAppBar(
                        title = {
                            Text(text = stringResource(R.string.app_name))
                        },
                        navigationIcon = {
                            IconButton(
                                onClick = {
                                    coroutine.launch { drawerState.open() }
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_menu_24),
                                    contentDescription = stringResource(R.string.navigate_up)
                                )
                            }
                        }
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = {
                            pickerLauncher.launch(
                                input = PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                            )
                        }
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
                    val context = LocalContext.current

                    LazyVerticalStaggeredGrid(
                        columns = StaggeredGridCells.Fixed(2),
                        contentPadding = PaddingValues(8.dp),
                        verticalItemSpacing = 8.dp,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(images) { image ->
                            Card(
                                shape = MaterialTheme.shapes.medium,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val painter = rememberAsyncImagePainter(
                                    model = ImageRequest.Builder(context)
                                        .data(image)
                                        .diskCachePolicy(CachePolicy.ENABLED)
                                        .memoryCachePolicy(CachePolicy.ENABLED)
                                        .build()
                                )

                                Image(
                                    painter = painter,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxWidth(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DrawerItem(
        modifier: Modifier = Modifier,
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

}

enum class FileType {
    IMAGE, VIDEO;

    companion object {
        private val imageExtensions = listOf("jpg", "jpeg", "png", "webp")
        private val videoExtensions = listOf("mp4", "avi", "mkv", "mov", "wmv")

        fun of(value: String): FileType? = when {
            imageExtensions.contains(value.lowercase()) -> IMAGE
            videoExtensions.contains(value.lowercase()) -> VIDEO
            else -> null
        }
    }
}

fun buildProperties(comment: String = "", block: Properties.() -> Unit): String {
    val properties = Properties()
    val out = ByteArrayOutputStream()

    block(properties)
    properties.store(out, comment)

    return out.toString("UTF-8")
}