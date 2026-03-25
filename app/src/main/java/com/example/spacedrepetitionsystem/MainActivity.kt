package com.example.spacedrepetitionsystem

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spacedrepetitionsystem.data.AppDatabase
import com.example.spacedrepetitionsystem.data.FlashcardRepository
import com.example.spacedrepetitionsystem.data.model.Flashcard
import com.example.spacedrepetitionsystem.ui.FlashcardViewModel
import com.example.spacedrepetitionsystem.ui.FlashcardViewModelFactory
import com.example.spacedrepetitionsystem.ui.theme.SpacedRepetitionSystemTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val database by lazy { AppDatabase.getDatabase(this) }
        val repository by lazy { FlashcardRepository(database.flashcardDao()) }
        val viewModel: FlashcardViewModel by viewModels { FlashcardViewModelFactory(repository) }

        setContent {
            SpacedRepetitionSystemTheme {
                var isReviewing by remember { mutableStateOf(false) }
                
                if (isReviewing) {
                    ReviewScreen(
                        viewModel = viewModel,
                        onFinish = { isReviewing = false }
                    )
                } else {
                    MainScreen(
                        viewModel = viewModel,
                        onStartReview = { isReviewing = true }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: FlashcardViewModel, onStartReview: () -> Unit) {
    val flashcards by viewModel.allCards.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Flashcard SRS") }) },
        floatingActionButton = {
            Column {
                FloatingActionButton(
                    onClick = onStartReview,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Start Review")
                }
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Flashcard")
                }
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            Text(
                text = "Danh sách thẻ (${flashcards.size})",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
            
            if (flashcards.isEmpty()) {
                Text(text = "Chưa có thẻ nào.", modifier = Modifier.padding(16.dp))
            } else {
                LazyColumn {
                    items(flashcards) { card ->
                        FlashcardItem(card)
                    }
                }
            }
        }

        if (showAddDialog) {
            AddCardDialog(
                onDismiss = { showAddDialog = false },
                onConfirm = { front, back ->
                    viewModel.addFlashcard(front, back)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun ReviewScreen(viewModel: FlashcardViewModel, onFinish: () -> Unit) {
    val allCards by viewModel.allCards.collectAsState()
    val currentTime = System.currentTimeMillis()
    // Lọc các thẻ đến hạn học (nextDueDate <= hiện tại)
    val reviewList = remember(allCards) {
        allCards.filter { it.nextDueDate <= currentTime }
    }

    var currentIndex by remember { mutableIntStateOf(0) }
    var showBack by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { 
            CenterAlignedTopAppBar(title = { Text("Đang học (${currentIndex + 1}/${reviewList.size})") }) 
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
            if (reviewList.isEmpty() || currentIndex >= reviewList.size) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Tuyệt vời! Bạn đã hoàn thành bài học hôm nay.")
                    Button(onClick = onFinish, modifier = Modifier.padding(top = 16.dp)) {
                        Text("Quay lại")
                    }
                }
            } else {
                val currentCard = reviewList[currentIndex]
                Column(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        onClick = { showBack = !showBack }
                    ) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = if (showBack) currentCard.back else currentCard.front,
                                style = MaterialTheme.typography.headlineMedium
                            )
                        }
                    }
                    
                    Text(
                        text = if (showBack) "Nhấn vào thẻ để xem câu hỏi" else "Nhấn vào thẻ để xem đáp án",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    if (showBack) {
                        Text(
                            text = "Bạn nhớ thẻ này ở mức nào?",
                            modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            // Các mức độ đánh giá: 1 (Quên) -> 5 (Rất nhớ)
                            listOf(1, 3, 5).forEach { quality ->
                                Button(onClick = {
                                    viewModel.reviewCard(currentCard, quality)
                                    showBack = false
                                    currentIndex++
                                }) {
                                    Text(when(quality) {
                                        1 -> "Quên"
                                        3 -> "Khó"
                                        else -> "Dễ"
                                    })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FlashcardItem(card: Flashcard) {
    Card(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Q: ${card.front}")
            Text(text = "A: ${card.back}", color = MaterialTheme.colorScheme.secondary)
        }
    }
}

@Composable
fun AddCardDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm thẻ mới") },
        text = {
            Column {
                OutlinedTextField(value = front, onValueChange = { front = it }, label = { Text("Mặt trước") })
                OutlinedTextField(value = back, onValueChange = { back = it }, label = { Text("Mặt sau") })
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(front, back) }) { Text("Lưu") }
        }
    )
}
