package edu.ap.project_mobile_dev.ui.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.ui.add.Category
import edu.ap.project_mobile_dev.ui.model.ActivityProfile
import edu.ap.project_mobile_dev.ui.model.ReviewProfile
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
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

    suspend fun getReviews() = coroutineScope {
        _uiState.update { it.copy(reviews = emptyList(), isReviewsLoaing = true) }

        val reviewIds = _uiState.value.reviewList
        val collection = db.collection("reviews")

        try {
            val reviewDocs = reviewIds.map { id ->
                async {
                    collection.document(id).get().await()
                }
            }.awaitAll()

            val validReviews = reviewDocs.filter { it.exists() }

            val activityIds = validReviews
                .mapNotNull { it.getString("activityId") }
                .toSet()

            val activityMap = if (activityIds.isNotEmpty()) {
                db.collection("activities")
                    .whereIn(FieldPath.documentId(), activityIds.toList())
                    .get()
                    .await()
                    .associate { doc ->
                        doc.id to (doc.getString("title") ?: "")
                    }
            } else emptyMap()

            val reviewList = validReviews.map { doc ->

                val activityTitle = activityMap[doc.getString("activityId")] ?: ""

                val rating = (doc.getLong("rating") ?: 0L).toInt()
                val description = doc.getString("description") ?: ""

                val timestamp = doc.getTimestamp("date") ?: com.google.firebase.Timestamp.now()
                val date = timestamp.toDate()
                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                    .format(date)

                val bitmap = decodeBase64ToBitmap(doc.getString("imageUrl") ?: "")

                ReviewProfile(
                    activityId = activityTitle,
                    rating = rating,
                    description = description,
                    date = formattedDate,
                    bitmap = bitmap,
                    likes = 0
                )
            }

            _uiState.update { it.copy(reviews = reviewList, isReviewsLoaing = false) }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun getFavorites() = coroutineScope{
        val collection = db.collection("activities")
        _uiState.update { it.copy(favorites = emptyList(), isFavLoading = true) }
        val favouriteIds = _uiState.value.favoritesList
        try {
            val favourites = favouriteIds.map{id->async {
                collection.document(id).get().await()
            }}.awaitAll()
            val validFav = favourites.filter { it.exists() }
            val favoritesList = validFav
                .map { doc ->
                    val location = (doc.getString("street") ?: "") +
                            " • " +
                            (doc.getString("city") ?: "")

                    ActivityProfile(
                        activityId = doc.id,
                        name = doc.getString("title") ?: "",
                        location = location,
                        category = Category.valueOf(doc.getString("category") ?: "OTHER")
                    )
                }
            _uiState.update { it.copy(favorites = favoritesList, isFavLoading = false) }
        }
        catch (e :Exception) {
            e.printStackTrace()
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

