package com.joehxblog.qrreader

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


@RunWith(AndroidJUnit4::class)
class QrCodeScannerTest {
    @Test
    fun testProcessImages() {
        listOf(
            "ADD_CONTACT_MECARD",
            "ADD_CONTACT_vCARD",
            "ADD_EVENT",
            "CONNECT_WIFI",
            "DIAL",
            "EMAIL",
            "MAP",
            "NONE",
            "TEXT",
            "VIEW"
        ).forEach { testProcessImage(it) }
    }

    fun testProcessImage(image: String) {
        val context = InstrumentationRegistry
            .getInstrumentation()
            .targetContext

        val filename = "$image.png"

        val file = File(context.cacheDir, filename)

        InstrumentationRegistry.getInstrumentation().context.assets.open(filename).use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val scanner = QrCodeScanner()
        val latch = CountDownLatch(1)

        var result: String? = null
        var error: Exception? = null

        scanner.processImage(
            context,
            Uri.fromFile(file),
            onSuccess = {
                result = it.firstOrNull()?.rawValue
                latch.countDown()
            },
            onError = {
                error = it
                latch.countDown()
            }
        )

        assertEquals(true, latch.await(600, TimeUnit.SECONDS))
        assertEquals(null, error)
        assertEquals(true, result != null)
    }
}