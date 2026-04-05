package com.example.cuoiky

data class Flashcard(
    val front: String,
    val back: String,
    var nextReview: Long = 0L
)