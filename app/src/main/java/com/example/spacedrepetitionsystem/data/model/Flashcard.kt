package com.example.spacedrepetitionsystem.data.model

/**
 * Model Flashcard cho hệ thống SRS.
 * Bao gồm các trường cần thiết cho thuật toán SM-2.
 */
data class Flashcard(
    val id: Int = 0,
    val front: String,              // Câu hỏi hoặc từ vựng
    val back: String,               // Đáp án hoặc định nghĩa
    
    // Các trường dành cho thuật toán SM-2 (SuperMemo-2)
    var interval: Int = 0,          // Khoảng cách ngày lặp lại (I)
    var repetitions: Int = 0,       // Số lần lặp lại thành công liên tiếp (n)
    var easeFactor: Double = 2.5,   // Hệ số độ dễ (EF) - Mặc định là 2.5
    var nextDueDate: Long = System.currentTimeMillis() // Ngày học tiếp theo (Unix Timestamp)
)
