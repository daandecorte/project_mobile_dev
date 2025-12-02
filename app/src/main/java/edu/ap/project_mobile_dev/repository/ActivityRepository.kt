package edu.ap.project_mobile_dev.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.dao.ActivityDao
import edu.ap.project_mobile_dev.ui.add.Category
import edu.ap.project_mobile_dev.ui.model.Activity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToInt

@Singleton
class ActivityRepository @Inject constructor(
    private val activityDao: ActivityDao,
    private val firestore: FirebaseFirestore
) {
    fun getActivities() : Flow<List<Activity>> {
        return activityDao.getAll()
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
            Log.d("ActivityRepository", "Fetched ${activities.size} activities from Firestore")
            activityDao.insertActivities(activities)
            val allActivities = activityDao.getAll().first()
            Log.d("ActivityRepository", "Room now has ${allActivities.size} activities")
        } catch (e: Exception) {
            Log.e("ActivityRepository", "Failed to fetch activities from Firestore", e)
            throw e
        }
    }

}