package com.example.spacedrepetitionsystem.data

import com.example.spacedrepetitionsystem.data.model.Flashcard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

class FlashcardRepository(private val flashcardDao: FlashcardDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    // Hàm lấy danh sách thẻ theo userId
    fun getUserCards(userId: String): Flow<List<Flashcard>> {
        if (userId.isEmpty()) return flowOf(emptyList())
        return flashcardDao.getAllCards(userId)
    }

    private val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    suspend fun insert(flashcard: Flashcard) {
        if (currentUserId.isEmpty()) return
        val localId = flashcardDao.insertCard(flashcard.copy(userId = currentUserId)).toInt()
        syncCardToCloud(flashcard.copy(id = localId, userId = currentUserId))
    }

    suspend fun update(flashcard: Flashcard) {
        if (currentUserId.isEmpty()) return
        flashcardDao.updateCard(flashcard)
        syncCardToCloud(flashcard)
    }

    suspend fun delete(flashcard: Flashcard) {
        if (currentUserId.isEmpty()) return
        flashcardDao.deleteCard(flashcard)
        try {
            firestore.collection("users").document(currentUserId)
                .collection("flashcards").document(flashcard.id.toString())
                .delete().await()
        } catch (e: Exception) { e.printStackTrace() }
    }

    private suspend fun syncCardToCloud(flashcard: Flashcard) {
        try {
            val updatedCard = flashcard.copy(isSynced = true)
            firestore.collection("users").document(currentUserId)
                .collection("flashcards").document(flashcard.id.toString())
                .set(updatedCard).await()
            flashcardDao.updateCard(updatedCard)
        } catch (e: Exception) { e.printStackTrace() }
    }

    suspend fun fetchFromCloud() {
        if (currentUserId.isEmpty()) return
        try {
            val snapshot = firestore.collection("users").document(currentUserId)
                .collection("flashcards").get().await()
            val cloudCards = snapshot.toObjects(Flashcard::class.java)
            for (card in cloudCards) {
                flashcardDao.insertCard(card.copy(userId = currentUserId))
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}
