package com.songs.common.extension

import java.util.Locale

private const val MINUTES_AND_SECONDS = "%02d:%02d"

fun Long.formatTime(): String {
    val totalSeconds = this / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), MINUTES_AND_SECONDS, minutes, seconds)
}
