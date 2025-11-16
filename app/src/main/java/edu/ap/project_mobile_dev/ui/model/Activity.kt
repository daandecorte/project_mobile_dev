package edu.ap.project_mobile_dev.ui.model

import edu.ap.project_mobile_dev.ui.add.Category

data class Activity(
    val documentId: String,
    val title: String,
    val description: String,
    val category: Category,
    val location: String,
    val city: String,
    val lat: String,
    val lon: String,
)
