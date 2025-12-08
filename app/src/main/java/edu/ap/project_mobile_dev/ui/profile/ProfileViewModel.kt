package edu.ap.project_mobile_dev.ui.profile

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ap.project_mobile_dev.repository.ActivityRepository
import edu.ap.project_mobile_dev.repository.ReviewRepository
import edu.ap.project_mobile_dev.ui.model.ActivityProfile
import edu.ap.project_mobile_dev.ui.model.ReviewProfile
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.String
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import javax.inject.Inject

sealed class ProfileEvent {
    data class NavigateToActivity(val id: String) : ProfileEvent()
}
@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val activityRepository: ActivityRepository
): ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val db = FirebaseFirestore.getInstance();
    private val currentUser = FirebaseAuth.getInstance().currentUser;

    fun getUser(){
        val uid = currentUser?.uid ?: return

        db.collection("users").document(uid)
            .addSnapshotListener { doc, error ->
                if (doc == null || !doc.exists()) return@addSnapshotListener

                val username = doc.getString("username") ?: ""
                val favorites = doc.get("favorites") as? List<String> ?: emptyList()
                val reviewList = doc.get("reviews") as? List<String> ?: emptyList()

                val profileBase64 = doc.getString("profilePicture") ?: ""
                val profileBitmap = decodeBase64ToBitmap(profileBase64)

                _uiState.update {
                    it.copy(
                        username = username,
                        favoritesList = favorites,
                        reviewList = reviewList,
                        photoBase64 = profileBase64,
                        photoBitmap = profileBitmap
                    )
                }

                viewModelScope.launch {
                    getFavorites()
                    getReviews()
                }
            }
    }


    fun changeDBUsername(){
        db.collection("users").document(currentUser?.uid ?: "").update("username", _uiState.value.username)
    }

    fun changeUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }

    fun getReviews() {
        _uiState.update { it.copy(reviews = emptyList(), isReviewsLoaing = true) }
        val uid = currentUser?.uid ?: ""
        viewModelScope.launch {
            reviewRepository.getReviewsByUser(uid).collect { reviews ->
                val activityIds = reviews.map { it.activityId }.toSet()

                val activityMap = if (activityIds.isNotEmpty()) {
                    withContext(Dispatchers.IO) {
                        activityRepository.getActivitiesByIds(activityIds.toList())
                            .associate { it.documentId to it.title }
                    }
                } else {
                    emptyMap()
                }

                val profileReviews = reviews.map{ review ->
                    val date=review.date.toDate()
                    val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
                    ReviewProfile(
                        activityId = activityMap[review.activityId] ?:"",
                        rating = review.rating,
                        description = review.description,
                        date = formattedDate,
                        bitmap = decodeBase64ToBitmap(review.imageUrl),
                        likes = review.likes.size
                    )
                }
                _uiState.update { it.copy(reviews = profileReviews, isReviewsLoaing = false) }
            }
        }
        viewModelScope.launch {
            reviewRepository.refreshReviews()
        }
    }

    suspend fun getFavorites() = coroutineScope{
        _uiState.update { it.copy(favorites = emptyList(), isFavLoading = true) }
        val favouriteIds = _uiState.value.favoritesList

        try {
            val roomFavorites = withContext(Dispatchers.IO) {
                activityRepository.getActivitiesByIds(favouriteIds)
            }

            val favoritesFromRoom = roomFavorites.map { activity ->
                ActivityProfile(
                    activityId = activity.documentId,
                    name = activity.title,
                    location = "${activity.street} • ${activity.city}",
                    category = activity.category
                )
            }

            _uiState.update { it.copy(favorites = favoritesFromRoom, isFavLoading = false) }
        } catch (e: Exception) {
            e.printStackTrace()
            _uiState.update { it.copy(isFavLoading = false) }
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

    val MAX_FIRESTORE_IMAGE_SIZE = 1048487

    fun onPhotoSelected(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { it.copy(isPhotoLoading = true, errorMessage = null) }
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)

            var quality = 80
            var base64: String
            do {
                base64 = bitmapToBase64(bitmap, quality = quality)
                quality -= 10
            } while (base64.length > MAX_FIRESTORE_IMAGE_SIZE && quality > 5)

            if (base64.length > MAX_FIRESTORE_IMAGE_SIZE) {
                _uiState.update { current ->
                    current.copy(errorMessage = "Afbeelding is te groot ${base64.length}", isPhotoLoading = false)
                }
                return@launch
            }

            _uiState.update { current ->
                current.copy(
                    photoUri = uri.toString(),
                    photoBase64 = base64,
                    photoBitmap = bitmap,
                    isPhotoLoading = false
                )
            }

            db.collection("users")
                .document(currentUser?.uid ?: "")
                .update("profilePicture", base64)
                .addOnFailureListener { e ->
                    _uiState.update { it.copy(errorMessage = "Failed to upload picture: ${e.message}") }
                }
        }
    }


    fun bitmapToBase64(bitmap: Bitmap, quality: Int =80): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream) // compress to reduce size
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
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

