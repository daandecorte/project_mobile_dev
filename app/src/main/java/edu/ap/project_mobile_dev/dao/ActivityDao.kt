package edu.ap.project_mobile_dev.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.ap.project_mobile_dev.ui.model.Activity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivities(activities: List<Activity>)

    @Query("SELECT * FROM activities")
    fun getAll(): Flow<List<Activity>>
}