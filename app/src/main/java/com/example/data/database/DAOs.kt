package com.example.data.database

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {
    @Query("SELECT * FROM books ORDER BY isCustom ASC, nameUrdu ASC")
    fun getAllBooks(): Flow<List<LocalBook>>

    @Query("SELECT * FROM books WHERE darja = :darja ORDER BY isCustom ASC, nameUrdu ASC")
    fun getBooksByDarja(darja: String): Flow<List<LocalBook>>

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    suspend fun getBookById(id: String): LocalBook?

    @Query("SELECT * FROM books WHERE id = :id LIMIT 1")
    fun getBookByIdFlow(id: String): Flow<LocalBook?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBook(book: LocalBook)

    @Update
    suspend fun updateBook(book: LocalBook)

    @Query("UPDATE books SET lastReadPage = :page WHERE id = :id")
    suspend fun updateLastReadPage(id: String, page: Int)

    @Query("UPDATE books SET downloadStatus = :status, downloadProgress = :progress, localFilePath = :filePath WHERE id = :id")
    suspend fun updateDownloadStatus(id: String, status: String, progress: Int, filePath: String?)

    @Delete
    suspend fun deleteBook(book: LocalBook)

    @Query("DELETE FROM books WHERE darja = 'Thania' AND isCustom = 0")
    suspend fun deleteDefaultThaniaBooks()
}

@Dao
interface BookNoteDao {
    @Query("SELECT * FROM book_notes WHERE bookId = :bookId ORDER BY pageNumber ASC, timestamp DESC")
    fun getNotesForBook(bookId: String): Flow<List<BookNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: BookNote)

    @Delete
    suspend fun deleteNote(note: BookNote)

    @Query("DELETE FROM book_notes WHERE id = :noteId")
    suspend fun deleteNoteById(noteId: Int)
}

@Dao
interface BookBookmarkDao {
    @Query("SELECT * FROM book_bookmarks WHERE bookId = :bookId ORDER BY pageNumber ASC")
    fun getBookmarksForBook(bookId: String): Flow<List<BookBookmark>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookBookmark)

    @Delete
    suspend fun deleteBookmark(bookmark: BookBookmark)

    @Query("DELETE FROM book_bookmarks WHERE bookId = :bookId AND pageNumber = :page")
    suspend fun deleteBookmarkAtPage(bookId: String, page: Int)
}

@Dao
interface TasbeehDao {
    @Query("SELECT * FROM tasbeeh_history ORDER BY date DESC")
    fun getAllTasbeehHistory(): Flow<List<TasbeehHistory>>

    @Query("SELECT * FROM tasbeeh_history WHERE date = :date")
    fun getTasbeehForDate(date: String): Flow<List<TasbeehHistory>>

    @Query("SELECT * FROM tasbeeh_history WHERE synced = 0")
    suspend fun getUnsyncedTasbeeh(): List<TasbeehHistory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasbeeh(tasbeeh: TasbeehHistory)

    @Query("UPDATE tasbeeh_history SET count = count + :count WHERE dhikrKey = :dhikrKey AND date = :date")
    suspend fun incrementTasbeehCount(dhikrKey: String, date: String, count: Int): Int

    @Query("UPDATE tasbeeh_history SET synced = 1 WHERE id IN (:ids)")
    suspend fun markSynced(ids: List<Int>)
}

@Dao
interface QuizScoreDao {
    @Query("SELECT * FROM quiz_scores ORDER BY timestamp DESC")
    fun getAllQuizScores(): Flow<List<QuizScore>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizScore(score: QuizScore)
}
