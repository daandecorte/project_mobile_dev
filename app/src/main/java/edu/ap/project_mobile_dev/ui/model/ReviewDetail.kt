package edu.ap.project_mobile_dev.ui.model

import android.graphics.Bitmap
import androidx.room.Entity
import androidx.room.PrimaryKey

data class ReviewDetail (
    val docId: String,
    val bitmapPicture: Bitmap?,
    val username: String,
    val rating: Int,
    val description: String,
    val date: String,
    val likes: Int,
    val bitmap: Bitmap?,
    val liked: Boolean
)