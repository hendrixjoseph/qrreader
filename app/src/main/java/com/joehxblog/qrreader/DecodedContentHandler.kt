package com.joehxblog.qrreader

import android.content.Context
import android.content.Intent
import android.provider.CalendarContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import biweekly.Biweekly
import biweekly.component.VEvent

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

    fun doIntentAction(context: Context) {
        val actionType = getActionType();
        val intentAction = actionType.intentAction

        if (intentAction != null) {
            if (intentAction == Intent.ACTION_INSERT) {
                if (actionType == QrActionType.ADD_EVENT) {
                    doEventAction(context)
                }
            } else {
                val uri = content.toUri()
                val intent = Intent(intentAction, uri)
                context.startActivity(intent)
            }
        }
    }

    fun doEventAction(context: Context) {
        val calendar = Biweekly.parse(content).first()
        val event = calendar.events.first()

        val start = event.dateStart?.getValue()
        val end = event.dateEnd?.getValue()

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI

            putExtra(
                CalendarContract.Events.TITLE,
                event.summary?.getValue()
            )

            putExtra(
                CalendarContract.Events.DESCRIPTION,
                event.description?.getValue()
            )

            putExtra(
                CalendarContract.Events.EVENT_LOCATION,
                event.location?.getValue()
            )

            start?.let {
                putExtra(
                    CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                    start.time
                )
            }

            end?.let {
                putExtra(
                    CalendarContract.EXTRA_EVENT_END_TIME,
                    it.time
                )
            }
        }

        context.startActivity(intent)
    }
}