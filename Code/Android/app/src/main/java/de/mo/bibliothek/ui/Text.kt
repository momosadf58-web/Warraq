package de.mo.bibliothek.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import de.mo.bibliothek.data.ReadStatus

/** true = arabische Oberfläche (rechts nach links). */
val LocalArabic = staticCompositionLocalOf { false }

/** Zweisprachiger Text direkt an der Verwendungsstelle; hält beide Fassungen nebeneinander lesbar. */
@Composable
@ReadOnlyComposable
fun tr(de: String, ar: String): String = if (LocalArabic.current) ar else de

@Composable
@ReadOnlyComposable
fun ReadStatus.label(): String = when (this) {
    ReadStatus.UNREAD -> tr("Ungelesen", "لم أقرأه")
    ReadStatus.READING -> tr("Lese ich gerade", "أقرأه الآن")
    ReadStatus.READ -> tr("Gelesen", "قرأته")
}

/** Kurzform für schmale Regalplätze, passt in eine Zeile. */
@Composable
@ReadOnlyComposable
fun ReadStatus.shortLabel(): String = when (this) {
    ReadStatus.UNREAD -> tr("Ungelesen", "لم أقرأه")
    ReadStatus.READING -> tr("Lese ich", "أقرأه")
    ReadStatus.READ -> tr("Gelesen", "قرأته")
}

@Composable
@ReadOnlyComposable
fun booksCount(count: Int): String =
    if (LocalArabic.current) "$count ${if (count in 3..10) "كتب" else "كتاب"}"
    else if (count == 1) "1 Buch" else "$count Bücher"
