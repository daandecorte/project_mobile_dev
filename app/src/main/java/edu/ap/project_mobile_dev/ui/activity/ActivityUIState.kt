package edu.ap.project_mobile_dev.ui.activity

import edu.ap.project_mobile_dev.ui.model.Activity

data class ActivityUIState(
    val activity: Activity? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
