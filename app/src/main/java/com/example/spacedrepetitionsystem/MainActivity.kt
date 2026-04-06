package com.example.spacedrepetitionsystem

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.*
import androidx.compose.runtime.*
import com.example.spacedrepetitionsystem.data.AppDatabase
import com.example.spacedrepetitionsystem.data.FlashcardRepository
import com.example.spacedrepetitionsystem.ui.DashboardScreen
import com.example.spacedrepetitionsystem.ui.FlashcardViewModel
import com.example.spacedrepetitionsystem.ui.FlashcardViewModelFactory
import com.example.spacedrepetitionsystem.ui.ReviewScreen
import com.example.spacedrepetitionsystem.ui.theme.SpacedRepetitionSystemTheme
import com.example.spacedrepetitionsystem.worker.ReminderWorker
import com.google.firebase.auth.FirebaseAuth
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.*
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity(), TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        tts = TextToSpeech(this, this)
        signInAnonymously()
        setupWorkManager()

        val database by lazy { AppDatabase.getDatabase(this) }
        val repository by lazy { FlashcardRepository(database.flashcardDao()) }
        val viewModel: FlashcardViewModel by viewModels { FlashcardViewModelFactory(repository) }

        setContent {
            SpacedRepetitionSystemTheme {
                var currentScreen by remember { mutableStateOf("dashboard") }
                var showAddDialog by remember { mutableStateOf(false) }

                when (currentScreen) {
                    "dashboard" -> DashboardScreen(
                        viewModel = viewModel, // Đã thêm viewModel vào đây
                        userName = auth.currentUser?.displayName ?: "Minh",
                        onStartReview = { currentScreen = "review" },
                        onAddCard = { showAddDialog = true },
                        onSync = { 
                            viewModel.syncFromCloud()
                            Toast.makeText(this, "Đang đồng bộ...", Toast.LENGTH_SHORT).show()
                        }
                    )
                    "review" -> ReviewScreen(
                        viewModel = viewModel,
                        onSpeak = { speak(it) },
                        onFinish = { currentScreen = "dashboard" }
                    )
                }

                if (showAddDialog) {
                    AddCardDialog(
                        onDismiss = { showAddDialog = false },
                        onConfirm = { front, back, deck -> 
                            viewModel.addFlashcard(front, back, deck) // Đã truyền đủ 3 tham số
                            showAddDialog = false 
                        }
                    )
                }
            }
        }
    }

    private fun signInAnonymously() {
        if (auth.currentUser == null) {
            auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Đã kết nối Cloud!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupWorkManager() {
        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(8, TimeUnit.HOURS).build()
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            "SRS_Reminder_Work",
            ExistingPeriodicWorkPolicy.KEEP,
            workRequest
        )
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) tts?.language = Locale.US
    }

    private fun speak(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        super.onDestroy()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardDialog(onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    var deck by remember { mutableStateOf("Mặc định") } // Thêm state cho bộ thẻ

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Thêm thẻ mới") },
        text = {
            androidx.compose.foundation.layout.Column {
                OutlinedTextField(value = front, onValueChange = { front = it }, label = { Text("Mặt trước (Câu hỏi)") })
                OutlinedTextField(value = back, onValueChange = { back = it }, label = { Text("Mặt sau (Đáp án)") })
                OutlinedTextField(value = deck, onValueChange = { deck = it }, label = { Text("Tên bộ thẻ") })
            }
        },
        confirmButton = { 
            Button(onClick = { 
                if (front.isNotBlank() && back.isNotBlank() && deck.isNotBlank()) {
                    onConfirm(front, back, deck)
                }
            }) { Text("Lưu") } 
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Hủy") }
        }
    )
}
