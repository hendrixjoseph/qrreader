package com.joehxblog.qrreader.composable

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
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
import com.joehxblog.qrreader.ImageProcessHandler
import java.util.concurrent.Executors

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

    val imageProcessHandler = remember {
        ImageProcessHandler(context)
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

    DisposableEffect(imageProcessHandler) {
        onDispose {
            imageProcessHandler.closeQrScanner()
        }
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    val imageCapture = remember {
        ImageCapture.Builder()
            .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
            .build()
    }

    val cameraExecutor = remember {
        Executors.newSingleThreadExecutor()
    }

    DisposableEffect(cameraExecutor) {
        onDispose {
            cameraExecutor.shutdown()
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
                imageCapture = imageCapture,
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
            scanError != null -> {
                Text("Error: $scanError")
            }

            sharedImageUri == null -> {
                Text("Point your camera at a QR code")
            }
        }

        Button(
            onClick = {
                decodedValues = emptyList()
                scanError = null
                isProcessing = true

                if (sharedImageUri != null) {
                    imageProcessHandler.processSharedImage(
                        sharedImageUri = sharedImageUri,
                        onSuccess = { isProcessing = false },
                        onError = { error ->
                            scanError = error
                            isProcessing = false
                        }
                    )
                } else {
                    imageProcessHandler.processCameraImage(
                        imageCapture,
                        cameraExecutor,
                        onSuccess = { isProcessing = false },
                        onError = { error ->
                            scanError = error
                            isProcessing = false
                        }
                    )
                }
            },
            enabled = !isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isProcessing) {
                LinearProgressIndicator()
            } else {
                Text("Scan Now")
            }
        }

        if (sharedImageUri != null) {
            Button(
                onClick = {
                    onClearImage()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Clear")
            }
        }
    }
}
