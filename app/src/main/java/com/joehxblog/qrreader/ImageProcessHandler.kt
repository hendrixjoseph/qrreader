package com.joehxblog.qrreader

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.joehxblog.qrreader.activity.DecodedContentActivity
import java.util.concurrent.ExecutorService

class ImageProcessHandler(val context: Context) {
    private val qrScanner = QrCodeScanner()

    fun launchDecodedContentActivity(barcodes: List<Barcode>): Boolean {
        val decodedValues = barcodes.mapNotNull {
            it.rawValue
        }

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

            return true
        } else {
            return false
        }
    }

    fun closeQrScanner() {
        qrScanner.close()
    }

    fun processCameraImage(
        imageCapture: ImageCapture,
        cameraExecutor: ExecutorService,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        imageCapture.takePicture(
            cameraExecutor,
            object : ImageCapture.OnImageCapturedCallback() {
                override fun onCaptureSuccess(image: ImageProxy) {
                    val mediaImage = image.image

                    if (mediaImage == null) {
                        image.close()
                        onError("no image?")
                        return
                    }

                    val inputImage = InputImage.fromMediaImage(
                        mediaImage,
                        image.imageInfo.rotationDegrees
                    )

                    qrScanner.processImage(
                        inputImage,
                        onSuccess = { barcodes ->
                            val launched = launchDecodedContentActivity(barcodes)

                            if (launched) {
                                onSuccess()
                            } else {
                                onError("no qr code?")
                            }

                            image.close()
                        },
                        onError = { exception ->
                            val scanError = exception.localizedMessage ?: "Unable to process image"
                            onError(scanError)
                            image.close()
                        }
                    )
                }

                override fun onError(exception: ImageCaptureException) {
                    val scanError = exception.localizedMessage ?: "Unable to capture image"
                    onError(scanError)
                }
            }
        )
    }

    fun processSharedImage(sharedImageUri: Uri, onSuccess: () -> Unit, onError: (String) -> Unit) {
        qrScanner.processImage(
            context = context,
            uri = sharedImageUri,
            onSuccess = { barcodes ->
                val launched = launchDecodedContentActivity(barcodes)

                if (launched) {
                    onSuccess()
                } else {
                    onError("no qr code?")
                }
            },
            onError = { exception ->
                val scanError = exception.localizedMessage ?: "Unable to process image"
                onError(scanError)
            }
        )
    }
}