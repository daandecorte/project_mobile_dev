package edu.ap.project_mobile_dev.ui.model

import com.google.firebase.Timestamp
import java.util.Date

data class Review(
    val activityId: String,
    val rating: Int,
    val description: String,
    val date: String
)