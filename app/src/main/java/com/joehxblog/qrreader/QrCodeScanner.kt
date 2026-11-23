package com.joehxblog.qrreader

import android.content.Context
import android.net.Uri

import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.io.IOException

class QrCodeScanner {

    private val options = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    private val scanner = BarcodeScanning.getClient(options)

    fun processImage(
        context: Context,
        uri: Uri,
        onSuccess: (List<Barcode>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val image = InputImage.fromFilePath(context, uri)

            scanner.process(image)
                .addOnSuccessListener(onSuccess)
                .addOnFailureListener(onError)

        } catch (e: IOException) {
            onError(e)
        }
    }

    fun close() {
        scanner.close()
    }
}
