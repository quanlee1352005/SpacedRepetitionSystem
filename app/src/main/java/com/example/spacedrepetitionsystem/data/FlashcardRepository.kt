package com.example.spacedrepetitionsystem.data

import com.example.spacedrepetitionsystem.data.model.Flashcard
import kotlinx.coroutines.flow.Flow

class FlashcardRepository(private val flashcardDao: FlashcardDao) {

    val allCards: Flow<List<Flashcard>> = flashcardDao.getAllCards()

    fun getCardsToReview(currentTime: Long): Flow<List<Flashcard>> {
        return flashcardDao.getCardsToReview(currentTime)
    }

    suspend fun insert(flashcard: Flashcard) {
        flashcardDao.insertCard(flashcard)
    }

    suspend fun update(flashcard: Flashcard) {
        flashcardDao.updateCard(flashcard)
    }

    suspend fun delete(flashcard: Flashcard) {
        flashcardDao.deleteCard(flashcard)
    }
}
