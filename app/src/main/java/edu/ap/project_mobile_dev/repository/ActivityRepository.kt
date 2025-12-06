package edu.ap.project_mobile_dev.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.animation.core.snap
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.dao.ActivityDao
import edu.ap.project_mobile_dev.ui.add.Category
import edu.ap.project_mobile_dev.ui.model.Activity
import edu.ap.project_mobile_dev.ui.model.ActivityDetail
import edu.ap.project_mobile_dev.ui.model.Review
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.String
import kotlin.math.roundToInt

@Singleton
class ActivityRepository @Inject constructor(
    private val activityDao: ActivityDao,
    private val firestore: FirebaseFirestore
) {
    fun getActivities() : Flow<List<Activity>> {
        return activityDao.getAll()
    }
    fun getActivitiesByIds(ids: List<String>): List<Activity> {
        return activityDao.getActivitiesByDocIds(ids)
    }
    fun getActivityDetailById(documentId: String): Flow<ActivityDetail> {
        return activityDao.getActivityByDocId(documentId)
            .map { activity ->
                // Launch a coroutine to fetch the image from Firebase asynchronously
                var bitmap: Bitmap? = null

                // This can be a suspend function
                runBlocking {
                    try {
                        val snapshot = firestore.collection("activities")
                            .document(documentId)
                            .get()
                            .await()
                        val base64 = snapshot.getString("imageUrl")
                        if (!base64.isNullOrEmpty()) {
                            bitmap = decodeBase64ToBitmap(base64)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }

                ActivityDetail(
                    documentId = activity.documentId,
                    category = activity.category,
                    title = activity.title,
                    ratingM = activity.averageRating,
                    location = activity.location,
                    city = activity.city,
                    description = activity.description,
                    bitmap = bitmap,
                    lat = activity.lat,
                    lon = activity.lon
                )
            }
    }
    suspend fun refreshActivities() {
        try {
            val snapshot = firestore.collection("activities").get().await()
            val activities = snapshot.documents.mapNotNull { doc ->
                try {
                    Activity(
                        documentId = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        category = Category.valueOf(doc.getString("category") ?: "OTHER"),
                        location = doc.getString("location") ?: "",
                        city = doc.getString("city") ?: "",
                        lat = doc.getString("lat") ?: "",
                        lon = doc.getString("lon") ?: "",
                        street = doc.getString("street") ?: "",
                        averageRating = (doc.getString("averageRating")?.toDoubleOrNull() ?: 0.0).roundToInt()
                    )
                } catch (e: Exception) {
                    Log.e("ActivityRepository", "Failed to parse document ${doc.id}", e)
                    null
                }
            }
            activityDao.insertActivities(activities)
        } catch (e: Exception) {
            throw e
        }
    }
    private fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}