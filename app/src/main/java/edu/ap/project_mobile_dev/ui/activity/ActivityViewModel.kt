package edu.ap.project_mobile_dev.ui.activity

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.ui.add.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import edu.ap.project_mobile_dev.ui.model.ActivityDetail
import edu.ap.project_mobile_dev.ui.model.ReviewPost
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import edu.ap.project_mobile_dev.ui.model.ReviewDetail
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.math.round

class ActivityViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ActivityUIState())
    val uiState: StateFlow<ActivityUIState> = _uiState.asStateFlow()
    private val db = FirebaseFirestore.getInstance()

    fun loadActivity(documentId: String) {
        if (documentId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Invalid id", isLoading = false) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, activityId = documentId) }

        db.collection("activities")
            .document(documentId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val activity = ActivityDetail(
                        documentId = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        category = Category.valueOf(doc.getString("category") ?: "OTHER"),
                        location = doc.getString("location") ?: "",
                        city = doc.getString("city") ?: "",
                        ratingM = (doc.getString("averageRating")?:"0").toInt(),
                        ratings = emptyList(),
                        bitmap = decodeBase64ToBitmap(doc.getString("imageUrl") ?: ""),
                    )
                    _uiState.update { it.copy(activity = activity, isLoading = false) }

                    viewModelScope.launch {
                        getSaved()
                        getReviews()
                    }
                } else {
                    _uiState.update { it.copy(errorMessage = "No such activity", isLoading = false) }
                    Log.w("Firestore", "No such document with id: $documentId")
                }
            }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to load", isLoading = false) }
                Log.e("Firestore", "Failed to load activity", e)
            }
    }

    fun changeSelectedTab(tab: Int){
        _uiState.update { it.copy(selectedTab = tab) }
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

    fun onPhotoSelected(uri: Uri, context: Context) {
        val MAX_FIRESTORE_IMAGE_SIZE = 1048487

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
                    current.copy(errorMessage = "Afbeelding is te groot ${base64.length}")
                }
                return@launch
            }
            _uiState.update { current ->
                current.copy(
                    photoUri = uri.toString(),
                    photoBase64 = base64,
                    isPhotoLoading = false
                )
            }
        }
    }

    fun bitmapToBase64(bitmap: Bitmap, quality: Int =80): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    fun showReviewDialog(show: Boolean){
        _uiState.update { it.copy(showReviewDialog = show) }
    }

    fun uploadReview(){
        if(_uiState.value.userRating in 1..5){
            val currentUser = FirebaseAuth.getInstance().currentUser;

            val review = ReviewPost (
                userId= currentUser?.uid ?: "Unknown",
                activityId = _uiState.value.activityId,
                rating = _uiState.value.userRating,
                imageUrl = _uiState.value.photoBase64,
                date = Timestamp.now(),
                description = _uiState.value.reviewText,
                likes = emptyList()
            )


            db.collection("reviews")
                .add(review)
                .addOnSuccessListener { documentRef ->
                    db.collection("users").document(currentUser?.uid ?: "")
                        .get()
                        .addOnSuccessListener { document ->
                            val bitmapProfile = decodeBase64ToBitmap(document.getString("profilePicture") ?: "")

                            _uiState.update { it.copy(reviews = _uiState.value.reviews+ ReviewDetail(
                                docId = documentRef.id,
                                username = document.getString("username") ?: "you",
                                bitmapPicture = bitmapProfile,
                                rating = _uiState.value.userRating,
                                date = SimpleDateFormat("dd/MM/yyyy HH:mm").format(Timestamp.now().toDate()),
                                description = _uiState.value.reviewText,
                                likes = 0,
                                bitmap = decodeBase64ToBitmap(_uiState.value.photoBase64),
                                liked=false
                            )) }
                        }

                    db.collection("users")
                        .document(currentUser?.uid ?: "")
                        .update("reviews", FieldValue.arrayUnion(documentRef.id))
                        .addOnSuccessListener {
                            println("User updated with review")
                            resetReview()
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firestore", "Failed to update user", e)
                        }
                    val activityDoc = db.collection("activities").document(uiState.value.activityId)
                    activityDoc.get()
                        .addOnSuccessListener {
                            activityDoc.update("averageRating", round(uiState.value.reviews.sumOf { it.rating.toDouble() }/(uiState.value.reviews.count())).toInt().toString())
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("Firestore", "Failed to upload review", e)
                }
        }
    }

    fun likeReview(reviewDetail : ReviewDetail){
        val currentUser = FirebaseAuth.getInstance().currentUser?.uid;
        val doc = db.collection("reviews").document(reviewDetail.docId)

        if(reviewDetail.liked){
            doc.get()
                .addOnSuccessListener {
                    doc.update("likes", FieldValue.arrayRemove(currentUser))
                        .addOnSuccessListener {
                            _uiState.update { currentState ->
                                val updatedReviews = currentState.reviews.map { review ->
                                    if (review.docId == reviewDetail.docId) {
                                        review.copy(
                                            likes = review.likes - 1,
                                            liked = false
                                        )
                                    } else review
                                }

                                currentState.copy(reviews = updatedReviews)
                            }
                        }
                }
        } else {
            doc.get()
                .addOnSuccessListener {
                    doc.update("likes", FieldValue.arrayUnion(currentUser))
                        .addOnSuccessListener {
                            _uiState.update { currentState ->
                                val updatedReviews = currentState.reviews.map { review ->
                                    if (review.docId == reviewDetail.docId) {
                                        review.copy(
                                            likes = review.likes + 1,
                                            liked = true
                                        )
                                    } else review
                                }

                                currentState.copy(reviews = updatedReviews)
                            }
                        }
                }
        }
    }

    fun resetReview(){
        _uiState.update { it.copy(showReviewDialog = false, userRating = 0, reviewText = "") }
    }

    fun updateReviewText(text: String){
        _uiState.update { it.copy(reviewText = text) }
    }

    fun updateRating(rating: Int){
        _uiState.update { it.copy(userRating = rating) }
    }

    suspend fun getReviews() {
        _uiState.update { it.copy(reviews = emptyList()) }

        val collection = db.collection("reviews")

        try{
            val reviews = collection.whereEqualTo("activityId", _uiState.value.activityId).get().await()

            for(review in reviews){
                val user = db.collection("users").document(review.getString("userId") ?: "").get().await()
                val username = user.getString("username") ?: ""
                val bitmapProfile = decodeBase64ToBitmap(user.getString("profilePicture") ?: "")

                val rating = (review.getLong("rating") ?: 0L).toInt()
                val description = review.getString("description") ?: ""

                val timestamp = review.getTimestamp("date") ?: com.google.firebase.Timestamp.now()
                val date = timestamp.toDate()
                val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)

                val bitmap = decodeBase64ToBitmap(review.getString("imageUrl") ?: "")

                val likes = review.get("likes") as? List<String> ?: emptyList()

                val uid = FirebaseAuth.getInstance().currentUser?.uid
                var liked = false
                if(likes.contains(uid)){
                    liked = true
                }

                val reviewDetail = ReviewDetail(
                    docId = review.id,
                    bitmapPicture = bitmapProfile,
                    username = username,
                    rating = rating,
                    description = description,
                    date = formattedDate,
                    likes = likes.size,
                    bitmap = bitmap,
                    liked = liked
                )

                if(bitmap != null){
                    _uiState.update { currentState ->
                        val updatedPhotos = currentState.photos.toMutableList()
                        updatedPhotos.add(bitmap)
                        currentState.copy(photos = updatedPhotos)
                    }
                }

                _uiState.update { currentState ->
                    val updatedReviews = currentState.reviews.toMutableList()
                    updatedReviews.add(reviewDetail)
                    currentState.copy(reviews = updatedReviews)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private val userUid = FirebaseAuth.getInstance().currentUser?.uid;

    fun getSaved(){
        val doc = db.collection("users").document(userUid ?: "")

        doc.get()
            .addOnSuccessListener { document ->
                val favorites = document.get("favorites") as? List<String> ?: emptyList()

                if(favorites.contains(uiState.value.activityId)){
                    _uiState.update { it.copy(saved = true) }
                }
            }
    }

    fun saveActivity(){
        val doc = db.collection("users").document(userUid ?: "")

        if(!_uiState.value.saved){
            doc.get()
                .addOnSuccessListener {
                    doc.update("favorites", FieldValue.arrayUnion(_uiState.value.activityId))
                        .addOnSuccessListener {
                            _uiState.update { it.copy(saved = true) }
                        }
                }
        } else {
            doc.get()
                .addOnSuccessListener {
                    doc.update("favorites", FieldValue.arrayRemove(_uiState.value.activityId))
                        .addOnSuccessListener {
                            _uiState.update { it.copy(saved = false) }
                        }
                }
        }
    }
}
