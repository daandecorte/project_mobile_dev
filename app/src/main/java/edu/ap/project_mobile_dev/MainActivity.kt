package edu.ap.project_mobile_dev

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.preference.PreferenceManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.FirebaseApp
import edu.ap.project_mobile_dev.ui.login.LoginScreen
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import dagger.hilt.components.SingletonComponent
import edu.ap.project_mobile_dev.dao.ActivityDao
import edu.ap.project_mobile_dev.dao.ReviewDao
import edu.ap.project_mobile_dev.dao.UserDao
import edu.ap.project_mobile_dev.database.AppDatabase
import edu.ap.project_mobile_dev.ui.add.AddScreen
import edu.ap.project_mobile_dev.ui.activity.ActivityScreen
import edu.ap.project_mobile_dev.ui.chat.ChatScreen
import edu.ap.project_mobile_dev.ui.chats.ChatsScreen
import edu.ap.project_mobile_dev.ui.home.HomeScreen
import edu.ap.project_mobile_dev.ui.home.HomeViewModel
import edu.ap.project_mobile_dev.ui.profile.ProfileScreen
import org.osmdroid.config.Configuration
import javax.inject.Singleton
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.INTERNET), 0)
        }
        FirebaseApp.initializeApp(this)
        FirebaseFirestore.setLoggingEnabled(true)
        DatabaseProvider.init(this)
        enableEdgeToEdge()
        setContent {
            CityTripTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    val navController = rememberNavController()
                    val user = FirebaseAuth.getInstance().currentUser
                    val homeViewModel: HomeViewModel = hiltViewModel()

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
                                onChatClick = { navController.navigate("chats") },
                                onProfileClick = { navController.navigate("profile") },
                                navController,
                                homeViewModel
                            )
                        }
                        composable("profile") {
                            ProfileScreen(
                                navController,
                                onBack = { navController.popBackStack() }
                            )
                        }
                        composable("add") {
                            AddScreen(
                                onNavigateBack = { navController.popBackStack() },
                                onLocationAdded = { newActivity ->
                                    homeViewModel.refreshActivities()  // update HomeViewModel
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
                        composable("chats") {
                            ChatsScreen(
                                onBack = { navController.popBackStack() },
                                onChatClick = { chat -> navController.navigate("chat/${chat.id}") }
                            )
                        }
                        composable("chat/{id}"){
                            backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id") ?: "0"

                            ChatScreen(
                                id,
                                onBack = { navController.popBackStack() },
                                onNavigateToActivity = {activityId->
                                    navController.navigate("activity/$activityId")
                                }
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
object DatabaseProvider {
    lateinit var database: AppDatabase
        private set

    fun init(context: Context) {
        database = Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app_database"
        ).build()
    }
}
@Module
@InstallIn(SingletonComponent::class)
object FirestoreModule {

    @Provides
    @Singleton
    fun provideFirestore(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }
}
@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(app: Application): AppDatabase {
        return Room.databaseBuilder(
            app,
            AppDatabase::class.java,
            "app_database"
        ).fallbackToDestructiveMigration().build()
    }

    @Provides
    fun provideActivityDao(database: AppDatabase):  ActivityDao {
        return database.activityDao()
    }
    @Provides
    fun provideReviewDao(database: AppDatabase):  ReviewDao {
        return database.reviewDao()
    }
    @Provides
    fun provideUserDao(database: AppDatabase):  UserDao {
        return database.userDao()
    }
}
