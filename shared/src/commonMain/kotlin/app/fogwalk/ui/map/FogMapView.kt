package app.fogwalk.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

enum class MapThemeStyle {
    DAY_SEPIA,
    NIGHT,
}

@Composable
expect fun FogMapView(
    modifier: Modifier = Modifier,
    latitude: Double,
    longitude: Double,
    zoom: Double = 15.0,
    revealedCells: Set<Long> = emptySet(),
    fogDensity: Float = 0.92f,
    themeStyle: MapThemeStyle = MapThemeStyle.DAY_SEPIA,
)
