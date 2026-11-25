package com.joehxblog.qrreader

import com.joehxblog.qrreader.activity.DecodedContentActivity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import tech.apter.junit.jupiter.robolectric.RobolectricExtension


@ExtendWith(RobolectricExtension::class)
@Config(sdk = [34])
class DecodedContentHandlerTest {

    @ParameterizedTest
    @CsvSource(
        textBlock = """
        https://example.com, View
        http://example.com,  View
        HTTPS://example.com, View
        HTTP://example.com,  View
        tel:5551234567,      Dial
        TEL:5551234567,      Dial
        some random text,    ''
        ftp://example.com,   ''"""
    )
    fun `returns expected button action text`(
        content: String,
        expected: String
    ) {
        val handler = DecodedContentHandler(content)

        assertEquals(expected, handler.getButtonActionText())
    }


    @ParameterizedTest
    @CsvSource(
        textBlock = """
        https://example.com?utm_p=yes&other=no, https://example.com?other=no, true
        https://example.com?utm_p=yes, https://example.com, true
        https://example.com,  https://example.com, false"""
    )
    fun testRemoveUtmParameters(
        content: String,
        expectedValue: String,
        exceptedSuccess: Boolean
    ) {
        val context = Robolectric.buildActivity(DecodedContentActivity::class.java)
            .setup()
            .get()

        val handler = DecodedContentHandler(content)
        val success = handler.removeUtmParameters(context)

        val shadowActivity = shadowOf(context)
        val intent = shadowActivity.nextStartedActivity

        val decodedValues = intent?.getStringArrayListExtra(
            DecodedContentActivity.EXTRA_DECODED_VALUES
        ) ?: arrayListOf(expectedValue)

        assertAll(
            { assertEquals(exceptedSuccess, success) },
            { assertEquals(arrayListOf(expectedValue), decodedValues) }
        )
    }

    @ParameterizedTest
    @CsvSource(
        textBlock = """
        https://example.com?utm_p=yes&other=no, https://example.com, true
        https://example.com?utm_p=yes, https://example.com, true
        https://example.com,  https://example.com, false"""
    )
    fun testRemoveQueryParameters(
        content: String,
        expectedValue: String,
        exceptedSuccess: Boolean
    ) {
        val context = Robolectric.buildActivity(DecodedContentActivity::class.java)
            .setup()
            .get()

        val handler = DecodedContentHandler(content)
        val success = handler.removeQueryParameters(context)

        val shadowActivity = shadowOf(context)
        val intent = shadowActivity.nextStartedActivity

        val decodedValues = intent?.getStringArrayListExtra(
            DecodedContentActivity.EXTRA_DECODED_VALUES
        ) ?: arrayListOf(expectedValue)

        assertAll(
            { assertEquals(exceptedSuccess, success) },
            { assertEquals(arrayListOf(expectedValue), decodedValues) }
        )
    }
}