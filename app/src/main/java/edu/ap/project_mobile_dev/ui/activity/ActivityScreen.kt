package edu.ap.project_mobile_dev.ui.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import edu.ap.project_mobile_dev.ui.model.Activity

data class Review(
    val userName: String,
    val initials: String,
    val timeAgo: String,
    val rating: Int,
    val text: String,
    val likes: Int,
    val hasImage: Boolean = false
)

@Composable
fun ActivityScreen(
    onProfileClick: () -> Unit,
    onBack: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var userRating by remember { mutableStateOf(0) }
    var reviewText by remember { mutableStateOf("") }

    val reviews = listOf(
        Review(
            userName = "Tom Janssens",
            initials = "TJ",
            timeAgo = "2 weken geleden",
            rating = 5,
            text = "Geweldige ervaring! De rondleiding was super interessant en het bier was heerlijk. Aanrader voor iedereen die van bier houdt.",
            likes = 12,
            hasImage = true
        ),
        Review(
            userName = "Sarah De Vries",
            initials = "SD",
            timeAgo = "1 maand geleden",
            rating = 4,
            text = "Mooie locatie en vriendelijk personeel. Een aanrader!",
            likes = 8
        )
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A)),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Header Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E2A3A)
            ) {
                Column {
                    // Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .background(Color(0xFF2C3E50))
                    ) {
                        // Placeholder
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Image,
                                contentDescription = null,
                                tint = Color(0xFF6B7A8F),
                                modifier = Modifier.size(64.dp)
                            )
                        }

                        // Back
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ){
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .size(32.dp)
                                    .padding(5.dp)
                                    .background(Color(0xFF1E2A3A), CircleShape)
                            ) {
                                Icon(
                                    Icons.Default.ArrowBack,
                                    contentDescription = "Terug",
                                    tint = Color.White
                                )
                            }

                            Spacer(modifier = Modifier.weight(1f))

                            IconButton(onClick = onProfileClick) {
                                Icon(
                                    Icons.Default.AccountCircle,
                                    contentDescription = "Profiel",
                                    tint = Color.White,
                                    modifier = Modifier.size(40.dp)
                                )
                            }
                        }
                        // Category, Title, Review stats
                        Column(
                            modifier = Modifier.align(Alignment.BottomStart).padding(16.dp)
                        ) {
                            Surface(
                                modifier = Modifier.padding(vertical = 8.dp),
                                color = Color(0xFFFF6B35),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Text(
                                    "category",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                "title",
                                fontSize = 30.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(top = 8.dp)
                            ) {
                                Text(
                                    "4.5",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                repeat(5) {
                                    Icon(
                                        Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFC107),
                                        modifier = Modifier.size(22.dp).padding(horizontal = 2.dp)
                                    )
                                }
                                Text(
                                    "(127 reviews)",
                                    color = Color(0xFFB0BEC5),
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tabs
        item {
            Row(modifier = Modifier.fillMaxWidth()) {
                Button(
                    onClick = { selectedTab = 0 },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 0) Color(0xFFFF6B35) else Color(0xFF2C3E50)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Informatie")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { selectedTab = 1 },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedTab == 1) Color(0xFFFF6B35) else Color(0xFF2C3E50)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
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

        // Location Card
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
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
                                "city",
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
                            containerColor = Color(0xFFFF6B35)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Directions,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Route")
                    }
                }
            }
        }

        // About Section
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
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
                        "description",
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
                modifier = Modifier.fillMaxWidth(),
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
                            "Reviews (${reviews.size})",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        IconButton(onClick = { }) {
                            Icon(Icons.Default.Add, "Voeg review toe", tint = Color.White)
                        }
                    }

                    reviews.forEach { review ->
                        Spacer(modifier = Modifier.height(16.dp))
                        ReviewItem(review)
                    }
                }
            }
        }

        // Write Review Section
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF1E2A3A)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Jouw review",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Text(
                        "Geef een rating",
                        color = Color(0xFFB0BEC5),
                        modifier = Modifier.padding(top = 12.dp, bottom = 8.dp),
                        fontSize = 14.sp
                    )

                    Row {
                        repeat(5) { index ->
                            IconButton(onClick = { userRating = index + 1 }) {
                                Icon(
                                    if (index < userRating) Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (index < userRating) Color(0xFFFFC107) else Color(0xFF6B7A8F),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    Text(
                        "Voeg een foto toe (optioneel)",
                        color = Color(0xFFB0BEC5),
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                        fontSize = 14.sp
                    )

                    Surface(
                        modifier = Modifier
                            .size(100.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        color = Color(0xFF2C3E50)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color(0xFF6B7A8F),
                                    modifier = Modifier.size(32.dp)
                                )
                                Text(
                                    "Voeg foto toe",
                                    color = Color(0xFF6B7A8F),
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = reviewText,
                        onValueChange = { reviewText = it },
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

                    Button(
                        onClick = { },
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF6B35)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            Icons.Default.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Verstuur")
                    }
                }
            }
        }
    }
}

@Composable
fun ReviewItem(review: Review) {
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
                            review.initials,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        review.userName,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        review.timeAgo,
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

        if (review.hasImage) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(8.dp)),
                color = Color(0xFF2C3E50)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Default.Image,
                        contentDescription = null,
                        tint = Color(0xFF6B7A8F),
                        modifier = Modifier.size(48.dp)
                    )
                }
            }
        }

        if (review.text.isNotEmpty()) {
            Text(
                review.text,
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