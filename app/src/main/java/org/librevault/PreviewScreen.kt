package org.librevault

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import cafe.adriel.voyager.core.screen.Screen
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import java.util.Properties

// TODO: make the preview screen independent activity
class PreviewScreen(private val fileName: String) : Screen {
    @Composable
    override fun Content() {
        val context = LocalContext.current
        val zoomState = rememberZoomState()

        var fileBytes by remember { mutableStateOf<ByteArray?>(null) }
        var fileInfo by remember { mutableStateOf<Properties?>(null) }

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
            )

            fileInfo = SecureFileCipher.decryptToBytes(
                inputFile = infoInput,
                key = baseKey
            ).decodeToString().toProperties()

            baseKey.fill(0)
        }

        val image = rememberAsyncImagePainter(
            model = ImageRequest.Builder(context)
                .data(fileBytes)
                .diskCachePolicy(CachePolicy.DISABLED)
                .memoryCachePolicy(CachePolicy.DISABLED)
                .build()
        )

        Scaffold { innerPadding ->
            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .zoomable(zoomState)
                    .padding(innerPadding),
                painter = image,
                contentDescription = fileInfo?.getProperty(Constants.Vault.InfoKeys.ORIGINAL_FILE_NAME)
            )
        }
    }
}