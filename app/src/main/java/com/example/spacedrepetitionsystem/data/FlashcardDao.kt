package com.example.spacedrepetitionsystem.data

import androidx.room.*
import com.example.spacedrepetitionsystem.data.model.Flashcard
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {
    @Query("SELECT * FROM flashcards WHERE userId = :userId AND nextDueDate <= :currentTime ORDER BY nextDueDate ASC")
    fun getCardsToReview(userId: String, currentTime: Long): Flow<List<Flashcard>>

    @Query("SELECT * FROM flashcards WHERE userId = :userId")
    fun getAllCards(userId: String): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(flashcard: Flashcard): Long

    @Update
    suspend fun updateCard(flashcard: Flashcard)

    @Delete
    suspend fun deleteCard(flashcard: Flashcard)

    // Xóa nguyên bộ thẻ
    @Query("DELETE FROM flashcards WHERE userId = :userId AND deckName = :deckName")
    suspend fun deleteDeck(userId: String, deckName: String)
}
