package edu.ap.project_mobile_dev.ui.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import edu.ap.project_mobile_dev.MainActivity
import edu.ap.project_mobile_dev.ui.login.LoginViewModel
import edu.ap.project_mobile_dev.ui.model.ActivityProfile
import edu.ap.project_mobile_dev.ui.model.ReviewProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController,
    onBack: () -> Unit,
    viewModel: ProfileViewModel = viewModel(),
    viewModelLogin: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.getUser()
    }

    var selectedTab by remember { mutableStateOf(0) }
    var isEditingUsername by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ProfileEvent.NavigateToActivity -> {
                    navController.navigate("activity/${event.id}")
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header with back button and profile info
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 0.dp),
                shape = RoundedCornerShape(0.dp),
                color = Color(0xFF1E2A3A)
            ) {
                Column {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(top = 30.dp, start = 8.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Terug",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    // Profile section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile picture placeholder
                        Surface(
                            modifier = Modifier.size(100.dp),
                            shape = CircleShape,
                            color = Color(0xFFFF6B35)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(60.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Username section
                        if (isEditingUsername) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = uiState.username,
                                    onValueChange = { viewModel.changeUsername(it) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 8.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        unfocusedBorderColor = Color(0xFF3F5266),
                                        focusedBorderColor = Color(0xFFFF6B35),
                                        unfocusedContainerColor = Color(0xFF2C3E50),
                                        focusedContainerColor = Color(0xFF2C3E50),
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                                IconButton(onClick = {
                                    isEditingUsername = false
                                    viewModel.changeDBUsername()
                                }) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Opslaan",
                                        tint = Color(0xFFFF6B35)
                                    )
                                }
                            }
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    uiState.username,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                IconButton(onClick = { isEditingUsername = true }) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = "Bewerk naam",
                                        tint = Color(0xFFB0BEC5),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Tabs
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Button(
                    onClick = { selectedTab = 0 },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = if(selectedTab == 0){
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFF6B35), Color(0xFFFF4757))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF2C3E50), Color(0xFF2C3E50))
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ){
                        Row(verticalAlignment = Alignment.CenterVertically){
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Favorieten (" + uiState.favorites.size + ")")
                        }
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { selectedTab = 1 },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Transparent
                    ),
                    contentPadding = PaddingValues(0.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = if(selectedTab == 1){
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFF6B35), Color(0xFFFF4757))
                                    )
                                } else {
                                    Brush.horizontalGradient(
                                        colors = listOf(Color(0xFF2C3E50), Color(0xFF2C3E50))
                                    )
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ){
                        Row(verticalAlignment = Alignment.CenterVertically){
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reviews (" + uiState.reviews.size + ")")
                        }
                    }
                }
            }
        }

        // Content based on selected tab
        if (selectedTab == 0) {
            // Favorites
            items(uiState.favorites) { activity ->
                FavoriteActivityCard(
                    activity = activity,
                    onRemove = { viewModel.removeFavorite(activity.activityId) },
                    onClick = { viewModel.goToActivity(it) }
                )
            }

            if (uiState.favorites.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.FavoriteBorder,
                        message = "Nog geen favorieten"
                    )
                }
            }
        } else {
            items(uiState.reviews) { review ->
                UserReviewCard(review = review)
            }

            if (uiState.reviews.isEmpty()) {
                item {
                    EmptyStateCard(
                        icon = Icons.Default.StarBorder,
                        message = "Nog geen reviews geschreven"
                    )
                }
            }
        }

        // Logout button at bottom
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E2A3A)
            ) {
                Button(
                    onClick = {
                        viewModelLogin.logout(context as? MainActivity)
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                    },
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
                        Text(
                            text = "Uitloggen",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun FavoriteActivityCard(
    activity: ActivityProfile,
    onRemove: () -> Unit,
    onClick: (String) -> Unit
) {
    Surface(
        onClick = { onClick(activity.activityId) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E2A3A)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    activity.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = Color(0xFFB0BEC5),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        activity.location,
                        color = Color(0xFFB0BEC5),
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFF6B35)
                ) {
                    Box(
                        modifier = Modifier
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFFFF6B35), Color(0xFFFF4757))
                                ),
                                shape = RoundedCornerShape(14.dp)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(start = 10.dp)
                        ) {
                            Icon(
                                imageVector = activity.category.icon,
                                contentDescription = "Icon",
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                activity.category.displayName,
                                modifier = Modifier
                                    .padding(horizontal = 12.dp, vertical = 4.dp)
                                    .offset(-4.dp),
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onRemove) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Verwijder",
                    tint = Color(0xFFFF6B35),
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun UserReviewCard(review: ReviewProfile) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E2A3A)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    review.activityId,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Row {
                    repeat(review.rating) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            review.bitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Activity image",
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .height(100.dp)
                        .aspectRatio(it.width.toFloat() / it.height.toFloat())
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            if (review.description.isNotEmpty()) {
                Text(
                    review.description,
                    color = Color(0xFFB0BEC5),
                    modifier = Modifier.padding(top = 8.dp),
                    lineHeight = 20.sp
                )
            }

            Text(
                review.date,
                color = Color(0xFF6B7A8F),
                fontSize = 12.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 8.dp)
            ) {
                IconButton(
                    onClick = { },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.ThumbUp,
                        contentDescription = null,
                        tint = Color(0xFF6B7A8F),
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    review.likes.toString(),
                    color = Color(0xFFB0BEC5),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun EmptyStateCard(icon: androidx.compose.ui.graphics.vector.ImageVector, message: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E2A3A)
    ) {
        Column(
            modifier = Modifier.padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = Color(0xFF6B7A8F),
                modifier = Modifier.size(64.dp)
            )
            Text(
                message,
                color = Color(0xFF6B7A8F),
                fontSize = 16.sp,
                modifier = Modifier.padding(top = 16.dp)
            )
        }
    }
}