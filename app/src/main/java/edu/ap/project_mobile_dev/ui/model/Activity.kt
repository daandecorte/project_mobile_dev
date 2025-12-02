package edu.ap.project_mobile_dev.ui.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import edu.ap.project_mobile_dev.ui.add.Category

@Entity(tableName = "activities")
@TypeConverters(CategoryConverter::class)
data class Activity(
    @PrimaryKey val documentId: String,
    val title: String,
    val description: String,
    val category: Category,
    val location: String,
    val street: String,
    val city: String,
    val lat: String,
    val lon: String,
    val averageRating: Int
)
