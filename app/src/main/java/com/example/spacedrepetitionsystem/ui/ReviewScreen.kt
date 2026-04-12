package com.example.spacedrepetitionsystem.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spacedrepetitionsystem.data.model.Flashcard
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VolumeUp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewScreen(
    reviewList: List<Flashcard>, // Thêm tham số này để nhận danh sách thẻ từ MainActivity
    viewModel: FlashcardViewModel,
    onSpeak: (String) -> Unit,
    onFinish: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var showBack by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { 
            CenterAlignedTopAppBar(title = { 
                Text("Đang học (${if (reviewList.isEmpty()) 0 else currentIndex + 1}/${reviewList.size})") 
            })
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize(), contentAlignment = Alignment.Center) {
            if (reviewList.isEmpty() || currentIndex >= reviewList.size) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Hoàn thành bài học!")
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
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = if (showBack) currentCard.back else currentCard.front,
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                IconButton(onClick = { onSpeak(if (showBack) currentCard.back else currentCard.front) }) {
                                    Icon(Icons.Default.VolumeUp, contentDescription = "Speak")
                                }
                            }
                        }
                    }
                    if (showBack) {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
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
