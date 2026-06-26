package com.example.data.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun askUstaad(
        question: String,
        bookName: String,
        author: String,
        currentPage: Int,
        isCustom: Boolean,
        language: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured.")
            return@withContext "السلام علیکم! AI Ustaad کی سروس کو فعال کرنے کے لیے براہ کرم Google AI Studio کی کی (API Key) سیٹنگز میں درج کریں۔"
        }

        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey"

        val systemInstruction = """
            You are AI Ustaad, an expert scholarly Islamic teacher in the Rasheed Islamic Education system.
            You are assisting a student with the book '$bookName' (Author: $author, Current Page: $currentPage, Custom Upload: $isCustom).
            
            Guidelines:
            1. Respond in the student's chosen language. Current user interface language is: $language. If they type in Urdu, answer in elegant Urdu. If they type in Arabic, answer in Amiri Arabic. If they type in English, answer in English.
            2. Your response must be scholarly, gentle, encouraging, and strictly rooted in Islamic teachings matching traditional Hanafi/Dars-e-Nizami curricula.
            3. CRITICAL: You must always format your response with the answer first, followed by a formal reference line matching this exact pattern:
               Urdu/Arabic: "[Your beautiful answer here]\n\nحوالہ: $bookName، صفحہ $currentPage"
               English: "[Your beautiful answer here]\n\nReference: $bookName, Page $currentPage"
            4. If the student asks something completely irrelevant to Islamic education or the book, politely guide them back to their Dars-e-Nizami studies.
            5. CRITICAL FOR QURAN: All queries, reading requests, or references related to the Quran must be strictly answered and referenced from the specific Quran PDF uploaded to this workspace (Quran Majeed, represented by Google Drive Document ID: 0B-e6qHPbxSdNQ0x0alZKZVZpdzQ). Do not use or reference other sources for the Quran tab content.
        """.trimIndent()

        val jsonRequest = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "user")
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", "Student Question: $question\nCurrent Book Context: Book Name: $bookName, Current Page: $currentPage")
                        })
                    })
                })
            })
            put("systemInstruction", JSONObject().apply {
                put("parts", JSONArray().apply {
                    put(JSONObject().apply {
                        put("text", systemInstruction)
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
            })
        }

        val requestBody = jsonRequest.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    val errBody = response.body?.string() ?: ""
                    Log.e(TAG, "API call failed: Status ${response.code}, Body: $errBody")
                    return@withContext "معذرت، رابطہ قائم کرنے میں کچھ دشواری پیش آرہی ہے۔ (Error Code: ${response.code})"
                }

                val bodyString = response.body?.string() ?: return@withContext "کوئی جواب موصول نہیں ہوا۔"
                val jsonResponse = JSONObject(bodyString)
                val candidates = jsonResponse.optJSONArray("candidates")
                val content = candidates?.optJSONObject(0)?.optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                val answerText = parts?.optJSONObject(0)?.optString("text")

                if (!answerText.isNullOrBlank()) {
                    answerText
                } else {
                    "معذرت، میں آپ کے سوال کا جواب تلاش نہیں کر سکا۔"
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "API error", e)
            "نیٹ ورک کا مسئلہ: براہ کرم انٹرنیٹ کنکشن چیک کریں۔"
        }
    }
}
