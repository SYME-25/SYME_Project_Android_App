package com.syme.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.syme.domain.model.enumeration.PeriodFilter
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

object TimeUtils {

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentTimestamp = System.currentTimeMillis()

    fun formatDate(timestamp: Long, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        val sdf = SimpleDateFormat(pattern, Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    fun getDate(): String {
        val dateFormat = SimpleDateFormat("yyyy MMM dd", Locale.getDefault())
        return dateFormat.format(Date(currentTimestamp))
    }

    fun formatDate2(ts: Long)     = SimpleDateFormat("MMMM d, yyyy",            Locale.ENGLISH).format(Date(ts))
    fun formatDateTime(ts: Long) = SimpleDateFormat("MMM d, yyyy 'at' HH:mm", Locale.ENGLISH).format(Date(ts))

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatDateForPeriod(
        period: PeriodFilter,
        date: LocalDate
    ): String {
        return when (period) {
            PeriodFilter.DAY ->
                date.format(DateTimeFormatter.ofPattern("dd MMM yyyy"))

            PeriodFilter.MONTH ->
                date.format(DateTimeFormatter.ofPattern("MMMM yyyy"))

            PeriodFilter.YEAR ->
                date.year.toString()
        }
    }

    /**
     * Retourne le timestamp (en millis) du début du jour pour une date donnée.
     * Si aucun paramètre n’est passé, prend la date d’aujourd’hui.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun startOfDayMillis(date: LocalDate = LocalDate.now(), zone: ZoneId = ZoneId.systemDefault()): Long {
        return date.atStartOfDay(zone).toInstant().toEpochMilli()
    }

}