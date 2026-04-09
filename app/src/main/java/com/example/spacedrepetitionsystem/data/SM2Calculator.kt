package com.example.spacedrepetitionsystem.data

import com.example.spacedrepetitionsystem.data.model.Flashcard
import java.util.Calendar

/**
 * Cài đặt thuật toán SuperMemo (SM-2).
 * Giúp tính toán thời điểm ôn tập tiếp theo dựa trên đánh giá của người dùng.
 */
object SM2Calculator {

    /**
     * @param flashcard Thẻ cần cập nhật
     * @param quality Mức độ ghi nhớ của người dùng (0-5)
     * @return Flashcard đã được cập nhật các chỉ số mới
     */
    fun calculate(flashcard: Flashcard, quality: Int): Flashcard {
        var interval = flashcard.interval
        var repetitions = flashcard.repetitions
        var easeFactor = flashcard.easeFactor

        if (quality >= 3) { // Trả lời đúng
            when (repetitions) {
                0 -> interval = 1
                1 -> interval = 6
                else -> interval = (interval * easeFactor).toInt()
            }
            repetitions++
        } else { // Trả lời sai
            repetitions = 0
            interval = 1
        }

        // Công thức tính Ease Factor mới: EF' = f(EF, q)
        easeFactor += (0.1 - (5 - quality) * (0.08 + (5 - quality) * 0.02))
        if (easeFactor < 1.3) easeFactor = 1.3

        // Tính ngày đến hạn tiếp theo
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, interval)
        
        return flashcard.copy(
            interval = interval,
            repetitions = repetitions,
            easeFactor = easeFactor,
            nextDueDate = calendar.timeInMillis
        )
    }
}
