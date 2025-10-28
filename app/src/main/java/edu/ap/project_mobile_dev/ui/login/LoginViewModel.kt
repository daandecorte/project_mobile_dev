package edu.ap.project_mobile_dev.ui.login

import android.content.Intent
import android.provider.ContactsContract
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import edu.ap.project_mobile_dev.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import edu.ap.project_mobile_dev.R
import kotlinx.coroutines.launch

class LoginViewModel: ViewModel() {
    val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    fun onEmailChanged(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }
    fun onPasswordChanged(password: String) {
        _uiState.value = _uiState.value.copy(password=password)
    }
    fun loginWithEmailPassword() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if(email.isEmpty() || password.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Vul alle velden in!")
            return
        }
        _uiState.value = _uiState.value.copy(isloading = true, errorMessage = null)

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _uiState.value= if(task.isSuccessful) {
                    _uiState.value.copy(isloading = false, success = true)
                } else {
                    _uiState.value.copy(isloading = false, errorMessage = task.exception?.message ?: "Login failed")
                }

            }
    }
    fun registerWithEmailPassword() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password.trim()

        if(email.isEmpty() || password.isEmpty()) {
            _uiState.value = _uiState.value.copy(errorMessage = "Vul alle velden in!")
            return
        }

        _uiState.value = _uiState.value.copy(isloading = true, errorMessage = null)

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                _uiState.value = if (task.isSuccessful) {
                    _uiState.value.copy(isloading = false, success = true)
                } else {
                    _uiState.value.copy(isloading = false, errorMessage = task.exception?.message ?: "Registratie mislukt")
                }
            }
    }
    fun logout(activity: MainActivity? = null) {
        // Sign out from Firebase
        auth.signOut()

        // If signed in with Google, sign out there too
        activity?.let {
            val googleSignInClient = getGoogleSignInClient(it)
            googleSignInClient.signOut()
        }

        // Reset UI state
        _uiState.value = LoginUiState()
    }
    fun getGoogleSignInClient(activity: MainActivity): GoogleSignInClient {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(activity.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        return GoogleSignIn.getClient(activity, gso)
    }
    fun handleGoogleSignInResult(data: Intent?, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account= task.result
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                auth.signInWithCredential(credential)
                    .addOnCompleteListener { signInTask ->
                        if(signInTask.isSuccessful) {
                            onResult(true, null)
                        }
                        else {
                            onResult(false, signInTask.exception?.message ?: "Login failed")
                        }
                    }
            }
            catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }
}