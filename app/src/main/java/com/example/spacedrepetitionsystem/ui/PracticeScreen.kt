package com.example.spacedrepetitionsystem.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PracticeScreen(viewModel: FlashcardViewModel) {
    val allCards by viewModel.allCards.collectAsState()
    val currentTime = System.currentTimeMillis()
    val dueCount = allCards.count { it.nextDueDate <= currentTime }
    val learnedCount = allCards.count { it.repetitions > 0 }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Tiến độ học tập",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // Biểu đồ thống kê đơn giản
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                StatRow("Tổng số thẻ", allCards.size.toString(), Color.Gray)
                StatRow("Đã thuộc", learnedCount.toString(), Color(0xFF66BB6A))
                StatRow("Cần ôn tập", dueCount.toString(), Color(0xFFE67E22))
            }
        }

        Text(
            text = "Chế độ luyện tập",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Start).padding(bottom = 16.dp)
        )

        PracticeModeCard("Ôn tập SRS", "Học các thẻ đến hạn", Icons.Default.Psychology, Color(0xFFE67E22))
        PracticeModeCard("Học thẻ mới", "Xem các thẻ chưa học", Icons.Default.Assignment, Color(0xFF42A5F5))
        PracticeModeCard("Kiểm tra", "Làm bài test trắc nghiệm", Icons.Default.BarChart, Color(0xFF66BB6A))
    }
}

@Composable
fun StatRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge)
        Text(text = value, style = MaterialTheme.typography.titleLarge, color = color, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun PracticeModeCard(title: String, desc: String, icon: ImageVector, color: Color) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = color.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.size(48.dp)
            ) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.padding(8.dp))
            }
            Column(modifier = Modifier.padding(start = 16.dp)) {
                Text(text = title, fontWeight = FontWeight.Bold)
                Text(text = desc, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
            }
        }
    }
}
