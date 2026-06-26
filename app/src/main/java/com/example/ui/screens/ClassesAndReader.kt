package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.database.BookBookmark
import com.example.data.database.BookNote
import com.example.data.database.LocalBook
import com.example.ui.theme.*
import com.example.ui.viewmodel.IslamicViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun BookCoverPreview(localFilePath: String?, modifier: Modifier = Modifier) {
    var coverBitmap by remember(localFilePath) { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(localFilePath) {
        if (localFilePath != null) {
            withContext(Dispatchers.IO) {
                try {
                    val file = File(localFilePath)
                    if (file.exists()) {
                        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                        val renderer = PdfRenderer(descriptor)
                        if (renderer.pageCount > 0) {
                            val page = renderer.openPage(0)
                            val bitmap = Bitmap.createBitmap(120 * 2, 170 * 2, Bitmap.Config.ARGB_8888)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            withContext(Dispatchers.Main) {
                                coverBitmap = bitmap
                            }
                            page.close()
                        }
                        renderer.close()
                        descriptor.close()
                    }
                } catch (e: Exception) {
                    Log.e("BookCoverPreview", "Failed to render cover page", e)
                }
            }
        }
    }

    if (coverBitmap != null) {
        Image(
            bitmap = coverBitmap!!.asImageBitmap(),
            contentDescription = "Book Cover Preview",
            modifier = modifier,
            contentScale = androidx.compose.ui.layout.ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier.background(GoldAccent.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MenuBook,
                    contentDescription = null,
                    tint = GoldAccent,
                    modifier = Modifier.size(36.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "لوڈنگ...",
                    style = UrduTextStyle.copy(fontSize = 10.sp, color = GoldAccent)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ClassesScreen(viewModel: IslamicViewModel, onNavigateToReader: (String) -> Unit, onNavigateToQuiz: (String) -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val darjasList = listOf(
        "Oola" to "درجہ اولیٰ - Darja Oola",
        "Thania" to "درجہ ثانیہ - Darja Thania",
        "Thalitha" to "درجہ ثالثہ - Darja Thalitha",
        "Rabia" to "درجہ رابعہ - Darja Rabia",
        "Khamisa" to "درجہ خامسہ - Darja Khamisa",
        "Sadisa" to "درجہ سادسہ - Darja Sadisa",
        "Sabia" to "درجہ سابعہ - Darja Sabia",
        "Thamina" to "درجہ ثامنہ - دورہ حدیث"
    )

    var selectedDarjaKey by remember { mutableStateOf<String?>(null) }
    var selectedDarjaTitle by remember { mutableStateOf("") }
    var showAddBookDialog by remember { mutableStateOf(false) }

    val currentUserEmail by viewModel.currentUserEmail.collectAsState()
    val safeEmailPrefix = remember(currentUserEmail) {
        currentUserEmail.replace("@", "_at_").replace(".", "_dot_")
    }

    // List of books for the selected Darja
    val booksList by viewModel.repository.getBooksByDarja(selectedDarjaKey ?: "Oola").collectAsState(initial = emptyList())
    val filteredBooksList = remember(booksList, safeEmailPrefix) {
        booksList.filter { book ->
            !book.isCustom || book.id.startsWith("${safeEmailPrefix}_")
        }
    }

    LaunchedEffect(selectedDarjaKey, filteredBooksList) {
        if (selectedDarjaKey == "Thania") {
            filteredBooksList.forEach { book ->
                if (book.downloadStatus != "DOWNLOADED" && book.downloadStatus != "DOWNLOADING") {
                    coroutineScope.launch {
                        viewModel.repository.downloadBookPdf(context, book.id)
                    }
                }
            }
        }
    }

    if (selectedDarjaKey != null) {
        // Books list in Selected Darja
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(selectedDarjaTitle, style = UrduTextStyle.copy(color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)) },
                    navigationIcon = {
                        IconButton(onClick = { selectedDarjaKey = null }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldAccent)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = IslamicGreen)
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddBookDialog = true },
                    containerColor = GoldAccent,
                    contentColor = Color.Black
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Custom Book")
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (filteredBooksList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            text = "اس درجہ میں کوئی کتابیں موجود نہیں ہیں۔\nبرائے مہربانی اپنی کتاب شامل کریں!",
                            style = UrduTextStyle.copy(color = Color.Gray, textAlign = TextAlign.Center)
                        )
                    }
                } else {
                    if (selectedDarjaKey == "Thania") {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            contentPadding = PaddingValues(16.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(filteredBooksList) { book ->
                                var showDeleteConfirm by remember { mutableStateOf(false) }

                                if (showDeleteConfirm) {
                                    AlertDialog(
                                        onDismissRequest = { showDeleteConfirm = false },
                                        title = { Text("کتاب حذف کریں؟", style = UrduTextStyle.copy(fontWeight = FontWeight.Bold)) },
                                        text = { Text("کیا آپ واقعی اس کتاب '${book.nameUrdu}' کو حذف کرنا چاہتے ہیں؟", style = UrduTextStyle.copy()) },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                coroutineScope.launch {
                                                    viewModel.repository.deleteBook(book, context)
                                                }
                                                showDeleteConfirm = false
                                            }) {
                                                Text("جی ہاں", color = Color.Red, style = UrduTextStyle.copy(fontWeight = FontWeight.Bold))
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showDeleteConfirm = false }) {
                                                Text("کینسل", style = UrduTextStyle.copy())
                                            }
                                        }
                                    )
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = { onNavigateToReader(book.id) },
                                            onLongClick = {
                                                if (book.isCustom) {
                                                    showDeleteConfirm = true
                                                }
                                            }
                                        ),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f))
                                ) {
                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        // Dynamic first-page PDF cover rendering
                                        BookCoverPreview(
                                            localFilePath = book.localFilePath,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(180.dp)
                                                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(start = 8.dp, end = 8.dp, bottom = 12.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = book.nameUrdu,
                                                style = UrduTextStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold, color = IslamicGreen),
                                                maxLines = 2,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = book.author,
                                                style = UrduTextStyle.copy(fontSize = 11.sp, color = Color.Gray),
                                                maxLines = 1,
                                                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Button(
                                                onClick = { onNavigateToQuiz(book.id) },
                                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.height(28.dp)
                                            ) {
                                                Text(
                                                    "ٹیسٹ دیں",
                                                    style = UrduTextStyle.copy(color = Color.Black, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredBooksList) { book ->
                                var showDeleteConfirm by remember { mutableStateOf(false) }

                                if (showDeleteConfirm) {
                                    AlertDialog(
                                        onDismissRequest = { showDeleteConfirm = false },
                                        title = { Text("کتاب حذف کریں؟", style = UrduTextStyle.copy(fontWeight = FontWeight.Bold)) },
                                        text = { Text("کیا آپ واقعی اس کتاب '${book.nameUrdu}' کو حذف کرنا چاہتے ہیں؟", style = UrduTextStyle.copy()) },
                                        confirmButton = {
                                            TextButton(onClick = {
                                                coroutineScope.launch {
                                                    viewModel.repository.deleteBook(book, context)
                                                }
                                                showDeleteConfirm = false
                                            }) {
                                                Text("جی ہاں", color = Color.Red, style = UrduTextStyle.copy(fontWeight = FontWeight.Bold))
                                            }
                                        },
                                        dismissButton = {
                                            TextButton(onClick = { showDeleteConfirm = false }) {
                                                Text("کینسل", style = UrduTextStyle.copy())
                                            }
                                        }
                                    )
                                }

                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .combinedClickable(
                                            onClick = { onNavigateToReader(book.id) },
                                            onLongClick = {
                                                if (book.isCustom) {
                                                    showDeleteConfirm = true
                                                }
                                            }
                                        ),
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
                                        Row(
                                            modifier = Modifier.weight(1f),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Book,
                                                contentDescription = null,
                                                tint = IslamicGreen,
                                                modifier = Modifier.size(36.dp)
                                            )
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(
                                                        text = book.nameUrdu,
                                                        style = UrduTextStyle.copy(fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                                    )
                                                    if (book.isCustom) {
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Box(
                                                            modifier = Modifier
                                                                .background(GoldAccent.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                                        ) {
                                                            Text(
                                                                text = "My Book",
                                                                fontSize = 10.sp,
                                                                fontWeight = FontWeight.Bold,
                                                                color = GoldDark
                                                            )
                                                        }
                                                    }
                                                }
                                                Text(
                                                    text = "مصنف: ${book.author} | صفحات: ${book.totalPages}",
                                                    style = UrduTextStyle.copy(fontSize = 12.sp, color = Color.Gray)
                                                )
                                            }
                                        }

                                        Button(
                                            onClick = { onNavigateToQuiz(book.id) },
                                            colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                "ٹیسٹ دیں",
                                                style = UrduTextStyle.copy(color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Custom Book Upload Dialog
        if (showAddBookDialog) {
            var bookNameUrdu by remember { mutableStateOf("") }
            var authorName by remember { mutableStateOf("") }
            var totalPagesStr by remember { mutableStateOf("") }
            var driveLink by remember { mutableStateOf("") }
            var pickedPdfUri by remember { mutableStateOf<Uri?>(null) }

            val pdfPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.OpenDocument()
            ) { uri ->
                pickedPdfUri = uri
            }

            Dialog(onDismissRequest = { showAddBookDialog = false }) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "نئی کتاب شامل کریں (Add Custom Book)",
                            style = UrduTextStyle.copy(color = IslamicGreen, fontSize = 18.sp, fontWeight = FontWeight.Bold),
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Card(
                            colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            border = BorderStroke(1.dp, GoldAccent)
                        ) {
                            Text(
                                text = "🔒 یہ کتاب آپ کے ذاتی اکاؤنٹ (Private Student Storage) میں شامل ہوگی اور کسی دوسرے طالب علم کو نظر نہیں آئے گی۔",
                                style = UrduTextStyle.copy(color = IslamicGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(8.dp),
                                textAlign = TextAlign.Center
                            )
                        }

                        OutlinedTextField(
                            value = bookNameUrdu,
                            onValueChange = { bookNameUrdu = it },
                            label = { Text("کتاب کا نام (Urdu Name)", color = IslamicGreen) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IslamicGreen),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = authorName,
                            onValueChange = { authorName = it },
                            label = { Text("مصنف کا نام (Author)", color = IslamicGreen) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IslamicGreen),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = totalPagesStr,
                            onValueChange = { totalPagesStr = it },
                            label = { Text("کل صفحات (Total Pages)", color = IslamicGreen) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IslamicGreen),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Divider(color = Color.LightGray)

                        Text("اختیار 1: گوگل ڈرائیو لنک ڈالیں", style = UrduTextStyle.copy(fontSize = 12.sp, color = Color.Gray))
                        OutlinedTextField(
                            value = driveLink,
                            onValueChange = { driveLink = it },
                            label = { Text("Google Drive Link", color = IslamicGreen) },
                            placeholder = { Text("https://drive.google.com/file/d/...") },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = IslamicGreen),
                            modifier = Modifier.fillMaxWidth()
                        )

                        Text("یا اختیار 2: مقامی پی ڈی ایف منتخب کریں", style = UrduTextStyle.copy(fontSize = 12.sp, color = Color.Gray))
                        Button(
                            onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                            colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen.copy(alpha = 0.15f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.UploadFile, contentDescription = null, tint = IslamicGreen)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (pickedPdfUri != null) "منتخب کردہ: " + pickedPdfUri!!.lastPathSegment else "پی ڈی ایف فائل منتخب کریں",
                                    color = IslamicGreen,
                                    style = UrduTextStyle.copy(fontSize = 12.sp)
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            TextButton(onClick = { showAddBookDialog = false }, modifier = Modifier.weight(1f)) {
                                Text("کینسل", style = UrduTextStyle.copy(color = Color.Gray))
                            }

                            Button(
                                onClick = {
                                    val pages = totalPagesStr.toIntOrNull() ?: 100
                                    // Extract Google Drive ID from link or generate unique custom ID
                                    var extractedId = "custom_${System.currentTimeMillis()}"
                                    if (driveLink.isNotEmpty()) {
                                        val match = "/d/([a-zA-Z0-9_-]+)".toRegex().find(driveLink)
                                        if (match != null) {
                                            extractedId = match.groupValues[1]
                                        }
                                    }

                                    val finalId = "${safeEmailPrefix}_${extractedId}"

                                    val customBook = LocalBook(
                                        id = finalId,
                                        nameUrdu = bookNameUrdu.ifEmpty { "میری کتاب" },
                                        author = authorName.ifEmpty { "طالب علم" },
                                        totalPages = pages,
                                        darja = selectedDarjaKey ?: "Oola",
                                        isCustom = true,
                                        localFilePath = pickedPdfUri?.toString() // Local uri stored as mock path
                                    )

                                    coroutineScope.launch {
                                        viewModel.repository.addCustomBook(customBook)
                                    }
                                    showAddBookDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = GoldAccent),
                                modifier = Modifier.weight(1.5f)
                            ) {
                                Text("شامل کریں", style = UrduTextStyle.copy(color = Color.Black, fontWeight = FontWeight.Bold))
                            }
                        }
                    }
                }
            }
        }
    } else {
        // Classes Main Menu Grid
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                text = "درسِ نظامی کلاسز (Curriculum Classes)",
                style = UrduTextStyle.copy(color = IslamicGreen, fontSize = 22.sp, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(bottom = 16.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(darjasList) { (key, title) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1.2f)
                            .clickable {
                                selectedDarjaKey = key
                                selectedDarjaTitle = title
                            },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = RoundedCornerShape(16.dp),
                        border = BorderStroke(1.dp, IslamicGreen.copy(alpha = 0.15f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(CircleShape)
                                    .background(IslamicGreen),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = title.split(" - ").first(),
                                style = UrduTextStyle.copy(fontSize = 16.sp, fontWeight = FontWeight.Bold),
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = title.split(" - ").getOrElse(1) { "" },
                                fontSize = 10.sp,
                                color = Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AdvancedReaderScreen(viewModel: IslamicViewModel, bookId: String, onNavigateBack: () -> Unit) {
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current

    val bookState = viewModel.repository.getBookByIdFlow(bookId).collectAsState(initial = null)
    val book = bookState.value ?: return

    val notesList by viewModel.repository.getNotesForBook(bookId).collectAsState(initial = emptyList())
    val bookmarksList by viewModel.repository.getBookmarksForBook(bookId).collectAsState(initial = emptyList())

    var currentPage by remember { mutableStateOf(book.lastReadPage) }
    var nightMode by remember { mutableStateOf(false) }

    // Floating panels
    var showAIUstaadChat by remember { mutableStateOf(false) }
    var showBookmarksNotesDialog by remember { mutableStateOf(false) }
    var showAddNoteDialog by remember { mutableStateOf(false) }
    var selectedHighlightColor by remember { mutableStateOf<String?>(null) } // "YELLOW", "GREEN", "PINK" or null

    // For pinch-to-zoom
    var scale by remember { mutableStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    val transformState = rememberTransformableState { zoomChange, offsetChange, _ ->
        scale *= zoomChange
        offset += offsetChange
    }

    // Dynamic pdf pages rendering state using Android PdfRenderer
    var pageCount by remember { mutableStateOf(0) }
    var renderedPages by remember { mutableStateOf<List<Bitmap>>(emptyList()) }

    // Initial render loader
    LaunchedEffect(book.localFilePath) {
        if (book.localFilePath != null) {
            withContext(Dispatchers.IO) {
                try {
                    val file = File(book.localFilePath)
                    if (file.exists()) {
                        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                        val renderer = PdfRenderer(descriptor)
                        val count = renderer.pageCount
                        pageCount = count

                        val bitmaps = mutableListOf<Bitmap>()
                        // Load first 5 pages for rapid UI response
                        val loadLimit = minOf(count, 5)
                        for (i in 0 until loadLimit) {
                            val page = renderer.openPage(i)
                            // High resolution page rendering
                            val bitmap = Bitmap.createBitmap(595 * 2, 842 * 2, Bitmap.Config.ARGB_8888)
                            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                            bitmaps.add(bitmap)
                            page.close()
                        }
                        renderer.close()
                        descriptor.close()

                        withContext(Dispatchers.Main) {
                            renderedPages = bitmaps
                        }
                    }
                } catch (e: Exception) {
                    Log.e("ReaderScreen", "Failed to render PDF page", e)
                }
            }
        }
    }

    // Auto-save last read page
    LaunchedEffect(currentPage) {
        viewModel.repository.updateBookLastReadPage(bookId, currentPage)
    }

    // AI Ustaad Chat Drawer State
    val chatHistoryMap by viewModel.chatMessages.collectAsState()
    val chatHistory = chatHistoryMap[bookId] ?: emptyList()
    var chatMessageInput by remember { mutableStateOf("") }
    val isChatLoading by viewModel.isChatLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(book.nameUrdu, style = UrduTextStyle.copy(color = GoldAccent, fontSize = 18.sp, fontWeight = FontWeight.Bold)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldAccent)
                    }
                },
                actions = {
                    IconButton(onClick = { nightMode = !nightMode }) {
                        Icon(
                            imageVector = if (nightMode) Icons.Default.WbSunny else Icons.Default.Nightlight,
                            contentDescription = "Night Mode",
                            tint = GoldAccent
                        )
                    }
                    IconButton(onClick = { showBookmarksNotesDialog = true }) {
                        Icon(Icons.Default.Assignment, contentDescription = "Notes & Bookmarks", tint = GoldAccent)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = IslamicGreen)
            )
        },
        bottomBar = {
            if (book.darja != "Oola" && book.darja != "Thania") {
                // Page navigation controllers
                Surface(tonalElevation = 8.dp, color = MaterialTheme.colorScheme.surface) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Download indicator
                    if (book.downloadStatus != "DOWNLOADED") {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("کتاب ڈاؤن لوڈ کریں (Offline Access)", style = UrduTextStyle.copy(fontSize = 11.sp))
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.repository.downloadBookPdf(context, bookId)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("ڈاؤن لوڈ", fontSize = 10.sp, color = Color.White)
                                }
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { if (currentPage > 1) currentPage-- },
                            enabled = currentPage > 1
                        ) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Prev Page")
                        }

                        Text(
                            text = "صفحہ نمبر: $currentPage / ${book.totalPages}",
                            style = UrduTextStyle.copy(fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        )

                        IconButton(
                            onClick = { if (currentPage < book.totalPages) currentPage++ },
                            enabled = currentPage < book.totalPages
                        ) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Next Page")
                        }
                    }
                }
            }
        }
    },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // AI Ustaad chat button
                FloatingActionButton(
                    onClick = { showAIUstaadChat = true },
                    containerColor = IslamicGreen,
                    contentColor = GoldAccent
                ) {
                    Icon(Icons.Default.Chat, contentDescription = "AI Ustaad Chat")
                }

                // Bookmark page button
                val isBookmarked = bookmarksList.any { it.pageNumber == currentPage }
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            if (isBookmarked) {
                                viewModel.repository.removeBookmarkAtPage(bookId, currentPage)
                            } else {
                                viewModel.repository.addBookmark(
                                    BookBookmark(bookId = bookId, pageNumber = currentPage, title = "صفحہ $currentPage")
                                )
                            }
                        }
                    },
                    containerColor = GoldAccent,
                    contentColor = Color.Black
                ) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark Page"
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(if (nightMode) Color.Black else Color.LightGray.copy(alpha = 0.3f))
        ) {
            if (book.darja == "Oola" || book.darja == "Thania") {
                var isWebLoading by remember { mutableStateOf(true) }
                val driveUrl = if (book.id.startsWith("http")) {
                    book.id
                } else {
                    "https://drive.google.com/file/d/${book.id}/preview"
                }

                Box(modifier = Modifier.fillMaxSize()) {
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
                                        isWebLoading = true
                                    }

                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        isWebLoading = false
                                    }
                                }
                                webChromeClient = WebChromeClient()
                                loadUrl(driveUrl)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    if (isWebLoading) {
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
            } else {
                // Document Viewer Canvas / Content List
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                        .graphicsLayer(
                            scaleX = scale,
                            scaleY = scale,
                            translationX = offset.x,
                            translationY = offset.y
                        )
                        .transformable(state = transformState)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { showAddNoteDialog = true }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    val currentBitmap = renderedPages.getOrNull(currentPage - 1)
                    if (currentBitmap != null) {
                        // Render high resolution native PDF bitmap
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(currentBitmap.width.toFloat() / currentBitmap.height.toFloat())
                                .background(Color.White)
                        ) {
                            Image(
                                bitmap = currentBitmap.asImageBitmap(),
                                contentDescription = "PDF Page",
                                modifier = Modifier.fillMaxSize(),
                                colorFilter = if (nightMode) {
                                    ColorFilter.colorMatrix(ColorMatrix(floatArrayOf(
                                        -1f,  0f,  0f, 0f, 255f,
                                         0f, -1f,  0f, 0f, 255f,
                                         0f,  0f, -1f, 0f, 255f,
                                         0f,  0f,  0f, 1f,   0f
                                    )))
                                } else null
                            )

                            // Render user Highlights overlay
                            val currentHighlight = notesList.firstOrNull { it.pageNumber == currentPage && it.highlightColor != null }
                            if (currentHighlight != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .fillMaxHeight(0.2f) // Highlight mock position
                                        .align(Alignment.Center)
                                        .background(
                                            when (currentHighlight.highlightColor) {
                                                "YELLOW" -> Color.Yellow.copy(alpha = 0.4f)
                                                "GREEN" -> Color.Green.copy(alpha = 0.4f)
                                                "PINK" -> Color.Magenta.copy(alpha = 0.4f)
                                                else -> Color.Transparent
                                            }
                                        )
                                )
                            }
                        }
                    } else {
                        // Standard beautifully rendered offline preview page
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            colors = CardDefaults.cardColors(containerColor = if (nightMode) Color.DarkGray else Color.White),
                            shape = RoundedCornerShape(16.dp),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "صفحہ نمبر $currentPage",
                                    style = UrduTextStyle.copy(color = if (nightMode) Color.White else IslamicGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "درس نظامی کورس مٹیریل مطالعہ کریں۔\n\nاس صفحے پر نوٹ شامل کرنے کے لیے تھوڑی دیر دبا کر رکھیں۔",
                                    style = UrduTextStyle.copy(color = if (nightMode) Color.LightGray else Color.DarkGray),
                                    textAlign = TextAlign.Center
                                )

                                // Render Notes for this page
                                val pageNotes = notesList.filter { it.pageNumber == currentPage }
                                if (pageNotes.isNotEmpty()) {
                                    Spacer(modifier = Modifier.height(24.dp))
                                    Text("اس صفحے کے نوٹ (Page Notes):", fontWeight = FontWeight.Bold, color = GoldAccent, fontSize = 12.sp)
                                    pageNotes.forEach { note ->
                                        Card(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                            colors = CardDefaults.cardColors(containerColor = IslamicGreen.copy(alpha = 0.05f))
                                        ) {
                                            Text(note.noteText, modifier = Modifier.padding(8.dp), style = UrduTextStyle.copy(fontSize = 12.sp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Note Addition Dialog
            if (showAddNoteDialog) {
                var noteText by remember { mutableStateOf("") }
                var highlightColor by remember { mutableStateOf<String?>("YELLOW") }

                Dialog(onDismissRequest = { showAddNoteDialog = false }) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("صفحہ پر نوٹ شامل کریں", style = UrduTextStyle.copy(fontWeight = FontWeight.Bold, color = IslamicGreen))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("YELLOW", "GREEN", "PINK").forEach { color ->
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when (color) {
                                                    "YELLOW" -> Color.Yellow
                                                    "GREEN" -> Color.Green
                                                    else -> Color.Magenta
                                                }
                                            )
                                            .border(
                                                width = if (highlightColor == color) 2.dp else 0.dp,
                                                color = Color.Black,
                                                shape = CircleShape
                                            )
                                            .clickable { highlightColor = color }
                                    )
                                }
                            }

                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                label = { Text("نوٹ لکھیں...", color = IslamicGreen) },
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { showAddNoteDialog = false }) { Text("کینسل") }
                                Button(
                                    onClick = {
                                        coroutineScope.launch {
                                            viewModel.repository.addNote(
                                                BookNote(
                                                    bookId = bookId,
                                                    pageNumber = currentPage,
                                                    noteText = noteText,
                                                    highlightColor = highlightColor,
                                                    selectedText = "Highlighted Text"
                                                )
                                            )
                                        }
                                        showAddNoteDialog = false
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldAccent)
                                ) {
                                    Text("محفوظ کریں", style = UrduTextStyle.copy(color = Color.Black))
                                }
                            }
                        }
                    }
                }
            }

            // AI Ustaad chat Drawer Panel
            AnimatedVisibility(
                visible = showAIUstaadChat,
                enter = slideInHorizontally(initialOffsetX = { it }),
                exit = slideOutHorizontally(targetOffsetX = { it }),
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .fillMaxHeight()
                    .fillMaxWidth(0.85f)
            ) {
                Surface(
                    tonalElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxHeight()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("AI استاد سمارٹ Q&A", style = UrduTextStyle.copy(fontWeight = FontWeight.Bold, color = IslamicGreen))
                            IconButton(onClick = { showAIUstaadChat = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close")
                            }
                        }

                        // Chat messages logs
                        LazyColumn(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            item {
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = IslamicGreen.copy(alpha = 0.05f))
                                ) {
                                    Text(
                                        text = "السلام علیکم! میں آپ کا AI استاد ہوں۔ کتاب '${book.nameUrdu}' کے متعلق مجھ سے کوئی بھی سوال پوچھیں۔",
                                        modifier = Modifier.padding(12.dp),
                                        style = UrduTextStyle.copy(fontSize = 12.sp, color = IslamicGreenDark)
                                    )
                                }
                            }

                            items(chatHistory) { msg ->
                                Box(
                                    modifier = Modifier.fillMaxWidth(),
                                    contentAlignment = if (msg.isUser) Alignment.CenterEnd else Alignment.CenterStart
                                ) {
                                    Card(
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (msg.isUser) IslamicGreen else Color.LightGray.copy(alpha = 0.3f)
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            text = msg.text,
                                            color = if (msg.isUser) Color.White else MaterialTheme.colorScheme.onSurface,
                                            modifier = Modifier.padding(10.dp),
                                            style = UrduTextStyle.copy(fontSize = 13.sp)
                                        )
                                    }
                                }
                            }

                            if (isChatLoading) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxWidth(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            color = GoldAccent,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Input field
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = chatMessageInput,
                                onValueChange = { chatMessageInput = it },
                                placeholder = { Text("سوال ٹائپ کریں...") },
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(
                                onClick = {
                                    if (chatMessageInput.isNotBlank()) {
                                        viewModel.askAIUstaad(chatMessageInput, book, currentPage)
                                        chatMessageInput = ""
                                    }
                                },
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(IslamicGreen)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                            }
                        }
                    }
                }
            }

            // Bookmarks & Notes listing Dialog
            if (showBookmarksNotesDialog) {
                Dialog(onDismissRequest = { showBookmarksNotesDialog = false }) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.7f),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("بک مارکس اور نوٹس کی فہرست", style = UrduTextStyle.copy(fontWeight = FontWeight.Bold, color = IslamicGreen))
                            Spacer(modifier = Modifier.height(12.dp))

                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                item { Text("بک مارکس (Bookmarks):", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                                if (bookmarksList.isEmpty()) {
                                    item { Text("کوئی بک مارک نہیں ہے۔", fontSize = 12.sp, color = Color.Gray) }
                                }
                                items(bookmarksList) { bookmark ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                currentPage = bookmark.pageNumber
                                                showBookmarksNotesDialog = false
                                            }
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("صفحہ نمبر ${bookmark.pageNumber}", style = UrduTextStyle.copy(fontSize = 13.sp))
                                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = IslamicGreen)
                                    }
                                }

                                item { Divider(modifier = Modifier.padding(vertical = 12.dp)) }

                                item { Text("نوٹس اور ہائی لائٹس (Notes):", fontWeight = FontWeight.Bold, fontSize = 14.sp) }
                                if (notesList.isEmpty()) {
                                    item { Text("کوئی نوٹ نہیں ہے۔", fontSize = 12.sp, color = Color.Gray) }
                                }
                                items(notesList) { note ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                currentPage = note.pageNumber
                                                showBookmarksNotesDialog = false
                                            }
                                            .padding(vertical = 4.dp),
                                        colors = CardDefaults.cardColors(containerColor = IslamicGreen.copy(alpha = 0.05f))
                                    ) {
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Text("صفحہ: ${note.pageNumber}", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                                IconButton(
                                                    onClick = {
                                                        coroutineScope.launch {
                                                            viewModel.repository.deleteNote(note)
                                                        }
                                                    },
                                                    modifier = Modifier.size(16.dp)
                                                ) {
                                                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                                                }
                                            }
                                            Text(note.noteText, style = UrduTextStyle.copy(fontSize = 12.sp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
