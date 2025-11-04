package edu.ap.project_mobile_dev.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.ui.model.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AddViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AddUiState())
    val uiState: StateFlow<AddUiState> = _uiState.asStateFlow()

    private val db = FirebaseFirestore.getInstance()
    fun updateLocationName(name: String) {
        _uiState.update { it.copy(name = name) }
        validateForm()
    }

    fun updateCity(city: String) {
        _uiState.update { it.copy(city = city) }
        validateForm()
    }

    fun updateDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    fun selectCategory(category: Category) {
        _uiState.update { it.copy(selectedCategory = category) }
        validateForm()
    }

    fun onPhotoClick() {

        viewModelScope.launch {

        }
    }

    fun useCurrentLocation() {
        viewModelScope.launch {

            _uiState.update { it.copy(city = "Antwerpen", isUsingCurrentLocation = true) }
            validateForm()
        }
    }

    fun saveLocation(onSuccess: (Activity) -> Unit) {
        if(_uiState.value.isFormValid) {
            viewModelScope.launch {
                try{
                    val newActivity = Activity(
                        id = 1, // bv. System.currentTimeMillis().toInt()
                        title = _uiState.value.name,
                        description = _uiState.value.description,
                        imageUrl = "", // of de URL van een foto
                        category = _uiState.value.selectedCategory?.displayName ?: "",
                        location = _uiState.value.city,
                        city = _uiState.value.city,
                        documentId = ""
                    )
                    db.collection("activities")
                        .add(newActivity)
                        .addOnSuccessListener {
                            resetForm()
                            onSuccess(newActivity)
                        }
                }catch (e: Exception) {
                    println("error saving to firebase: ${e.message}")
                }
            }
        }
    }

    private fun validateForm() {
        val state = _uiState.value
        val isValid = state.name.isNotBlank() &&
                state.city.isNotBlank() &&
                state.selectedCategory != null

        _uiState.update { it.copy(isFormValid = isValid) }
    }

    private fun resetForm() {
        _uiState.value = AddUiState()
    }
}