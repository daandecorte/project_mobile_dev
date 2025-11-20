package edu.ap.project_mobile_dev.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.ui.model.Review
import edu.ap.project_mobile_dev.ui.model.ReviewDetail
import edu.ap.project_mobile_dev.ui.model.ReviewProfile
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.String

class ProfileViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val db = FirebaseFirestore.getInstance();
    private val currentUser = FirebaseAuth.getInstance().currentUser;

    fun getUser(){
        val uid = currentUser?.uid ?: ""

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()){
                    val username = document.getString("username") ?: ""
                    val favorites = document.get("favorites") as? List<String> ?: emptyList()
                    val reviewList = document.get("reviews") as? List<String> ?: emptyList()

                    _uiState.update { it.copy(username = username, favorites = favorites, reviewList = reviewList) }

                    viewModelScope.launch {
                        getReviews()
                    }
                } else {
                    // Throw something
                }
            }.addOnFailureListener { exception -> println("Error fetching user: $exception") }

    }

    fun changeDBUsername(){
        db.collection("users").document(currentUser?.uid ?: "").update("username", _uiState.value.username)
    }

    fun changeUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    suspend fun getReviews() {
        val collection = db.collection("reviews")

        for (id in _uiState.value.reviewList) {
            try {
                val doc = collection.document(id).get().await()
                if (!doc.exists()) continue

                val activityId = doc.getString("activityId") ?: ""

                val activityDoc = db.collection("activities").document(activityId).get().await()
                val activityTitle = if (activityDoc.exists()) activityDoc.getString("title") ?: "" else ""

                val rating = (doc.getLong("rating") ?: 0L).toInt()
                val description = doc.getString("description") ?: ""

                val timestamp = doc.getTimestamp("date") ?: com.google.firebase.Timestamp.now()
                val date = timestamp.toDate()
                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)

                val bitmap = decodeBase64ToBitmap(doc.getString("imageUrl") ?: "")

                val reviewProfile = ReviewProfile(
                    activityId = activityTitle,
                    rating = rating,
                    description = description,
                    date = formattedDate,
                    bitmap = bitmap
                )

                // Update uiState safely
                _uiState.update { currentState ->
                    val updatedReviews = currentState.reviews.toMutableList()
                    updatedReviews.add(reviewProfile)
                    currentState.copy(reviews = updatedReviews)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun decodeBase64ToBitmap(base64String: String): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}

