package edu.ap.project_mobile_dev.ui.add

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.ui.model.Activity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import androidx.core.graphics.scale

class AddViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AddUiState())
    val uiState: StateFlow<AddUiState> = _uiState.asStateFlow()
    val MAX_FIRESTORE_IMAGE_SIZE = 1048487

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

    fun onPhotoSelected(uri: Uri, context: Context) {
        viewModelScope.launch(Dispatchers.IO) {
            val bitmap = MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
            var quality = 80
            var base64: String
            do {
                base64 = bitmapToBase64(bitmap, quality = quality)
                quality -= 10
            } while (base64.length > MAX_FIRESTORE_IMAGE_SIZE && quality > 5)

            if (base64.length > MAX_FIRESTORE_IMAGE_SIZE) {
                _uiState.update { current ->
                    current.copy(errorMessage = "Afbeelding is te groot ${base64.length}")
                }
                return@launch
            }
            _uiState.update { current ->
                current.copy(
                    photoUri = uri.toString(),
                    photoBase64 = base64
                )
            }
            validateForm()
        }
    }
    fun bitmapToBase64(bitmap: Bitmap, quality: Int =80): String {
        val maxWidth=720;
        val maxHeight=720;

        val ratio = minOf(maxWidth.toFloat() / bitmap.width, maxHeight.toFloat() / bitmap.height, 1f)
        val newWidth = (bitmap.width * ratio).toInt()
        val newHeight = (bitmap.height * ratio).toInt()

        val resizedBitmap = bitmap.scale(newWidth, newHeight)

        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream) // compress to reduce size
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
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
                        imageUrl = _uiState.value.photoBase64 ?: "",
                        category = _uiState.value.selectedCategory ?: Category.OTHER,
                        location = _uiState.value.city,
                        city = _uiState.value.city
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
                state.photoBase64.isNotBlank() &&
                state.city.isNotBlank() &&
                state.selectedCategory != null

        _uiState.update { it.copy(isFormValid = isValid) }
    }

    private fun resetForm() {
        _uiState.value = AddUiState()
    }
}