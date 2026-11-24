package com.joehxblog.qrreader

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class DecodedContentHandler(initialContent: String) {
    var content by mutableStateOf(initialContent)

    fun getActionType(): QrActionType =
        when {
            content.startsWith("https://", true) ||
            content.startsWith("http://", true) -> QrActionType.VIEW

            content.startsWith("tel:", true) -> QrActionType.DIAL

            content.startsWith("mailto:", true) -> QrActionType.EMAIL

            content.startsWith("sms:", true) ||
            content.startsWith("smsto:", true) -> QrActionType.TEXT

            content.startsWith("geo:", true) -> QrActionType.MAP

            content.startsWith("WIFI:", true) -> QrActionType.CONNECT_WIFI

            content.startsWith("BEGIN:VEVENT", true) -> QrActionType.ADD_EVENT

            content.startsWith("BEGIN:VCARD", true) ||
            content.startsWith("MECARD:", true) -> QrActionType.ADD_CONTACT

            else -> QrActionType.NONE
        }

    fun getButtonActionText(): String {
        return getActionType().buttonText
    }

    fun getIntentAction(): String? {
        return getActionType().intentAction
    }
}