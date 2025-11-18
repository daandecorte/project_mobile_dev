package edu.ap.project_mobile_dev.ui.activity

import edu.ap.project_mobile_dev.ui.model.ActivityDetail

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
    val showReviewDialog: Boolean = false
)
