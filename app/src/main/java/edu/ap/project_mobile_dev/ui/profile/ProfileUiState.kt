package edu.ap.project_mobile_dev.ui.profile

import edu.ap.project_mobile_dev.ui.model.Review

data class ProfileUiState(
    val username: String = "",
    val favorites: List<String> = emptyList(),
    val reviews: List<Review> = emptyList(),
    val reviewList: List<String> = emptyList()
)
