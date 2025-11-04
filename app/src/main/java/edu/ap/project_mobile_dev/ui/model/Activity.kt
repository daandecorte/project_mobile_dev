package edu.ap.project_mobile_dev.ui.model

import edu.ap.project_mobile_dev.ui.add.Category

data class Activity(
    val id: Int,
    val title: String,
    val description: String,
    val imageUrl: String,
    val category: Category,
    val location: String,
    val city: String
)
