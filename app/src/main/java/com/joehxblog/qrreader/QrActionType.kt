package com.joehxblog.qrreader

enum class QrActionType(val buttonText: String) {
    VIEW("View"),
    DIAL("Dial"),
    EMAIL("Email"),
    TEXT("Text"),
    MAP("Map"),
    CONNECT_WIFI("Connect"),
    ADD_EVENT("Add Event"),
    ADD_CONTACT("Add Contact"),
    NONE("")
}