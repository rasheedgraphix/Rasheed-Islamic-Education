package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        LocalBook::class,
        BookNote::class,
        BookBookmark::class,
        TasbeehHistory::class,
        QuizScore::class
    ],
    version = 1,
    exportSchema = false
)
abstract class IslamicDatabase : RoomDatabase() {
    abstract fun bookDao(): BookDao
    abstract fun bookNoteDao(): BookNoteDao
    abstract fun bookBookmarkDao(): BookBookmarkDao
    abstract fun tasbeehDao(): TasbeehDao
    abstract fun quizScoreDao(): QuizScoreDao

    companion object {
        @Volatile
        private var INSTANCE: IslamicDatabase? = null

        fun getDatabase(context: Context): IslamicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    IslamicDatabase::class.java,
                    "islamic_education_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
