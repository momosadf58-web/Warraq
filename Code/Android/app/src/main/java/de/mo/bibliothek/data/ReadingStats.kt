package de.mo.bibliothek.data

import java.util.Calendar
import java.util.Locale

enum class BookKind { NOVEL, NONFICTION, OTHER, UNKNOWN }

/** Missing genre metadata must never become an invented novel classification. */
fun Book.kind(): BookKind {
    val key = searchKey(genre)
    return when {
        key.isBlank() -> BookKind.UNKNOWN
        Regex("non.?fiction|sachbuch|philosoph|psycholog|history|geschichte|biograph|science|polit|self help|فلسف|تاريخ|علوم|سير|سياس").containsMatchIn(key) -> BookKind.NONFICTION
        Regex("fiction|novel|roman|رواي").containsMatchIn(key) -> BookKind.NOVEL
        else -> BookKind.OTHER
    }
}

fun readingYear(millis: Long): Int? = if (millis <= 0L) null else
    Calendar.getInstance().apply { timeInMillis = millis }.get(Calendar.YEAR)

fun Book.publicationYear(): Int? = Regex("(?<!\\d)([12]\\d{3})(?!\\d)")
    .find(published)?.groupValues?.get(1)?.toIntOrNull()

fun booksReadInYear(books: List<Book>, year: Int): List<Book> =
    books.filter { it.readStatus == ReadStatus.READ && readingYear(it.readAt) == year }.sortedByDescending { it.readAt }

fun oracleCandidates(books: List<Book>): List<Book> = books.filter { it.readStatus == ReadStatus.UNREAD }

fun languageName(code: String): String {
    val normalized = when (code.lowercase(Locale.ROOT)) {
        "ger", "deu" -> "de"
        "ara" -> "ar"
        "eng" -> "en"
        "fre", "fra" -> "fr"
        "por" -> "pt"
        "spa" -> "es"
        "tur" -> "tr"
        "rus" -> "ru"
        else -> code.lowercase(Locale.ROOT)
    }
    if (normalized.isBlank()) return ""
    return Locale.forLanguageTag(normalized).getDisplayLanguage(Locale.GERMAN).ifBlank { code }
}
