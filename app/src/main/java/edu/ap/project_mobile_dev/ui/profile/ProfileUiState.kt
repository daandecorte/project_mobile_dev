package edu.ap.project_mobile_dev.ui.profile

import edu.ap.project_mobile_dev.ui.model.ActivityProfile
import edu.ap.project_mobile_dev.ui.model.ReviewProfile

data class ProfileUiState(
    val username: String = "",
    val reviews: List<ReviewProfile> = emptyList(),
    val reviewList: List<String> = emptyList(),
    val favorites: List<ActivityProfile> = emptyList(),
    val favoritesList: List<String> = emptyList(),
    val selectedTab: Int = 0,
    val isEditingUsername: Boolean = false
    )
