package com.joehxblog.qrreader.composable

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.joehxblog.qrreader.DecodedContentHandler
import kotlinx.coroutines.launch

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
                    if (!decodedContentHandler.removeUtmParameters(context)) {
                        Toast.makeText(
                            context,
                            "no utm parameters found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                enabled = decodedContentHandler.urlButtonsEnabled(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("remove utm parameters")
            }

            Button(
                onClick = {
                    if(!decodedContentHandler.removeQueryParameters(context)) {
                        Toast.makeText(
                            context,
                            "no query parameters found",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                },
                enabled = decodedContentHandler.urlButtonsEnabled(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("remove query parameters")
            }

            val scope = rememberCoroutineScope()

            Button(
                onClick = {
                    scope.launch {
                        try {
                            if (!decodedContentHandler.checkRedirect(context)) {
                                Toast.makeText(
                                    context,
                                    "no redirect found",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } catch (exception: Exception) {
                            val error = exception.localizedMessage ?: "error checking redirect"
                            Toast.makeText(
                                context,
                                error,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                },
                enabled = decodedContentHandler.urlButtonsEnabled(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("check for redirects")
            }

            Button(
                onClick = {decodedContentHandler.doIntentAction(context)},
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
