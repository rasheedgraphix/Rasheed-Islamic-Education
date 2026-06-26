package com.example.data.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "books")
data class LocalBook(
    @PrimaryKey val id: String,
    val nameUrdu: String,
    val author: String,
    val totalPages: Int,
    val darja: String,
    val isCustom: Boolean,
    val lastReadPage: Int = 1,
    val localFilePath: String? = null,
    val downloadProgress: Int = 0,
    val downloadStatus: String = "NOT_DOWNLOADED" // "NOT_DOWNLOADED", "DOWNLOADING", "DOWNLOADED"
)

@Entity(tableName = "book_notes")
data class BookNote(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: String,
    val pageNumber: Int,
    val noteText: String,
    val highlightColor: String?, // "YELLOW", "GREEN", "PINK" or null
    val selectedText: String?,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "book_bookmarks")
data class BookBookmark(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: String,
    val pageNumber: Int,
    val title: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tasbeeh_history")
data class TasbeehHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val dhikrKey: String,
    val count: Int,
    val date: String, // e.g. "2026-06-25"
    val synced: Boolean = false
)

@Entity(tableName = "quiz_scores")
data class QuizScore(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val bookId: String,
    val bookName: String,
    val score: Int,
    val total: Int,
    val timestamp: Long = System.currentTimeMillis()
)
