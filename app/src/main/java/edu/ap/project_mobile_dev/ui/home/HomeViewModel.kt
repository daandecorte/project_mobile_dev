package edu.ap.project_mobile_dev.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.ui.add.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import edu.ap.project_mobile_dev.ui.model.Activity
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val db = FirebaseFirestore.getInstance()
    init {
        loadActivities()
    }

    fun refreshActivities() {
        _uiState.update {
            state -> state.copy(isRefreshing = true)
        }

        viewModelScope.launch {
            loadActivities()
        }
    }
    private fun loadActivities() {
        var activities = listOf<Activity>()

        db.collection("activities")
            .get()
            .addOnSuccessListener { snapshot ->
                val firebaseActivities = snapshot.documents.mapNotNull { doc ->
                    try {
                        Activity(
                            id = (doc.getLong("id") ?: 0L).toInt(),
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            category = Category.valueOf(doc.getString("category") ?: "OTHER"),
                            location = doc.getString("location") ?: "",
                            city = doc.getString("city") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                _uiState.update {
                    it.copy(
                        activities = firebaseActivities,
                        filteredActivities = firebaseActivities,
                        isRefreshing = false
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to load activities", e)
            }
    }
    fun addActivity(activity: Activity) {
        _uiState.update { currentState ->
            val updatedActivities = currentState.activities + activity
            currentState.copy(
                activities = updatedActivities,
                filteredActivities = updatedActivities
            )
        }
    }
    fun updateSearchQuery(query: String) {
        _uiState.update {
            it.copy(searchQuery = query)
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
            val filtered = currentState.activities.filter { activity ->
                val matchesCategory = currentState.selectedCategories.isEmpty() ||
                        currentState.selectedCategories.contains(activity.category.displayName)
                val matchesSearch = currentState.searchQuery.isEmpty() ||
                        activity.location.contains(currentState.searchQuery, ignoreCase = true) ||
                        activity.city.contains(currentState.searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
            currentState.copy(filteredActivities = filtered)
        }
    }
}
