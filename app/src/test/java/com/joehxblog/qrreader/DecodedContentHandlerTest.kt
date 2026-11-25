package com.joehxblog.qrreader

import android.content.Intent
import android.net.wifi.WifiNetworkSuggestion
import android.provider.CalendarContract
import android.provider.ContactsContract
import android.provider.Settings
import androidx.core.net.toUri
import com.joehxblog.qrreader.activity.DecodedContentActivity
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertAll
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import tech.apter.junit.jupiter.robolectric.RobolectricExtension
import kotlin.text.equals
import kotlin.text.isNullOrEmpty
import kotlin.text.uppercase


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

    @ParameterizedTest
    @CsvSource(
        textBlock = """
        $VIEW, ${Intent.ACTION_VIEW}
        $DIAL, ${Intent.ACTION_DIAL}
        $EMAIL, ${Intent.ACTION_SENDTO}
        $TEXT, ${Intent.ACTION_SENDTO}
        '$MAP', ${Intent.ACTION_VIEW}"""
    )
    fun testDoIntentAction(uri: String, expectedAction: String) {
        val context = Robolectric.buildActivity(DecodedContentActivity::class.java)
            .setup()
            .get()

        val handler = DecodedContentHandler(uri)

        handler.doIntentAction(context)

        val shadowActivity = shadowOf(context)
        val intent = shadowActivity.nextStartedActivity

        assertAll(
            { assertEquals(expectedAction, intent.action) },
            { assertEquals(uri.toUri(), intent.data) }
        )
    }

    @Test
    fun testDoEventAction() {
        val context = Robolectric.buildActivity(DecodedContentActivity::class.java)
            .setup()
            .get()

        val handler = DecodedContentHandler(ADD_EVENT)

        handler.doIntentAction(context)

        val shadowActivity = shadowOf(context)
        val intent = shadowActivity.nextStartedActivity

        assertAll(
            { assertEquals(Intent.ACTION_INSERT, intent.action) },
            { assertEquals(CalendarContract.Events.CONTENT_URI, intent.data) }
        )
    }

    @ParameterizedTest
    @ValueSource(strings = [ADD_CONTACT_vCARD, ADD_CONTACT_MECARD])
    fun testDoContactAction(addContact: String) {
        val context = Robolectric.buildActivity(DecodedContentActivity::class.java)
            .setup()
            .get()

        val handler = DecodedContentHandler(addContact)

        handler.doIntentAction(context)

        val shadowActivity = shadowOf(context)
        val intent = shadowActivity.nextStartedActivity

        assertAll(
            { assertEquals(Intent.ACTION_INSERT, intent.action) },
            { assertEquals(ContactsContract.Contacts.CONTENT_URI, intent.data) }
        )
    }

    @Test
    fun testDoWifiAction() {
        val context = Robolectric.buildActivity(DecodedContentActivity::class.java)
            .setup()
            .get()

        val handler = DecodedContentHandler(CONNECT_WIFI)

        handler.doIntentAction(context)

        val shadowActivity = shadowOf(context)
        val intent = shadowActivity.nextStartedActivity

        val suggestion = intent.getParcelableArrayListExtra(
            Settings.EXTRA_WIFI_NETWORK_LIST,
            WifiNetworkSuggestion::class.java
        )?.firstOrNull()

        assertAll(
            { assertEquals(Settings.ACTION_WIFI_ADD_NETWORKS, intent.action) },
            { assertEquals("TestNetwork", suggestion?.ssid) },
            { assertEquals("TestPas;s:word123", suggestion?.passphrase) },
            { assertEquals(false, suggestion?.isHiddenSsid) }
        )
    }
}
