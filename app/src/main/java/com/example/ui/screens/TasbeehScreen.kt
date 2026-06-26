package com.example.ui.screens

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.IslamicViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TasbeehScreen(viewModel: IslamicViewModel) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val vibrator = context.getSystemService(Vibrator::class.java)

    // Dhikr items
    val dhikrPresets = listOf(
        "سُبْحَانَ اللَّهِ" to "SubhanAllah (33x)",
        "الْحَمْدُ لِلَّهِ" to "Alhamdulillah (33x)",
        "اللَّهُ أَكْبَرُ" to "Allahu Akbar (34x)",
        "أَسْتَغْفِرُ اللَّهَ" to "Astaghfirullah",
        "اللَّهُمَّ صَلِّ عَلَىٰ مُحَمَّدٍ" to "Darood Shareef"
    )

    var selectedDhikrIndex by remember { mutableStateOf(0) }
    var currentCount by remember { mutableStateOf(0) }
    var totalSessionCount by remember { mutableStateOf(0) }

    val currentDhikr = dhikrPresets[selectedDhikrIndex]
    val limit = when (selectedDhikrIndex) {
        0 -> 33
        1 -> 33
        2 -> 34
        else -> 9999 // Unlimited for custom
    }

    val historyList by viewModel.repository.getTasbeehHistory().collectAsState(initial = emptyList())

    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val currentDate = dateFormat.format(Date())

    // Tap scale animation
    var isTapped by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isTapped) 0.92f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        finishedListener = { isTapped = false },
        label = "Scale"
    )

    fun handleTap() {
        isTapped = true
        currentCount++
        totalSessionCount++

        try {
            // Vibrate when tapped
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }

            // Limit haptic alert (Vibrate twice when completed target)
            if (currentCount == limit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 100, 100, 150), -1))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(200)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("TasbeehScreen", "Vibration failed: ${e.message}")
        }

        // Save progress to offline-first cache
        coroutineScope.launch {
            viewModel.repository.saveTasbeehCount(
                dhikrKey = currentDhikr.second,
                count = 1,
                date = currentDate
            )
        }
    }

    fun handleReset() {
        currentCount = 0
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Selector Header Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "ذکر منتخب کریں (Select Dhikr)",
                        style = UrduTextStyle.copy(color = IslamicGreen, fontSize = 16.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ScrollableTabRow(
                            selectedTabIndex = selectedDhikrIndex,
                            edgePadding = 0.dp,
                            containerColor = Color.Transparent,
                            indicator = {}
                        ) {
                            dhikrPresets.forEachIndexed { index, (arabic, translation) ->
                                Tab(
                                    selected = selectedDhikrIndex == index,
                                    onClick = {
                                        selectedDhikrIndex = index
                                        currentCount = 0
                                    },
                                    text = {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(arabic, style = ArabicTextStyle.copy(fontSize = 14.sp))
                                            Text(translation, fontSize = 10.sp)
                                        }
                                    },
                                    modifier = Modifier
                                        .padding(horizontal = 4.dp, vertical = 2.dp)
                                        .background(
                                            color = if (selectedDhikrIndex == index) IslamicGreen.copy(alpha = 0.15f) else Color.Transparent,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (selectedDhikrIndex == index) IslamicGreen else Color.LightGray.copy(alpha = 0.5f),
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tasbeeh Main Counter Card
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = currentDhikr.first,
                        style = ArabicTextStyle.copy(color = IslamicGreen, fontSize = 32.sp, fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = currentDhikr.second,
                        fontSize = 16.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Large Circular Tapping Button
                    Box(
                        modifier = Modifier
                            .size(200.dp)
                            .scale(scale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(IslamicGreenLight, IslamicGreen)
                                )
                            )
                            .border(6.dp, GoldAccent, CircleShape)
                            .clickable { handleTap() },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$currentCount",
                                style = Typography.displayLarge.copy(color = GoldAccent, fontSize = 54.sp, fontWeight = FontWeight.Bold)
                            )
                            if (limit != 9999) {
                                Text(
                                    text = "/ $limit",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { handleReset() },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color.Red.copy(alpha = 0.1f))
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Reset", tint = Color.Red)
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "کل ذکر (Total Session)",
                                style = UrduTextStyle.copy(fontSize = 12.sp, color = Color.Gray)
                            )
                            Text(
                                text = "$totalSessionCount",
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreen,
                                fontSize = 18.sp
                            )
                        }

                        // Sync indicator
                        Icon(
                            imageVector = Icons.Default.CloudDone,
                            contentDescription = "Synced Offline",
                            tint = IslamicGreen,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }

        // Daily Tasbeeh History Logs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "تاریخی ریکارڈ (Daily History Log)",
                    style = UrduTextStyle.copy(color = IslamicGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(start = 4.dp)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("آٹو کلاؤڈ سنک", style = UrduTextStyle.copy(fontSize = 11.sp, color = Color.Gray))
                }
            }
        }

        if (historyList.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = "ابھی تک کوئی ریکارڈ موجود نہیں ہے۔ تسبیح کا آغاز کریں!",
                        style = UrduTextStyle.copy(fontSize = 13.sp, color = Color.Gray),
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp)
                    )
                }
            }
        } else {
            // Group by date to show sum
            val grouped = historyList.groupBy { it.date }
            items(grouped.entries.toList().take(5)) { (date, entries) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = date,
                                fontWeight = FontWeight.Bold,
                                color = IslamicGreen
                            )
                            Text(
                                text = "مجموعہ: ${entries.sumOf { it.count }} بار",
                                style = UrduTextStyle.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                            )
                        }
                        Divider(modifier = Modifier.padding(vertical = 8.dp), color = Color.LightGray.copy(alpha = 0.5f))
                        entries.forEach { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(entry.dhikrKey, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
                                Text("${entry.count} counts", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}
