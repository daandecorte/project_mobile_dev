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
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
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
import edu.ap.project_mobile_dev.repository.UserRepository
import edu.ap.project_mobile_dev.ui.model.ChatOverview
import edu.ap.project_mobile_dev.ui.model.Message
import edu.ap.project_mobile_dev.ui.model.ReviewDetail
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
    private val activityRepository: ActivityRepository,
    private val userRepository: UserRepository
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
            _uiState.update { it.copy(isReviewsLoading = true) }

            val hasLocalReviews = reviewRepository.hasLocalReviews(uiState.value.activityId)

            if (!hasLocalReviews) {
                reviewRepository.refreshReviews()
            }

            reviewRepository.getReviewsByActivity(uiState.value.activityId).collect { reviews ->
                val photos = mutableListOf<Bitmap>()
                val userIds = reviews.map { it.userId }.toSet()

                val roomUsers = userRepository.getUserNamesByUids(userIds.toList())
                val usernameMap = roomUsers.associate { it.uid to it.username }

                val existingProfilePictures = _uiState.value.reviews.associate {
                    it.username to it.bitmapPicture
                }.filterValues { it != null }

                val reviewsDetails = reviews.map { review ->
                    val date = review.date.toDate()
                    val formattedDate = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(date)
                    val photoBitmap = decodeBase64ToBitmap(review.imageUrl)
                    if (photoBitmap != null) photos.add(photoBitmap)

                    val username = usernameMap[review.userId] ?: "Unknown"

                    ReviewDetail(
                        docId = review.documentId,
                        username = username,
                        bitmapPicture = existingProfilePictures[username],
                        rating = review.rating,
                        description = review.description,
                        date = formattedDate,
                        likes = review.likes.size,
                        liked = review.likes.contains(review.userId),
                        bitmap = photoBitmap
                    )
                }

                _uiState.update { it.copy(reviews = reviewsDetails, photos = photos, isReviewsLoading = false) }

                launch {
                    val profileMap = userRepository.refreshUserNames(userIds.toList())

                    val updatedRoomUsers = userRepository.getUserNamesByUids(userIds.toList())
                    val updatedUsernameMap = updatedRoomUsers.associate { it.uid to it.username }

                    val updatedReviews = _uiState.value.reviews.map { review ->
                        val userId = reviews.find { it.documentId == review.docId }?.userId
                        review.copy(
                            username = if (userId != null) updatedUsernameMap[userId] ?: review.username else review.username,
                            bitmapPicture = profileMap[userId] ?: review.bitmapPicture
                        )
                    }
                    _uiState.update { it.copy(reviews = updatedReviews) }
                }
            }
        }

        // If we had local reviews, refresh in background
        viewModelScope.launch {
            if (reviewRepository.hasLocalReviews(uiState.value.activityId)) {
                reviewRepository.refreshReviews()
            }
        }
    }

    private val userUid = FirebaseAuth.getInstance().currentUser?.uid;

    fun getSaved() {
        val uid = userUid ?: return
        val usersRef = db.collection("users")
        val chatsRef = db.collection("chats")

        usersRef.document(uid).get()
            .addOnSuccessListener { document ->
                val favorites = (document.get("favorites") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()
                val userName = document.getString("username") ?: ""
                val chatIds = (document.get("chats") as? List<*>)
                    ?.filterIsInstance<String>() ?: emptyList()

                if (chatIds.isEmpty()) {
                    _uiState.update { it.copy(saved = favorites.contains(it.activityId)) }
                    return@addOnSuccessListener
                }

                chatsRef.whereIn(FieldPath.documentId(), chatIds)
                    .get()
                    .addOnSuccessListener { snapshot ->

                        // Collect asynchronous tasks
                        val chatTasks = snapshot.documents.map { chatDoc ->
                            val groupName = chatDoc.getString("groupName") ?: ""
                            val participants = chatDoc.get("users") as? List<String> ?: emptyList()

                            if (groupName.isNotBlank()) {
                                // Group chat → immediate result
                                Tasks.forResult(
                                    ChatOverview(
                                        id = chatDoc.id,
                                        name = groupName
                                    )
                                )
                            } else {
                                // Direct message → fetch the other user's name
                                val otherUserId = participants.firstOrNull { it != uid }

                                if (otherUserId == null) {
                                    Tasks.forResult(
                                        ChatOverview(
                                            id = chatDoc.id,
                                            name = "Onbekende gebruiker"
                                        )
                                    )
                                } else {
                                    // Fetch other user's document
                                    usersRef.document(otherUserId)
                                        .get()
                                        .continueWith { t ->
                                            val uDoc = t.result
                                            ChatOverview(
                                                id = chatDoc.id,
                                                name = uDoc?.getString("username") ?: "Onbekend"
                                            )
                                        }
                                }
                            }
                        }

                        // Wait for all async tasks to finish
                        Tasks.whenAllSuccess<ChatOverview>(chatTasks)
                            .addOnSuccessListener { chatList ->
                                _uiState.update {
                                    it.copy(
                                        saved = favorites.contains(it.activityId),
                                        userChats = chatList,
                                        currentUserName = userName
                                    )
                                }
                            }
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
    fun shareActivityToChat(chatId: String, activityId: String) {
        val message = Message(
            username = uiState.value.currentUserName,
            message = "activityId=${activityId}",
            dateTime = Timestamp.now()
        )

        db.collection("chats")
            .document(chatId)
            .collection("messages")
            .add(message)
    }
}
