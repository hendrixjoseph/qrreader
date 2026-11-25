package com.joehxblog.qrreader.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.joehxblog.qrreader.composable.CameraScreen
import com.joehxblog.qrreader.theme.SqrTheme

class MainActivity : ComponentActivity() {
    private var sharedImageUri by mutableStateOf<Uri?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        sharedImageUri = extractImageUri(intent)

        setContent {
            SqrTheme {
                Surface {
                    CameraScreen(
                        sharedImageUri = sharedImageUri,
                        onClearImage = {
                            sharedImageUri = null
                        }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)

        sharedImageUri = extractImageUri(intent)
    }

    private fun extractImageUri(intent: Intent?): Uri? {
        if (intent == null) return null

        return when (intent.action) {
            Intent.ACTION_SEND ->
                intent.getParcelableExtra(
                    Intent.EXTRA_STREAM,
                    Uri::class.java
                )

            Intent.ACTION_SEND_MULTIPLE ->
                intent.getParcelableArrayListExtra(
                    Intent.EXTRA_STREAM,
                    Uri::class.java
                )?.firstOrNull()

            Intent.ACTION_VIEW -> intent.data

            else -> null
        } ?: intent.clipData?.getItemAt(0)?.uri
    }
}
