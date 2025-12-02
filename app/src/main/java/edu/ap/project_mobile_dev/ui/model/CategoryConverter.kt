package edu.ap.project_mobile_dev.ui.model

import androidx.room.TypeConverter
import edu.ap.project_mobile_dev.ui.add.Category

class CategoryConverter {
    @TypeConverter
    fun fromCategory(category: Category): String = category.name

    @TypeConverter
    fun toCategory(value: String): Category = try {
        Category.valueOf(value)
    } catch (e: Exception) {
        Category.OTHER
    }
}