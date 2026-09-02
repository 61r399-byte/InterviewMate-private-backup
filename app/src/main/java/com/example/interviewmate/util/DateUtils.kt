package com.example.interviewmate.util

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val DisplayDatePattern = "yyyy-MM-dd"

private fun formatter(): SimpleDateFormat =
    SimpleDateFormat(DisplayDatePattern, Locale.getDefault()).apply {
        isLenient = false
    }

fun formatDate(timestamp: Long): String = formatter().format(Date(timestamp))

fun todayText(): String = formatDate(System.currentTimeMillis())

fun parseDateOrNull(value: String): Long? {
    return try {
        formatter().parse(value.trim())?.time
    } catch (_: ParseException) {
        null
    }
}
