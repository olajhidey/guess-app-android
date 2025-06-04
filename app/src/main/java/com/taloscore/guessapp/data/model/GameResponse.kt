package com.taloscore.guessapp.data.model

data class CategoryResponse(
    val ID: String,
    val name: String,
    val description: String
)

data class TopicResponse(
    val ID: String,
    val name: String,
    val description: String,
    val category_id: Int
)