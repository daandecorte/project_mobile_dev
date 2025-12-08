package edu.ap.project_mobile_dev.ui.model

import com.google.firebase.Timestamp

data class Message (
    val username: String,
    val message: String,
    val dateTime: Timestamp
)
