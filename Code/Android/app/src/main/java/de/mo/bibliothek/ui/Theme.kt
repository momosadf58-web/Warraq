package de.mo.bibliothek.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Zusatzfarben, die Material nicht kennt: Holz des Regals, Rückwand, Statusgrün. */
@Immutable
data class Wood(val plankTop: Color, val plankBottom: Color, val back: Color, val shadow: Color, val read: Color, val reading: Color)

val LocalWood = staticCompositionLocalOf {
    Wood(Color(0xFFD9BE90), Color(0xFFB08A5B), Color(0xFFEAE2D3), Color(0x33000000), Color(0xFF3E5A3E), Color(0xFFB7792F))
}

private val Light = lightColorScheme(
    primary = Color(0xFF3E5A3E),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6E3CC),
    onPrimaryContainer = Color(0xFF1A2C1A),
    secondary = Color(0xFF8A6A43),
    secondaryContainer = Color(0xFFEDE1CB),
    onSecondaryContainer = Color(0xFF3A2B17),
    background = Color(0xFFF4EFE6),
    onBackground = Color(0xFF1F2320),
    surface = Color(0xFFF4EFE6),
    onSurface = Color(0xFF1F2320),
    surfaceVariant = Color(0xFFE8E1D4),
    onSurfaceVariant = Color(0xFF5E625B),
    surfaceContainerLowest = Color(0xFFFFFDF8),
    surfaceContainerLow = Color(0xFFF8F4EC),
    surfaceContainer = Color(0xFFEFE8DC),
    surfaceContainerHigh = Color(0xFFE9E2D5),
    surfaceContainerHighest = Color(0xFFE3DBCD),
    outline = Color(0xFFB9B1A3),
    outlineVariant = Color(0xFFD8D0C2),
    error = Color(0xFFA23B2F),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFAFC9A0),
    onPrimary = Color(0xFF173018),
    primaryContainer = Color(0xFF2F4630),
    onPrimaryContainer = Color(0xFFD6E9CB),
    secondary = Color(0xFFD9BD92),
    secondaryContainer = Color(0xFF3B3022),
    onSecondaryContainer = Color(0xFFF0E1C8),
    background = Color(0xFF121413),
    onBackground = Color(0xFFECE6DA),
    surface = Color(0xFF121413),
    onSurface = Color(0xFFECE6DA),
    surfaceVariant = Color(0xFF2A2D2A),
    onSurfaceVariant = Color(0xFFA8A69E),
    surfaceContainerLowest = Color(0xFF0C0E0D),
    surfaceContainerLow = Color(0xFF181B19),
    surfaceContainer = Color(0xFF1C1F1D),
    surfaceContainerHigh = Color(0xFF242725),
    surfaceContainerHighest = Color(0xFF2E312F),
    outline = Color(0xFF5B5E59),
    outlineVariant = Color(0xFF393C38),
    error = Color(0xFFF2A194),
)

private val Serif = FontFamily.Serif

/** Aref Ruqaa (OFL) für den kalligrafischen Schriftzug. */
val Calligraphy = FontFamily(androidx.compose.ui.text.font.Font(de.mo.bibliothek.R.font.aref_ruqaa_bold, FontWeight.Bold))

private val Type = Typography().let { base ->
    base.copy(
        displaySmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Medium, fontSize = 40.sp, lineHeight = 46.sp),
        headlineLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Medium, fontSize = 32.sp, lineHeight = 38.sp),
        headlineMedium = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Medium, fontSize = 27.sp, lineHeight = 33.sp),
        headlineSmall = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Medium, fontSize = 22.sp, lineHeight = 28.sp),
        titleLarge = TextStyle(fontFamily = Serif, fontWeight = FontWeight.Medium, fontSize = 20.sp, lineHeight = 26.sp),
        titleMedium = base.titleMedium.copy(fontFamily = Serif, fontSize = 16.sp),
    )
}

@Composable
fun BookieTheme(content: @Composable () -> Unit) {
    val dark = isSystemInDarkTheme()
    val wood = if (dark) Wood(
        plankTop = Color(0xFF5A4532), plankBottom = Color(0xFF3A2B1E), back = Color(0xFF1A1C1B),
        shadow = Color(0x66000000), read = Color(0xFFAFC9A0), reading = Color(0xFFE0B36E),
    ) else LocalWood.current
    CompositionLocalProvider(LocalWood provides wood) {
        MaterialTheme(colorScheme = if (dark) Dark else Light, typography = Type, content = content)
    }
}
