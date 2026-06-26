package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.List
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
import com.example.data.database.LocalBook
import com.example.ui.theme.*
import com.example.ui.viewmodel.IslamicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(viewModel: IslamicViewModel, bookId: String, onNavigateBack: () -> Unit) {
    val bookState = viewModel.repository.getBookByIdFlow(bookId).collectAsState(initial = null)
    val book = bookState.value ?: return

    val questions by viewModel.currentBookQuestions.collectAsState()

    var activeQuestionIndex by remember { mutableStateOf(0) }
    var selectedAnswerIndex by remember { mutableStateOf<Int?>(null) }
    var score by remember { mutableStateOf(0) }
    var isQuizCompleted by remember { mutableStateOf(false) }

    // Track wrong answers for review: Pair(QuestionIndex, SelectedIndex)
    val wrongAnswersList = remember { mutableStateListOf<Pair<Int, Int>>() }

    // Load or generate questions on entrance
    LaunchedEffect(bookId) {
        viewModel.generateQuestionsForBook(book)
    }

    if (questions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = GoldAccent)
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ٹیسٹ دیں - ${book.nameUrdu}", style = UrduTextStyle.copy(color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IslamicGreen)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            if (!isQuizCompleted) {
                // Active Quiz Progress
                val activeQuestion = questions[activeQuestionIndex]

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Progress Header
                    Column {
                        LinearProgressIndicator(
                            progress = (activeQuestionIndex + 1).toFloat() / questions.size,
                            color = GoldAccent,
                            trackColor = Color.LightGray.copy(alpha = 0.3f),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "سوال نمبر: ${activeQuestionIndex + 1} / ${questions.size}",
                                style = UrduTextStyle.copy(fontSize = 14.sp, color = Color.Gray)
                            )
                            Text(
                                text = "مجموعی سکور: $score",
                                style = UrduTextStyle.copy(fontSize = 14.sp, color = IslamicGreen, fontWeight = FontWeight.Bold)
                            )
                        }
                    }

                    // Question Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = activeQuestion.question,
                                style = UrduTextStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    // Options list
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        activeQuestion.options.forEachIndexed { idx, option ->
                            val isSelected = selectedAnswerIndex == idx
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedAnswerIndex = idx },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) IslamicGreen.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
                                ),
                                shape = RoundedCornerShape(12.dp),
                                border = if (isSelected) BorderStroke(1.5.dp, IslamicGreen) else BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) IslamicGreen else Color.LightGray.copy(alpha = 0.4f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = when (idx) {
                                                0 -> "الف"
                                                1 -> "ب"
                                                2 -> "ج"
                                                else -> "د"
                                            },
                                            color = if (isSelected) Color.White else Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = option,
                                        style = UrduTextStyle.copy(fontSize = 15.sp)
                                    )
                                }
                            }
                        }
                    }

                    // Bottom Navigation Button
                    Button(
                        onClick = {
                            if (selectedAnswerIndex != null) {
                                val selected = selectedAnswerIndex!!
                                if (selected == activeQuestion.correctIndex) {
                                    score++
                                } else {
                                    wrongAnswersList.add(activeQuestionIndex to selected)
                                }

                                if (activeQuestionIndex < questions.size - 1) {
                                    activeQuestionIndex++
                                    selectedAnswerIndex = null
                                } else {
                                    // Submit score
                                    viewModel.submitQuizScore(book, score, questions.size)
                                    isQuizCompleted = true
                                }
                            }
                        },
                        enabled = selectedAnswerIndex != null,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                    ) {
                        Text(
                            text = if (activeQuestionIndex == questions.size - 1) "ٹیسٹ مکمل کریں" else "اگلا سوال",
                            style = UrduTextStyle.copy(color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        )
                    }
                }
            } else {
                // Quiz Completed Screen
                val percentage = (score.toFloat() / questions.size) * 100

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = IslamicGreen),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(72.dp))
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "ٹیسٹ مکمل ہو گیا!",
                                    style = UrduTextStyle.copy(color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "آپ کا سکور: $score / ${questions.size}",
                                    style = UrduTextStyle.copy(color = GoldAccent, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = String.format("فیصد: %.1f%%", percentage),
                                    style = UrduTextStyle.copy(color = Color.White.copy(alpha = 0.8f), fontSize = 16.sp)
                                )
                            }
                        }
                    }

                    if (wrongAnswersList.isNotEmpty()) {
                        item {
                            Text(
                                text = "غلط جوابات کی درستگی اور حوالہ جات (Review & References):",
                                style = UrduTextStyle.copy(color = Color.Red, fontSize = 14.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        itemsIndexed(wrongAnswersList.toList()) { _, (qIdx, selectedIdx) ->
                            val q = questions[qIdx]
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, Color.Red.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "سوال: ${q.question}",
                                        style = UrduTextStyle.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "آپ کا جواب: ${q.options[selectedIdx]} ❌",
                                        style = UrduTextStyle.copy(color = Color.Red, fontSize = 13.sp)
                                    )
                                    Text(
                                        text = "صحیح جواب: ${q.options[q.correctIndex]} \uD83D\uDDF8",
                                        style = UrduTextStyle.copy(color = IslamicGreen, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Box(
                                        modifier = Modifier
                                            .background(GoldAccent.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = "حوالہ: ${q.pageReference}",
                                            style = UrduTextStyle.copy(color = GoldDark, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Button(
                            onClick = onNavigateBack,
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "واپس جائیں",
                                style = UrduTextStyle.copy(color = Color.White, fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
            }
        }
    }
}
