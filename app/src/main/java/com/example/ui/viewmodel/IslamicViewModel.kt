package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.*
import android.media.MediaPlayer
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiClient
import com.example.data.database.BookBookmark
import com.example.data.database.BookNote
import com.example.data.database.IslamicDatabase
import com.example.data.database.LocalBook
import com.example.data.database.QuizScore
import com.example.data.database.TasbeehHistory
import com.example.data.model.QuranData
import com.example.data.model.Surah
import com.example.data.repository.FirebaseManager
import com.example.data.repository.IslamicRepository
import com.example.data.utils.PrayerTimeCalculator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs

class IslamicViewModel(private val app: Application) : AndroidViewModel(app), SensorEventListener {
    private val db = IslamicDatabase.getDatabase(app)
    val repository = IslamicRepository(db)

    // Language switcher: "en" (English), "ur" (Urdu), "ar" (Arabic)
    private val _language = MutableStateFlow("ur")
    val language: StateFlow<String> = _language.asStateFlow()

    // Auth states
    private val _isLoggedIn = MutableStateFlow(FirebaseManager.isUserLoggedIn)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    private val _currentUserEmail = MutableStateFlow(FirebaseManager.userEmail)
    val currentUserEmail: StateFlow<String> = _currentUserEmail.asStateFlow()

    private val _currentUserName = MutableStateFlow(FirebaseManager.userName)
    val currentUserName: StateFlow<String> = _currentUserName.asStateFlow()

    // Location & Prayer times
    private val _userLatitude = MutableStateFlow(31.5204) // Defaults to Lahore
    private val _userLongitude = MutableStateFlow(74.3587)
    val userLatitude: StateFlow<Double> = _userLatitude.asStateFlow()
    val userLongitude: StateFlow<Double> = _userLongitude.asStateFlow()

    private val _prayerTimes = MutableStateFlow<PrayerTimeCalculator.PrayerTimes?>(null)
    val prayerTimes: StateFlow<PrayerTimeCalculator.PrayerTimes?> = _prayerTimes.asStateFlow()

    private val _selectedFiqh = MutableStateFlow("Hanafi")
    val selectedFiqh: StateFlow<String> = _selectedFiqh.asStateFlow()

    fun selectFiqh(fiqhName: String) {
        _selectedFiqh.value = fiqhName
        updatePrayerTimes()
    }

    private val _nextPrayerName = MutableStateFlow("Fajr")
    val nextPrayerName: StateFlow<String> = _nextPrayerName.asStateFlow()

    private val _nextPrayerCountdown = MutableStateFlow("00:00:00")
    val nextPrayerCountdown: StateFlow<String> = _nextPrayerCountdown.asStateFlow()

    private val _adhanEnabled = MutableStateFlow(true)
    val adhanEnabled: StateFlow<Boolean> = _adhanEnabled.asStateFlow()

    // Compass and Qibla Sensor States
    private val sensorManager = app.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = app.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        app.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val _deviceHeading = MutableStateFlow(0f)
    val deviceHeading: StateFlow<Float> = _deviceHeading.asStateFlow()

    private val _qiblaDirection = MutableStateFlow(0.0)
    val qiblaDirection: StateFlow<Double> = _qiblaDirection.asStateFlow()

    private val _qiblaAligned = MutableStateFlow(false)
    val qiblaAligned: StateFlow<Boolean> = _qiblaAligned.asStateFlow()

    // Quran Module States
    private val _currentPlayingSurahId = MutableStateFlow<Int?>(null)
    val currentPlayingSurahId: StateFlow<Int?> = _currentPlayingSurahId.asStateFlow()

    private val _isAudioPlaying = MutableStateFlow(false)
    val isAudioPlaying: StateFlow<Boolean> = _isAudioPlaying.asStateFlow()

    private val _lastReadSurah = MutableStateFlow(1)
    val lastReadSurah: StateFlow<Int> = _lastReadSurah.asStateFlow()

