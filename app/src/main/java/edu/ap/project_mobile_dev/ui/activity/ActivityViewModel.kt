package edu.ap.project_mobile_dev.ui.activity

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.ui.add.Category
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import edu.ap.project_mobile_dev.ui.model.Activity
import edu.ap.project_mobile_dev.ui.model.ActivityDetail

class ActivityViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ActivityUIState())
    val uiState: StateFlow<ActivityUIState> = _uiState.asStateFlow()
    private val db = FirebaseFirestore.getInstance()

    fun loadActivity(documentId: String) {
        if (documentId.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Invalid id", isLoading = false) }
            return
        }

        _uiState.update { it.copy(isLoading = true, errorMessage = null) }

        db.collection("activities")
            .document(documentId)
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val activity = ActivityDetail(
                        documentId = doc.id,
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        baseImageUrl = doc.getString("imageUrl") ?: "",
                        category = Category.valueOf(doc.getString("category") ?: "OTHER"),
                        location = doc.getString("location") ?: "",
                        city = doc.getString("city") ?: "",
                        ratingM = 0,
                        ratings = emptyList(),
                    )
                    _uiState.update { it.copy(activity = activity, isLoading = false) }
                } else {
                    _uiState.update { it.copy(errorMessage = "No such activity", isLoading = false) }
                    Log.w("Firestore", "No such document with id: $documentId")
                }
            }
            .addOnFailureListener { e ->
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to load", isLoading = false) }
                Log.e("Firestore", "Failed to load activity", e)
            }
    }

    fun changeSelectedTab(tab: Int){
        _uiState.update { it.copy(selectedTab = tab) }
    }
}
