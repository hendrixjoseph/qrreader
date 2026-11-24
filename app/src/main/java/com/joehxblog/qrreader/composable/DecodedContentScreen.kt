package com.joehxblog.qrreader.composable

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.joehxblog.qrreader.DecodedContentHandler
import androidx.core.net.toUri

@Composable
fun DecodedContentScreen(
    initialContent: String,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    val decodedContentHandler = remember {
        DecodedContentHandler(initialContent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QR Code Contents") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = decodedContentHandler.content,
                onValueChange = { decodedContentHandler.content = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                label = { Text("Decoded content") },
                placeholder = { Text("QR code contents...") },
                minLines = 5
            )

            Button(
                onClick = {
                    val intentAction = decodedContentHandler.getIntentAction()

                    if (intentAction != null) {
                        val uri = decodedContentHandler.content.toUri()
                        val intent = Intent(intentAction, uri)
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                val text = decodedContentHandler.getButtonActionText()
                Text(text)
            }

            Button(
                onClick = {
                    val clipboard = context.getSystemService(
                        Context.CLIPBOARD_SERVICE
                    ) as ClipboardManager

                    clipboard.setPrimaryClip(
                        ClipData.newPlainText(
                            "QR Code Contents",
                            decodedContentHandler.content
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Copy")
            }

            OutlinedButton(
                onClick = onClose,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Close")
            }
        }
    }
}
