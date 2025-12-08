package edu.ap.project_mobile_dev.ui.home

import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import edu.ap.project_mobile_dev.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.util.Locale
import javax.inject.Inject
import kotlin.math.pow
import kotlin.math.sqrt

@HiltViewModel
class HomeViewModel @Inject constructor(private val repository: ActivityRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val db = FirebaseFirestore.getInstance()
    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            // Collect activities from Room continuously
            repository.getActivities().collect { activities ->
                _uiState.update { currentState ->
                    currentState.copy(
                        activities = activities,
                        filteredActivities = activities,
                        isLoading = false,
                        isRefreshing = false
                    )
                }
            }
        }
        refreshActivities()
    }
    fun getCurrentLocation(context: android.content.Context) {
        viewModelScope.launch {
            try {
                if (ContextCompat.checkSelfPermission(
                        context,
                        android.Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    // Permission not granted, exit or show message
                    println("Location permission not granted!")
                    return@launch
                }
                val fused = LocationServices.getFusedLocationProviderClient(context)
                val location = fused.lastLocation.await()
                    ?: return@launch // location unavailable

                val latitude = location.latitude
                val longitude = location.longitude

                // --- Reverse Geocode ---
                val geocoder = Geocoder(context, Locale.getDefault())
                val results = withContext(Dispatchers.IO) {
                    geocoder.getFromLocation(latitude, longitude, 1)
                }

                _uiState.update {
                    it.copy(
                        currentLocation = GeoPoint(latitude, longitude)
                    )
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    fun refreshActivities() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)

        viewModelScope.launch {
            try {
                repository.refreshActivities()
            } catch (e: Exception) {
                e.printStackTrace()
                _uiState.value = _uiState.value.copy(isRefreshing = false)
            }
        }
    }
//    private fun loadActivities() {
//        _uiState.value = _uiState.value.copy(isLoading = true)
//        viewModelScope.launch {
//            repository.getActivities().collect { activities ->
//                _uiState.value = _uiState.value.copy(
//                    activities = activities,
//                    filteredActivities=activities,
//                    isLoading = false,
//                    isRefreshing = false
//                )
//            }
//        }
//    }
    fun updateSearchQuery(query: String) {
        _uiState.update {
            it.copy(searchQuery = query)
        }
        filterActivities()
    }
    fun updateSortBy(sortBy: SortBy) {
        _uiState.update {
            it.copy(sortBy=sortBy)
        }
        filterActivities()
    }

    fun toggleCategory(category: String) {
        _uiState.update { currentState ->
            val newCategories = if (currentState.selectedCategories.contains(category)) {
                currentState.selectedCategories - category
            } else {
                currentState.selectedCategories + category
            }
            currentState.copy(selectedCategories = newCategories)
        }
        filterActivities()
    }

    fun setSelectedTab(tab: Int) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    private fun filterActivities() {
        _uiState.update { currentState ->
            var filtered = currentState.activities.filter { activity ->
                val matchesCategory = currentState.selectedCategories.isEmpty() ||
                        currentState.selectedCategories.contains(activity.category.displayName)
                val matchesSearch = currentState.searchQuery.isEmpty() ||
                        activity.location.contains(currentState.searchQuery.trim(), ignoreCase = true) ||
                        activity.city.contains(currentState.searchQuery.trim(), ignoreCase = true)
                matchesCategory && matchesSearch
            }
            filtered = when(currentState.sortBy) {
                SortBy.NONE->filtered
                SortBy.LOCATION -> {
                    filtered.sortedBy {
                        distanceInMeters(
                            currentState.currentLocation.latitude,
                            currentState.currentLocation.longitude,
                            it.lat.toDouble(),
                            it.lon.toDouble()
                        )
                    }
                }
                SortBy.RATINGH -> filtered.sortedByDescending { it.averageRating }
                SortBy.RATINGL -> filtered.sortedBy { it.averageRating }
                SortBy.ALPHABETICAL -> filtered.sortedBy { it.title.lowercase() }
            }
            currentState.copy(filteredActivities = filtered)
        }
    }
    fun distanceInMeters(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        return sqrt((lat2 - lat1).pow(2) + (lon2 - lon1).pow(2))
    }
}
