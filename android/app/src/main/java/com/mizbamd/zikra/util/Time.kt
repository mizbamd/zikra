package com.mizbamd.zikra.util

import com.mizbamd.zikra.data.local.ResetAt
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.acos
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.tan

/** Makkah — used when location is denied or unavailable. */
val SAMPLE_LAT = 21.4225
val SAMPLE_LON = 39.8262

data class DisplayDates(
    val gregorian: String,
    val hijri: String,
    val locationLabelKey: LocationLabel,
)

enum class LocationLabel { REAL, SAMPLE }

object ZikraTime {
    private val gregorianFmt: DateTimeFormatter =
        DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.ENGLISH)
    private val hijriFmt: DateTimeFormatter =
        DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.ENGLISH)
            .withChronology(HijrahChronology.INSTANCE)

    fun todayKey(
        resetAt: ResetAt,
        lat: Double,
        lon: Double,
        zone: ZoneId = ZoneId.systemDefault(),
        now: ZonedDateTime = ZonedDateTime.now(zone),
    ): String {
        val localDate = now.toLocalDate()
        if (resetAt == ResetAt.MIDNIGHT) return localDate.toString()
        val fajr = solarEvent(lat, lon, localDate, zone, zenithDeg = 108.0, rising = true)
            ?: LocalTime.of(5, 0)
        return if (now.toLocalTime().isBefore(fajr)) localDate.minusDays(1).toString() else localDate.toString()
    }

    fun displayDates(
        lat: Double,
        lon: Double,
        hasRealLocation: Boolean,
        zone: ZoneId = ZoneId.systemDefault(),
        now: ZonedDateTime = ZonedDateTime.now(zone),
        locale: Locale = Locale.getDefault(),
    ): DisplayDates {
        val gDate = now.toLocalDate()
        val sunset = solarEvent(lat, lon, gDate, zone, zenithDeg = 90.833, rising = false)
        val hijriCivil = if (sunset != null && !now.toLocalTime().isBefore(sunset)) {
            gDate.plusDays(1)
        } else {
            gDate
        }
        val hijrah = HijrahDate.from(hijriCivil)
        val gFmt = DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", locale)
        val hFmt = DateTimeFormatter.ofPattern("d MMMM yyyy", locale).withChronology(HijrahChronology.INSTANCE)
        return DisplayDates(
            gregorian = gDate.format(gFmt),
            hijri = hijrah.format(hFmt),
            locationLabelKey = if (hasRealLocation) LocationLabel.REAL else LocationLabel.SAMPLE,
        )
    }

    fun nowIso(): String = Instant.now().toString()

    /**
     * NOAA-style sunrise/sunset. zenith 90.833 = official sunset;
     * 108 = 18° below horizon (astronomical / common Fajr approximation).
     */
    fun solarEvent(
        lat: Double,
        lon: Double,
        date: LocalDate,
        zone: ZoneId,
        zenithDeg: Double,
        rising: Boolean,
    ): LocalTime? {
        val day = date.dayOfYear
        val lngHour = lon / 15.0
        val t = day + ((if (rising) 6 else 18) - lngHour) / 24.0
        val m = (0.9856 * t) - 3.289
        var l = m + (1.916 * sin(Math.toRadians(m))) + (0.020 * sin(Math.toRadians(2 * m))) + 282.634
        l = (l + 360) % 360
        var ra = Math.toDegrees(kotlin.math.atan(0.91764 * tan(Math.toRadians(l))))
        ra = (ra + 360) % 360
        val lQuad = kotlin.math.floor(l / 90.0) * 90.0
        val raQuad = kotlin.math.floor(ra / 90.0) * 90.0
        ra = (ra + (lQuad - raQuad)) / 15.0
        val sinDec = 0.39782 * sin(Math.toRadians(l))
        val cosDec = cos(asin(sinDec))
        val cosH = (cos(Math.toRadians(zenithDeg)) - sinDec * sin(Math.toRadians(lat))) /
            (cosDec * cos(Math.toRadians(lat)))
        if (cosH > 1 || cosH < -1) return null
        var h = if (rising) {
            360 - Math.toDegrees(acos(cosH))
        } else {
            Math.toDegrees(acos(cosH))
        }
        h /= 15.0
        val localT = h + ra - (0.06571 * t) - 6.622
        val utcHours = (localT - lngHour + 24) % 24
        val instant = date.atTime(toLocalTime(utcHours)).atZone(ZoneId.of("UTC")).toInstant()
        return LocalDateTime.ofInstant(instant, zone).toLocalTime()
    }

    private fun toLocalTime(hours: Double): LocalTime {
        val h = hours.toInt().coerceIn(0, 23)
        val m = ((hours - hours.toInt()) * 60).toInt().coerceIn(0, 59)
        val s = ((((hours - hours.toInt()) * 60) - m) * 60).toInt().coerceIn(0, 59)
        return LocalTime.of(h, m, s)
    }
}
