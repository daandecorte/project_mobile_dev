package edu.ap.project_mobile_dev.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    Column(modifier = Modifier.padding(50.dp)) {
        Text(
            text = "home"
        )
        Button(
            onClick = {navController.navigate("profile")},
            modifier= Modifier.fillMaxWidth()
        ) {

            Text("naar profiel")
        }
    }
}