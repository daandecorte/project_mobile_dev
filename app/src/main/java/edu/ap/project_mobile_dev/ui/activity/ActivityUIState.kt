package edu.ap.project_mobile_dev.ui.activity

import edu.ap.project_mobile_dev.ui.model.ActivityDetail

data class ActivityUIState(
    val activity: ActivityDetail? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val selectedTab: Int = 0
)
