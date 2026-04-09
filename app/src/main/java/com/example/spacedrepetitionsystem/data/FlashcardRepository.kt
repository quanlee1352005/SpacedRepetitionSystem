package com.example.spacedrepetitionsystem.data

import com.example.spacedrepetitionsystem.data.model.Flashcard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class FlashcardRepository(private val flashcardDao: FlashcardDao) {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    
    val allCards: Flow<List<Flashcard>> = flashcardDao.getAllCards()

    // Lấy ID người dùng (Nếu chưa đăng nhập thì dùng "default_user")
    private val userId: String
        get() = auth.currentUser?.uid ?: "default_user"

    fun getCardsToReview(currentTime: Long): Flow<List<Flashcard>> {
        return flashcardDao.getCardsToReview(currentTime)
    }

    /**
     * Thêm thẻ mới: Lưu vào Room trước, sau đó đồng bộ lên Firebase
     */
    suspend fun insert(flashcard: Flashcard) {
        // 1. Lưu vào Room lấy ID tự tăng
        val localId = flashcardDao.insertCard(flashcard).toInt()
        
        // 2. Đồng bộ lên Firebase
        syncCardToCloud(flashcard.copy(id = localId))
    }

    /**
     * Cập nhật thẻ: Lưu vào Room, sau đó cập nhật Firebase
     */
    suspend fun update(flashcard: Flashcard) {
        flashcardDao.updateCard(flashcard)
        syncCardToCloud(flashcard)
    }

    /**
     * Xóa thẻ: Xóa Room, sau đó xóa Firebase
     */
    suspend fun delete(flashcard: Flashcard) {
        flashcardDao.deleteCard(flashcard)
        try {
            firestore.collection("users")
                .document(userId)
                .collection("flashcards")
                .document(flashcard.id.toString())
                .delete()
                .await()
        } catch (e: Exception) { e.printStackTrace() }
    }

    /**
     * Hàm phụ trợ đồng bộ dữ liệu lên Firebase Firestore
     */
    private suspend fun syncCardToCloud(flashcard: Flashcard) {
        try {
            val updatedCard = flashcard.copy(isSynced = true)
            firestore.collection("users")
                .document(userId)
                .collection("flashcards")
                .document(flashcard.id.toString())
                .set(updatedCard)
                .await()
            
            // Đánh dấu đã đồng bộ trong Room
            flashcardDao.updateCard(updatedCard)
        } catch (e: Exception) {
            e.printStackTrace()
            // Nếu lỗi mạng, vẫn giữ isSynced = false để đồng bộ sau
        }
    }

    /**
     * Tải toàn bộ dữ liệu từ Cloud về (Dùng khi đăng nhập máy mới)
     */
    suspend fun fetchFromCloud() {
        try {
            val snapshot = firestore.collection("users")
                .document(userId)
                .collection("flashcards")
                .get()
                .await()
            
            val cloudCards = snapshot.toObjects(Flashcard::class.java)
            for (card in cloudCards) {
                flashcardDao.insertCard(card)
            }
        } catch (e: Exception) { e.printStackTrace() }
    }
}
