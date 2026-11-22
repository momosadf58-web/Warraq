package de.mo.bibliothek.data

/** Fehlende Angaben eines Buches. Allam schlägt Ergänzungen nur aus Katalogen vor; übernommen wird nur nach Bestätigung. */
enum class Gap { ISBN, COVER, YEAR, PUBLISHER, LANGUAGE, PAGES }

enum class AuditKind { INCOMPLETE, DUPLICATE, MISSING_COVER_FILE }

/** Ein Hinweis im Aufräumbericht. [otherId] nur bei möglichen Dubletten. */
data class AuditFinding(val kind: AuditKind, val bookId: String, val otherId: String? = null, val gaps: List<Gap> = emptyList())

/** Lücken eines Buches; [coverExists] prüft eine lokale Coverdatei nur lesend. */
fun Book.gaps(coverExists: (String) -> Boolean): List<Gap> = buildList {
    if (isbnKey(isbn).length !in setOf(10, 13)) add(Gap.ISBN)
    val hasCover = (cover.isNotEmpty() && coverExists(cover)) || (cover.isEmpty() && coverUrl.isNotEmpty())
    if (!hasCover) add(Gap.COVER)
    if (!Regex("[0-9]{4}").containsMatchIn(published)) add(Gap.YEAR)
    if (publisher.isBlank()) add(Gap.PUBLISHER)
    if (language.isBlank()) add(Gap.LANGUAGE)
    if (pages <= 0) add(Gap.PAGES)
}

/** Lücken, die für die Arbeit mit dem Buch wirklich zählen (Bericht). Seiten/Verlag allein sind kein Hinweis. */
private val CORE = setOf(Gap.ISBN, Gap.COVER, Gap.YEAR, Gap.LANGUAGE)

/** Lokaler Aufräumbericht: verändert nichts, nutzt keine KI und sendet keine Daten. */
fun auditLibrary(books: List<Book>, coverExists: (String) -> Boolean): List<AuditFinding> {
    val findings = mutableListOf<AuditFinding>()
    for (book in books) {
        if (book.cover.isNotEmpty() && !coverExists(book.cover)) findings += AuditFinding(AuditKind.MISSING_COVER_FILE, book.id)
    }
    for (i in books.indices) for (j in i + 1 until books.size) {
        if (books[i].sameAs(books[j])) findings += AuditFinding(AuditKind.DUPLICATE, books[i].id, books[j].id)
    }
    for (book in books) {
        val gaps = book.gaps(coverExists)
        if (gaps.any { it in CORE }) findings += AuditFinding(AuditKind.INCOMPLETE, book.id, gaps = gaps)
    }
    return findings
}
