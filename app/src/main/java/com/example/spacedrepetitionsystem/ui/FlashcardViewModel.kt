package com.example.spacedrepetitionsystem.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spacedrepetitionsystem.data.FlashcardRepository
import com.example.spacedrepetitionsystem.data.SM2Calculator
import com.example.spacedrepetitionsystem.data.model.Flashcard
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FlashcardViewModel(private val repository: FlashcardRepository) : ViewModel() {

    // Danh sách toàn bộ thẻ học
    val allCards: StateFlow<List<Flashcard>> = repository.allCards.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Thêm một thẻ mới
    fun addFlashcard(front: String, back: String) {
        viewModelScope.launch {
            val newCard = Flashcard(front = front, back = back)
            repository.insert(newCard)
        }
    }

    // Cập nhật khi người dùng đánh giá mức độ nhớ
    fun reviewCard(flashcard: Flashcard, quality: Int) {
        viewModelScope.launch {
            val updatedCard = SM2Calculator.calculate(flashcard, quality)
            repository.update(updatedCard)
        }
    }

    // Xóa thẻ
    fun deleteCard(flashcard: Flashcard) {
        viewModelScope.launch {
            repository.delete(flashcard)
        }
    }
}

// Factory để khởi tạo ViewModel với Repository
class FlashcardViewModelFactory(private val repository: FlashcardRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlashcardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FlashcardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
