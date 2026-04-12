package com.example.spacedrepetitionsystem.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.spacedrepetitionsystem.data.FlashcardRepository
import com.example.spacedrepetitionsystem.data.SM2Calculator
import com.example.spacedrepetitionsystem.data.model.Flashcard
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.abs

data class DeckInfo(val name: String, val cardCount: Int, val dueCount: Int, val color: Long)

class FlashcardViewModel(private val repository: FlashcardRepository) : ViewModel() {

    private val auth = FirebaseAuth.getInstance()

    // Luồng lắng nghe trạng thái đăng nhập thay đổi để xóa/hiện thẻ ngay lập tức
    private val userIdFlow = callbackFlow<String> {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser?.uid ?: "")
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val allCards: StateFlow<List<Flashcard>> = userIdFlow.flatMapLatest { uid ->
        if (uid.isEmpty()) flowOf(emptyList<Flashcard>())
        else repository.getUserCards(uid)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val decks: StateFlow<List<DeckInfo>> = allCards.map { cards ->
        cards.groupBy { it.deckName }.map { (name, deckCards) ->
            val currentTime = System.currentTimeMillis()
            DeckInfo(
                name = name,
                cardCount = deckCards.size,
                dueCount = deckCards.count { it.nextDueDate <= currentTime },
                color = deckCards.firstOrNull()?.colorHex ?: generateColorFromName(name)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getCardsToReviewByDeck(deckName: String): Flow<List<Flashcard>> {
        val currentTime = System.currentTimeMillis()
        return allCards.map { cards ->
            cards.filter { it.deckName == deckName && it.nextDueDate <= currentTime }
        }
    }

    val learnCards: Flow<List<Flashcard>> = allCards.map { cards ->
        val currentTime = System.currentTimeMillis()
        cards.filter { it.nextDueDate <= currentTime }
    }

    val reviewDifficultCards: Flow<List<Flashcard>> = allCards.map { cards ->
        cards.filter { it.easeFactor < 2.0 || (it.repetitions == 0 && it.id != 0) }
    }

    fun getQuizCards(): List<Flashcard> {
        return allCards.value.shuffled().take(10)
    }

    fun deleteDeck(deckName: String) {
        viewModelScope.launch { repository.deleteDeck(deckName) }
    }

    fun addFlashcard(front: String, back: String, deckName: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            val color = generateColorFromName(deckName)
            val newCard = Flashcard(id = 0, userId = uid, front = front, back = back, deckName = deckName, colorHex = color)
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
        viewModelScope.launch { repository.fetchFromCloud() }
    }

    private fun generateColorFromName(name: String): Long {
        val colors = listOf(0xFF42A5F5, 0xFF66BB6A, 0xFFFFA726, 0xFFAB47BC, 0xFFEF5350, 0xFF26A69A, 0xFF78909C)
        val index = abs(name.hashCode()) % colors.size
        return colors[index]
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