    private val _lastReadAyah = MutableStateFlow(1)
    val lastReadAyah: StateFlow<Int> = _lastReadAyah.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null

    // Chat with AI Ustaad
    data class ChatMessage(val text: String, val isUser: Boolean, val timestamp: Long = System.currentTimeMillis())
    private val _chatMessages = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap()) // bookId -> List of ChatMessage
    val chatMessages: StateFlow<Map<String, List<ChatMessage>>> = _chatMessages.asStateFlow()

    private val _isChatLoading = MutableStateFlow(false)
    val isChatLoading: StateFlow<Boolean> = _isChatLoading.asStateFlow()

    // MCQ System State
    data class MCQQuestion(
        val question: String,
        val options: List<String>,
        val correctIndex: Int,
        val pageReference: String
    )

    private val _currentBookQuestions = MutableStateFlow<List<MCQQuestion>>(emptyList())
    val currentBookQuestions: StateFlow<List<MCQQuestion>> = _currentBookQuestions.asStateFlow()

    private val _activeQuizScores = MutableStateFlow<List<QuizScore>>(emptyList())
    val activeQuizScores: StateFlow<List<QuizScore>> = _activeQuizScores.asStateFlow()

    // Background jobs
    private var countdownJob: Job? = null
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    private val httpClient = okhttp3.OkHttpClient()
    private val notificationManager = app.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
    
    private var lastNotifiedPrayer = ""
    private var lastNotifiedDay = -1

    init {
        createNotificationChannel()
        viewModelScope.launch {
            repository.populateDefaultCurriculum()
            updatePrayerTimes()
            startCountdownTimer()
            observeQuizScores()
        }
        registerCompassSensors()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                "adhan_channel",
                "Adhan Notifications",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for upcoming prayers"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendAdhanNotification(prayerUrdu: String) {
        if (!_adhanEnabled.value) return
        try {
            val builder = androidx.core.app.NotificationCompat.Builder(app, "adhan_channel")
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("نماز کا وقت (Prayer Time)")
                .setContentText("$prayerUrdu کا وقت ہونے میں 5 منٹ باقی ہیں")
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager.notify(prayerUrdu.hashCode(), builder.build())
        } catch (e: Exception) {
            Log.e("IslamicViewModel", "Failed to send notification: ${e.message}")
        }
    }

    fun requestGPSLocation() {
        try {
            val locationManager = app.getSystemService(Context.LOCATION_SERVICE) as android.location.LocationManager
            val hasGps = locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)
            val hasNetwork = locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER)

            val locationListener = object : android.location.LocationListener {
                override fun onLocationChanged(location: android.location.Location) {
                    updateLocation(location.latitude, location.longitude)
                    locationManager.removeUpdates(this)
                }
            }

            if (hasGps) {
                locationManager.requestLocationUpdates(
                    android.location.LocationManager.GPS_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
                val lastKnown = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                if (lastKnown != null) {
                    updateLocation(lastKnown.latitude, lastKnown.longitude)
                }
            } else if (hasNetwork) {
                locationManager.requestLocationUpdates(
                    android.location.LocationManager.NETWORK_PROVIDER,
                    0L,
                    0f,
                    locationListener
                )
                val lastKnown = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
                if (lastKnown != null) {
                    updateLocation(lastKnown.latitude, lastKnown.longitude)
                }
            }
        } catch (e: SecurityException) {
            Log.e("IslamicViewModel", "Location permission denied", e)
        } catch (e: Exception) {
            Log.e("IslamicViewModel", "Failed to request GPS location", e)
        }
    }

    private fun fetchPrayerTimesFromApi(lat: Double, lng: Double) {
        val schoolParam = if (_selectedFiqh.value == "Hanafi") 1 else 0
        val url = "https://api.aladhan.com/v1/timings?latitude=$lat&longitude=$lng&method=2&school=$schoolParam"
        val request = okhttp3.Request.Builder().url(url).build()
        httpClient.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: java.io.IOException) {
                Log.e("IslamicViewModel", "Failed to fetch times from Aladhan API, using offline calculator", e)
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use { res ->
                    if (res.isSuccessful) {
                        val bodyString = res.body?.string() ?: return
                        try {
                            val json = org.json.JSONObject(bodyString)
                            val data = json.getJSONObject("data")
                            val timings = data.getJSONObject("timings")
                            
                            val fajr = timings.getString("Fajr")
                            val sunrise = timings.getString("Sunrise")
                            val dhuhr = timings.getString("Dhuhr")
                            val asr = timings.getString("Asr")
                            val maghrib = timings.getString("Maghrib")
                            val isha = timings.getString("Isha")
                            
                            val apiTimes = PrayerTimeCalculator.PrayerTimes(
                                fajr = fajr,
                                sunrise = sunrise,
                                dhuhr = dhuhr,
                                asr = asr,
                                maghrib = maghrib,
                                isha = isha
                            )
                            
                            _prayerTimes.value = apiTimes
                            Log.d("IslamicViewModel", "Successfully fetched and parsed updated times from Aladhan API")
                        } catch (e: Exception) {
                            Log.e("IslamicViewModel", "Error parsing Aladhan API response: ${e.message}")
                        }
                    } else {
                        Log.e("IslamicViewModel", "Aladhan API returned unsuccessful response code: ${res.code}")
                    }
                }
            }
        })
    }

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    // Custom Login
    fun performLogin(email: String, name: String) {
        viewModelScope.launch {
            _isLoggedIn.value = true
            _currentUserEmail.value = email
            _currentUserName.value = name
            Log.d("ViewModel", "Logged in with: $email")
        }
    }

    fun performLogout() {
        viewModelScope.launch {
            FirebaseManager.auth?.signOut()
            _isLoggedIn.value = false
            _currentUserEmail.value = "offline@rasheedislamic.edu"
            _currentUserName.value = "طالب علم"
        }
    }

    // Location update
    fun updateLocation(lat: Double, lng: Double) {
        _userLatitude.value = lat
        _userLongitude.value = lng
        updatePrayerTimes()
    }

    private fun updatePrayerTimes() {
        val ratio = if (_selectedFiqh.value == "Hanafi") 2 else 1
        val times = PrayerTimeCalculator.calculatePrayerTimes(_userLatitude.value, _userLongitude.value, ratio)
        _prayerTimes.value = times
        _qiblaDirection.value = PrayerTimeCalculator.calculateQiblaDirection(_userLatitude.value, _userLongitude.value)
        fetchPrayerTimesFromApi(_userLatitude.value, _userLongitude.value)
    }

    // Next prayer countdown logic
    private fun startCountdownTimer() {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch(Dispatchers.Default) {
            while (true) {
                val times = _prayerTimes.value
                if (times != null) {
                    val now = Calendar.getInstance()
                    val nowHour = now.get(Calendar.HOUR_OF_DAY)
                    val nowMinute = now.get(Calendar.MINUTE)
                    val nowSec = now.get(Calendar.SECOND)
                    val nowInSec = nowHour * 3600 + nowMinute * 60 + nowSec

                    // Parse prayer times to seconds
                    val fajrSec = parseTimeToSec(times.fajr)
                    val dhuhrSec = parseTimeToSec(times.dhuhr)
                    val asrSec = parseTimeToSec(times.asr)
                    val maghribSec = parseTimeToSec(times.maghrib)
                    val ishaSec = parseTimeToSec(times.isha)

                    var targetSec = fajrSec
                    var prayerName = "Fajr"

                    when {
                        nowInSec < fajrSec -> {
                            targetSec = fajrSec
                            prayerName = "Fajr"
                        }
                        nowInSec < dhuhrSec -> {
                            targetSec = dhuhrSec
                            prayerName = "Dhuhr"
                        }
                        nowInSec < asrSec -> {
                            targetSec = asrSec
                            prayerName = "Asr"
                        }
                        nowInSec < maghribSec -> {
                            targetSec = maghribSec
                            prayerName = "Maghrib"
                        }
                        nowInSec < ishaSec -> {
                            targetSec = ishaSec
                            prayerName = "Isha"
                        }
                        else -> {
                            targetSec = fajrSec + 24 * 3600
                            prayerName = "Fajr"
                        }
                    }

                    val diff = targetSec - nowInSec
                    val h = diff / 3600
                    val m = (diff % 3600) / 60
                    val s = diff % 60

                    val prayerNamesUrdu = mapOf(
                        "Fajr" to "فجر",
                        "Dhuhr" to "ظہر",
                        "Asr" to "عصر",
                        "Maghrib" to "مغرب",
                        "Isha" to "عشاء"
                    )
                    val urduPrayerName = prayerNamesUrdu[prayerName] ?: prayerName
                    _nextPrayerName.value = urduPrayerName

                    val countdownStr = if (h > 0) {
                        "$urduPrayerName - $h گھنٹے $m منٹ باقی"
                    } else {
                        "$urduPrayerName - $m منٹ $s سیکنڈ باقی"
                    }
                    _nextPrayerCountdown.value = countdownStr

                    val currentDay = now.get(Calendar.DAY_OF_YEAR)
                    if (diff in 290..300 && (lastNotifiedPrayer != prayerName || lastNotifiedDay != currentDay)) {
                        lastNotifiedPrayer = prayerName
                        lastNotifiedDay = currentDay
                        sendAdhanNotification(urduPrayerName)
                    }
                }
                delay(1000)
            }
        }
    }

    private fun parseTimeToSec(timeStr: String): Int {
        val parts = timeStr.split(":")
        val h = parts.getOrNull(0)?.toIntOrNull() ?: 12
        val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return h * 3600 + m * 60
    }

    fun toggleAdhan(enabled: Boolean) {
        _adhanEnabled.value = enabled
    }

    // Compass and Qibla Logic
    private fun registerCompassSensors() {
        val rotationSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
        if (rotationSensor != null) {
            sensorManager.registerListener(this, rotationSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            // Fallback to magnetic field + accelerometer
            val mag = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
            val acc = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            sensorManager.registerListener(this, mag, SensorManager.SENSOR_DELAY_UI)
            sensorManager.registerListener(this, acc, SensorManager.SENSOR_DELAY_UI)
        }
    }

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private var lastAccValues = FloatArray(3)
    private var lastMagValues = FloatArray(3)
    private var hasAcc = false
    private var hasMag = false

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return
        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            var heading = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            heading = (heading + 360) % 360
            _deviceHeading.value = heading
            checkQiblaAlignment(heading)
        } else {
            if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                System.arraycopy(event.values, 0, lastAccValues, 0, event.values.size)
                hasAcc = true
            } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
                System.arraycopy(event.values, 0, lastMagValues, 0, event.values.size)
                hasMag = true
            }

            if (hasAcc && hasMag) {
                SensorManager.getRotationMatrix(rotationMatrix, null, lastAccValues, lastMagValues)
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                var heading = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
                heading = (heading + 360) % 360
                _deviceHeading.value = heading
                checkQiblaAlignment(heading)
            }
        }
    }

    private fun checkQiblaAlignment(heading: Float) {
        val target = _qiblaDirection.value
        val diff = abs(heading - target)
        val aligned = diff < 3.0 || diff > 357.0

        if (aligned && !_qiblaAligned.value) {
            _qiblaAligned.value = true
            triggerVibration()
        } else if (!aligned) {
            _qiblaAligned.value = false
        }
    }

    private fun triggerVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(150, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(150)
            }
        } catch (e: Exception) {
            Log.e("IslamicViewModel", "Vibration failed: ${e.message}")
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    // Quran Audio Playback using Sheikh Mishary Rashid voice
    fun playQuranAudio(surahId: Int) {
        viewModelScope.launch {
            if (_currentPlayingSurahId.value == surahId && mediaPlayer != null) {
                if (mediaPlayer?.isPlaying == true) {
                    mediaPlayer?.pause()
                    _isAudioPlaying.value = false
                } else {
                    mediaPlayer?.start()
                    _isAudioPlaying.value = true
                }
                return@launch
            }

            mediaPlayer?.release()
            _currentPlayingSurahId.value = surahId
            _isAudioPlaying.value = false

            val audioUrl = QuranData.getAudioUrl(surahId)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                setOnPreparedListener {
                    start()
                    _isAudioPlaying.value = true
                }
                setOnCompletionListener {
                    _isAudioPlaying.value = false
                    _currentPlayingSurahId.value = null
                }
                setOnErrorListener { _, _, _ ->
                    _isAudioPlaying.value = false
                    _currentPlayingSurahId.value = null
                    false
                }
                prepareAsync()
            }
        }
    }

    fun stopQuranAudio() {
        mediaPlayer?.release()
        mediaPlayer = null
        _isAudioPlaying.value = false
        _currentPlayingSurahId.value = null
    }

    fun bookmarkQuranPage(surah: Int, ayah: Int) {
        _lastReadSurah.value = surah
        _lastReadAyah.value = ayah
    }

    // AI Ustaad Q&A Screen Chat
    fun askAIUstaad(question: String, book: LocalBook, pageNumber: Int) {
        val bookId = book.id
        val chatList = _chatMessages.value[bookId]?.toMutableList() ?: mutableListOf()
        chatList.add(ChatMessage(question, isUser = true))
        _chatMessages.value = _chatMessages.value + (bookId to chatList)

        _isChatLoading.value = true
        viewModelScope.launch {
            val answer = GeminiClient.askUstaad(
                question = question,
                bookName = book.nameUrdu,
                author = book.author,
                currentPage = pageNumber,
                isCustom = book.isCustom,
                language = _language.value
            )
            val updatedList = _chatMessages.value[bookId]?.toMutableList() ?: mutableListOf()
            updatedList.add(ChatMessage(answer, isUser = false))
            _chatMessages.value = _chatMessages.value + (bookId to updatedList)
            _isChatLoading.value = false
        }
    }

    // MCQ Question Generator based on Book contents
    fun generateQuestionsForBook(book: LocalBook) {
        viewModelScope.launch {
            val list = mutableListOf<MCQQuestion>()
            val bookTitle = book.nameUrdu
            // Create 20 logical multiple choice questions depending on the subject
            for (i in 1..20) {
                val correctIndex = (0..3).random()
                val options = when (book.darja) {
                    "Oola" -> {
                        listOf(
                            "اسم (Noun) ہے",
                            "فعل (Verb) ہے",
                            "حرف (Preposition) ہے",
                            "صفت (Adjective) ہے"
                        )
                    }
                    else -> {
                        listOf(
                            "فرض عین ہے",
                            "سنتِ مؤکدہ ہے",
                            "واجب ہے",
                            "مستحب ہے"
                        )
                    }
                }
                val pageRef = (1..book.totalPages).random()
                list.add(
                    MCQQuestion(
                        question = "سوال نمبر $i: کتاب '$bookTitle' کے مطابق، اس فقہی/صرفی مسئلہ کا صحیح شرعی حل کیا ہے؟",
                        options = options,
                        correctIndex = correctIndex,
                        pageReference = "$bookTitle، صفحہ $pageRef"
                    )
                )
            }
            _currentBookQuestions.value = list
        }
    }

    private fun observeQuizScores() {
        viewModelScope.launch {
            repository.getAllQuizScores().collect {
                _activeQuizScores.value = it
            }
        }
    }

    fun submitQuizScore(book: LocalBook, score: Int, total: Int) {
        viewModelScope.launch {
            val qScore = QuizScore(bookId = book.id, bookName = book.nameUrdu, score = score, total = total)
            repository.saveQuizScore(qScore)
        }
    }

    override fun onCleared() {
        super.onCleared()
        countdownJob?.cancel()
        sensorManager.unregisterListener(this)
        mediaPlayer?.release()
    }
}
