package edu.ap.project_mobile_dev.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.ui.add.Category
import edu.ap.project_mobile_dev.ui.model.ActivityProfile
import edu.ap.project_mobile_dev.ui.model.ReviewProfile
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.String
import kotlinx.coroutines.flow.asSharedFlow

sealed class ProfileEvent {
    data class NavigateToActivity(val id: String) : ProfileEvent()
}

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

                    _uiState.update { it.copy(username = username, favoritesList = favorites, reviewList = reviewList) }

                    viewModelScope.launch {
                        getFavorites()
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

        _uiState.update { it.copy(reviews = emptyList()) }

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
                    bitmap = bitmap,
                    likes = 0
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

    suspend fun getFavorites(){
        val collection = db.collection("activities")
        _uiState.update { it.copy(favorites = emptyList()) }

        for (fav in _uiState.value.favoritesList){
            collection.document(fav)
                .get()
                .addOnSuccessListener { document ->
                    val location = document.getString("street") + " • " + document.getString("city")

                    val activity = ActivityProfile (
                        activityId = fav,
                        name = document.getString("title") ?: "",
                        location = location,
                        category = Category.valueOf(document.getString("category") ?: "OTHER"),
                    )

                    _uiState.update { currentState ->
                        val updatedFavorites = currentState.favorites.toMutableList()
                        updatedFavorites.add(activity)
                        currentState.copy(favorites = updatedFavorites)
                    }
                }
        }
    }

    private val _events = MutableSharedFlow<ProfileEvent>()
    val events = _events.asSharedFlow()

    fun goToActivity(id: String) {
        viewModelScope.launch {
            _events.emit(ProfileEvent.NavigateToActivity(id))
        }
    }

    fun removeFavorite(id: String) {
        val uid = currentUser?.uid ?: ""

        db.collection("users")
            .document(uid)
            .update("favorites", FieldValue.arrayRemove(id))
            .addOnSuccessListener {
                _uiState.update { currentState ->
                    val updatedFavorites = currentState.favorites.filter { it.activityId != id }
                    currentState.copy(favorites = updatedFavorites)
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

