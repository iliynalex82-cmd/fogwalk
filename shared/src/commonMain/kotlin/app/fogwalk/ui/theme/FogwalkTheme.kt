package app.fogwalk.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

private val LightColors = lightColorScheme(
    primary = Color(0xFF4F6B57),
    onPrimary = Color(0xFFF7F4EC),
    secondary = Color(0xFF8A6A42),
    onSecondary = Color(0xFFFDFBF7),
    background = Color(0xFFF6F1E6),
    onBackground = Color(0xFF23201B),
    surface = Color(0xFFFBF7EE),
    onSurface = Color(0xFF23201B),
    surfaceVariant = Color(0xFFE9E0CE),
    onSurfaceVariant = Color(0xFF5A5246),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF8DB398),
    onPrimary = Color(0xFF141E17),
    secondary = Color(0xFFC9A675),
    onSecondary = Color(0xFF241B0F),
    background = Color(0xFF151816),
    onBackground = Color(0xFFEBE6DC),
    surface = Color(0xFF1D221F),
    onSurface = Color(0xFFEBE6DC),
    surfaceVariant = Color(0xFF2A312D),
    onSurfaceVariant = Color(0xFFB8B0A2),
)

private val FogwalkTypography = Typography(
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 24.sp,
        lineHeight = 30.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Serif,
        fontSize = 18.sp,
        lineHeight = 24.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontSize = 14.sp,
        lineHeight = 20.sp,
    ),
)

@Composable
fun FogwalkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = FogwalkTypography,
        content = content,
    )
}
