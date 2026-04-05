package com.example.cuoiky

import androidx.compose.runtime.*
import androidx.compose.material3.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun FlashcardScreen() {

    val allCards = remember {
        mutableStateListOf(
            Flashcard("Hello", "Xin chào"),
            Flashcard("Apple", "Quả táo"),
            Flashcard("Dog", "Con chó")
        )
    }

    var index by remember { mutableStateOf(0) }
    var showAnswer by remember { mutableStateOf(false) }

    // 🔥 lọc card cần học
    val cards = allCards.filter {
        it.nextReview == 0L || it.nextReview <= System.currentTimeMillis()
    }

    // 🔥 nếu không có card
    if (cards.isEmpty()) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🎉 Không có thẻ cần học!", fontSize = 22.sp)
        }
        return
    }

    val currentCard = cards[index % cards.size]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {

        Text("Flashcard Learning", fontSize = 26.sp)

        // phần card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (showAnswer) Color(0xFFD0F0C0) else Color(0xFFE3F2FD)
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (showAnswer) currentCard.back else currentCard.front,
                    fontSize = 32.sp
                )
            }
        }

        Button(
            onClick = { showAnswer = !showAnswer },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Flip Card")
        }


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(onClick = {
                index--
                showAnswer = false
            }) {
                Text("Prev")
            }

            Button(onClick = {
                index++
                showAnswer = false
            }) {
                Text("Next")
            }
        }

        // đặt lịch
        Column {

            Text("📅 Set Review Schedule", fontSize = 18.sp)

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {

                Button(onClick = {
                    currentCard.nextReview =
                        System.currentTimeMillis() + 1 * 24 * 60 * 60 * 1000
                }) {
                    Text("1 day")
                }

                Button(onClick = {
                    currentCard.nextReview =
                        System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000
                }) {
                    Text("3 days")
                }

                Button(onClick = {
                    currentCard.nextReview =
                        System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000
                }) {
                    Text("7 days")
                }
            }
        }

        // hiển thị lịch
        Text(
            text = "Next review: ${
                if (currentCard.nextReview == 0L)
                    "Chưa đặt"
                else
                    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                        .format(Date(currentCard.nextReview))
            }"
        )
    }
}