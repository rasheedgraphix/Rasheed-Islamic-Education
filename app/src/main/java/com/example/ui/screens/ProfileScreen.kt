package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*
import com.example.ui.viewmodel.IslamicViewModel
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(viewModel: IslamicViewModel, onNavigateToReader: (String) -> Unit) {
    val context = LocalContext.current

    val email by viewModel.currentUserEmail.collectAsState()
    val name by viewModel.currentUserName.collectAsState()

    val quizScores by viewModel.activeQuizScores.collectAsState()
    val tasbeehHistory by viewModel.repository.getTasbeehHistory().collectAsState(initial = emptyList())

    // Calculations for Stats
    val totalTasbeeh = tasbeehHistory.sumOf { it.count }
    val averageScore = if (quizScores.isNotEmpty()) {
        quizScores.map { (it.score.toFloat() / it.total) * 100 }.average()
    } else 0.0

    // List downloaded files locally
    val downloadedBooksList = remember { mutableStateListOf<File>() }

    LaunchedEffect(key1 = true) {
        val downloadFolder = File(context.filesDir, "downloads")
        if (downloadFolder.exists()) {
            downloadedBooksList.clear()
            downloadedBooksList.addAll(downloadFolder.listFiles()?.toList() ?: emptyList())
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Upper Profile Info
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(IslamicGreen)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Beautiful initial avatar
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(GoldAccent)
                        .border(3.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 32.sp
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = name,
                    style = UrduTextStyle.copy(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                )

                Text(
                    text = email,
                    color = GoldLight,
                    fontSize = 12.sp
                )
            }
        }

        // Stats Content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
                // Stats Card Grid
                item {
                    Text(
                        text = "تفصیلی رپورٹ (Performance Report)",
                        style = UrduTextStyle.copy(color = IslamicGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.FilterFrames, contentDescription = null, tint = IslamicGreen)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("کل تسبیح", style = UrduTextStyle.copy(fontSize = 12.sp, color = Color.Gray))
                                Text("$totalTasbeeh بار", fontWeight = FontWeight.Bold, color = IslamicGreen, fontSize = 18.sp)
                            }
                        }

                        Card(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                        ) {
                            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Quiz, contentDescription = null, tint = GoldAccent)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("اوسط سکور", style = UrduTextStyle.copy(fontSize = 12.sp, color = Color.Gray))
                                Text(String.format("%.1f%%", averageScore), fontWeight = FontWeight.Bold, color = GoldDark, fontSize = 18.sp)
                            }
                        }
                    }
                }

                // Download Manager section
                item {
                    Text(
                        text = "ڈاؤن لوڈ کردہ کتابیں (Offline Downloads)",
                        style = UrduTextStyle.copy(color = IslamicGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (downloadedBooksList.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                "کوئی کتاب ڈاؤن لوڈ نہیں کی گئی۔",
                                style = UrduTextStyle.copy(fontSize = 13.sp, color = Color.Gray),
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(24.dp)
                            )
                        }
                    }
                } else {
                    items(downloadedBooksList.toList()) { file ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color.Red)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = file.name.substringBefore(".pdf"),
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Text(
                                            text = String.format("%.2f MB", file.length().toFloat() / (1024 * 1024)),
                                            fontSize = 11.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }

                                IconButton(
                                    onClick = {
                                        file.delete()
                                        downloadedBooksList.remove(file)
                                    }
                                ) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete File", tint = Color.Red)
                                }
                            }
                        }
                    }
                }

                // Logout Button
                item {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { viewModel.performLogout() },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = "لاگ آؤٹ (Sign Out)",
                            style = UrduTextStyle.copy(color = Color.White, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
