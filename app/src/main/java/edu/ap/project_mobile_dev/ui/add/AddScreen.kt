package edu.ap.project_mobile_dev.ui.add

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.rememberAsyncImagePainter
import edu.ap.project_mobile_dev.ui.model.ActivityPost

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    viewModel: AddViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onLocationAdded: (ActivityPost) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Locatie Toevoegen", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Terug",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1E2837)
                )
            )
        },
        containerColor = Color(0xFF0F172A)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            val context = LocalContext.current
            val photoPickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.PickVisualMedia()
            ) { uri ->
                uri?.let {
                    viewModel.onPhotoSelected(it, context)
                }
            }
            // Foto sectie
            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage ?: "",
                    color = Color.Red,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
            PhotoSection(
                photoUri = uiState.photoUri,
                onPhotoClick = {
                    photoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                },
                isPhotoLoading=uiState.isPhotoLoading
            )

            LocationNameField(
                value = uiState.name,
                onValueChange = { viewModel.updateLocationName(it) }
            )

            // Stad
            CityField(
                value = uiState.city,
                onValueChange = { viewModel.updateCity(it) }
            )

            // Gebruik huidige locatie
            UseCurrentLocationButton(
                onClick = { viewModel.useCurrentLocation() }
            )

            // Categorie sectie
            Text(
                text = "Categorie",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            CategoryGrid(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { viewModel.selectCategory(it) }
            )

            // Beschrijving
            DescriptionField(
                value = uiState.description,
                onValueChange = { viewModel.updateDescription(it) }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Locatie Opslaan button
            Button(
                onClick = {
                    viewModel.saveLocation(onSuccess = {
                            newActivity -> onLocationAdded(newActivity)
                    })
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Transparent
                ),
                contentPadding = PaddingValues(0.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = uiState.isFormValid
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.horizontalGradient(
                                colors = listOf(Color(0xFFFF6B35), Color(0xFFFF4757))
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        .then(
                            if (!uiState.isFormValid) {
                                Modifier.background(
                                    color = Color(0xFF334155).copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(12.dp)
                                )
                            } else Modifier
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if(uiState.isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 3.dp
                        )
                    }
                    else {
                        Text(
                            text = "Locatie Opslaan",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PhotoSection(
    photoUri: String?,
    onPhotoClick: () -> Unit,
    isPhotoLoading: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .background(
                color = Color(0xFF1E2837),
                shape = RoundedCornerShape(12.dp)
            )
            .border(
                width = 2.dp,
                color = Color(0xFF2A3441),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onPhotoClick),
        contentAlignment = Alignment.Center
    ) {
        when {
            isPhotoLoading -> {
                CircularProgressIndicator(
                    color = Color(0xFFFF6B35),
                    strokeWidth = 3.dp
                )
            }

            photoUri != null -> {
                Image(
                    painter = rememberAsyncImagePainter(photoUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp))
                )
            }

            else -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PhotoCamera,
                        contentDescription = "Upload foto",
                        tint = Color(0xFF7A8694),
                        modifier = Modifier.size(48.dp)
                    )
                    Text(
                        text = "Klik om foto te uploaden",
                        color = Color(0xFF7A8694),
                        fontSize = 14.sp
                    )
                }
            }
        }

    }
}

@Composable
private fun LocationNameField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Naam van de locatie",
            color = Color.White,
            fontSize = 14.sp
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Bijv. De Koninck Brouwerij", color = Color(0xFF5A6470)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF1E2837),
                unfocusedContainerColor = Color(0xFF1E2837),
                focusedBorderColor = Color(0xFF3A4451),
                unfocusedBorderColor = Color(0xFF2A3441)
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun CityField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Stad",
            color = Color.White,
            fontSize = 14.sp
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Bijv. Antwerpen", color = Color(0xFF5A6470)) },
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF1E2837),
                unfocusedContainerColor = Color(0xFF1E2837),
                focusedBorderColor = Color(0xFF3A4451),
                unfocusedBorderColor = Color(0xFF2A3441)
            ),
            shape = RoundedCornerShape(12.dp)
        )
    }
}

@Composable
private fun UseCurrentLocationButton(
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = Color(0xFFFF6B35),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Gebruik huidige locatie",
            color = Color(0xFFFF6B35),
            fontSize = 14.sp
        )
    }
}

@Composable
private fun CategoryGrid(
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryCard(
                category = Category.RESTAURANT,
                isSelected = selectedCategory == Category.RESTAURANT,
                onSelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )
            CategoryCard(
                category = Category.CAFE,
                isSelected = selectedCategory == Category.CAFE,
                onSelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryCard(
                category = Category.HOTEL,
                isSelected = selectedCategory == Category.HOTEL,
                onSelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )
            CategoryCard(
                category = Category.MONUMENT,
                isSelected = selectedCategory == Category.MONUMENT,
                onSelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryCard(
                category = Category.SHOPPING,
                isSelected = selectedCategory == Category.SHOPPING,
                onSelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )
            CategoryCard(
                category = Category.NIGHTLIFE,
                isSelected = selectedCategory == Category.NIGHTLIFE,
                onSelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CategoryCard(
                category = Category.CULTURE,
                isSelected = selectedCategory == Category.CULTURE,
                onSelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )
            CategoryCard(
                category = Category.SPORT,
                isSelected = selectedCategory == Category.SPORT,
                onSelected = onCategorySelected,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun CategoryCard(
    category: Category,
    isSelected: Boolean,
    onSelected: (Category) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(80.dp)
            .clickable { onSelected(category) },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) Color(0xFFFF6B35).copy(alpha = 0.5f) else Color(0xFF1E2837)
        ),
        shape = RoundedCornerShape(12.dp),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFF6B35))
        } else null
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = category.icon,
                contentDescription = category.displayName,
                tint = if (isSelected) Color(0xFFFF6B35) else Color(0xFF7A8694),
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.displayName,
                color = Color.White,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun DescriptionField(
    value: String,
    onValueChange: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Beschrijving",
            color = Color.White,
            fontSize = 14.sp
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text("Vertel iets over deze locatie...", color = Color(0xFF5A6470)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = Color(0xFF1E2837),
                unfocusedContainerColor = Color(0xFF1E2837),
                focusedBorderColor = Color(0xFF3A4451),
                unfocusedBorderColor = Color(0xFF2A3441)
            ),
            shape = RoundedCornerShape(12.dp),
            maxLines = 5
        )
    }
}