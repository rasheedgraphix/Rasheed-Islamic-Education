package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.utils.PrayerTimeCalculator
import com.example.ui.theme.*
import com.example.ui.viewmodel.IslamicViewModel
import kotlin.math.abs

@Composable
fun HomeScreen(
    viewModel: IslamicViewModel,
    onNavigateToQuranPdf: () -> Unit
) {
    val language by viewModel.language.collectAsState()
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val nextPrayerName by viewModel.nextPrayerName.collectAsState()
    val countdown by viewModel.nextPrayerCountdown.collectAsState()
    val adhanEnabled by viewModel.adhanEnabled.collectAsState()
    val deviceHeading by viewModel.deviceHeading.collectAsState()
    val qiblaDir by viewModel.qiblaDirection.collectAsState()
    val qiblaAligned by viewModel.qiblaAligned.collectAsState()
    val lat by viewModel.userLatitude.collectAsState()
    val lng by viewModel.userLongitude.collectAsState()

    val hijriDate = remember { PrayerTimeCalculator.getHijriDate() }
    var showAllTimes by remember { mutableStateOf(false) }

    // Rotate compass arrow dynamically
    val compassRotation = (qiblaDir - deviceHeading).toFloat()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Header Card with Mosque silhouettes and Crescent Moon
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = IslamicGreen)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    // Custom Mosque Drawing
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val width = size.width
                        val height = size.height

                        // Draw background ambient circles
                        drawCircle(
                            color = GoldAccent.copy(alpha = 0.08f),
                            radius = 280f,
                            center = Offset(width * 0.8f, height * 0.3f)
                        )

                        // Draw Crescent Moon
                        drawArc(
                            color = GoldAccent.copy(alpha = 0.4f),
                            startAngle = -20f,
                            sweepAngle = 100f,
                            useCenter = false,
                            topLeft = Offset(width * 0.75f - 80f, height * 0.2f - 80f),
                            size = androidx.compose.ui.geometry.Size(120f, 120f),
                            style = Stroke(width = 4f)
                        )

                        // Simple minimalist Mosque Dome outline
                        val domePath = androidx.compose.ui.graphics.Path().apply {
                            moveTo(0f, height)
                            lineTo(width * 0.2f, height)
                            quadraticTo(width * 0.25f, height - 80f, width * 0.3f, height - 100f)
                            quadraticTo(width * 0.35f, height - 140f, width * 0.4f, height - 140f)
                            quadraticTo(width * 0.45f, height - 140f, width * 0.5f, height - 100f)
                            quadraticTo(width * 0.55f, height - 80f, width * 0.6f, height)
                            lineTo(width, height)
                        }
                        drawPath(domePath, color = IslamicGreenDark.copy(alpha = 0.5f))
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp),
                        verticalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "رشید اسلامک ایجوکیشن",
                                style = UrduTextStyle.copy(
                                    color = GoldAccent,
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                            IconButton(onClick = { viewModel.toggleAdhan(!adhanEnabled) }) {
                                Icon(
                                    imageVector = if (adhanEnabled) Icons.Default.Notifications else Icons.Default.NotificationsOff,
                                    contentDescription = "Adhan",
                                    tint = GoldAccent
                                )
                            }
                        }

                        Column {
                            Text(
                                text = hijriDate,
                                style = UrduTextStyle.copy(color = Color.White, fontSize = 16.sp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Bottom
                            ) {
                                Column {
                                    Text(
                                        text = "اگلی نماز: $nextPrayerName",
                                        style = UrduTextStyle.copy(color = Color.White.copy(alpha = 0.9f), fontSize = 14.sp)
                                    )
                                    Text(
                                        text = countdown,
                                        style = Typography.displayLarge.copy(color = GoldAccent, fontSize = 32.sp, fontWeight = FontWeight.Bold)
                                    )
                                }
                                Text(
                                    text = "السلام علیکم",
                                    style = ArabicTextStyle.copy(color = GoldAccent, fontSize = 24.sp)
                                )
                            }
                        }
                    }
                }
            }
        }



        // GPS Location Presets Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = IslamicGreen)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "موجودہ مقام (Location Settings)",
                                style = UrduTextStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = String.format("%.4f, %.4f", lat, lng),
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.updateLocation(31.5204, 74.3587) }, // Lahore
                            colors = ButtonDefaults.buttonColors(containerColor = if (lat == 31.5204) IslamicGreen else Color.Gray.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("لاہور", color = if (lat == 31.5204) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                        Button(
                            onClick = { viewModel.updateLocation(24.8607, 67.0011) }, // Karachi
                            colors = ButtonDefaults.buttonColors(containerColor = if (lat == 24.8607) IslamicGreen else Color.Gray.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("کراچی", color = if (lat == 24.8607) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                        Button(
                            onClick = { viewModel.updateLocation(51.5074, -0.1278) }, // London
                            colors = ButtonDefaults.buttonColors(containerColor = if (lat == 51.5074) IslamicGreen else Color.Gray.copy(alpha = 0.3f)),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("لندن", color = if (lat == 51.5074) Color.White else MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }

        // Qibla Compass Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "قبلہ رخ کمپاس (Qibla Direction)",
                        style = UrduTextStyle.copy(color = IslamicGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "مکہ مکرمہ کی سمت: ${qiblaDir.toInt()}° (بائیں/دائیں گھومیں)",
                        style = UrduTextStyle.copy(fontSize = 12.sp, color = Color.Gray),
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Rotating Compass Circle Canvas
                    Box(
                        modifier = Modifier
                            .size(180.dp)
                            .clip(CircleShape)
                            .background(IslamicGreenDark.copy(alpha = 0.1f))
                            .border(3.dp, if (qiblaAligned) GoldAccent else IslamicGreen, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        // Drawing custom compass dial
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val center = Offset(size.width / 2, size.height / 2)
                            val radius = size.width / 2

                            // Compass Dial ticks
                            for (angle in 0 until 360 step 30) {
                                rotate(angle.toFloat(), center) {
                                    drawLine(
                                        color = IslamicGreen.copy(alpha = 0.5f),
                                        start = Offset(center.x, 15f),
                                        end = Offset(center.x, 30f),
                                        strokeWidth = 3f
                                    )
                                }
                            }
                        }

                        // Rotating Arrow representing Qibla Direction
                        Icon(
                            imageVector = Icons.Default.CompassCalibration,
                            contentDescription = "Qibla Pointer",
                            tint = if (qiblaAligned) GoldAccent else IslamicGreen,
                            modifier = Modifier
                                .size(90.dp)
                                .rotate(compassRotation)
                        )

                        // Center Aligned indicator text
                        if (qiblaAligned) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 110.dp)
                                    .background(GoldAccent, RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "الائنڈ (Aligned!)",
                                    style = UrduTextStyle.copy(color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = if (qiblaAligned) "درست سمت! کمپاس وائبریٹ ہو رہا ہے۔" else "کمپاس کو گھمائیں تاکہ تیر کا رخ اوپر کی سمت آ جائے",
                        style = UrduTextStyle.copy(
                            color = if (qiblaAligned) GoldAccent else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 12.sp
                        ),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // Daily Prayer Clock Timings List
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 4.dp, end = 4.dp, top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (showAllTimes) "نمازوں کے اوقات (Prayer Times)" else "موجودہ نماز کا وقت (Current Prayer)",
                    style = UrduTextStyle.copy(color = IslamicGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                )
                TextButton(
                    onClick = { showAllTimes = !showAllTimes }
                ) {
                    Text(
                        text = if (showAllTimes) "صرف موجودہ" else "تمام اوقات دیکھیں",
                        style = UrduTextStyle.copy(color = GoldAccent, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    )
                }
            }
        }

        prayerTimes?.let { times ->
            val list = listOf(
                "فجر" to times.fajr,
                "ظہر" to times.dhuhr,
                "عصر" to times.asr,
                "مغرب" to times.maghrib,
                "عشاء" to times.isha
            )

            val now = java.util.Calendar.getInstance()
            val nowHour = now.get(java.util.Calendar.HOUR_OF_DAY)
            val nowMinute = now.get(java.util.Calendar.MINUTE)
            val nowInSec = nowHour * 3600 + nowMinute * 60

            fun getSec(tStr: String): Int {
                return try {
                    val parts = tStr.split(":")
                    parts[0].trim().toInt() * 3600 + parts[1].trim().toInt() * 60
                } catch (e: Exception) {
                    0
                }
            }

            val fajrSec = getSec(times.fajr)
            val sunriseSec = getSec(times.sunrise)
            val dhuhrSec = getSec(times.dhuhr)
            val asrSec = getSec(times.asr)
            val maghribSec = getSec(times.maghrib)
            val ishaSec = getSec(times.isha)

            val currentPrayerUrduMatch = when {
                nowInSec >= fajrSec && nowInSec < dhuhrSec -> "فجر"
                nowInSec >= dhuhrSec && nowInSec < asrSec -> "ظہر"
                nowInSec >= asrSec && nowInSec < maghribSec -> "عصر"
                nowInSec >= maghribSec && nowInSec < ishaSec -> "مغرب"
                else -> "عشاء"
            }

            val filteredList = if (showAllTimes) {
                list
            } else {
                list.filter { it.first == currentPrayerUrduMatch }
            }

            items(filteredList) { (name, time) ->
                val isNext = nextPrayerName == name

                val prayerSec = try {
                    val parts = time.split(":")
                    val h = parts[0].toInt()
                    val m = parts[1].toInt()
                    h * 3600 + m * 60
                } catch (e: Exception) {
                    0
                }

                val isPast = !isNext && prayerSec < nowInSec

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isNext) IslamicGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                    ),
                    border = if (isNext) BorderStroke(1.5.dp, GoldAccent) else null
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(if (isNext) GoldAccent else if (isPast) IslamicGreen else Color.Gray.copy(alpha = 0.4f))
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = name,
                                style = UrduTextStyle.copy(
                                    fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 18.sp,
                                    color = if (isNext) IslamicGreen else MaterialTheme.colorScheme.onSurface
                                )
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = formatTo12Hour(time),
                                style = Typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = if (isNext) GoldAccent else MaterialTheme.colorScheme.onSurface
                                )
                            )
                            Spacer(modifier = Modifier.width(20.dp))
                            if (isNext) {
                                Text("→", style = UrduTextStyle.copy(color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold))
                            } else if (isPast) {
                                Text("✓", style = UrduTextStyle.copy(color = IslamicGreen, fontSize = 20.sp, fontWeight = FontWeight.Bold))
                            } else {
                                Spacer(modifier = Modifier.width(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatTo12Hour(time24: String): String {
    try {
        val parts = time24.split(":")
        val h24 = parts[0].toInt()
        val m = parts[1].toInt()
        val ampm = if (h24 >= 12) "PM" else "AM"
        val h12 = when {
            h24 == 0 -> 12
            h24 > 12 -> h24 - 12
            else -> h24
        }
        return String.format("%02d:%02d %s", h12, m, ampm)
    } catch (e: Exception) {
        return time24
    }
}
