package edu.ap.project_mobile_dev.ui.activity

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import edu.ap.project_mobile_dev.ui.model.ReviewDetail
import androidx.compose.ui.unit.DpOffset

@Composable
fun ActivityScreen(
    activityId: String,
    onProfileClick: () -> Unit,
    onBack: () -> Unit,
    viewModel: ActivityViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = LocalContext.current
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let {
            viewModel.onPhotoSelected(it, context)
        }
    }

    LaunchedEffect(activityId) {
        viewModel.loadActivity(activityId)
    }

    if (uiState.isLoading) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
        return
    }

    uiState.errorMessage?.let { err ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = err, color = Color.White)
        }
        return
    }

    val activity = uiState.activity

    if (activity == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0F172A)),
            contentAlignment = Alignment.Center
        ) {
            Text("Geen activiteit geladen", color = Color.White)
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentPadding = PaddingValues(0.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Card
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp) // adjust as you like
            ) {
                // Background Image
                uiState.activity?.bitmap?.let {
                    Image(
                        bitmap = it.asImageBitmap(),
                        contentDescription = "Activity image",
                        modifier = Modifier
                            .fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } ?: run {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF2C3E50)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Image,
                            contentDescription = null,
                            tint = Color(0xFF6B7A8F),
                            modifier = Modifier.size(64.dp)
                        )
                    }
                }

                // Overlay gradient (optional for contrast)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color(0xCC0F172A)),
                                startY = 100f
                            )
                        )
                )

                // Top bar (back + profile)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 30.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .background(Color(0x80000000), CircleShape)
                            .size(36.dp)
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Terug",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.AccountCircle, contentDescription = "Profiel", tint = Color.White, modifier = Modifier.size(40.dp))
                    }
                }

                // Heart icon (bottom right)
                IconButton(
                    onClick = { viewModel.saveActivity() },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .background(Color(0x80000000), CircleShape)
                        .size(48.dp)
                ) {
                    Icon(
                        imageVector = if (uiState.saved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Bewaar activiteit",
                        tint = if (uiState.saved) Color(0xFFFF6B35) else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Bottom section: title + stats
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Surface(
                        color = Color.Transparent,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFF6B35), Color(0xFFFF4757))
                                    ),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                activity.category.displayName,
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Text(
                        activity.title,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(top = 8.dp)
                    )

                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 6.dp)) {
                        repeat(uiState.activity?.ratingM?:0) {
                            Icon(
                                Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(18.dp).padding(horizontal = 2.dp)
                            )
                        }
                        Text("("+uiState.reviews.count().toString() + " reviews)", color = Color(0xFFB0BEC5), fontSize = 12.sp, modifier = Modifier.padding(start = 4.dp))
                    }
                }
            }
        }


        // Tabs
        item {
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                Button(
                    onClick = { viewModel.changeSelectedTab(0) },
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
                                brush = if (uiState.selectedTab == 0) {
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
                        Text("Informatie", color = Color.White)
                    }
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { viewModel.changeSelectedTab(1) },
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
                                brush = if (uiState.selectedTab == 1) {
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
                    ){
                        Row() {
                            Icon(
                                Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Foto's (8)")
                        }
                    }
                }
            }
        }

        // Location Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                activity.city,
                                color = Color.White,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                Icons.Default.Navigation,
                                contentDescription = null,
                                tint = Color(0xFFB0BEC5),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "2.3 km",
                                color = Color(0xFFB0BEC5),
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }

                    Button(
                        onClick = { },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent
                        ),
                        contentPadding = PaddingValues(0.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(
                                    brush = Brush.horizontalGradient(
                                        colors = listOf(Color(0xFFFF6B35), Color(0xFFFF4757))
                                    ),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Directions,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp),
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Route", color = Color.White)
                            }
                        }
                    }
                }
            }
        }

        // About Section
        item {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E2A3A)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Over deze locatie",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        activity.description,
                        color = Color(0xFFB0BEC5),
                        modifier = Modifier.padding(top = 8.dp),
                        lineHeight = 20.sp
                    )
                }
            }
        }

        // Reviews Section
        item {
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
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
                            "Reviews (${uiState.reviews.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { viewModel.showReviewDialog(true) }) {
                            Icon(Icons.Default.Add, "Voeg review toe", tint = Color.White)
                        }
                    }
                    if(uiState.isReviewsLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                        contentAlignment=Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    else {
                        uiState.reviews.forEachIndexed { index, review ->
                            Spacer(modifier = Modifier.height(16.dp))

                            ReviewItem(review, viewModel)

                            if (index < uiState.reviews.lastIndex) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Divider(color = Color(0xFF2E3A47), thickness = 1.dp)
                            }
                        }
                    }
                }
            }
        }
    }
    // Write Review Section
    if (uiState.showReviewDialog) {
        Dialog(
            onDismissRequest = { viewModel.showReviewDialog(false) }
        ) {
            // container to control size and background
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF1E2A3A)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        // Title row with close button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "Jouw review",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            IconButton(onClick = { viewModel.showReviewDialog(false) }) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = "Sluit",
                                    tint = Color.White
                                )
                            }
                        }

                        Text(
                            "Geef een rating",
                            color = Color(0xFFB0BEC5),
                            modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                            fontSize = 14.sp
                        )

                        Row {
                            repeat(5) { index ->
                                IconButton(onClick = { viewModel.updateRating(index + 1) }) {
                                    Icon(
                                        if (index < uiState.userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                        contentDescription = null,
                                        tint = if (index < uiState.userRating) Color(0xFFFFC107) else Color(0xFF6B7A8F),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                        }

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

                        OutlinedTextField(
                            value = uiState.reviewText,
                            onValueChange = { viewModel.updateReviewText(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            placeholder = { Text("Deel jouw ervaring...", color = Color(0xFF6B7A8F)) },
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFF3F5266),
                                focusedBorderColor = Color(0xFFFF6B35),
                                unfocusedContainerColor = Color(0xFF2C3E50),
                                focusedContainerColor = Color(0xFF2C3E50),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            minLines = 3
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            // Optional Cancel
                            TextButton(onClick = { viewModel.showReviewDialog(false) }) {
                                Text("Annuleren", color = Color(0xFFB0BEC5))
                            }

                            Spacer(modifier = Modifier.width(8.dp))

                            Button(
                                onClick = { viewModel.uploadReview() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B35)),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Verstuur")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: ReviewDetail, viewModel: ActivityViewModel) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(40.dp),
                    shape = CircleShape,
                    color = Color(0xFFFF6B35)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "+",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        review.username,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        review.date,
                        color = Color(0xFFB0BEC5),
                        fontSize = 12.sp
                    )
                }
            }

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
                modifier = Modifier.padding(top = 12.dp),
                lineHeight = 20.sp
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 8.dp)
        ) {
            IconButton(
                onClick = { viewModel.likeReview(review) },
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