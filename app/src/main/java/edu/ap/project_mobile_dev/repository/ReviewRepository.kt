package edu.ap.project_mobile_dev.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.dao.ReviewDao
import edu.ap.project_mobile_dev.ui.model.ReviewDetail
import edu.ap.project_mobile_dev.ui.model.ReviewEntity
import edu.ap.project_mobile_dev.ui.model.ReviewPost
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReviewRepository @Inject constructor(
    private val reviewDao: ReviewDao,
    private val firestore: FirebaseFirestore
) {
    fun getReviews(): Flow<List<ReviewEntity>> {
        return reviewDao.getAll()
    }
    fun getReviewsByActivity(activityId: String): Flow<List<ReviewEntity>> {
        return reviewDao.getReviewsForActivity(activityId);
    }
    fun getReviewsByUser(userId: String): Flow<List<ReviewEntity>> {
        return reviewDao.getReviewsForUser(userId);
    }
    suspend fun hasLocalReviews(activityId: String): Boolean {
        return reviewDao.getReviewCountByActivity(activityId) > 0
    }
    suspend fun refreshReviews() {
        try {
            val snapshot = firestore.collection("reviews").get().await()

            val reviews = snapshot.documents.mapNotNull { doc ->
                ReviewEntity(
                    documentId = doc.id,
                    userId = doc.getString("userId") ?: "",
                    rating = (doc.getLong("rating") ?: 0).toInt(),
                    description = doc.getString("description") ?: "",
                    date = doc.getTimestamp("date") ?: Timestamp.now(),
                    likes = (doc.get("likes") as? List<String>) ?: emptyList<String>(),
                    imageUrl = doc.getString("imageUrl") ?: "",
                    activityId =  doc.getString("activityId") ?: ""
                )
            }
            reviewDao.insertReviews(reviews)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}