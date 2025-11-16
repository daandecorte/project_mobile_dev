package edu.ap.project_mobile_dev.ui.profile

import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.ui.login.LoginViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class ProfileViewModel: ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState

    private val db = FirebaseFirestore.getInstance();
    private val currentUser = FirebaseAuth.getInstance().currentUser;

    fun getUser(){
        val uid = currentUser?.uid ?: ""

        db.collection("users").document(uid).get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()){
                    val username = document.getString("username") ?: ""
                    val favorites = document.get("favorites") as? List<String> ?: emptyList()
                    val reviews = document.get("reviews") as? List<String> ?: emptyList()

                    _uiState.update { it.copy(username = username, favorites = favorites, reviews = reviews) }
                } else {
                    // Throw something
                }
            }.addOnFailureListener { exception -> println("Error fetching user: $exception") }

    }

    fun changeDBUsername(){
        db.collection("users").document(currentUser?.uid ?: "").update("username", _uiState.value.username)
    }

    fun changeUsername(username: String) {
        _uiState.value = _uiState.value.copy(username = username)
    }
}

