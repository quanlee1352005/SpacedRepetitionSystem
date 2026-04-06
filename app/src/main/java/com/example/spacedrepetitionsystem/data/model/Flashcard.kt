package com.example.spacedrepetitionsystem.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "flashcards")
data class Flashcard(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val front: String = "",
    val back: String = "",
    val deckName: String = "Tiếng Anh Giao Tiếp", // Tên bộ thẻ
    val colorHex: Long = 0xFF42A5F5,           // Màu sắc (Ví dụ: Xanh dương)
    var interval: Int = 0,
    var repetitions: Int = 0,
    var easeFactor: Double = 2.5,
    var nextDueDate: Long = System.currentTimeMillis(),
    var isSynced: Boolean = false,
    var firestoreId: String = ""
) {
    // Constructor cho Firebase
    constructor() : this(0, "", "", "Tiếng Anh Giao Tiếp", 0xFF42A5F5, 0, 0, 2.5, System.currentTimeMillis(), false, "")
}
