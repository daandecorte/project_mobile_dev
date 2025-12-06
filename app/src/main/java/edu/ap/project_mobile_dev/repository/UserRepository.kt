package edu.ap.project_mobile_dev.repository

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.dao.UserDao
import edu.ap.project_mobile_dev.ui.model.UserEntity
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao,
    private val firestore: FirebaseFirestore
) {
    suspend fun getUserNamesByUids(uids: List<String>): List<UserEntity> {
        return userDao.getUsersByIds(uids)
    }
    suspend fun refreshUserNames(uids: List<String>): Map<String, Bitmap?> {
        if(uids.isEmpty()) return emptyMap()
        val snapshot = firestore.collection("users")
            .whereIn(FieldPath.documentId(), uids).get().await()
        val userEntities = snapshot.map { doc ->
            UserEntity(
                uid = doc.id,
                username = doc.getString("username") ?: ""
            )
        }
        userDao.insertUsers(userEntities)
        return snapshot.associate { doc->
            val bitmap=doc.getString("profilePicture")?.let{base64->decodeBase64ToBitmap(base64)}
            doc.id to bitmap
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