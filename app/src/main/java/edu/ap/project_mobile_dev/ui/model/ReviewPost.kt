package edu.ap.project_mobile_dev.ui.model

import com.google.firebase.Timestamp

data class ReviewPost(
    val userId: String,
    val activityId: String,
    val rating: Int,
    val date: Timestamp,
    val description: String,
    val likes: List<String>,
)