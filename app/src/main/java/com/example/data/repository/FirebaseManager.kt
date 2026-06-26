package com.example.data.repository

import android.net.Uri
import android.util.Log
import com.example.data.database.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

object FirebaseManager {
    private const val TAG = "FirebaseManager"

    val auth: FirebaseAuth? by lazy {
        try {
            FirebaseAuth.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Auth not initialized: ${e.message}")
            null
        }
    }

    val db: FirebaseFirestore? by lazy {
        try {
            FirebaseFirestore.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Firestore not initialized: ${e.message}")
            null
        }
    }

    val storage: FirebaseStorage? by lazy {
        try {
            FirebaseStorage.getInstance()
        } catch (e: Exception) {
            Log.e(TAG, "Firebase Storage not initialized: ${e.message}")
            null
        }
    }

    val currentUserId: String
        get() = auth?.currentUser?.uid ?: "local_offline_user"

    val isUserLoggedIn: Boolean
        get() = auth?.currentUser != null

    val userEmail: String
        get() = auth?.currentUser?.email ?: "offline@rasheedislamic.edu"

    val userName: String
        get() = auth?.currentUser?.displayName ?: "طالب علم"

    // Custom Books Firestore Sync
    suspend fun uploadCustomBookMetadata(book: LocalBook) {
        val firestore = db ?: return
        val userId = auth?.currentUser?.uid ?: return
        try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("customBooks")
                .document(book.darja)
                .collection("books")
                .document(book.id)

            val data = mapOf(
                "id" to book.id,
                "nameUrdu" to book.nameUrdu,
                "author" to book.author,
                "totalPages" to book.totalPages,
                "darja" to book.darja,
                "isCustom" to true,
                "lastReadPage" to book.lastReadPage,
                "localFilePath" to book.localFilePath
            )
            docRef.set(data).await()
            Log.d(TAG, "Custom book metadata synced to Firestore.")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync book to Firestore: ${e.message}")
        }
    }

    suspend fun deleteCustomBookMetadata(darja: String, bookId: String) {
        val firestore = db ?: return
        val userId = auth?.currentUser?.uid ?: return
        try {
            firestore.collection("users")
                .document(userId)
                .collection("customBooks")
                .document(darja)
                .collection("books")
                .document(bookId)
                .delete()
                .await()
            Log.d(TAG, "Custom book deleted from Firestore.")
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete custom book from Firestore: ${e.message}")
        }
    }

    // Upload custom book PDF to Storage
    suspend fun uploadPDFToStorage(file: File, bookId: String): String? {
        val store = storage ?: return null
        val userId = auth?.currentUser?.uid ?: return null
        try {
            val ref = store.reference.child("users/$userId/customBooks/$bookId.pdf")
            ref.putFile(Uri.fromFile(file)).await()
            val downloadUrl = ref.downloadUrl.await().toString()
            Log.d(TAG, "PDF uploaded successfully. URL: $downloadUrl")
            return downloadUrl
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload PDF: ${e.message}")
            return null
        }
    }

    // Sync notes to Firestore
    suspend fun syncNote(note: BookNote) {
        val firestore = db ?: return
        val userId = auth?.currentUser?.uid ?: return
        try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("notes")
                .document(note.bookId)
                .collection("userNotes")
                .document(note.id.toString())

            val data = mapOf(
                "id" to note.id,
                "bookId" to note.bookId,
                "pageNumber" to note.pageNumber,
                "noteText" to note.noteText,
                "highlightColor" to note.highlightColor,
                "selectedText" to note.selectedText,
                "timestamp" to note.timestamp
            )
            docRef.set(data).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync note: ${e.message}")
        }
    }

    suspend fun deleteNote(bookId: String, noteId: String) {
        val firestore = db ?: return
        val userId = auth?.currentUser?.uid ?: return
        try {
            firestore.collection("users")
                .document(userId)
                .collection("notes")
                .document(bookId)
                .collection("userNotes")
                .document(noteId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete note from firestore: ${e.message}")
        }
    }

    // Sync Bookmarks to Firestore
    suspend fun syncBookmark(bookmark: BookBookmark) {
        val firestore = db ?: return
        val userId = auth?.currentUser?.uid ?: return
        try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("bookmarks")
                .document(bookmark.bookId)
                .collection("userBookmarks")
                .document(bookmark.pageNumber.toString())

            val data = mapOf(
                "bookId" to bookmark.bookId,
                "pageNumber" to bookmark.pageNumber,
                "title" to bookmark.title,
                "timestamp" to bookmark.timestamp
            )
            docRef.set(data).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync bookmark: ${e.message}")
        }
    }

    suspend fun deleteBookmark(bookId: String, pageNumber: Int) {
        val firestore = db ?: return
        val userId = auth?.currentUser?.uid ?: return
        try {
            firestore.collection("users")
                .document(userId)
                .collection("bookmarks")
                .document(bookId)
                .collection("userBookmarks")
                .document(pageNumber.toString())
                .delete()
                .await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to delete bookmark from Firestore: ${e.message}")
        }
    }

    // Sync Tasbeeh
    suspend fun syncTasbeeh(tasbeeh: TasbeehHistory) {
        val firestore = db ?: return
        val userId = auth?.currentUser?.uid ?: return
        try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("tasbeeh")
                .document(tasbeeh.date)

            val data = mapOf(
                "dhikrKey" to tasbeeh.dhikrKey,
                "count" to tasbeeh.count,
                "date" to tasbeeh.date
            )
            docRef.set(data).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync tasbeeh: ${e.message}")
        }
    }

    // Sync Quiz score
    suspend fun syncQuizScore(quizScore: QuizScore) {
        val firestore = db ?: return
        val userId = auth?.currentUser?.uid ?: return
        try {
            val docRef = firestore.collection("users")
                .document(userId)
                .collection("quizScores")
                .document(quizScore.bookId)

            val data = mapOf(
                "bookId" to quizScore.bookId,
                "bookName" to quizScore.bookName,
                "score" to quizScore.score,
                "total" to quizScore.total,
                "timestamp" to quizScore.timestamp
            )
            docRef.set(data).await()
        } catch (e: Exception) {
            Log.w(TAG, "Failed to sync quiz score: ${e.message}")
        }
    }
}
