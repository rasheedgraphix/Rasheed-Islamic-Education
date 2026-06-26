package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ArabicTextStyle
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.UrduTextStyle
import com.example.ui.viewmodel.IslamicViewModel
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "Alpha"
    )

    val scaleAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1.1f else 0.8f,
        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
        label = "Scale"
    )

    LaunchedEffect(key1 = true) {
        startAnimation = true
        delay(2500)
        onAnimationFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(IslamicGreen, Color(0xFF003D21))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Aesthetic Background Crescent and Mosque silhouette drawn on Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = GoldAccent.copy(alpha = 0.05f),
                radius = 400f,
                center = Offset(size.width / 2, size.height / 2)
            )
            // Beautiful Crescent Moon Line
            drawArc(
                color = GoldAccent.copy(alpha = 0.15f),
                startAngle = -45f,
                sweepAngle = 130f,
                useCenter = false,
                topLeft = Offset(size.width / 2 - 150f, size.height / 2 - 350f),
                size = androidx.compose.ui.geometry.Size(300f, 300f),
                style = Stroke(width = 8f)
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                style = ArabicTextStyle.copy(
                    color = GoldAccent,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                ),
                modifier = Modifier.animateContentSize()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "رشید اسلامک ایجوکیشن",
                style = UrduTextStyle.copy(
                    color = Color.White,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Rasheed Islamic Education",
                color = GoldAccent,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.5.sp
            )

            Spacer(modifier = Modifier.height(48.dp))

            CircularProgressIndicator(
                color = GoldAccent,
                strokeWidth = 3.dp,
                modifier = Modifier.size(36.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(viewModel: IslamicViewModel, onAuthSuccess: () -> Unit) {
    var email by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isSignUp by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(IslamicGreen, Color(0xFF00331C))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .background(Color.White.copy(alpha = 0.08f), shape = RoundedCornerShape(24.dp))
                .clip(RoundedCornerShape(24.dp))
                .padding(24.dp)
        ) {
            Text(
                text = "السلام علیکم",
                style = ArabicTextStyle.copy(
                    color = GoldAccent,
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Bold
                )
            )

            Text(
                text = if (isSignUp) "نیا اکاؤنٹ بنائیں" else "لاگ ان کریں",
                style = UrduTextStyle.copy(
                    color = Color.White,
                    fontSize = 20.sp
                ),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            if (isSignUp) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("نام (Name)", color = GoldAccent) },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null, tint = GoldAccent) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldAccent,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                        focusedLabelColor = GoldAccent,
                        cursorColor = GoldAccent
                    ),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("ای میل (Email)", color = GoldAccent) },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = GoldAccent) },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedLabelColor = GoldAccent,
                    cursorColor = GoldAccent
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("پاس ورڈ (Password)", color = GoldAccent) },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = GoldAccent) },
                visualTransformation = PasswordVisualTransformation(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = GoldAccent,
                    unfocusedBorderColor = Color.White.copy(alpha = 0.5f),
                    focusedLabelColor = GoldAccent,
                    cursorColor = GoldAccent
                ),
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)
            )

            Button(
                onClick = {
                    val finalName = if (isSignUp) name else email.substringBefore("@")
                    viewModel.performLogin(email.ifEmpty { "student@rasheed.edu" }, finalName.ifEmpty { "طالب علم" })
                    onAuthSuccess()
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = if (isSignUp) "اکاؤنٹ بنائیں" else "داخل ہوں (Login)",
                    style = UrduTextStyle.copy(
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = { isSignUp = !isSignUp }
            ) {
                Text(
                    text = if (isSignUp) "پہلے سے اکاؤنٹ ہے؟ لاگ ان کریں" else "نیا اکاؤنٹ بنانے کے لیے یہاں دبائیں",
                    style = UrduTextStyle.copy(
                        color = Color.White,
                        fontSize = 14.sp
                    )
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Standard Google Sign in simulation button
            Button(
                onClick = {
                    viewModel.performLogin("google.user@gmail.com", "Hafiz Nouman")
                    onAuthSuccess()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text(
                    text = "Google کے ساتھ داخل ہوں",
                    color = Color.DarkGray,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
