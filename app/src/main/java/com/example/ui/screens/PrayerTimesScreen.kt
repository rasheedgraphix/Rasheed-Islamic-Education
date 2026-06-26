package com.example.ui.screens

import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.IslamicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesScreen(viewModel: IslamicViewModel) {
    val prayerTimes by viewModel.prayerTimes.collectAsState()
    val selectedFiqh by viewModel.selectedFiqh.collectAsState()

    val fiqhOptions = listOf(
        "Hanafi" to "حنفی (Hanafi)",
        "Shafi'i" to "شافعی (Shafi'i)",
        "Hanbali" to "حنبلی (Hanbali)"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "اوقات نماز (Prayer Times)",
                        style = UrduTextStyle.copy(color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IslamicGreen)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Live Status Banner
            Card(
                colors = CardDefaults.cardColors(containerColor = IslamicGreen.copy(alpha = 0.1f)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, IslamicGreen.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = "Sync",
                        tint = IslamicGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "آپ کے لائیو مقام اور منتخب کردہ فقہ کے مطابق لائیو اوقاتِ نماز۔",
                        style = UrduTextStyle.copy(fontSize = 14.sp, color = IslamicGreen),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Fiqh (School of Thought) Selection Header
            Text(
                text = "فقہی مکتبہ فکر منتخب کریں (Select Fiqh):",
                style = UrduTextStyle.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Right
            )

            // Fiqh Options in a Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                fiqhOptions.forEach { (key, label) ->
                    val isSelected = selectedFiqh == key
                    val cardBg = if (isSelected) IslamicGreen else MaterialTheme.colorScheme.surface
                    val textColor = if (isSelected) GoldAccent else MaterialTheme.colorScheme.onSurface
                    val borderColor = if (isSelected) GoldAccent else Color.Gray.copy(alpha = 0.3f)

                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { viewModel.selectFiqh(key) },
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp, horizontal = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label.substringBefore(" ("),
                                style = UrduTextStyle.copy(
                                    color = textColor,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                ),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // Prayer Times Display Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    val times = prayerTimes
                    if (times != null) {
                        PrayerTimeRow("فجر (Fajr)", times.fajr, Icons.Default.WbTwilight, IslamicGreen)
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        PrayerTimeRow("طلوع آفتاب (Sunrise)", times.sunrise, Icons.Default.LightMode, Color(0xFFFFA500))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        PrayerTimeRow("ظہر (Dhuhr)", times.dhuhr, Icons.Default.WbSunny, GoldAccent)
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        PrayerTimeRow("عصر (Asr)", times.asr, Icons.Default.WbCloudy, Color(0xFFC19A6B))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        PrayerTimeRow("مغرب (Maghrib)", times.maghrib, Icons.Default.NightsStay, Color(0xFF333399))
                        HorizontalDivider(color = Color.LightGray.copy(alpha = 0.5f))
                        PrayerTimeRow("عشاء (Isha)", times.isha, Icons.Default.Bedtime, Color(0xFF111144))
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = IslamicGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerTimeRow(name: String, time: String, icon: ImageVector, iconColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = name,
                style = UrduTextStyle.copy(fontSize = 16.sp, fontWeight = FontWeight.Medium)
            )
        }

        Text(
            text = time,
            style = UrduTextStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = IslamicGreen)
        )
    }
}
