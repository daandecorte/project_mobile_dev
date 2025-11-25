package edu.ap.project_mobile_dev.ui.profile

import edu.ap.project_mobile_dev.ui.model.ReviewProfile

data class ProfileUiState(
    val username: String = "",
    val favorites: List<String> = emptyList(),
    val reviews: List<ReviewProfile> = emptyList(),
    val reviewList: List<String> = emptyList()
)
