package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.UrduTextStyle

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranPdfScreen(
    showBackButton: Boolean = true,
    onNavigateBack: () -> Unit = {}
) {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("islamic_settings", Context.MODE_PRIVATE) }
    var lastPage by remember { mutableStateOf(sharedPrefs.getInt("quran_last_page", 1)) }
    var isLoading by remember { mutableStateOf(true) }

    // Rebuild URL when page changes so WebView loads that exact page if supported by Drive viewer
    val baseDriveUrl = "https://drive.google.com/file/d/0B-e6qHPbxSdNQ0x0alZKZVZpdzQ/preview?resourcekey=0-UHTDfsV8TS7SWrznsf276w"
    val driveUrlWithPage = "$baseDriveUrl#page=$lastPage"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("قرآن مجید (Quran Majeed PDF)", style = UrduTextStyle.copy(color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    if (showBackButton) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldAccent)
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            downloadQuranToDownloads(context)
                        }
                    ) {
                        Icon(Icons.Default.Download, contentDescription = "Download Quran", tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IslamicGreen)
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "آخری پڑھا ہوا صفحہ: $lastPage",
                        style = UrduTextStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IslamicGreen)
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (lastPage > 1) {
                                    lastPage--
                                    sharedPrefs.edit().putInt("quran_last_page", lastPage).apply()
                                }
                            },
                            enabled = lastPage > 1,
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("پچھلا", style = UrduTextStyle.copy(fontSize = 12.sp, color = Color.White))
                        }

                        Button(
                            onClick = {
                                lastPage++
                                sharedPrefs.edit().putInt("quran_last_page", lastPage).apply()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen, contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("اگلا", style = UrduTextStyle.copy(fontSize = 12.sp, color = Color.White))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            // AndroidView wrapping high-performance WebView
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.useWideViewPort = true
                        settings.loadWithOverviewMode = true
                        settings.supportZoom()
                        settings.builtInZoomControls = true
                        settings.displayZoomControls = false

                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                                isLoading = true
                            }

                            override fun onPageFinished(view: WebView?, url: String?) {
                                isLoading = false
                            }
                        }
                        webChromeClient = WebChromeClient()
                        tag = lastPage
                        loadUrl(driveUrlWithPage)
                    }
                },
                update = { webView ->
                    val currentTag = webView.tag as? Int
                    if (currentTag != lastPage) {
                        webView.tag = lastPage
                        webView.loadUrl(driveUrlWithPage)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.White.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = GoldAccent,
                        modifier = Modifier.size(50.dp)
                    )
                }
            }
        }
    }
}

private fun downloadQuranToDownloads(context: Context) {
    try {
        val url = "https://drive.google.com/uc?export=download&id=0B-e6qHPbxSdNQ0x0alZKZVZpdzQ&confirm=t"
        val request = android.app.DownloadManager.Request(android.net.Uri.parse(url))
            .setTitle("Quran Majeed PDF")
            .setDescription("Downloading Quran Majeed")
            .setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
            .setDestinationInExternalPublicDir(android.os.Environment.DIRECTORY_DOWNLOADS, "Quran_Majeed.pdf")
            .setAllowedOverMetered(true)
            .setAllowedOverRoaming(true)

        val dm = context.getSystemService(Context.DOWNLOAD_SERVICE) as android.app.DownloadManager
        dm.enqueue(request)
        android.widget.Toast.makeText(context, "ڈاؤنلوڈ شروع ہو گئی ہے...", android.widget.Toast.LENGTH_SHORT).show()
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "ڈاؤنلوڈ میں خرابی: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
    }
}
