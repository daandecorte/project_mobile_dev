package edu.ap.project_mobile_dev

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.firebase.FirebaseApp
import edu.ap.project_mobile_dev.ui.login.LoginScreen
import edu.ap.project_mobile_dev.ui.theme.Project_mobile_devTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import edu.ap.project_mobile_dev.ui.add.AddScreen
import edu.ap.project_mobile_dev.ui.activity.ActivityScreen
import edu.ap.project_mobile_dev.ui.chat.ChatScreen
import edu.ap.project_mobile_dev.ui.home.HomeScreen
import edu.ap.project_mobile_dev.ui.home.HomeViewModel
import edu.ap.project_mobile_dev.ui.profile.ProfileScreen
import org.osmdroid.config.Configuration

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 0)
        }
        FirebaseApp.initializeApp(this)
        FirebaseFirestore.setLoggingEnabled(true)
        enableEdgeToEdge()
        setContent {
            CityTripTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val navController = rememberNavController()
                    val user = FirebaseAuth.getInstance().currentUser
                    val homeViewModel: HomeViewModel = viewModel()

                    NavHost(
                        navController = navController,
                        startDestination = if (user == null) "login" else "home"
                    ) {
                        composable("login") {
                            LoginScreen(
                                onLoginSuccess = {
                                    navController.navigate("home") {
                                        popUpTo("login") { inclusive = true }
                                    }
                                }
                            )
                        }
                        composable("home"){

                         HomeScreen(
                             onActivityClick = { activity -> navController.navigate("activity/${activity.documentId}") },
                             onChatClick = { navController.navigate("chat") },
                             onProfileClick = { navController.navigate("profile") },
                             navController,
                             homeViewModel
                        )}
                        composable("profile") {
                            ProfileScreen(
                                navController,
                                onBack = { navController.popBackStack()
                                }
                            )
                        }
                        composable("add") {
                            AddScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onLocationAdded = { newActivity ->
                                    homeViewModel.loadActivities()  // update HomeViewModel
                                    navController.popBackStack()           // ga terug naar HomeScreen
                                }
                            )
                        }
                        composable("activity/{id}") {
                            backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id") ?: "0"

                            ActivityScreen(
                                id,
                                onProfileClick = {
                                    navController.navigate("profile")
                                },
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("chat") {
                            ChatScreen(
                                onBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
        val ctx = applicationContext
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx))
        Configuration.getInstance().userAgentValue = "MapApp"
    }
    @Composable
    fun CityTripTheme(content: @Composable () -> Unit) {
        MaterialTheme(
            colorScheme = darkColorScheme(
                primary = Color(0xFFFF6B35),
                background = Color(0xFF0F172A),
                surface = Color(0xFF242F3F),
                onPrimary = Color.White,
                onBackground = Color.White,
                onSurface = Color.White
            )
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF2C3E50),
                                Color(0xFF0F172A)
                            )
                        )
                    )
            ) {
                content()
            }
        }
    }
}
