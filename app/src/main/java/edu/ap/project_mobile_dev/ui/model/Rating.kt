package edu.ap.project_mobile_dev.ui.model

data class Rating(
    val userId: Int,
    val rating: Int,
    val images: List<String> = emptyList(),
    val description: String,
    val likes: Int,

)
