package edu.ap.project_mobile_dev.ui.activity

import android.graphics.Bitmap
import edu.ap.project_mobile_dev.ui.model.ActivityDetail
import edu.ap.project_mobile_dev.ui.model.ReviewDetail
import org.osmdroid.util.GeoPoint

data class ActivityUIState(
    val activity: ActivityDetail? = null,
    val activityId: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: Int = 0,
    val photoUri: String? = null,
    val photoBase64: String = "",
    val isPhotoLoading: Boolean = false,
    val userRating: Int = 0,
    val reviewText: String = "",
    val showReviewDialog: Boolean = false,
    val reviews: List<ReviewDetail> = emptyList(),
    val reviewList: List<String> = emptyList(),
    val saved: Boolean = false,
    val isReviewsLoading:Boolean = false,
    val photos: List<Bitmap> = emptyList(),
    val currentLocation: GeoPoint = GeoPoint(0.0, 0.0),
)
