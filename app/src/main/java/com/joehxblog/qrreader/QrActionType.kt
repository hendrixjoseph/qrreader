package com.joehxblog.qrreader

import android.content.Intent
import android.provider.Settings

enum class QrActionType(val buttonText: String, val intentAction: String?) {
    VIEW("View", Intent.ACTION_VIEW),
    DIAL("Dial", Intent.ACTION_DIAL),
    EMAIL("Email", Intent.ACTION_SENDTO),
    TEXT("Text", Intent.ACTION_SENDTO),
    MAP("Map", Intent.ACTION_VIEW),
    CONNECT_WIFI("Connect", Settings.ACTION_WIFI_ADD_NETWORKS),
    ADD_EVENT("Add Event", Intent.ACTION_INSERT),
    ADD_CONTACT("Add Contact", Intent.ACTION_INSERT),
    NONE("--", null)
}