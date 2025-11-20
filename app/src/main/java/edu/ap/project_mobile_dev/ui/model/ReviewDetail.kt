package edu.ap.project_mobile_dev.ui.model

import android.graphics.Bitmap

data class ReviewDetail (
    val username: String,
    val rating: Int,
    val description: String,
    val date: String,
    val likes: Int,
    val bitmap: Bitmap?
)