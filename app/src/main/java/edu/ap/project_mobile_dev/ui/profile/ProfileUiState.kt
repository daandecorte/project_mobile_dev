package edu.ap.project_mobile_dev.ui.profile

data class ProfileUiState(
    val username: String = "",
    val favorites: List<String> = emptyList(),
    val reviews: List<String> = emptyList()
)
