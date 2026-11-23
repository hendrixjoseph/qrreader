package com.joehxblog.qrreader.composable

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import coil3.compose.AsyncImage
import androidx.compose.material3.CircularProgressIndicator
import com.joehxblog.qrreader.QrCodeScanner
import com.joehxblog.qrreader.activity.DecodedContentActivity

@Composable
fun CameraScreen(
    sharedImageUri: Uri?,
    onClearImage: () -> Unit
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher =
        rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { granted ->
            hasCameraPermission = granted
        }

    val qrScanner = remember {
        QrCodeScanner()
    }

    var decodedValues by remember {
        mutableStateOf<List<String>>(emptyList())
    }

    var isProcessing by remember {
        mutableStateOf(false)
    }

    var scanError by remember {
        mutableStateOf<String?>(null)
    }

    DisposableEffect(qrScanner) {
        onDispose {
            qrScanner.close()
        }
    }

    LaunchedEffect(sharedImageUri) {
        decodedValues = emptyList()
        scanError = null

        if (sharedImageUri != null) {
            isProcessing = true

            qrScanner.processImage(
                context = context,
                uri = sharedImageUri,
                onSuccess = { barcodes ->
                    decodedValues = barcodes.mapNotNull {
                        it.rawValue
                    }
                    isProcessing = false

                    if (decodedValues.isNotEmpty()) {
                        val intent = Intent(
                            context,
                            DecodedContentActivity::class.java
                        ).apply {
                            putStringArrayListExtra(
                                DecodedContentActivity.EXTRA_DECODED_VALUES,
                                ArrayList(decodedValues)
                            )
                        }

                        context.startActivity(intent)
                    }
                },
                onError = { exception ->
                    scanError = exception.localizedMessage
                        ?: "Unable to process image"
                    isProcessing = false
                }
            )
        } else {
            isProcessing = false
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .padding(16.dp)
            .statusBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(
            space = 12.dp,
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (sharedImageUri != null) {
            AsyncImage(
                model = sharedImageUri,
                contentDescription = "Shared image",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize(0.65f)
            )
        } else if (hasCameraPermission) {
            CameraPreview(
                modifier = Modifier
                    .fillMaxSize(0.65f)
            )
        } else {
            Button(
                onClick = {
                    permissionLauncher.launch(
                        Manifest.permission.CAMERA
                    )
                }
            ) {
                Text("Grant Camera Permission")
            }
        }

        when {
            isProcessing -> {
                CircularProgressIndicator()
            }

            scanError != null -> {
                Text("Error: $scanError")
            }

            decodedValues.isEmpty() && sharedImageUri != null -> {
                Text("No QR codes found")
            }

            decodedValues.isNotEmpty() -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Found ${decodedValues.size} QR code(s)")

                    decodedValues.forEach { value ->
                        Text(
                            text = value,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }
            }

            else -> {
                Text("Point your camera at a QR code")
            }
        }
    }


}