package edu.ap.project_mobile_dev.ui.home

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUpAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.ap.project_mobile_dev.ui.model.Activity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.window.Popup
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import edu.ap.project_mobile_dev.ui.add.Category
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import edu.ap.project_mobile_dev.ui.map.OsmdroidMapView

@SuppressLint("PermissionLaunchedDuringComposition")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    onActivityClick: (Activity) -> Unit,
    onChatClick: () -> Unit,
    onProfileClick: () -> Unit,
    navController: NavController,
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    var sortByExpanded by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val permissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    LaunchedEffect(permissionState.status) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
            viewModel.getCurrentLocation(context)
        }
        else viewModel.getCurrentLocation(context)
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onChatClick) {
                        Icon(
                            imageVector = Icons.Default.Chat,
                            contentDescription = "Menu",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profiel",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            "CityTrip",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Ontdek activiteiten in jouw buurt",
                            fontSize = 12.sp,
                            color = Color(0xFFB0BEC5)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF1E2A3A)
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("add") },
                containerColor = Color.Transparent,
                modifier = Modifier.padding(8.dp),
                contentColor = Color.White,
                shape = RoundedCornerShape(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFF6B35), Color(0xFFFF4757))
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ),
                    contentAlignment = Alignment.Center
                ){
                    Icon(Icons.Default.Add, contentDescription = "Voeg activiteit toe")
                }
            }
        },
        floatingActionButtonPosition = FabPosition.End,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Color(0xFF0F172A))
        ) {
            // Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                TabButton(
                    tab = 0,
                    text = "Lijst",
                    icon = Icons.Default.FilterList,
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    modifier = Modifier.weight(1f),
                    uiState = uiState
                )
                Spacer(modifier = Modifier.width(8.dp))
                TabButton(
                    tab = 1,
                    text = "Kaart",
                    icon = Icons.Default.Map,
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    modifier = Modifier.weight(1f),
                    uiState = uiState
                )
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier.wrapContentSize(Alignment.TopEnd)
                ) {
                    IconButton(
                        onClick = { sortByExpanded=!sortByExpanded },
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color(0xFF2C3E50), RoundedCornerShape(8.dp))
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filter",
                            tint = Color.White
                        )
                    }
                    if(sortByExpanded) {
                        Popup(
                            alignment = Alignment.TopEnd,
                            offset = IntOffset(0, 170)
                        ) {
                            Surface(
                                shape=RoundedCornerShape(8.dp),
                                color = Color(0xFF2C3E50),
                                shadowElevation = 8.dp,
                                modifier = Modifier.width(200.dp)
                            ) {

                                Column(
                                    modifier = Modifier.background(Color(0xFF2C3E50)).padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text("Sorteer op:")
                                    Divider(
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    SortOption(
                                        text = "Afstand",
                                        selected = uiState.sortBy == SortBy.LOCATION,
                                        onClick = {
                                            viewModel.updateSortBy(SortBy.LOCATION)
                                            sortByExpanded = false
                                        }
                                    )
                                    SortOption(
                                        text = "Rating (Hoog-Laag)",
                                        selected = uiState.sortBy == SortBy.RATINGH,
                                        onClick = {
                                            viewModel.updateSortBy(SortBy.RATINGH)
                                            sortByExpanded = false
                                        }
                                    )
                                    SortOption(
                                        text = "Rating (Laag-Hoog)",
                                        selected = uiState.sortBy == SortBy.RATINGL,
                                        onClick = {
                                            viewModel.updateSortBy(SortBy.RATINGL)
                                            sortByExpanded = false
                                        }
                                    )
                                    SortOption(
                                        text = "Alfabetisch",
                                        selected = uiState.sortBy == SortBy.ALPHABETICAL,
                                        onClick = {
                                            viewModel.updateSortBy(SortBy.ALPHABETICAL)
                                            sortByExpanded = false
                                        }
                                    )
                                }

                            }
                        }
                    }
                }

            }
            var isExpanded by remember { mutableStateOf(false) }
                Column(
                    modifier = Modifier
                        .padding(horizontal = 16.dp) //outer padding
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF1E2A3A))
                        .padding(12.dp) //inner padding
                ) {
                    // --- Header row with title + toggle icon ---
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isExpanded = !isExpanded },
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "Filters",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Icon(
                            imageVector = if (isExpanded) Icons.Default.Remove else Icons.Default.Add,
                            contentDescription = if (isExpanded) "Verberg filters" else "Toon filters",
                            tint = Color.White
                        )
                    }

                    // --- Expandable content ---
                    AnimatedVisibility(visible = isExpanded) {
                        Column(
                        ) {
                            Spacer(modifier = Modifier.height(12.dp))

                            Text(
                                "Locatie",
                                fontSize = 14.sp,
                                color = Color(0xFFB0BEC5)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = uiState.searchQuery,
                                onValueChange = { viewModel.updateSearchQuery(it) },
                                placeholder = {
                                    Text("Zoek een locatie...", color = Color(0xFF6B7A8F))
                                },
                                trailingIcon = {
                                    Icon(
                                        Icons.Default.Search,
                                        contentDescription = null,
                                        tint = Color(0xFF6B7A8F)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp)),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = Color(0xFF2C3E50),
                                    unfocusedContainerColor = Color(0xFF2C3E50),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    focusedBorderColor = Color(0xFF3F5266),
                                    unfocusedBorderColor = Color(0xFF3F5266)
                                )
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                "Categorie",
                                fontSize = 14.sp,
                                color = Color(0xFFB0BEC5)
                            )

                            Spacer(modifier = Modifier.height(8.dp))
                            val categories = uiState.activities
                                .map { it.category }
                                .distinct()
                                .sorted()
                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                categories.forEach { category ->
                                    CategoryChip(
                                        category = category,
                                        selected = uiState.selectedCategories.contains(category.displayName),
                                        onClick = { viewModel.toggleCategory(category.displayName) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Activities List
                if (uiState.selectedTab == 0) {
                    val isRefreshing = uiState.isRefreshing // Boolean from ViewModel
                    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing)
                    SwipeRefresh(
                        state = swipeRefreshState,
                        onRefresh = { viewModel.refreshActivities() } // Define this in ViewModel
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color(0xFF0F172A)),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if(uiState.isLoading) {
                                item {
                                    Box(
                                        modifier = Modifier.fillMaxSize(),
                                        contentAlignment=Alignment.Center
                                    ) {
                                        CircularProgressIndicator()
                                    }
                                }
                            }
                            else {
                                items(uiState.filteredActivities) { activity ->
                                    ActivityCard(
                                        activity = activity,
                                        onClick = { onActivityClick(activity) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(Color(0xFF0F172A))
                            .padding(16.dp)
                            .clip(RoundedCornerShape(16.dp)),
                    ) {
                        OsmdroidMapView(viewModel=viewModel, onActivityClick={activity->onActivityClick(activity)})
                    }
                }

        }
    }
}

@Composable
fun TabButton(
    tab: Int,
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    uiState: HomeUiState
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = if (uiState.selectedTab == tab) {
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFF6B35), Color(0xFFFF4757))
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF2C3E50), Color(0xFF2C3E50))
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(text)
            }
        }
    }
}

@Composable
fun CategoryChip(category: Category, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(
                    brush = if (selected) {
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFF6B35), Color(0xFFFF4757))
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFF334155), Color(0xFF334155))
                        )
                    },
                    shape = RoundedCornerShape(20.dp)
                )
        ){
            Row(
                modifier = Modifier.padding(start = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = "icon",
                    modifier = Modifier.size(20.dp)
                )
                Text(
                    category.displayName,
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .offset((-6).dp),
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun ActivityCard(activity: Activity, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF1E2A3A)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                activity.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFB0BEC5),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    "${activity.street} • ${activity.city}",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row() {
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
                Spacer(modifier = Modifier.width(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                    repeat(activity.averageRating ?: 0) {
                        Icon(
                            Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(18.dp).padding(horizontal = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
@Composable
fun SortOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = if (selected) Color(0xFF2C3E50) else Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = if (selected) {
                        Brush.horizontalGradient(
                            colors = listOf(Color(0xFFFF6B35), Color(0xFFFF4757))
                        )
                    } else {
                        Brush.horizontalGradient(
                            colors = listOf(Color.Transparent, Color.Transparent)
                        )
                    },
                    shape = RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text,
                color = Color.White,
                fontSize = 14.sp
            )
        }
    }
}