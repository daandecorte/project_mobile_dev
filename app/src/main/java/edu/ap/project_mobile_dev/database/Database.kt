package edu.ap.project_mobile_dev.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import edu.ap.project_mobile_dev.dao.ActivityDao
import edu.ap.project_mobile_dev.dao.ReviewDao
import edu.ap.project_mobile_dev.ui.model.Activity
import edu.ap.project_mobile_dev.ui.model.Converters
import edu.ap.project_mobile_dev.ui.model.ReviewDetail
import edu.ap.project_mobile_dev.ui.model.ReviewEntity
import edu.ap.project_mobile_dev.ui.model.ReviewPost

@Database(
    entities = [Activity::class, ReviewEntity::class],
    version=5
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun reviewDao(): ReviewDao
}