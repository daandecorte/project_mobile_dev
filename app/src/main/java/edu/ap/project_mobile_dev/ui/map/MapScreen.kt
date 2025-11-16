package edu.ap.project_mobile_dev.ui.map

import android.graphics.LightingColorFilter
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.tileprovider.tilesource.XYTileSource
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView

@Composable
fun OsmdroidMapView() {
    val context = LocalContext.current
    var inputText by remember { mutableStateOf("") }
    var mapView by remember { mutableStateOf<MapView?>(null) }
    val ap = GeoPoint(51.230167, 4.416129)


    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val mapView = MapView(context)
            mapView.setTileSource(TileSourceFactory.MAPNIK)

            mapView.setBuiltInZoomControls(true)
            mapView.setMultiTouchControls(true)
            mapView.setZoomLevel(15.0)
            mapView.setInitCenter(ap)
            mapView
        }
    )
}