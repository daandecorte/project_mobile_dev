package edu.ap.project_mobile_dev.ui.map

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.LightingColorFilter
import android.graphics.drawable.BitmapDrawable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import edu.ap.project_mobile_dev.ui.home.HomeViewModel
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import edu.ap.project_mobile_dev.R
import edu.ap.project_mobile_dev.ui.home.HomeScreen
import edu.ap.project_mobile_dev.ui.model.Activity

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun OsmdroidMapView(
    onActivityClick: (Activity) -> Unit,
    viewModel: HomeViewModel
) {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val uiState by viewModel.uiState.collectAsState()
    val permissionState = rememberPermissionState(android.Manifest.permission.ACCESS_FINE_LOCATION)
    if (!permissionState.status.isGranted) {
        permissionState.launchPermissionRequest()
        viewModel.getCurrentLocation(context)
    }
    else viewModel.getCurrentLocation(context)
    var selectedActivity by remember { mutableStateOf<Activity?>(null) }
    Box(Modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { context ->
                val mapView = MapView(context)
                mapView.setTileSource(TileSourceFactory.MAPNIK)
                mapView.setMultiTouchControls(true)
                mapView.setBuiltInZoomControls(false)
                mapView
            },
            update = { mapView ->
                mapView.setZoomLevel(15.0)
                mapView.overlays.clear()
                mapView.setInitCenter(uiState.currentLocation)
                uiState.filteredActivities.forEach { activity ->
                    if (activity.lat.isNotEmpty() && activity.lon.isNotEmpty()) {
                        val original = ContextCompat.getDrawable(context, R.drawable.logo_transparent_orange)
                        val bitmap = (original as BitmapDrawable).bitmap
                        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 40, 40, true)
                        val scaledDrawable = BitmapDrawable(context.resources, scaledBitmap)

                        val marker = Marker(mapView).apply {
                            icon = scaledDrawable
                            position = GeoPoint(activity.lat.toDouble(), activity.lon.toDouble())
                            title = activity.title
                            snippet = "${activity.location}, ${activity.city}"
                            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)

                            setOnMarkerClickListener { _, _ ->
                                selectedActivity = activity
                                true
                            }
                        }
                        mapView.overlays.add(marker)
                    }
                }
            }
        )
        IconButton(
            modifier = Modifier.align(Alignment.BottomStart).padding(8.dp)
                .size(56.dp)
                .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(androidx.compose.ui.graphics.Color(0xFFFF6B35), androidx.compose.ui.graphics.Color(0xFFFF4757))
                ),
                    shape = RoundedCornerShape(16.dp))
                ,
            onClick = {viewModel.refreshActivities()},
        ) {
            Icon(imageVector = Icons.Default.Refresh,
                tint = androidx.compose.ui.graphics.Color.White,
                contentDescription = "",
                )
        }
        selectedActivity?.let { activity ->
            Box(modifier = Modifier
                .padding(16.dp)
            ) {
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = androidx.compose.ui.graphics.Color(0xFF1E2A3A))
                ) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(activity.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = androidx.compose.ui.graphics.Color(0xFFB0BEC5),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                "${activity.street} • ${activity.city}",
                                fontSize = 14.sp,
                                color = androidx.compose.ui.graphics.Color(0xFFB0BEC5)
                            )
                        }
                        Button(shape = RoundedCornerShape(16.dp),onClick = {onActivityClick(activity)}) {
                            Text("Bekijk Activiteit")
                        }
                    }

                }

                IconButton(
                    onClick = {selectedActivity=null},
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = androidx.compose.ui.graphics.Color(Color.GRAY)
                    )
                }
            }
        }
    }
}