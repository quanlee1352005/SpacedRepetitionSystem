package com.example.spacedrepetitionsystem.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen(
    viewModel: FlashcardViewModel,
    userName: String = "Minh",
    onStartReview: () -> Unit,
    onCreateNewDeck: () -> Unit,
    onAddCardToDeck: (String) -> Unit,
    onSync: () -> Unit
) {
    val decks by viewModel.decks.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFDEFD9))
    ) {
        HeaderSection(userName, onSync)
        
        // Quick Actions (Đổi tên thành Tạo bộ thẻ)
        QuickActionsSection(onStartReview, onCreateNewDeck)

        Text(
            text = "Các bộ thẻ của bạn",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
            fontWeight = FontWeight.Bold
        )

        if (decks.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có bộ thẻ nào. Hãy tạo bộ thẻ mới!")
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(decks) { deck ->
                    DeckCard(deck, onClick = { onAddCardToDeck(deck.name) })
                }
            }
        }
    }
}

@Composable
fun DeckCard(deck: DeckInfo, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(deck.color)),
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Text(text = deck.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Column {
                Text(text = "${deck.cardCount} thẻ", color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { 1f }, 
                    modifier = Modifier.fillMaxWidth().height(4.dp), 
                    color = Color.White, 
                    trackColor = Color.White.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = if (deck.dueCount > 0) "Cần ôn tập: ${deck.dueCount}" else "Nhấn để thêm thẻ",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
fun HeaderSection(userName: String, onSync: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Column {
            Text(text = "Flashcard", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE67E22))
            Text(text = "Chào mừng, $userName!", style = MaterialTheme.typography.titleMedium)
        }
        IconButton(onClick = onSync) { Icon(Icons.Default.CloudDownload, contentDescription = "Sync", tint = Color(0xFFE67E22)) }
    }
}

@Composable
fun QuickActionsSection(onStartReview: () -> Unit, onCreateNewDeck: () -> Unit) {
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Card(modifier = Modifier.weight(1f).clickable { onStartReview() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.RocketLaunch, contentDescription = null, tint = Color(0xFFE67E22), modifier = Modifier.size(32.dp))
                Text(text = "Học ngay", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
        Card(modifier = Modifier.weight(1f).clickable { onCreateNewDeck() }, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
            Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Add, contentDescription = null, tint = Color(0xFFE67E22), modifier = Modifier.size(32.dp))
                Text(text = "Tạo bộ thẻ", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }
}
