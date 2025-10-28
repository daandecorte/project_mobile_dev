package edu.ap.project_mobile_dev.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import edu.ap.project_mobile_dev.MainActivity
import edu.ap.project_mobile_dev.ui.login.LoginViewModel

@Composable
fun ProfileScreen(navController: NavController, viewModel: LoginViewModel = viewModel()) {
    val context = LocalContext.current
    Column(modifier = Modifier.padding(50.dp)) {
        Text(text = "Profiel")
        Button(
            onClick = {
                viewModel.logout(context as? MainActivity)
                navController.navigate("login")
            },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Text("Logout")
        }
    }

}