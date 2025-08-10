package com.dscvit.vitty.util

import java.net.URLDecoder
import java.nio.charset.StandardCharsets

object Constants {
    const val GHOST_MODE = "false"
    const val PERIODS = "periods"
    const val TIME_SLOTS = "time_slots"
    const val UID = "uid"
    const val TOKEN = "token"
    const val EMAIL = "email"
    const val NAME = "name"
    const val USER_INFO = "login_info"
    const val UPDATE = "is_update"
    const val TIMETABLE_AVAILABLE = "is_available"
    const val NOTIFICATION_CHANNELS = "notif_channels"
    const val NOTIF_DELAY = 20
    const val GROUP_ID = "vitty_01"
    const val GROUP_ID_2 = "vitty_02"
    const val FIRST_TIME_SETUP = "firstTimeSetup"
    const val VERSION_CODE = "verCode"
    const val UPDATE_CODE = "upCode"
    const val EXAM_MODE = "exam_mode"
    const val IND_NOTIF = "individual_notif"
    const val BATTERY_OPTIM = "battery_optim"
    const val GITHUB_REPO = "github_repo"
    const val GITHUB_REPO_LINK = "https://github.com/GDGVIT/vitty-app"
    const val GDSCVIT_TAG = "gdscvit_website"
    const val GDSCVIT_WEBSITE = "https://dscvit.com/"
    const val VITTY_APP_URL = "https://dscv.it/app/vitty"
    const val VITTY_URL = "https://dscv.it/vittyconnect"
    const val ALARM_INTENT = 0
    const val TODAY_INTENT = 1
    const val NEXT_CLASS_INTENT = 2
    const val NEXT_CLASS_NAV_INTENT = 3
    const val SHARE_INTENT = 4
    const val NOTIF_INTENT = 5
    const val NOTIF_START = 6
    const val DEFAULT_QUOTE = "Catch up on some sleep"
    const val VIBRATION_MODE = "vib_mode"
    const val SAT_MODE = "sat_mode"
    const val CHANGE_TIMETABLE = "change_timetable"

    // Support & Feedback
    const val SUPPORT_EMAIL = "support_email"
    const val SUPPORT_EMAIL_ADDRESS = "dscvit.vitty@gmail.com"
    const val GITHUB_ISSUES = "github_issues"
    const val GITHUB_ISSUES_LINK = "https://github.com/GDGVIT/vitty-app/issues"

    const val COMMUNITY_TOKEN = "community_token"
    const val COMMUNITY_USERNAME = "community_username"
    const val COMMUNITY_NAME = "community_name"
    const val COMMUNITY_PICTURE = "community_picture"
    const val COMMUNITY_REGNO = "community_regno"
    const val COMMUNITY_CAMPUS = "community_campus"
    const val COMMUNITY_TIMETABLE_AVAILABLE = "community_timetable_available"
    const val COMMUNITY_PINNED_FRIEND_1 = "community_pinned_friend_1"
    const val COMMUNITY_PINNED_FRIEND_2 = "community_pinned_friend_2"
    const val COMMUNITY_PINNED_FRIEND_3 = "community_pinned_friend_3"

    const val CACHE_COMMUNITY_TIMETABLE = "cache_community_timetable"

    const val REMINDER_CHANNEL_ID = "reminder_channel"
    const val REMINDER_CHANNEL_NAME = "Reminders"
    const val REMINDER_CHANNEL_DESC = "Notifications for course reminders"

    const val PREF_LAST_REVIEW_REQUEST = "last_review_request"
}

fun String.urlDecode(): String =
    try {
        URLDecoder.decode(this, StandardCharsets.UTF_8.toString())
    } catch (e: Exception) {
        this
    }
