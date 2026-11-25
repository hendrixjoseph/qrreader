package com.joehxblog.qrreader

import android.content.Context
import android.content.Intent
import android.net.wifi.WifiNetworkSuggestion
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import biweekly.Biweekly
import com.joehxblog.qrreader.activity.DecodedContentActivity
import ezvcard.Ezvcard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

class DecodedContentHandler(initialContent: String) {
    var content by mutableStateOf(initialContent)

    var checkingRedirect by mutableStateOf(false)

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

            content.startsWith("BEGIN:VCALENDAR", true) -> QrActionType.ADD_EVENT

            content.startsWith("BEGIN:VCARD", true) ||
            content.startsWith("MECARD:", true) -> QrActionType.ADD_CONTACT

            else -> QrActionType.NONE
        }

    fun getButtonActionText(): String {
        return getActionType().buttonText
    }

    fun hasAction(): Boolean {
        return getActionType() != QrActionType.NONE
    }

    fun urlButtonsEnabled(): Boolean {
        return getActionType() == QrActionType.VIEW && !checkingRedirect
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
        checkingRedirect = true
        try {
            withContext(Dispatchers.IO) {
                val redirectCodes = setOf(300, 301, 302, 303, 307, 308)

                var response = request(content, "HEAD")

                // Some servers don't support HEAD.
                if (response.first == 405 || response.first == 501) {
                    response = request(content, "GET")
                }

                val (status, location) = response

                return@withContext !(status !in redirectCodes || location == null) && go(
                    location,
                    context
                )
            }
        } finally {
            checkingRedirect = false
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
        val actionType = getActionType()
        val intentAction = actionType.intentAction

        if (intentAction != null) {
            when (intentAction) {
                Intent.ACTION_INSERT -> {
                    when (actionType) {
                        QrActionType.ADD_EVENT -> doEventAction(context)
                        QrActionType.ADD_CONTACT -> doContactAction(context)
                        else -> {}
                    }
                }
                Settings.ACTION_WIFI_ADD_NETWORKS -> {
                    doWifiAction(context)
                }
                else -> {
                    val uri = content.toUri()
                    val intent = Intent(intentAction, uri)
                    context.startActivity(intent)
                }
            }
        }
    }

    private fun doWifiAction(context: Context) {
        val wifiString = content.removePrefix("WIFI:")

        val parts = mutableMapOf(
            "authType" to "",
            "ssid" to "",
            "password" to "",
            "hidden" to "",
            "" to ""
        )

        var step = ""
        var parsing = false
        var escaped = false

        for (char in wifiString) {
            if (parsing) {
                if (escaped) {
                    parts[step] = parts[step] + char
                    escaped = false
                } else if (char == '\\') {
                    escaped = true
                } else if (char == ';') {
                    parsing = false
                } else {
                    parts[step] = parts[step] + char
                }
            } else if (char == ':') {
                parsing = true
            } else {
                step = when (char) {
                    'T' -> "authType"
                    'S' -> "ssid"
                    'P' -> "password"
                    'H' -> "hidden"
                    else -> ""
                }
            }
        }

        val suggestion = WifiNetworkSuggestion.Builder()
            .setSsid(parts["ssid"] ?: "")
            .apply {
                when (parts["authType"]?.uppercase()) {
                    "WPA", "WPA2", "WPA3" -> {
                        val password = parts["password"] ?: ""
                        setWpa2Passphrase(password)
                    }
                    else -> Unit
                }

                if (!parts["hidden"].isNullOrEmpty() && parts["hidden"].equals("true", ignoreCase = true)) {
                    setIsHiddenSsid(true)
                }
            }
            .build()

        val intent = Intent(Settings.ACTION_WIFI_ADD_NETWORKS).apply {
            putParcelableArrayListExtra(
                Settings.EXTRA_WIFI_NETWORK_LIST,
                arrayListOf(suggestion)
            )
        }

        context.startActivity(intent)
    }

    private fun doContactAction(context: Context) {
        val mapping = if (content.startsWith("BEGIN:VCARD")) {
            getVCardMapping()
        } else {
            getMeCardMapping()
        }

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = ContactsContract.Contacts.CONTENT_URI

            mapping.forEach { (name, value) ->
                value?.let {
                    putExtra(name, it)
                }
            }
        }

        context.startActivity(intent)
    }

    private fun getMeCardMapping(): Map<String, String?> {
        val mecard = content
            .removePrefix("MECARD:")
            .split(";")
            .filter { it.contains(":") }
            .associate {
                val (key, value) = it.split(":", limit = 2)
                key to value
            }

        return mapOf(
            ContactsContract.Intents.Insert.NAME to mecard["N"]?.replace(",", " "),
            ContactsContract.Intents.Insert.PHONE to mecard["TEL"],
            ContactsContract.Intents.Insert.EMAIL to mecard["EMAIL"],
            ContactsContract.Intents.Insert.COMPANY to mecard["ORG"],
            ContactsContract.Intents.Insert.NOTES to mecard["NOTE"]
        )
    }

    private fun getVCardMapping(): Map<String, String?> {
        val vcard = Ezvcard.parse(content).first()

        return mapOf(
            ContactsContract.Intents.Insert.NAME to vcard.formattedName?.value,
            ContactsContract.Intents.Insert.PHONE to vcard.telephoneNumbers.firstOrNull()?.text,
            ContactsContract.Intents.Insert.EMAIL to vcard.emails.firstOrNull()?.value,
            ContactsContract.Intents.Insert.COMPANY to vcard.organizations.firstOrNull()?.values?.firstOrNull(),
            ContactsContract.Intents.Insert.JOB_TITLE to vcard.titles.firstOrNull()?.value,
            ContactsContract.Intents.Insert.POSTAL to vcard.addresses.firstOrNull()?.streetAddressFull,
            ContactsContract.Intents.Insert.NOTES to vcard.notes.firstOrNull()?.value
        )
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