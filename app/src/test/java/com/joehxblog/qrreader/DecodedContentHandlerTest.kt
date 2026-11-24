package com.joehxblog.qrreader

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource

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
}