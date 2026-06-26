package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.QuranData
import com.example.data.model.Surah
import com.example.ui.theme.*
import com.example.ui.viewmodel.IslamicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranScreen(viewModel: IslamicViewModel) {
    var isSurahTab by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedSurah by remember { mutableStateOf<Surah?>(null) }
    var selectedPara by remember { mutableStateOf<Int?>(null) }
    var showTranslation by remember { mutableStateOf(true) }

    val currentPlayingSurahId by viewModel.currentPlayingSurahId.collectAsState()
    val isAudioPlaying by viewModel.isAudioPlaying.collectAsState()
    val lastReadSurah by viewModel.lastReadSurah.collectAsState()
    val lastReadAyah by viewModel.lastReadAyah.collectAsState()

    if (selectedSurah != null) {
        // Surah Reader Screen
        val surah = selectedSurah!!
        val ayahs = QuranData.ayahs[surah.id] ?: listOf(
            com.example.data.model.Ayah(surah.id, 1, "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", "اللہ کے نام سے شروع جو نہایت مہربان ہمیشہ رحم فرمانے والا ہے"),
            com.example.data.model.Ayah(surah.id, 2, "الْحَمْدُ لِلَّهِ رَبِّ الْعَالَمِينَ", "سب تعریفیں اللہ کے لیے ہیں جو تمام جہانوں کا رب ہے")
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "${surah.nameUrdu} - ${surah.nameEnglish}",
                            style = UrduTextStyle.copy(color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectedSurah = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldAccent)
                        }
                    },
                    actions = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("ترجمہ", color = Color.White, fontSize = 12.sp)
                            Switch(
                                checked = showTranslation,
                                onCheckedChange = { showTranslation = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = GoldAccent, checkedTrackColor = IslamicGreen)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = IslamicGreen)
                )
            },
            bottomBar = {
                // Bottom Audio Player bar
                Surface(
                    tonalElevation = 8.dp,
                    color = IslamicGreenDark,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { viewModel.playQuranAudio(surah.id) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(GoldAccent)
                            ) {
                                Icon(
                                    imageVector = if (currentPlayingSurahId == surah.id && isAudioPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play",
                                    tint = Color.Black
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "تلاوت: شیخ مشاری راشد العفاسی",
                                    style = UrduTextStyle.copy(color = Color.White, fontSize = 14.sp)
                                )
                                Text(
                                    text = if (currentPlayingSurahId == surah.id && isAudioPlaying) "آڈیو چل رہی ہے..." else "آڈیو سننے کے لیے پلے دبائیں",
                                    fontSize = 11.sp,
                                    color = GoldAccent
                                )
                            }
                        }

                        // Download Button representation
                        var isDownloaded by remember { mutableStateOf(false) }
                        IconButton(onClick = { isDownloaded = true }) {
                            Icon(
                                imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                                contentDescription = "Download",
                                tint = if (isDownloaded) GoldAccent else Color.White
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Surah Header Details
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = IslamicGreen.copy(alpha = 0.05f)),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, IslamicGreen.copy(alpha = 0.2f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                                style = ArabicTextStyle.copy(color = IslamicGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "مقامِ نزول: ${if (surah.revelationType == "Meccan") "مکہ مکرمہ" else "مدینہ منورہ"} | آیات: ${surah.totalAyahs}",
                                style = UrduTextStyle.copy(fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            )
                        }
                    }
                }

                items(ayahs) { ayah ->
                    val isBookmarked = lastReadSurah == surah.id && lastReadAyah == ayah.numberInSurah
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { viewModel.bookmarkQuranPage(surah.id, ayah.numberInSurah) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (isBookmarked) GoldAccent.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surface
                        ),
                        shape = RoundedCornerShape(12.dp),
                        border = if (isBookmarked) BorderStroke(1.5.dp, GoldAccent) else null
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(IslamicGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${ayah.numberInSurah}",
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                }

                                IconButton(onClick = { viewModel.bookmarkQuranPage(surah.id, ayah.numberInSurah) }) {
                                    Icon(
                                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                        contentDescription = "Bookmark",
                                        tint = if (isBookmarked) GoldAccent else IslamicGreen
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                text = ayah.textArabic,
                                style = ArabicTextStyle.copy(
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 22.sp,
                                    textAlign = TextAlign.Right
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (showTranslation) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = ayah.textUrdu,
                                    style = UrduTextStyle.copy(
                                        color = IslamicGreenDark,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Right
                                    ),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }
        }
    } else if (selectedPara != null) {
        // Para Navigation
        val para = selectedPara!!
        val surahsInPara = QuranData.surahs.filter { it.paraNumber == para }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "پارہ نمبر $para (Juz $para)",
                            style = UrduTextStyle.copy(color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { selectedPara = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldAccent)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = IslamicGreen)
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(surahsInPara) { surah ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedSurah = surah },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(12.dp)
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
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(IslamicGreen),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${surah.id}",
                                        color = GoldAccent,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(16.dp))
                                Column {
                                    Text(
                                        text = surah.nameUrdu,
                                        style = UrduTextStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    )
                                    Text(
                                        text = surah.nameEnglish,
                                        fontSize = 12.sp,
                                        color = Color.Gray
                                    )
                                }
                            }
                            Text(
                                text = surah.nameArabic,
                                style = ArabicTextStyle.copy(color = IslamicGreen, fontSize = 20.sp)
                            )
                        }
                    }
                }
            }
        }
    } else {
        // Primary Quran Navigation (Surah / Para lists)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search & Tab Segment
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IslamicGreen)
                    .padding(16.dp)
            ) {
                Column {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("سورہ کا نام تلاش کریں...", color = Color.White.copy(alpha = 0.6f)) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = GoldAccent) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = GoldAccent,
                            unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            cursorColor = GoldAccent
                        ),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { isSurahTab = true },
                            colors = ButtonDefaults.buttonColors(containerColor = if (isSurahTab) GoldAccent else Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "سورتیں (Surah)",
                                color = if (isSurahTab) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Button(
                            onClick = { isSurahTab = false },
                            colors = ButtonDefaults.buttonColors(containerColor = if (!isSurahTab) GoldAccent else Color.White.copy(alpha = 0.15f)),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "پارے (Para)",
                                color = if (!isSurahTab) Color.Black else Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // List Content
            if (isSurahTab) {
                val filteredSurahs = QuranData.surahs.filter {
                    it.nameUrdu.contains(searchQuery) || it.nameEnglish.contains(searchQuery, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredSurahs) { surah ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedSurah = surah },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
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
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(IslamicGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${surah.id}",
                                            color = GoldAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = surah.nameUrdu,
                                            style = UrduTextStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                        )
                                        Text(
                                            text = "آیات: ${surah.totalAyahs} | Juz ${surah.paraNumber}",
                                            fontSize = 12.sp,
                                            color = Color.Gray
                                        )
                                    }
                                }
                                Text(
                                    text = surah.nameArabic,
                                    style = ArabicTextStyle.copy(color = IslamicGreen, fontSize = 22.sp)
                                )
                            }
                        }
                    }
                }
            } else {
                // Paras Grid
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val juzList = QuranData.paras
                    items(juzList) { juz ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPara = juz },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            shape = RoundedCornerShape(12.dp)
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
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(IslamicGreen),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$juz",
                                            color = GoldAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = "پارہ نمبر $juz (Juz $juz)",
                                        style = UrduTextStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                    )
                                }
                                Icon(Icons.Default.ChevronRight, contentDescription = null, tint = IslamicGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}
