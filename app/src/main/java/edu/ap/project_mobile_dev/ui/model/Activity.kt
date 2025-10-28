package edu.ap.project_mobile_dev.ui.model

data class Activity(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val category: String,
    val location: String,
    val city: String
)