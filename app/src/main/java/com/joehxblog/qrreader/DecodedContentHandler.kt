package com.joehxblog.qrreader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class DecodedContentHandler(initialContent: String) {
    var content by mutableStateOf(initialContent)

    fun getButtonActionText(): String {
        return when {
            content.startsWith("https://", ignoreCase = true) ||
            content.startsWith("http://", ignoreCase = true) -> "View"

            content.startsWith("tel:", ignoreCase = true) -> "Dial"

            else -> "Open"
        }
    }
}