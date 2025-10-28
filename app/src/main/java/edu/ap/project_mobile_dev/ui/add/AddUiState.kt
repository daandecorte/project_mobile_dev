package edu.ap.project_mobile_dev.ui.add

data class AddUiState(
    val name: String = "",
    val city: String = "",
    val description: String = "",
    val selectedCategory: Category? = null,
    val photoUri: String? = null,
    val isUsingCurrentLocation: Boolean = false,
    val isFormValid: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
