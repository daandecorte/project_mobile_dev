package edu.ap.project_mobile_dev.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import edu.ap.project_mobile_dev.ui.model.Activity
import edu.ap.project_mobile_dev.ui.model.ReviewDetail
import edu.ap.project_mobile_dev.ui.model.ReviewEntity
import edu.ap.project_mobile_dev.ui.model.ReviewPost
import kotlinx.coroutines.flow.Flow

@Dao
interface ReviewDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReviews(reviews: List<ReviewEntity>)
    @Query("SELECT * FROM reviews WHERE activityId = :activityId")
    fun getReviewsForActivity(activityId: String): Flow<List<ReviewEntity>>
    @Query("Select * from reviews where userId = :userId")
    fun getReviewsForUser(userId: String): Flow<List<ReviewEntity>>
    @Query("SELECT * FROM reviews")
    fun getAll(): Flow<List<ReviewEntity>>
}