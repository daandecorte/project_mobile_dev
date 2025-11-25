package edu.ap.project_mobile_dev.ui.model

import edu.ap.project_mobile_dev.ui.add.Category

data class ActivityProfile (
    val activityId: String,
    val name: String,
    val location: String,
    val category: Category
)