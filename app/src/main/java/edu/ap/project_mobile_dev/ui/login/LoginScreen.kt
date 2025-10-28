package edu.ap.project_mobile_dev.ui.login

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.project_mobile_dev.MainActivity
import edu.ap.project_mobile_dev.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    if (state.success) {
        onLoginSuccess()
    }

    if (state.isloading) {
        CircularProgressIndicator()
    }

    state.errorMessage?.let { error ->
        Text(text = error, color = Color.Red)
    }

    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableStateOf(0) }
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        viewModel.handleGoogleSignInResult(result.data) { success, error ->
            if (success) onLoginSuccess()
            else Toast.makeText(context, error ?: "Google sign-in failed", Toast.LENGTH_SHORT).show()
        }
    }


    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF1A2332)),
        contentAlignment = Alignment.Center
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Logo (vervang door je eigen logo drawable)
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFFF6B35), Color(0xFFFF4757))
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter= painterResource(id=R.drawable.logo_transparent),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(50.dp),
                    contentScale = ContentScale.Crop,
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // App titel
            Text(
                text = "CityTrip",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Ondertitel
            Text(
                text = "Ontdek en deel jouw favoriete plekken",
                fontSize = 14.sp,
                color = Color(0xFF8B9AAB),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Tab selector
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF242F3F)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Tabs
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        TabButton(
                            text = "Inloggen",
                            isSelected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            modifier = Modifier.weight(1f)
                        )
                        TabButton(
                            text = "Registreren",
                            isSelected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(24.dp))
                    if(state.errorMessage!=null) {
                        Text(text = state.errorMessage.toString(), color = Color.Red)
                    }
                    // Email veld
                    Text(
                        text = "Email",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.email,
                        onValueChange = { viewModel.onEmailChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("je@email.com", color = Color(0xFF5A6B7D)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Email,
                                contentDescription = null,
                                tint = Color(0xFF8B9AAB)
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3A4A5C),
                            unfocusedBorderColor = Color(0xFF2A3A4C),
                            focusedContainerColor = Color(0xFF1F2A38),
                            unfocusedContainerColor = Color(0xFF1F2A38),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Wachtwoord veld
                    Text(
                        text = "Wachtwoord",
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = state.password,
                        onValueChange = { viewModel.onPasswordChanged(it) },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("••••••••", color = Color(0xFF5A6B7D)) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = Color(0xFF8B9AAB)
                            )
                        },
                        trailingIcon = {
                            IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                Icon(
                                    if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle password visibility",
                                    tint = Color(0xFF8B9AAB)
                                )
                            }
                        },
                        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF3A4A5C),
                            unfocusedBorderColor = Color(0xFF2A3A4C),
                            focusedContainerColor = Color(0xFF1F2A38),
                            unfocusedContainerColor = Color(0xFF1F2A38),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Onthoud mij & Wachtwoord vergeten
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(
                                checked = rememberMe,
                                onCheckedChange = { rememberMe = it },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color(0xFFFF6B35),
                                    uncheckedColor = Color(0xFF5A6B7D)
                                )
                            )
                            Text(
                                text = "Onthoud mij",
                                fontSize = 14.sp,
                                color = Color.White
                            )
                        }
                        TextButton(onClick = { /* Handle forgot password */ }) {
                            Text(
                                text = "Wachtwoord vergeten?",
                                color = Color(0xFFFF6B35),
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Login knop
                    Button(

                        onClick = {if(selectedTab==0) {viewModel.loginWithEmailPassword()}else if(selectedTab==1) {viewModel.registerWithEmailPassword()} },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFF6B35), Color(0xFFFF4757))
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if(selectedTab==0) {
                                Text(
                                    text = "Inloggen",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            else if(selectedTab==1) {
                                Text(
                                    text = "Registreren",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Of divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF3A4A5C)
                        )
                        Text(
                            text = "of",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            color = Color(0xFF8B9AAB),
                            fontSize = 14.sp
                        )
                        HorizontalDivider(
                            modifier = Modifier.weight(1f),
                            color = Color(0xFF3A4A5C)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Google login knop

                    OutlinedButton(
                        onClick = {
                            val signInClient = viewModel.getGoogleSignInClient(context as MainActivity)
                            launcher.launch(signInClient.signInIntent)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFF1F2A38)
                        ),
                        border = ButtonDefaults.outlinedButtonBorder.copy(
                            brush = Brush.linearGradient(
                                colors = listOf(Color(0xFF3A4A5C), Color(0xFF3A4A5C))
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("G", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Doorgaan met Google",
                            fontSize = 16.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSelected) Color.Transparent else Color.Transparent,
            contentColor = if (isSelected) Color.White else Color(0xFF8B9AAB)
        ),
        shape = RoundedCornerShape(8.dp),
        contentPadding = PaddingValues(0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (isSelected) {
                        Modifier.background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFF6B35), Color(0xFFFF4757))
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    } else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}