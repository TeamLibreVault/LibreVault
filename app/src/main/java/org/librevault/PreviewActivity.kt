package org.librevault

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.librevault.Constants.Vault.InfoKeys
import org.librevault.ui.theme.LibreVaultTheme
import java.io.File
import java.util.Properties

class PreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val fileName = intent.getStringExtra("fileName")
            ?: throw IllegalStateException("No file name provided")

        setContent {
            var fileBytes by remember { mutableStateOf<ByteArray?>(null) }
            var progress by remember { mutableFloatStateOf(0f) }
            var fileInfo = Properties()

            LibreVaultTheme {
                LaunchedEffect(key1 = Unit) {
                    val baseKey = getBaseKey()

                    val fileInput = Constants.Vault.DATA.resolve(fileName)
                    val infoInput = Constants.Vault.INFO.resolve(fileName)

                    if (fileInput.exists().not())
                        throw IllegalStateException("File not found: $fileInput")

                    if (infoInput.exists().not())
                        throw IllegalStateException("Info not found: $infoInput")

                    fileBytes = SecureFileCipher.decryptToBytes(
                        inputFile = fileInput,
                        key = baseKey
                    ) {
                        progress = it
                    }

                    fileInfo = SecureFileCipher.decryptToBytes(
                        inputFile = infoInput,
                        key = baseKey
                    ).decodeToString().toProperties()

                    baseKey.fill(0)
                }

                ImagePreview(
                    fileBytes = fileBytes ?: byteArrayOf(),
                    fileInfo = fileInfo,
                    progress = progress,
                ) {
                    finishActivity(0)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Suppress("DEPRECATION")
    @Composable
    private fun ImagePreview(
        fileBytes: ByteArray,
        fileInfo: Properties?,
        progress: Float,
        onBackClick: () -> Unit,
    ) {
        val image = rememberAsyncImagePainter(
            model = ImageRequest.Builder(this)
                .data(fileBytes)
                .diskCachePolicy(CachePolicy.DISABLED)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .build()
        )
        val zoomState = rememberZoomState()

        var isUiVisible by remember { mutableStateOf(true) }
        var detailsDialog by remember { mutableStateOf(false) }

        // Handles system UI visibility (status + nav bars)
        val systemUiController = rememberSystemUiController()
        LaunchedEffect(key1 = isUiVisible) {
            systemUiController.isSystemBarsVisible = isUiVisible
        }

        Scaffold(
            topBar = {
                AnimatedVisibility(visible = isUiVisible) {
                    TopAppBar(
                        title = {
                            Text(
                                text = fileInfo?.getProperty(InfoKeys.ORIGINAL_FILE_NAME)
                                    ?: "Image Preview",
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
                            IconButton(
                                onClick = { detailsDialog = true }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.outline_info_24),
                                    contentDescription = "Details"
                                )
                            }

                            IconButton(
                                onClick = {
                                    val originalPath =
                                        fileInfo?.getProperty(InfoKeys.ORIGINAL_PATH)!!
                                    val originalFile = File(originalPath)

                                    originalFile.writeBytes(fileBytes)

                                    Toast.makeText(
                                        this@PreviewActivity,
                                        "Image restored to: $originalPath",
                                        Toast.LENGTH_SHORT
                                    )
                                        .show()
                                }
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.baseline_settings_backup_restore_24),
                                    contentDescription = stringResource(R.string.restore_file)
                                )
                            }
                        }
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                val interaction = remember { MutableInteractionSource() }
                Image(
                    modifier = Modifier
                        .fillMaxSize()
                        .zoomable(zoomState)
                        .clickable(interactionSource = null, indication = null) {
                            isUiVisible = !isUiVisible
                        },
                    painter = image,
                    contentDescription = fileInfo?.getProperty(InfoKeys.ORIGINAL_FILE_NAME),
                    contentScale = ContentScale.Fit
                )

                // Loading progress indicator
                if (progress < 1f) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        progress = { progress },
                        color = Color.White
                    )
                }
            }
        }

        if (detailsDialog) AlertDialog(
            onDismissRequest = { detailsDialog = false },
            title = {
                Text(text = "Image Details:")
            },
            text = {
                val info = fileInfo!!.toFileInfo()
                Text(
                    """
                    Original path: ${info.originalPath}
                    Original file name: ${info.originalFileName}
                    File size: TODO
                    File type: ${info.fileType}
                    File extension: ${info.fileExtension}
                """.trimIndent()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        detailsDialog = false
                    }
                ) {
                    Text(text = "Close")
                }
            }
        )
    }

    companion object {
        fun startIntent(context: Context, fileName: String) {
            context.startActivity(
                Intent(context, PreviewActivity::class.java).apply {
                    putExtra("fileName", fileName)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
            )
        }
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