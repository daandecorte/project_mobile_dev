package edu.ap.project_mobile_dev.ui.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.firebase.Timestamp

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey val documentId: String,
    val userId: String,
    val activityId: String,
    val rating: Int,
    val imageUrl: String,
    val date: Timestamp,
    val description: String,
    val likes: List<String>,
)
