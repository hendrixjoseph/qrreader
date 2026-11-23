package com.joehxblog.qrreader.composable

import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner

@Composable
fun CameraPreview(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val previewView = remember {
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier
    )

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture =
            ProcessCameraProvider.getInstance(context)

        var disposed = false

        cameraProviderFuture.addListener({
            if (!disposed) {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder()
                    .build()
                    .also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }

                val cameraSelector =
                    CameraSelector.DEFAULT_BACK_CAMERA

                try {
                    cameraProvider.unbindAll()

                    cameraProvider.bindToLifecycle(
                        lifecycleOwner,
                        cameraSelector,
                        preview
                    )
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            disposed = true

            if (cameraProviderFuture.isDone) {
                cameraProviderFuture.get().unbindAll()
            }
        }
    }
}
