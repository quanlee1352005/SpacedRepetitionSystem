package com.example.spacedrepetitionsystem.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.spacedrepetitionsystem.data.model.Flashcard

@Database(entities = [Flashcard::class], version = 2, exportSchema = false) // Tăng version lên 2
abstract class AppDatabase : RoomDatabase() {
    abstract fun flashcardDao(): FlashcardDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "flashcard_database"
                )
                .fallbackToDestructiveMigration() // Cho phép xóa data cũ khi đổi cấu trúc
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
