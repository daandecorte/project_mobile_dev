package edu.ap.project_mobile_dev.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import edu.ap.project_mobile_dev.ui.model.Activity

class HomeViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    private val db = FirebaseFirestore.getInstance()
    init {
        loadActivities()
    }
    private fun loadActivities() {
        db.collection("activities")
            .get()
            .addOnSuccessListener { snapshot ->
                val firebaseActivities = snapshot.documents.mapNotNull { doc ->
                    try {
                        Activity(
                            id = (doc.getLong("id") ?: 0L).toInt(),
                            documentId = doc.id,
                            title = doc.getString("title") ?: "",
                            description = doc.getString("description") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            category = doc.getString("category") ?: "",
                            location = doc.getString("location") ?: "",
                            city = doc.getString("city") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                }

                //val allActivities = activities + firebaseActivities

                _uiState.update {
                    it.copy(
                        activities = firebaseActivities,
                        filteredActivities = firebaseActivities
                    )
                }
            }
            .addOnFailureListener { e ->
                Log.e("Firestore", "Failed to load activities", e)
            }

//            .addOnFailureListener { e ->
//                // Optional: handle error
//                _uiState.update {
//                    it.copy(
//                        activities = activities,
//                        filteredActivities = activities
//                    )
//                }
//            }
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
                        currentState.selectedCategories.contains(activity.category)
                val matchesSearch = currentState.searchQuery.isEmpty() ||
                        activity.location.contains(currentState.searchQuery, ignoreCase = true) ||
                        activity.city.contains(currentState.searchQuery, ignoreCase = true)
                matchesCategory && matchesSearch
            }
            currentState.copy(filteredActivities = filtered)
        }
    }
}
