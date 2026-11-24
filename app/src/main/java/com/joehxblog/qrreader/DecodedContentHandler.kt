package com.joehxblog.qrreader

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.CalendarContract
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import biweekly.Biweekly
import biweekly.component.VEvent
import com.joehxblog.qrreader.activity.DecodedContentActivity
import ezvcard.Ezvcard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

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

    fun removeUtmParameters(context: Context): Boolean {
        val uri = content.toUri()
        val builder = uri.buildUpon().clearQuery()

        uri.queryParameterNames
            .filterNot { it.startsWith("utm_", ignoreCase = true) }
            .forEach { name ->
                uri.getQueryParameters(name).forEach { value ->
                    builder.appendQueryParameter(name, value)
                }
            }

        return go(builder.build().toString(), context)
    }

    fun removeQueryParameters(context: Context): Boolean {
        val newContent = content.toUri()
            .buildUpon()
            .clearQuery()
            .build()
            .toString()

        return go(newContent, context)
    }

    suspend fun checkRedirect(context: Context): Boolean {
        withContext(Dispatchers.IO) {
            val redirectCodes = setOf(300, 301, 302, 303, 307, 308)

            var response = request(content, "HEAD")

            // Some servers don't support HEAD.
            if (response.first == 405 || response.first == 501) {
                response = request(content, "GET")
            }

            val (status, location) = response

            return@withContext !(status !in redirectCodes || location == null) && go(location, context)
        }
        return false
    }

    private fun go(newContent: String, context: Context): Boolean {
        if (newContent == content) {
            return false
        } else {
            val intent = Intent(
                context,
                DecodedContentActivity::class.java
            ).apply {
                putStringArrayListExtra(
                    DecodedContentActivity.EXTRA_DECODED_VALUES,
                    ArrayList(listOf(newContent))
                )
            }

            context.startActivity(intent)

            return true
        }
    }

    fun doIntentAction(context: Context) {
        val actionType = getActionType();
        val intentAction = actionType.intentAction

        if (intentAction != null) {
            if (intentAction == Intent.ACTION_INSERT) {
                when (actionType) {
                    QrActionType.ADD_EVENT -> doEventAction(context)
                    QrActionType.ADD_CONTACT -> doContactAction(context)
                    else -> {}
                }
            } else {
                val uri = content.toUri()
                val intent = Intent(intentAction, uri)
                context.startActivity(intent)
            }
        }
    }

    private fun doContactAction(context: Context) {
        val vcard = Ezvcard.parse(content).first()
        // TODO: this function
    }

    private fun doEventAction(context: Context) {
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

    private fun request(url: String, method: String): Pair<Int, String?> {
        val connection = URL(url).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = method
            connection.instanceFollowRedirects = false
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000
            connection.setRequestProperty("User-Agent", "QRReader")
            connection.setRequestProperty("Range", "bytes=0-0")

            val status = connection.responseCode
            val location = connection.getHeaderField("Location")

            return status to location
        } finally {
            connection.disconnect()
        }
    }
}