package com.example.spacedrepetitionsystem.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(viewModel: FlashcardViewModel) {
    val flashcards by viewModel.allCards.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredCards = flashcards.filter {
        it.front.contains(searchQuery, ignoreCase = true) || it.back.contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            placeholder = { Text("Tìm kiếm thẻ...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = MaterialTheme.shapes.medium
        )

        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(filteredCards) { card ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(text = card.front, style = MaterialTheme.typography.titleMedium)
                        Text(text = card.back, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                        Text(text = "Bộ thẻ: ${card.deckName}", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }
        }
    }
}
