package com.example

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.UrduTextStyle
import com.example.ui.viewmodel.IslamicViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import android.util.Log
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.compose.ui.text.font.FontWeight

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val options = FirebaseOptions.Builder()
                    .setApiKey("AIzaSyDummyKeyForGracefulInitialization")
                    .setApplicationId("1:1234567890:android:abcdef123456")
                    .setProjectId("dummy-project-id")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("MainActivity", "FirebaseApp initialized programmatically successfully")
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Failed to initialize Firebase programmatically: ${e.message}")
        }

        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                AppMainEntry()
            }
        }
    }
}

@Composable
fun AppMainEntry() {
    val viewModel: IslamicViewModel = viewModel()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()

    val navController = rememberNavController()

    var showUpdateDialog by remember { mutableStateOf(false) }
    var updateUrl by remember { mutableStateOf("https://play.google.com/store") }
    val context = androidx.compose.ui.platform.LocalContext.current

    LaunchedEffect(Unit) {
        try {
            val config = FirebaseRemoteConfig.getInstance()
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .setMinimumFetchIntervalInSeconds(60)
                .build()
            config.setConfigSettingsAsync(configSettings)

            val defaults = mapOf(
                "latest_version" to "1",
                "update_url" to "https://play.google.com/store"
            )
            config.setDefaultsAsync(defaults)

            config.fetchAndActivate()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val latestVersionStr = config.getString("latest_version")
                        val latestVersion = latestVersionStr.toIntOrNull() ?: 1

                        val currentVersionCode = try {
                            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                pInfo.longVersionCode.toInt()
                            } else {
                                @Suppress("DEPRECATION")
                                pInfo.versionCode
                            }
                        } catch (e: Exception) {
                            1
                        }

                        Log.d("RemoteConfig", "Latest version from config: $latestVersionStr -> $latestVersion, Current version: $currentVersionCode")
                        if (latestVersion > currentVersionCode) {
                            updateUrl = config.getString("update_url")
                            showUpdateDialog = true
                        }
                    } else {
                        Log.e("RemoteConfig", "Fetch failed: ${task.exception?.message}")
                    }
                }
        } catch (e: Exception) {
            Log.e("RemoteConfig", "Remote config init error: ${e.message}")
        }
    }

    if (showUpdateDialog) {
        AlertDialog(
            onDismissRequest = { showUpdateDialog = false },
            title = {
                Text(
                    text = "اپڈیٹ دستیاب ہے (Update Available)",
                    style = UrduTextStyle.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp)
                )
            },
            text = {
                Text(
                    text = "نیا ورژن دستیاب ہے! بہترین کارکردگی اور نئے فیچرز کے لیے ابھی اپڈیٹ کریں۔\n\nNaya Update Available Hai! Behtareen features ke liye abhi update karein.",
                    style = UrduTextStyle.copy(fontSize = 14.sp)
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Log.e("RemoteConfig", "Failed to open update URL: ${e.message}")
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IslamicGreen)
                ) {
                    Text(
                        text = "اپڈیٹ کریں (Update)",
                        style = UrduTextStyle.copy(color = androidx.compose.ui.graphics.Color.White, fontWeight = FontWeight.Bold)
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showUpdateDialog = false }
                ) {
                    Text(
                        text = "بعد میں (Later)",
                        style = UrduTextStyle.copy(color = androidx.compose.ui.graphics.Color.Gray)
                    )
                }
            }
        )
    }

    // Location & Notification permission requester
    val permissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true) {
            viewModel.requestGPSLocation()
        }
    }

    LaunchedEffect(key1 = true) {
        permissionsLauncher.launch(
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        )
        viewModel.requestGPSLocation()
    }

    NavHost(
        navController = navController,
        startDestination = "splash",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("splash") {
            SplashScreen {
                if (isLoggedIn) {
                    navController.navigate("main") {
                        popUpTo("splash") { inclusive = true }
                    }
                } else {
                    navController.navigate("auth") {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            }
        }

        composable("auth") {
            AuthScreen(viewModel) {
                navController.navigate("main") {
                    popUpTo("auth") { inclusive = true }
                }
            }
        }

        composable("main") {
            MainTabsShell(viewModel, navController)
        }

        composable("quran_pdf") {
            QuranPdfScreen(
                showBackButton = true,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "reader/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            AdvancedReaderScreen(
                viewModel = viewModel,
                bookId = bookId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "quiz/{bookId}",
            arguments = listOf(navArgument("bookId") { type = NavType.StringType })
        ) { backStackEntry ->
            val bookId = backStackEntry.arguments?.getString("bookId") ?: ""
            QuizScreen(
                viewModel = viewModel,
                bookId = bookId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
fun MainTabsShell(viewModel: IslamicViewModel, rootNavController: NavHostController) {
    val nestedNavController = rememberNavController()
    var selectedItem by remember { mutableStateOf(0) }

    val items = listOf(
        Triple("ہوم", Icons.Default.Home, "home"),
        Triple("اوقات", Icons.Default.AccessTime, "prayer_times"),
        Triple("قرآن", Icons.Default.MenuBook, "quran"),
        Triple("کلاسز", Icons.Default.School, "classes"),
        Triple("پروفائل", Icons.Default.Person, "profile")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = IslamicGreen,
                contentColor = GoldAccent
            ) {
                items.forEachIndexed { index, (label, icon, route) ->
                    NavigationBarItem(
                        icon = { Icon(icon, contentDescription = label, tint = if (selectedItem == index) GoldAccent else MaterialTheme.colorScheme.onPrimary) },
                        label = { Text(label, style = com.example.ui.theme.UrduTextStyle.copy(fontSize = 12.sp, color = if (selectedItem == index) GoldAccent else MaterialTheme.colorScheme.onPrimary)) },
                        selected = selectedItem == index,
                        onClick = {
                            selectedItem = index
                            nestedNavController.navigate(route) {
                                popUpTo(nestedNavController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(
                            indicatorColor = IslamicGreen.copy(alpha = 0.2f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = nestedNavController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    viewModel = viewModel,
                    onNavigateToQuranPdf = { rootNavController.navigate("quran_pdf") }
                )
            }
            composable("prayer_times") {
                PrayerTimesScreen(viewModel)
            }
            composable("quran") {
                QuranPdfScreen(showBackButton = false)
            }
            composable("classes") {
                ClassesScreen(
                    viewModel = viewModel,
                    onNavigateToReader = { bookId -> rootNavController.navigate("reader/$bookId") },
                    onNavigateToQuiz = { bookId -> rootNavController.navigate("quiz/$bookId") }
                )
            }
            composable("profile") {
                ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToReader = { bookId -> rootNavController.navigate("reader/$bookId") }
                )
            }
        }
    }
}
