package edu.ap.project_mobile_dev.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onActivityClick: (Activity) -> Unit,
    onProfileClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
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
                navigationIcon = {
                    Box(modifier = Modifier.width(48.dp))
                },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(
                            Icons.Default.AccountCircle,
                            contentDescription = "Profiel",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E2A3A)
                )
            )
        }
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
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                TabButton(
                    text = "Lijst",
                    icon = Icons.Default.FilterList,
                    selected = uiState.selectedTab == 0,
                    onClick = { viewModel.setSelectedTab(0) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                TabButton(
                    text = "Kaart",
                    icon = Icons.Default.Map,
                    selected = uiState.selectedTab == 1,
                    onClick = { viewModel.setSelectedTab(1) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { },
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
            }
            var isExpanded by remember { mutableStateOf(true) }
            if (uiState.selectedTab == 0) {
                Column(
                    modifier = Modifier
                        .padding(horizontal= 16.dp) //outer padding
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
                                        text = category,
                                        selected = uiState.selectedCategories.contains(category),
                                        onClick = { viewModel.toggleCategory(category) }
                                    )
                                }
                            }
                        }
                    }
                }

                // Activities List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF0F172A)),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.filteredActivities) { activity ->
                        ActivityCard(
                            activity = activity,
                            onClick = { onActivityClick(activity) }
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF1E2A3A)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Kaart weergave", color = Color.White)
                }
            }
        }
    }
}

@Composable
fun TabButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) Color(0xFFFF6B35) else Color(0xFF2C3E50),
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text)
    }
}

@Composable
fun CategoryChip(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = if (selected) Color(0xFFFF6B35) else Color(0xFF334155)
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            color = Color.White,
            fontSize = 14.sp
        )
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
                    "${activity.location} • ${activity.city}",
                    fontSize = 14.sp,
                    color = Color(0xFFB0BEC5)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFFF6B35)
            ) {
                Text(
                    activity.category,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                    color = Color.White,
                    fontSize = 12.sp
                )
            }
        }
    }
}