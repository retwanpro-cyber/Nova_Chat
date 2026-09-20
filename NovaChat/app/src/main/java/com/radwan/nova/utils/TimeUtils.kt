package com.radwan.nova.utils

fun formatDisplayTime(raw: String?): String {
    if (raw.isNullOrBlank()) return ""
    val str = raw.trim().replace("\"", "")
    try {
        val millis = str.toLongOrNull()
        if (millis != null && millis > 100000000000L) {
            val sdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            sdf.timeZone = java.util.TimeZone.getDefault()
            return sdf.format(java.util.Date(millis))
        }
        val parsers = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ssXXX",
            "yyyy-MM-dd HH:mm:ss",
            "yyyy-MM-dd HH:mm"
        )
        for (p in parsers) {
            try {
                val sdf = java.text.SimpleDateFormat(p, java.util.Locale.US)
                if (str.contains("+") || str.contains("Z")) {
                    sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
                }
                val d = sdf.parse(str)
                if (d != null) {
                    val outSdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                    outSdf.timeZone = java.util.TimeZone.getDefault()
                    return outSdf.format(d)
                }
            } catch (e: Exception) {}
        }
        val timeMatch = Regex("(\\d{1,2}):(\\d{2})").find(str)
        if (timeMatch != null) {
            val h = timeMatch.groupValues[1].toIntOrNull() ?: 0
            val m = timeMatch.groupValues[2]
            val cal = if (str.contains("+00") || str.contains("Z")) {
                val c = java.util.Calendar.getInstance(java.util.TimeZone.getTimeZone("UTC"))
                c.set(java.util.Calendar.HOUR_OF_DAY, h)
                c.set(java.util.Calendar.MINUTE, m.toIntOrNull() ?: 0)
                val localC = java.util.Calendar.getInstance()
                localC.timeInMillis = c.timeInMillis
                localC
            } else {
                val c = java.util.Calendar.getInstance()
                c.set(java.util.Calendar.HOUR_OF_DAY, h)
                c.set(java.util.Calendar.MINUTE, m.toIntOrNull() ?: 0)
                c
            }
            val outSdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
            return outSdf.format(cal.time)
        }
        if (str.contains("+00:00")) {
            val minuteStr = str.substringBefore("+").trim()
            val m = minuteStr.toIntOrNull()
            if (m != null && m in 0..59) {
                val now = java.util.Calendar.getInstance()
                now.set(java.util.Calendar.MINUTE, m)
                val outSdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
                return outSdf.format(now.time)
            }
        }
    } catch (e: Exception) {}
    val fallbackSdf = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
    return fallbackSdf.format(java.util.Date())
}
