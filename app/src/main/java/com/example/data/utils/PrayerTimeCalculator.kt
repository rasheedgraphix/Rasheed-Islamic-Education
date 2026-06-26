package com.example.data.utils

import java.util.*
import kotlin.math.*

object PrayerTimeCalculator {
    // Coordinates of Makkah
    private const val MAKKAH_LAT = 21.4225
    private const val MAKKAH_LNG = 39.8262

    data class PrayerTimes(
        val fajr: String,
        val sunrise: String,
        val dhuhr: String,
        val asr: String,
        val maghrib: String,
        val isha: String
    )

    // Calculate Qibla Direction in degrees from North
    fun calculateQiblaDirection(userLat: Double, userLng: Double): Double {
        val userLatRad = Math.toRadians(userLat)
        val userLngRad = Math.toRadians(userLng)
        val makkahLatRad = Math.toRadians(MAKKAH_LAT)
        val makkahLngRad = Math.toRadians(MAKKAH_LNG)

        val deltaLng = makkahLngRad - userLngRad

        val y = sin(deltaLng)
        val x = cos(userLatRad) * tan(makkahLatRad) - sin(userLatRad) * cos(deltaLng)

        var qiblaRad = atan2(y, x)
        var qiblaDeg = Math.toDegrees(qiblaRad)
        qiblaDeg = (qiblaDeg + 360) % 360
        return qiblaDeg
    }

    // Simplistic offline Prayer Time estimation (Karachi/Standard Method)
    fun calculatePrayerTimes(userLat: Double, userLng: Double, fiqhShadowRatio: Int = 2, timezoneOffset: Double = 5.0): PrayerTimes {
        // Safe defaults for Lahore/Karachi coordinates if GPS is un-fetched or zero
        val lat = if (userLat == 0.0) 31.5204 else userLat
        val lng = if (userLng == 0.0) 74.3587 else userLng
        val tz = timezoneOffset

        val calendar = Calendar.getInstance()
        val dayOfYear = calendar.get(Calendar.DAY_OF_YEAR)

        // Solar Declination calculation
        val d = dayOfYear.toDouble()
        val g = 357.529 + 0.98560028 * d
        val q = 280.459 + 0.98564736 * d
        val l = q + 1.915 * sin(Math.toRadians(g)) + 0.020 * sin(Math.toRadians(2 * g))
        val r = Math.toRadians(l)
        val obliq = 23.439 - 0.00000036 * d
        val dec = asin(sin(Math.toRadians(obliq)) * sin(r)) // declination

        // Equation of Time
        val e = 4.0 * Math.toDegrees(dec) - 4.0 * tan(Math.toRadians(obliq / 2.0)) // simple estimate in minutes
        val eqt = 9.87 * sin(2.0 * Math.toRadians(l)) - 7.53 * cos(Math.toRadians(g)) - 1.5 * sin(Math.toRadians(g))

        // Mid Day (Dhuhr)
        val noon = 12.0 + tz - lng / 15.0 - eqt / 60.0

        // Fajr (18 degrees twilight)
        val fajrHourAngle = calculateHourAngle(lat, dec, -18.0)
        val fajrTime = noon - fajrHourAngle / 15.0

        // Sunrise
        val sunriseHourAngle = calculateHourAngle(lat, dec, -0.833)
        val sunriseTime = noon - sunriseHourAngle / 15.0

        // Sunset (Maghrib)
        val sunsetTime = noon + sunriseHourAngle / 15.0

        // Asr (Hanafi shadow ratio = 2, Shafi'i/Hanbali shadow ratio = 1)
        val asrAngle = calculateAsrAngle(lat, dec, fiqhShadowRatio)
        val asrHourAngle = calculateHourAngle(lat, dec, asrAngle)
        val asrTime = noon + asrHourAngle / 15.0

        // Isha (18 degrees twilight)
        val ishaHourAngle = calculateHourAngle(lat, dec, -18.0)
        val ishaTime = noon + ishaHourAngle / 15.0

        return PrayerTimes(
            fajr = formatTime(fajrTime),
            sunrise = formatTime(sunriseTime),
            dhuhr = formatTime(noon),
            asr = formatTime(asrTime),
            maghrib = formatTime(sunsetTime),
            isha = formatTime(ishaTime)
        )
    }

    private fun calculateHourAngle(lat: Double, decRad: Double, angle: Double): Double {
        val latRad = Math.toRadians(lat)
        val angleRad = Math.toRadians(angle)
        val cosH = (sin(angleRad) - sin(latRad) * sin(decRad)) / (cos(latRad) * cos(decRad))
        val clampedCosH = max(-1.0, min(1.0, cosH))
        return Math.toDegrees(acos(clampedCosH))
    }

    private fun calculateAsrAngle(lat: Double, decRad: Double, shadowFactor: Int): Double {
        val latRad = Math.toRadians(lat)
        val dec = decRad
        val diff = abs(latRad - dec)
        val cotAsr = shadowFactor.toDouble() + tan(diff)
        val asrRad = atan(1.0 / cotAsr)
        return Math.toDegrees(asrRad)
    }

    private fun formatTime(hours: Double): String {
        var h = hours
        if (h.isNaN()) return "12:00"
        h = (h + 24.0) % 24.0
        val m = round((h - floor(h)) * 60.0).toInt()
        val finalH = if (m == 60) (floor(h).toInt() + 1) % 24 else floor(h).toInt() % 24
        val finalM = if (m == 60) 0 else m
        return String.format("%02d:%02d", finalH, finalM)
    }

    // Get current Hijri Date string
    fun getHijriDate(): String {
        // Simplistic estimate of Islamic calendar based on 2026 dates
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        // 2026: Muharram 1st is approximately July 16, 2026
        // Let's calculate a robust, dynamic Hijri date
        val jd = getJulianDate(year, month + 1, day)
        val l = jd - 1948440 + 10632
        val n = floor((l - 1) / 10631.0).toInt()
        val lRest = l - 10631 * n + 354
        val j = (floor((10985 - lRest) / 5316.0) * floor((50 * lRest) / 17719.0) +
                floor(lRest / 5670.0) * floor((43 * lRest) / 15238.0)).toInt()
        val lRest2 = lRest - floor((30 - j) / 15.0) * floor((17719 * j) / 50.0) -
                floor(j / 16.0) * floor((15238 * j) / 43.0) + 29
        val m = floor((24 * lRest2) / 709.0).toInt()
        val d = lRest2 - floor((709 * m) / 24.0).toInt()
        val y = 30 * n + j - 30

        val hijriMonths = listOf(
            "محرم", "صفر", "ربیع الاول", "ربیع الثانی",
            "جمادی الاول", "جمادی الثانی", "رجب", "شعبان",
            "رمضان", "شوال", "ذوالقعدہ", "ذوالحجہ"
        )

        val monthName = hijriMonths.getOrElse(m - 1) { "رمضان" }
        return "$d $monthName $y"
    }

    private fun getJulianDate(year: Int, month: Int, day: Int): Int {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0).toInt()
        val b = 2 - a + floor(a / 4.0).toInt()
        return floor(365.25 * (y + 4716)).toInt() + floor(30.6001 * (m + 1)).toInt() + day + b - 1524
    }
}
