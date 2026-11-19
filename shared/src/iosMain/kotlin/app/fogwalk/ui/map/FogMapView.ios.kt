package app.fogwalk.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.interop.UIKitView
import app.fogwalk.geo.Grid
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKCoordinateRegionMakeWithDistance
import platform.MapKit.MKMapView
import platform.MapKit.MKMapViewDelegateProtocol
import platform.MapKit.MKOverlayProtocol
import platform.MapKit.MKOverlayRenderer
import platform.MapKit.MKTileOverlay
import platform.MapKit.MKTileOverlayRenderer
import platform.UIKit.UIColor
import platform.darwin.NSObject
import kotlin.math.cos

private class OsmMapDelegate : NSObject(), MKMapViewDelegateProtocol {
    override fun mapView(mapView: MKMapView, rendererForOverlay: MKOverlayProtocol): MKOverlayRenderer {
        return if (rendererForOverlay is MKTileOverlay) {
            MKTileOverlayRenderer(tileOverlay = rendererForOverlay)
        } else {
            MKOverlayRenderer(overlay = rendererForOverlay)
        }
    }
}

@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun FogMapView(
    modifier: Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Double,
    revealedCells: Set<Long>,
    fogDensity: Float,
    themeStyle: MapThemeStyle,
) {
    val delegate = remember { OsmMapDelegate() }

    Box(modifier = modifier) {
        UIKitView(
            modifier = Modifier.fillMaxSize(),
            factory = {
                MKMapView().apply {
                    this.delegate = delegate
                    showsUserLocation = true
                    val tileOverlay = MKTileOverlay(
                        URLTemplate = "https://tile.openstreetmap.org/{z}/{x}/{y}.png"
                    ).apply {
                        canReplaceMapContent = true
                    }
                    addOverlay(tileOverlay)
                    val spanMeters = 2000.0
                    val region = MKCoordinateRegionMakeWithDistance(
                        CLLocationCoordinate2DMake(latitude, longitude),
                        spanMeters,
                        spanMeters,
                    )
                    setRegion(region, animated = false)
                    backgroundColor = if (themeStyle == MapThemeStyle.NIGHT) {
                        UIColor.blackColor
                    } else {
                        UIColor.systemBackgroundColor
                    }
                }
            },
            update = { mapView ->
                val spanMeters = (160000.0 / (1 shl (zoom.toInt().coerceIn(8, 18) - 8))).coerceIn(250.0, 50000.0)
                val region = MKCoordinateRegionMakeWithDistance(
                    CLLocationCoordinate2DMake(latitude, longitude),
                    spanMeters,
                    spanMeters,
                )
                mapView.setRegion(region, animated = true)
            },
        )

        // Subtle color-grading layer for Sepia / Night theme over OSM tiles
        val tintColor = when (themeStyle) {
            MapThemeStyle.DAY_SEPIA -> Color(0x22A88450)
            MapThemeStyle.NIGHT -> Color(0x550D141C)
        }
        Box(modifier = Modifier.fillMaxSize().background(tintColor))

        // High-performance Skia/Compose Canvas Fog-of-War overlay
        val fogColor = if (themeStyle == MapThemeStyle.NIGHT) {
            Color(0xFF0E1318).copy(alpha = fogDensity.coerceIn(0.6f, 1f))
        } else {
            Color(0xFFD9D1C1).copy(alpha = fogDensity.coerceIn(0.6f, 1f))
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
        ) {
            drawRect(color = fogColor)

            // Punch out revealed cells around the current viewport
            val centerLat = latitude
            val centerLon = longitude
            val metersPerScreen = 2000.0
            val degLatPerPx = (metersPerScreen / 111_320.0) / size.height.coerceAtLeast(1f)
            val cosLat = cos(centerLat * kotlin.math.PI / 180.0).coerceAtLeast(0.1)
            val degLonPerPx = (metersPerScreen / (111_320.0 * cosLat)) / size.width.coerceAtLeast(1f)

            for (cellId in revealedCells) {
                val (cLat, cLon) = Grid.center(cellId)
                val x = ((cLon - centerLon) / degLonPerPx + size.width / 2f).toFloat()
                val y = ((centerLat - cLat) / degLatPerPx + size.height / 2f).toFloat()
                if (x in -80f..(size.width + 80f) && y in -80f..(size.height + 80f)) {
                    drawRect(
                        color = Color.Transparent,
                        topLeft = Offset(x - 24f, y - 24f),
                        size = Size(48f, 48f),
                        blendMode = BlendMode.Clear,
                    )
                }
            }
        }
    }
}
