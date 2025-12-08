package edu.ap.project_mobile_dev.ui.home

import edu.ap.project_mobile_dev.ui.model.Activity
import org.osmdroid.util.GeoPoint

data class HomeUiState(
    val activities: List<Activity> = emptyList(),
    val selectedTab: Int = 0,
    val selectedCategories: Set<String> = emptySet(),
    val searchQuery: String = "",
    val filteredActivities: List<Activity> = emptyList(),
    val isRefreshing: Boolean=false,
    val currentLocation: GeoPoint = GeoPoint(0.0, 0.0),
    val sortBy: SortBy = SortBy.NONE,
    val isLoading: Boolean = false,
)