package org.librevault

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import coil3.compose.rememberAsyncImagePainter
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable
import org.librevault.Constants.Vault.InfoKeys
import org.librevault.ui.theme.LibreVaultTheme
import java.util.Properties

private const val TAG = "PreviewActivity"

class PreviewActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        val fileName = intent.getStringExtra("fileName")
            ?: throw IllegalStateException("No file name provided")

        setContent {
            val zoomState = rememberZoomState()

            var fileBytes by remember { mutableStateOf<ByteArray?>(null) }
            var fileInfo by remember { mutableStateOf<Properties?>(null) }
            var progress by remember { mutableFloatStateOf(0f) }

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
                        Toast.makeText(this@PreviewActivity, "progress = $it", Toast.LENGTH_SHORT)
                            .show()
                        Log.d(TAG, "onCreate: progress = $it")
                    }

                    fileInfo = SecureFileCipher.decryptToBytes(
                        inputFile = infoInput,
                        key = baseKey
                    ).decodeToString().toProperties()

                    baseKey.fill(0)
                }

                val image = rememberAsyncImagePainter(
                    model = ImageRequest.Builder(this)
                        .data(fileBytes)
                        .diskCachePolicy(CachePolicy.DISABLED)
                        .memoryCachePolicy(CachePolicy.DISABLED)
                        .build()
                )

                Scaffold { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        Image(
                            modifier = Modifier
                                .fillMaxSize()
                                .zoomable(zoomState),
                            painter = image,
                            contentDescription = fileInfo?.getProperty(InfoKeys.ORIGINAL_FILE_NAME)
                        )

                        if (progress < 1f) CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center),
                            progress = { progress }
                        )
                    }
                }
            }
        }
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