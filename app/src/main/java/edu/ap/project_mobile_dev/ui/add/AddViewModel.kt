package edu.ap.project_mobile_dev.ui.add

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AddViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AddUiState())
    val uiState: StateFlow<AddUiState> = _uiState.asStateFlow()

    fun updateLocationName(name: String) {
        _uiState.update { it.copy(locationName = name) }
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
        // Hier zou je de camera/gallery picker triggeren
        // Voor nu updaten we alleen de state
        viewModelScope.launch {
            // Implementatie voor foto selectie
            // bijvoorbeeld met ActivityResultContracts
        }
    }

    fun useCurrentLocation() {
        viewModelScope.launch {
            // Hier zou je de GPS locatie ophalen
            // Voor nu zetten we een placeholder stad
            _uiState.update { it.copy(city = "Antwerpen", isUsingCurrentLocation = true) }
            validateForm()
        }
    }

    fun saveLocation() {
        if (_uiState.value.isFormValid) {
            viewModelScope.launch {
                // Hier zou je de locatie opslaan in je database/repository
                // bijvoorbeeld:
                // locationRepository.saveLocation(
                //     name = _uiState.value.locationName,
                //     city = _uiState.value.city,
                //     category = _uiState.value.selectedCategory,
                //     description = _uiState.value.description,
                //     photoUri = _uiState.value.photoUri
                // )

                // Reset form na opslaan
                resetForm()
            }
        }
    }

    private fun validateForm() {
        val state = _uiState.value
        val isValid = state.locationName.isNotBlank() &&
                state.city.isNotBlank() &&
                state.selectedCategory != null

        _uiState.update { it.copy(isFormValid = isValid) }
    }

    private fun resetForm() {
        _uiState.value = AddUiState()
    }
}