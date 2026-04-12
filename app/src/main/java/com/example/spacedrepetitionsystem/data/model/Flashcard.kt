package com.example.spacedrepetitionsystem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "",           // ID người dùng (UID từ Firebase)
    val front: String = "",
    val back: String = "",
    val deckName: String = "Mặc định",
    val colorHex: Long = 0xFF42A5F5,
    var interval: Int = 0,
    var repetitions: Int = 0,
    var easeFactor: Double = 2.5,
    var nextDueDate: Long = System.currentTimeMillis(),
    var isSynced: Boolean = false,
    var firestoreId: String = ""
) {
    // Constructor cho Firebase
    constructor() : this(0, "", "", "", "Mặc định", 0xFF42A5F5, 0, 0, 2.5, System.currentTimeMillis(), false, "")
}
