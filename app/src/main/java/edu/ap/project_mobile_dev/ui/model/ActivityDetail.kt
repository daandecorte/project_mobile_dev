package edu.ap.project_mobile_dev.ui.model

import android.graphics.Bitmap
import edu.ap.project_mobile_dev.ui.add.Category

data class ActivityDetail (
    val documentId: String,
    val category: Category,
    val title: String,
    val ratingM: Int,
    val ratings: List<Rating> = emptyList(),
    val location: String,
    val city: String,
    val description: String,
    val imageUrls: List<String> = emptyList(),
    val bitmap: Bitmap?,
)