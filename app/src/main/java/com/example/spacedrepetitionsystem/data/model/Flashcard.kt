package com.example.spacedrepetitionsystem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val front: String = "",
    val back: String = "",
    var interval: Int = 0,
    var repetitions: Int = 0,
    var easeFactor: Double = 2.5,
    var nextDueDate: Long = System.currentTimeMillis(),
    
    // Các trường phục vụ đồng bộ Firebase
    var isSynced: Boolean = false,
    var firestoreId: String = "" // Để map giữa Room và Firestore
) {
    // Constructor không đối số cho Firebase
    constructor() : this(0, "", "", 0, 0, 2.5, System.currentTimeMillis(), false, "")
}
