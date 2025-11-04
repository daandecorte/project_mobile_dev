package edu.ap.project_mobile_dev.ui.model

import edu.ap.project_mobile_dev.ui.add.Category

data class ActivityPost(
    val title: String,
    val imageUrl: String,
    val description: String,
    val category: Category,
    val location: String,
    val city: String
)
