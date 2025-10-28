package edu.ap.project_mobile_dev.ui.login

data class LoginUiState(
    val email: String="",
    val password: String="",
    val isloading: Boolean=false,
    val errorMessage: String?=null,
    val success: Boolean = false
)
