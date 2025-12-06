package edu.ap.project_mobile_dev.ui.activity

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
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
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ap.project_mobile_dev.repository.ActivityRepository
import edu.ap.project_mobile_dev.repository.ReviewRepository
import edu.ap.project_mobile_dev.ui.model.ReviewDetail
import edu.ap.project_mobile_dev.ui.model.UserInfo
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import kotlin.String
import kotlin.math.round

@HiltViewModel
class ActivityViewModel @Inject constructor(
    private val reviewRepository: ReviewRepository,
    private val activityRepository: ActivityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ActivityUIState())
    val uiState: StateFlow<ActivityUIState> = _uiState.asStateFlow()
    private val db = FirebaseFirestore.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser;
    fun loadActivity(documentId: String) {
        if (documentId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Invalid id", isLoading = false) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null, activityId = documentId) }
        viewModelScope.launch {
            activityRepository.getActivityDetailById(documentId).collect {activity ->
                _uiState.update { it.copy(activity=activity, isLoading=false) }
                viewModelScope.launch {
                    getSaved()
                    getReviews()
                }
            }
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

    fun getReviews() {
        viewModelScope.launch {
            val photos = mutableListOf<Bitmap>()
            _uiState.update { it.copy(isReviewsLoading = true) }

            reviewRepository.getReviewsByActivity(uiState.value.activityId).collect { reviews ->
                val userIds = reviews.map { it.userId }.toSet()

                val userMap = if (userIds.isNotEmpty()) {
                    val userDocs = db.collection("users")
                        .whereIn(FieldPath.documentId(), userIds.toList())
                        .get()
                        .await()
                    userDocs.associate { doc ->
                        doc.id to UserInfo(
                            username = doc.getString("username") ?: "",
                            profilePicture = doc.getString("profilePicture") ?: ""
                        )
                    }
                } else {
                    emptyMap()
                }

                val reviewsDetails = reviews.map { review ->
                    val userInfo = userMap[review.userId]
                    val date=review.date.toDate()
                    val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
                    val photoBitmap = decodeBase64ToBitmap(review.imageUrl)
                    if(photoBitmap!=null) {
                        photos.add(photoBitmap)
                    }
                    ReviewDetail(
                        docId=review.documentId,
                        bitmapPicture= decodeBase64ToBitmap(userInfo?.profilePicture ?: ""),
                        username= userInfo?.username ?: "",
                        rating= review.rating,
                        description= review.description,
                        date=formattedDate,
                        likes= review.likes.size,
                        liked= review.likes.contains(review.userId),
                        bitmap = photoBitmap
                    )
                }
                _uiState.update { it.copy(reviews = reviewsDetails, isReviewsLoading = false, photos = photos) }
            }

        }
        viewModelScope.launch {
            reviewRepository.refreshReviews()
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
    fun getCurrentLocation(context: android.content.Context) {
        viewModelScope.launch {
            try {
                if (ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission not granted, exit or show message
                    println("Location permission not granted!")
                    return@launch
                }
                val fused = LocationServices.getFusedLocationProviderClient(context)
                val location = fused.lastLocation.await()
                    ?: return@launch // location unavailable

                val latitude = location.latitude
                val longitude = location.longitude

                // --- Reverse Geocode ---
                val geocoder = Geocoder(context, Locale.getDefault())
                val results = withContext(Dispatchers.IO) {
                    geocoder.getFromLocation(latitude, longitude, 1)
                }

                _uiState.update {
                    it.copy(
                        currentLocation = GeoPoint(latitude, longitude)
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun getDistance(): String {
        var earthRadiusKm = 6371;

        var lat1 = uiState.value.currentLocation.latitude
        var lat2 = (uiState.value.activity?.lat?.toDouble() ?: 0.0)
        var dLat = degreesToRadians(lat1- lat2);
        var dLon = degreesToRadians(uiState.value.currentLocation.longitude- (uiState.value.activity?.lon?.toDouble() ?: 0.0));

        lat1 = degreesToRadians(lat1);
        lat2 = degreesToRadians(lat2);

        var a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.sin(dLon/2) * Math.sin(dLon/2) * Math.cos(lat1) * Math.cos(lat2);
        var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        return "%.1f".format(Locale.GERMANY,  earthRadiusKm * c);
    }
    fun degreesToRadians(degrees: Double): Double {
        return degrees * Math.PI / 180;
    }
}
