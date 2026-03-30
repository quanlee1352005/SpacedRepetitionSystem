package com.example.spacedrepetitionsystem.data

import androidx.room.*
import com.example.spacedrepetitionsystem.data.model.Flashcard
import kotlinx.coroutines.flow.Flow

@Dao
interface FlashcardDao {

    /**
     * Lấy danh sách thẻ cần học trong ngày (nextDueDate <= thời gian hiện tại)
     */
    @Query("SELECT * FROM flashcards WHERE nextDueDate <= :currentTime ORDER BY nextDueDate ASC")
    fun getCardsToReview(currentTime: Long): Flow<List<Flashcard>>

    /**
     * Lấy toàn bộ thẻ
     */
    @Query("SELECT * FROM flashcards")
    fun getAllCards(): Flow<List<Flashcard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(flashcard: Flashcard)

    @Update
    suspend fun updateCard(flashcard: Flashcard)

    @Delete
    suspend fun deleteCard(flashcard: Flashcard)
}
