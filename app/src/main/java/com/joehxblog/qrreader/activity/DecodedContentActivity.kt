package com.joehxblog.qrreader.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import com.joehxblog.qrreader.composable.DecodedContentScreen


class DecodedContentActivity : ComponentActivity() {

    companion object {
        const val EXTRA_DECODED_VALUES = "decoded_values"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val values = intent.getStringArrayListExtra(
            EXTRA_DECODED_VALUES
        ) ?: arrayListOf()

        setContent {
            MaterialTheme {
                Surface {
                    DecodedContentScreen(
                        initialContent = values.joinToString("\n\n"),
                        onClose = { finish() }
                    )
                }
            }
        }
    }
}

