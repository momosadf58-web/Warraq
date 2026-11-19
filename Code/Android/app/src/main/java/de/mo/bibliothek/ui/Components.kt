package de.mo.bibliothek.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AutoStories
import androidx.compose.material.icons.outlined.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import de.mo.bibliothek.data.Book
import de.mo.bibliothek.data.ReadStatus
import java.io.File

/** Small tactile response; Compose observes Android's animation duration scale. */
@Composable
fun Modifier.bookTap(onClick: () -> Unit): Modifier {
    val interaction = remember { MutableInteractionSource() }
    val pressed by interaction.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.965f else 1f, tween(160), label = "book-press")
    return graphicsLayer {
        scaleX = scale; scaleY = scale
        rotationY = (1f - scale) * -70f
    }.clickable(interactionSource = interaction, indication = LocalIndication.current, role = Role.Button, onClick = onClick)
}

/** Leinenfarben für Bücher ohne Coverbild. */
private val Cloth = listOf(
    Color(0xFF3E5A3E), Color(0xFF6E2E2A), Color(0xFF2E4358), Color(0xFF8A6A2F),
    Color(0xFF2F5552), Color(0xFF5B3A55), Color(0xFF7A4B2A), Color(0xFF384038),
)

fun clothColor(title: String): Color = Cloth[Math.floorMod(title.hashCode(), Cloth.size)]

/** Modell für Coil: lokale Datei hat Vorrang, sonst Online-Adresse, sonst nichts. */
fun coverModel(book: Book, coverFile: (String) -> File): Any? = when {
    book.cover.isNotEmpty() -> runCatching { coverFile(book.cover) }.getOrNull()
    book.coverUrl.isNotEmpty() -> book.coverUrl
    else -> null
}

@Composable
fun BookCover(
    title: String,
    author: String,
    model: Any?,
    modifier: Modifier = Modifier,
    corner: Dp = 4.dp,
    elevation: Dp = 6.dp,
    titleSize: Int = 15,
    onBitmap: ((android.graphics.Bitmap) -> Unit)? = null,
) {
    val shape = RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp, topEnd = corner, bottomEnd = corner)
    Box(
        modifier
            .shadow(elevation, shape, clip = false)
            .clip(shape)
            .semantics { contentDescription = title },
    ) {
        if (model == null) {
            Placeholder(title, author, titleSize)
        } else {
            val context = LocalContext.current
            SubcomposeAsyncImage(
                model = ImageRequest.Builder(context).data(model).crossfade(220)
                    .allowHardware(onBitmap == null)
                    .listener(onSuccess = { _, result ->
                        val drawable = result.drawable
                        if (onBitmap != null && drawable is android.graphics.drawable.BitmapDrawable) onBitmap(drawable.bitmap)
                    })
                    .build(),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = { Placeholder(title, author, titleSize) },
                error = { Placeholder(title, author, titleSize) },
            )
        }
        // Buchrücken-Falz links für Tiefe.
        Box(
            Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        0f to Color.Black.copy(alpha = 0.22f), 0.035f to Color.White.copy(alpha = 0.10f),
                        0.07f to Color.Transparent, 1f to Color.Transparent,
                    )
                )
        )
    }
}

@Composable
private fun Placeholder(title: String, author: String, titleSize: Int) {
    val base = clothColor(title)
    Box(
        Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(listOf(base, base.copy(alpha = 0.86f).compositeOver(Color.Black)))),
    ) {
        Column(
            Modifier
                .fillMaxSize()
                .padding(horizontal = 10.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFD9BE90).copy(alpha = 0.7f)))
            Text(
                title,
                color = Color(0xFFF7F0E2),
                fontFamily = FontFamily.Serif,
                fontSize = titleSize.sp,
                lineHeight = (titleSize * 1.18f).sp,
                textAlign = TextAlign.Center,
                maxLines = 5,
                overflow = TextOverflow.Ellipsis,
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (author.isNotEmpty()) Text(
                    author,
                    color = Color(0xFFE9DCC2).copy(alpha = 0.85f),
                    fontSize = (titleSize * 0.66f).sp,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFFD9BE90).copy(alpha = 0.7f)))
            }
        }
    }
}

