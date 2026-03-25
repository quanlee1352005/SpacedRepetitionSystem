package com.example.spacedrepetitionsystem.data

import androidx.room.*
import com.example.spacedrepetitionsystem.data.model.Flashcard
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE nextDueDate <= :currentTime ORDER BY nextDueDate ASC")
    fun getCardsToReview(currentTime: Long): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards")
    fun getAllCards(): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(flashcard: Flashcard): Long // Trả về ID để dùng cho đồng bộ

    @Update
    suspend fun updateCard(flashcard: Flashcard)

    @Delete
    suspend fun deleteCard(flashcard: Flashcard)
}
