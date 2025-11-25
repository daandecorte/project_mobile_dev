package edu.ap.project_mobile_dev.ui.model

import android.graphics.Bitmap

data class ReviewProfile (
    val activityId: String,
    val rating: Int,
    val description: String,
    val date: String,
    val bitmap: Bitmap?,
    val likes: Int
)