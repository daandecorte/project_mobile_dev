package edu.ap.project_mobile_dev.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import edu.ap.project_mobile_dev.dao.ActivityDao
import edu.ap.project_mobile_dev.ui.model.Activity
import edu.ap.project_mobile_dev.ui.model.CategoryConverter

@Database(
    entities = [Activity::class],
    version=1
)
@TypeConverters(CategoryConverter::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun activityDao(): ActivityDao
}