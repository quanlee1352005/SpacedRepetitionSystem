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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    userName: String = "Minh",
    onStartReview: () -> Unit,
    onAddCard: () -> Unit,
    onSync: () -> Unit
) {
    Scaffold(
        bottomBar = { AppBottomNavigation() }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFFFDEFD9)) // Màu nền nhạt
        ) {
            // Header
            HeaderSection(userName, onSync)

            // Quick Actions
            QuickActionsSection(onStartReview, onAddCard)

            // Decks List
            Text(
                text = "Các bộ thẻ của bạn",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                fontWeight = FontWeight.Bold
            )

            val decks = listOf(
                DeckItemData("Tiếng Anh Giao Tiếp", "80 thẻ", "Đang học: 25%", Color(0xFF42A5F5), Icons.Default.Translate),
                DeckItemData("Từ vựng JLPT N3", "120 thẻ", "Học: 50 thẻ mới", Color(0xFF66BB6A), Icons.Default.Eco),
                DeckItemData("Sự kiện Lịch sử", "45 thẻ", "Đã hoàn thành", Color(0xFFFFA726), Icons.Default.Museum),
                DeckItemData("Công thức Toán", "85 thẻ", "Cần ôn tập", Color(0xFFAB47BC), Icons.Default.Functions)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(decks) { deck ->
                    DeckCard(deck)
                }
            }
        }
    }
}

@Composable
fun HeaderSection(userName: String, onSync: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = "Flashcard", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = Color(0xFFE67E22))
            Text(text = "Chào mừng, $userName!", style = MaterialTheme.typography.titleMedium)
        }
        IconButton(onClick = onSync) {
            Icon(Icons.Default.CloudDownload, contentDescription = "Sync", tint = Color(0xFFE67E22))
        }
    }
}

@Composable
fun QuickActionsSection(onStartReview: () -> Unit, onAddCard: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        QuickActionButton("Học ngay", Icons.Default.RocketLaunch, Color(0xFFE67E22), Modifier.weight(1f), onStartReview)
        QuickActionButton("Thêm thẻ", Icons.Default.Add, Color(0xFFE67E22), Modifier.weight(1f), onAddCard)
    }
}

@Composable
fun QuickActionButton(text: String, icon: ImageVector, color: Color, modifier: Modifier, onClick: () -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = modifier.clickable { onClick() },
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(32.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}

@Composable
fun DeckCard(deck: DeckItemData) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = deck.color),
        modifier = Modifier.fillMaxWidth().height(160.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = deck.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp, modifier = Modifier.weight(1f))
                Icon(deck.icon, contentDescription = null, tint = Color.White.copy(alpha = 0.7f))
            }
            Column {
                Text(text = deck.count, color = Color.White.copy(alpha = 0.9f), fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(progress = { 0.4f }, modifier = Modifier.fillMaxWidth().height(4.dp), color = Color.White, trackColor = Color.White.copy(alpha = 0.3f))
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = deck.status, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
        }
    }
}

@Composable
fun AppBottomNavigation() {
    NavigationBar(containerColor = Color.White) {
        NavigationBarItem(selected = true, onClick = {}, icon = { Icon(Icons.Default.Home, "Home") }, label = { Text("Trang chủ") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Book, "Library") }, label = { Text("Thư viện") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.AddCircle, "Add", tint = Color(0xFFE67E22), modifier = Modifier.size(40.dp)) }, label = {})
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.CheckCircle, "Practice") }, label = { Text("Luyện tập") })
        NavigationBarItem(selected = false, onClick = {}, icon = { Icon(Icons.Default.Person, "Account") }, label = { Text("Tài khoản") })
    }
}

data class DeckItemData(val name: String, val count: String, val status: String, val color: Color, val icon: ImageVector)
