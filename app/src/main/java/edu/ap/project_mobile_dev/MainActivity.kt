package edu.ap.project_mobile_dev

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.navigation.NavController
import com.google.firebase.FirebaseApp
import edu.ap.project_mobile_dev.ui.login.LoginScreen
import edu.ap.project_mobile_dev.ui.theme.Project_mobile_devTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import edu.ap.project_mobile_dev.ui.add.AddScreen
import edu.ap.project_mobile_dev.ui.home.HomeScreen
import edu.ap.project_mobile_dev.ui.profile.ProfileScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        setContent {
            CityTripTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val navController = rememberNavController()
                    val user = FirebaseAuth.getInstance().currentUser

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
                            onActivityClick = { activity ->
                                navController.navigate("profile"/*"activity/${activity.id}"*/) {
                                    popUpTo("home") { inclusive = false }
                                }
                            },
                            onProfileClick = {
                                navController.navigate("profile") {
                                    popUpTo("home") { inclusive = true }
                                }
                            },
                             navController
                        )}
                        composable("profile") {
                            ProfileScreen(navController)
                        }
                        composable("add") {
                            AddScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onLocationAdded = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
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
            ),
            content = content
        )
    }
}
