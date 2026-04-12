package com.example.spacedrepetitionsystem

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.spacedrepetitionsystem.data.AppDatabase
import com.example.spacedrepetitionsystem.data.FlashcardRepository
import com.example.spacedrepetitionsystem.data.model.Flashcard
import com.example.spacedrepetitionsystem.ui.*
import com.example.spacedrepetitionsystem.ui.theme.SpacedRepetitionSystemTheme
import com.example.spacedrepetitionsystem.worker.ReminderWorker
import com.google.firebase.auth.FirebaseAuth
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
                var currentTab by remember { mutableStateOf("home") }
                var isReviewing by remember { mutableStateOf(false) }
                var showAddDialog by remember { mutableStateOf(false) }
                var prefilledDeckName by remember { mutableStateOf("") }
                
                var currentReviewList by remember { mutableStateOf<List<Flashcard>>(emptyList()) }
                val scope = rememberCoroutineScope()

                if (isReviewing) {
                    ReviewScreen(
                        reviewList = currentReviewList,
                        viewModel = viewModel,
                        onSpeak = { speak(it) },
                        onFinish = { isReviewing = false }
                    )
                } else {
                    Scaffold(
                        bottomBar = {
                            AppBottomNavigation(
                                currentTab = currentTab,
                                onTabSelected = { currentTab = it },
                                onAddClick = { 
                                    prefilledDeckName = ""
                                    showAddDialog = true 
                                }
                            )
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentTab) {
                                "home" -> DashboardScreen(
                                    viewModel = viewModel,
                                    userName = auth.currentUser?.email ?: "Người dùng",
                                    onStartReviewByDeck = { deckName ->
                                        scope.launch {
                                            currentReviewList = if (deckName == "Tất cả") {
                                                viewModel.learnCards.first()
                                            } else {
                                                viewModel.getCardsToReviewByDeck(deckName).first()
                                            }
                                            isReviewing = true
                                        }
                                    },
                                    onCreateNewDeck = { 
                                        prefilledDeckName = ""
                                        showAddDialog = true 
                                    },
                                    onAddCardToDeck = { deckName ->
                                        prefilledDeckName = deckName
                                        showAddDialog = true
                                    },
                                    onSync = { 
                                        viewModel.syncFromCloud()
                                        Toast.makeText(this@MainActivity, "Đang đồng bộ...", Toast.LENGTH_SHORT).show()
                                    }
                                )
                                "library" -> LibraryScreen(viewModel)
                                "practice" -> PracticeScreen(
                                    viewModel = viewModel,
                                    onReviewDifficult = {
                                        scope.launch {
                                            currentReviewList = viewModel.reviewDifficultCards.first()
                                            isReviewing = true
                                        }
                                    },
                                    onLearnNew = {
                                        scope.launch {
                                            currentReviewList = viewModel.learnCards.first()
                                            isReviewing = true
                                        }
                                    },
                                    onStartQuiz = {
                                        currentReviewList = viewModel.getQuizCards()
                                        isReviewing = true
                                    }
                                )
                                "account" -> AccountScreen(
                                    userName = auth.currentUser?.email ?: "Người dùng",
                                    onSync = { viewModel.syncFromCloud() }
                                )
                            }
                        }
                    }
                }

                if (showAddDialog) {
                    AddCardDialog(
                        initialDeckName = prefilledDeckName,
                        onDismiss = { showAddDialog = false },
                        onConfirm = { front, back, deck ->
                            viewModel.addFlashcard(front, back, deck)
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

@Composable
fun AppBottomNavigation(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    onAddClick: () -> Unit
) {
    NavigationBar(containerColor = androidx.compose.ui.graphics.Color.White) {
        NavigationBarItem(
            selected = currentTab == "home",
            onClick = { onTabSelected("home") },
            icon = { Icon(Icons.Default.Home, "Trang chủ") },
            label = { Text("Trang chủ") }
        )
        NavigationBarItem(
            selected = currentTab == "library",
            onClick = { onTabSelected("library") },
            icon = { Icon(Icons.Default.Book, "Thư viện") },
            label = { Text("Thư viện") }
        )
        Box(modifier = Modifier.weight(1f), contentAlignment = androidx.compose.ui.Alignment.Center) {
            FloatingActionButton(
                onClick = onAddClick,
                containerColor = androidx.compose.ui.graphics.Color(0xFFE67E22),
                contentColor = androidx.compose.ui.graphics.Color.White,
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier.size(56.dp).offset(y = (-10).dp)
            ) {
                Icon(Icons.Default.Add, "Thêm", modifier = Modifier.size(32.dp))
            }
        }
        NavigationBarItem(
            selected = currentTab == "practice",
            onClick = { onTabSelected("practice") },
            icon = { Icon(Icons.AutoMirrored.Filled.Assignment, "Luyện tập") },
            label = { Text("Luyện tập") }
        )
        NavigationBarItem(
            selected = currentTab == "account",
            onClick = { onTabSelected("account") },
            icon = { Icon(Icons.Default.Person, "Tài khoản") },
            label = { Text("Tài khoản") }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCardDialog(initialDeckName: String, onDismiss: () -> Unit, onConfirm: (String, String, String) -> Unit) {
    var front by remember { mutableStateOf("") }
    var back by remember { mutableStateOf("") }
    var deck by remember { mutableStateOf(if (initialDeckName.isEmpty()) "Mặc định" else initialDeckName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initialDeckName.isEmpty()) "Tạo bộ thẻ mới" else "Thêm thẻ vào bộ $initialDeckName") },
        text = {
            Column {
                OutlinedTextField(value = front, onValueChange = { front = it }, label = { Text("Mặt trước (Câu hỏi)") })
                OutlinedTextField(value = back, onValueChange = { back = it }, label = { Text("Mặt sau (Đáp án)") })
                OutlinedTextField(
                    value = deck, 
                    onValueChange = { deck = it }, 
                    label = { Text("Tên bộ thẻ") },
                    enabled = initialDeckName.isEmpty()
                )
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
