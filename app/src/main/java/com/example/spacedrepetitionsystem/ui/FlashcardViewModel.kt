package com.example.spacedrepetitionsystem.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spacedrepetitionsystem.data.FlashcardRepository
import com.example.spacedrepetitionsystem.data.SM2Calculator
import com.example.spacedrepetitionsystem.data.model.Flashcard
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class DeckInfo(
    val name: String,
    val cardCount: Int,
    val dueCount: Int,
    val color: Long
)

class FlashcardViewModel(private val repository: FlashcardRepository) : ViewModel() {

    val allCards: StateFlow<List<Flashcard>> = repository.allCards.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Lấy danh sách các bộ thẻ kèm thông tin số lượng
    val decks: StateFlow<List<DeckInfo>> = allCards.map { cards ->
        cards.groupBy { it.deckName }.map { (name, deckCards) ->
            val currentTime = System.currentTimeMillis()
            DeckInfo(
                name = name,
                cardCount = deckCards.size,
                dueCount = deckCards.count { it.nextDueDate <= currentTime },
                color = deckCards.firstOrNull()?.colorHex ?: 0xFF42A5F5
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addFlashcard(front: String, back: String, deckName: String) {
        viewModelScope.launch {
            val color = when(deckName) {
                "Tiếng Anh" -> 0xFF42A5F5
                "Tiếng Nhật" -> 0xFF66BB6A
                "Lịch sử" -> 0xFFFFA726
                else -> 0xFFAB47BC
            }
            val newCard = Flashcard(front = front, back = back, deckName = deckName, colorHex = color)
            repository.insert(newCard)
        }
    }

    fun reviewCard(flashcard: Flashcard, quality: Int) {
        viewModelScope.launch {
            val updatedCard = SM2Calculator.calculate(flashcard, quality)
            repository.update(updatedCard)
        }
    }

    fun syncFromCloud() {
        viewModelScope.launch {
            repository.fetchFromCloud()
        }
    }
}

class FlashcardViewModelFactory(private val repository: FlashcardRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FlashcardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return FlashcardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