private fun Color.compositeOver(background: Color): Color {
    val a = alpha
    return Color(red * a + background.red * (1 - a), green * a + background.green * (1 - a), blue * a + background.blue * (1 - a), 1f)
}

@Composable
fun statusIcon(status: ReadStatus): ImageVector = when (status) {
    ReadStatus.UNREAD -> Icons.Outlined.RadioButtonUnchecked
    ReadStatus.READING -> Icons.Outlined.AutoStories
    ReadStatus.READ -> Icons.Filled.CheckCircle
}

@Composable
fun statusColor(status: ReadStatus): Color = when (status) {
    ReadStatus.UNREAD -> MaterialTheme.colorScheme.onSurfaceVariant
    ReadStatus.READING -> LocalWood.current.reading
    ReadStatus.READ -> LocalWood.current.read
}

/**
 * Statusknopf am Buch. Ein Tipp öffnet die drei Stufen direkt an dieser Stelle,
 * der zweite Tipp wählt. [compact] zeigt nur Symbol und Kurztext für schmale Regalplätze.
 */
@Composable
fun StatusPill(status: ReadStatus, title: String, onChange: (ReadStatus) -> Unit, modifier: Modifier = Modifier, compact: Boolean = false) {
    var open by remember { mutableStateOf(false) }
    val color = statusColor(status)
    val description = "$title: ${status.label()}"
    Box(modifier) {
        Surface(
            onClick = { open = true },
            shape = RoundedCornerShape(50),
            color = if (status == ReadStatus.UNREAD) Color.Transparent else color.copy(alpha = if (compact) 0.10f else 0.14f),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                // Im Regal ohne Rahmen: die Cover sollen auffallen, nicht die Knöpfe.
                .border(1.dp, if (status == ReadStatus.UNREAD && !compact) MaterialTheme.colorScheme.outlineVariant else Color.Transparent, RoundedCornerShape(50))
                .semantics { contentDescription = description },
        ) {
            Row(
                Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
            ) {
                Icon(statusIcon(status), null, tint = color, modifier = Modifier.size(if (compact) 14.dp else 16.dp))
                Spacer(Modifier.width(5.dp))
                Text(
                    if (compact) status.shortLabel() else status.label(),
                    color = if (status == ReadStatus.UNREAD) MaterialTheme.colorScheme.onSurfaceVariant else color,
                    style = MaterialTheme.typography.labelMedium,
                    fontSize = if (compact) 11.sp else 12.sp,
                    maxLines = if (compact) 1 else 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
        DropdownMenu(expanded = open, onDismissRequest = { open = false }, shape = RoundedCornerShape(16.dp)) {
            ReadStatus.entries.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label(), color = if (option == status) statusColor(option) else MaterialTheme.colorScheme.onSurface) },
                    leadingIcon = { Icon(statusIcon(option), null, tint = statusColor(option)) },
                    trailingIcon = { if (option == status) Icon(Icons.Filled.Check, null, tint = statusColor(option)) },
                    onClick = {
                        open = false
                        if (option != status) onChange(option)
                    },
                )
            }
        }
    }
}

@Composable
fun Stars(rating: Int, onRate: ((Int) -> Unit)?, size: Dp = 28.dp) {
    val haptics = androidx.compose.ui.platform.LocalHapticFeedback.current
    Row {
        for (i in 1..5) {
            val filled = i <= rating
            Icon(
                if (filled) Icons.Filled.Star else Icons.Outlined.StarBorder,
                contentDescription = "$i",
                tint = if (filled) Color(0xFFD9A441) else MaterialTheme.colorScheme.outline,
                modifier = Modifier
                    .size(size + 8.dp)
                    .clip(RoundedCornerShape(50))
                    .then(if (onRate != null) Modifier.clickable { haptics.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress); onRate(if (rating == i) 0 else i) } else Modifier)
                    .padding(4.dp),
            )
        }
    }
}

@Composable
fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        letterSpacing = 1.2.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier,
    )
}
