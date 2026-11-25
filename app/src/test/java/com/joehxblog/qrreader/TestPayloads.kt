package com.joehxblog.qrreader

const val VIEW = "https://example.com"
const val DIAL = "tel:+15551234567"
const val EMAIL = "mailto:test@example.com"
const val TEXT = "sms:+15551234567?body=Hello%20there"
const val MAP = "geo:39.7589,-84.1916"
const val CONNECT_WIFI = "WIFI:T:WPA;S:TestNetwork;P:TestPassword123;;"

const val ADD_EVENT = """
BEGIN:VCALENDAR
VERSION:2.0
BEGIN:VEVENT
SUMMARY:Test Meeting
DTSTART:20261201T100000
DTEND:20261201T110000
LOCATION:Conference Room
DESCRIPTION:Sample calendar event
END:VEVENT
END:VCALENDAR
"""

const val ADD_CONTACT_vCARD = """
BEGIN:VCARD
VERSION:3.0
N:Doe;John;;;
FN:John Doe
ORG:Example Company
TITLE:Software Engineer
TEL;TYPE=CELL:+15551234567
TEL;TYPE=WORK:+15559876543
EMAIL:john.doe@example.com
ADR;TYPE=WORK:;;123 Main Street;Dayton;OH;45402;USA
URL:https://example.com
END:VCARD
"""

const val ADD_CONTACT_MECARD = "MECARD:N:Doe,John;TEL:+15551234567;EMAIL:john.doe@example.com;URL:https://example.com;;"

const val NONE = "This is just plain text, not an actionable QR code."