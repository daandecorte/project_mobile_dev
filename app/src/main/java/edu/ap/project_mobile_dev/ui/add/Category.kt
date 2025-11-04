package edu.ap.project_mobile_dev.ui.add

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Hotel
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.TheaterComedy
import androidx.compose.ui.graphics.vector.ImageVector

enum class Category(    val displayName: String,
                        val icon: ImageVector
) {
    RESTAURANT("Restaurant", Icons.Default.Restaurant),
    CAFE("Cafe", Icons.Default.LocalCafe),
    HOTEL("Hotel", Icons.Default.Hotel),
    MONUMENT("Monument", Icons.Default.AccountBalance),
    SHOPPING("Shopping", Icons.Default.ShoppingBag),
    NIGHTLIFE("Nightlife", Icons.Default.MusicNote),
    CULTURE("Cultuur", Icons.Default.TheaterComedy),
    SPORT("Sport", Icons.Default.Sports),
    OTHER("Other", Icons.Default.Android)
}