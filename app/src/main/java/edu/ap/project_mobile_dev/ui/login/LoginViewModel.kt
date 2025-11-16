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
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.SetOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.MainActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import edu.ap.project_mobile_dev.R
import kotlinx.coroutines.launch

class LoginViewModel: ViewModel() {
    val auth = FirebaseAuth.getInstance()
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState

    private val db = FirebaseFirestore.getInstance();

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
                if (task.isSuccessful) {
                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid ?: return@addOnCompleteListener

                    val user = mapOf(
                        "username" to "",
                        "favorites" to emptyList<String>(),
                        "reviews" to emptyList<String>(),
                    )

                    db.collection("users")
                        .document(uid)
                        .set(user)
                        .addOnSuccessListener {
                            _uiState.value = _uiState.value.copy(isloading = false, success = true)
                        }
                        .addOnFailureListener { e ->
                            _uiState.value = _uiState.value.copy(
                                isloading = false,
                                errorMessage = "Firestore error: ${e.message}"
                            )
                        }

                } else {
                    _uiState.value = _uiState.value.copy(
                        isloading = false,
                        errorMessage = task.exception?.message ?: "Registratie mislukt"
                    )
                }
            }
    }

    fun logout(activity: MainActivity? = null) {
        auth.signOut()

        activity?.let {
            val googleSignInClient = getGoogleSignInClient(it)
            googleSignInClient.signOut()
        }

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
                val account = task.result
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)

                auth.signInWithCredential(credential)
                    .addOnCompleteListener { signInTask ->
                        if (!signInTask.isSuccessful) {
                            onResult(false, signInTask.exception?.message ?: "Google sign-in failed")
                            return@addOnCompleteListener
                        }

                        val firebaseUser = auth.currentUser
                        if (firebaseUser == null) {
                            onResult(false, "Firebase user is null after sign-in")
                            return@addOnCompleteListener
                        }

                        createUserInFirestore(firebaseUser) { ok, err ->
                            if (ok) onResult(true, null)
                            else onResult(false, err ?: "Failed creating user document")
                        }
                    }
            } catch (e: Exception) {
                onResult(false, e.message)
            }
        }
    }

    private fun createUserInFirestore(user: FirebaseUser, callback: (Boolean, String?) -> Unit) {
        val uid = user.uid

        val userRef = db.collection("users").document(uid)

        userRef.get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    callback(true, null)
                } else {
                    val user = hashMapOf(
                        "username" to "",
                        "favorites" to emptyList<String>(),
                        "reviews" to emptyList<String>()
                    )

                    userRef.set(user, SetOptions.merge())
                        .addOnSuccessListener {
                            callback(true, null)
                        }
                        .addOnFailureListener { e ->
                            callback(false, e.message)
                        }
                }
            }
            .addOnFailureListener { e ->
                callback(false, e.message)
            }
    }
}