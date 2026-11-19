package app.fogwalk.ui.map

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

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
    Box(modifier = modifier) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val bg = if (themeStyle == MapThemeStyle.NIGHT) Color(0xFF14181A) else Color(0xFFE7DFCF)
            drawRect(color = bg)
        }
    }
}
