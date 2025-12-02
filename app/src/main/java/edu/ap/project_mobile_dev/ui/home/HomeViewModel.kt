package edu.ap.project_mobile_dev.ui.home

import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.toLowerCase
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.api.Context
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.ui.add.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import edu.ap.project_mobile_dev.ui.model.ActivityPost
import edu.ap.project_mobile_dev.ui.model.Activity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import java.util.Locale
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val db = FirebaseFirestore.getInstance()
    init {
        _uiState.update{it.copy(isLoading = true)}
        loadActivities()
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
        _uiState.update {
            state -> state.copy(isRefreshing = true)
        }

        viewModelScope.launch {
            loadActivities()
        }
    }
    fun loadActivities() {
        db.collection("activities")
            .get()
            .addOnSuccessListener { snapshot ->
                val firebaseActivities = snapshot.documents.mapNotNull { doc ->
                    try {
                        Activity(
                            documentId = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            category = Category.valueOf(doc.getString("category") ?: "OTHER"),
                            location = doc.getString("location") ?: "",
                            city = doc.getString("city") ?: "",
                            lat=doc.getString("lat") ?: "",
                            lon=doc.getString("lon") ?: "",
                            street = doc.getString("street")?:"",
                            averageRating = (doc.getString("averageRating")?.toDoubleOrNull() ?: 0.0).roundToInt()
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                _uiState.update {
                    it.copy(
                        activities = firebaseActivities,
                        filteredActivities = firebaseActivities,
                        isRefreshing = false,
                        isLoading = false
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to load activities", e)
            }
    }
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
