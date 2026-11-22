package de.mo.bibliothek

import de.mo.bibliothek.data.AuditKind
import de.mo.bibliothek.data.Book
import de.mo.bibliothek.data.Gap
import de.mo.bibliothek.data.auditLibrary
import de.mo.bibliothek.data.gaps
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CleanupTest {
    private val full = Book("1", "Sofies Welt", "Jostein Gaarder", isbn = "9783446173477", language = "Deutsch",
        cover = "a.jpg", publisher = "Hanser", published = "1993", pages = 613)

    @Test fun completeBookHasNoGaps() = assertEquals(emptyList<Gap>(), full.gaps { true })

    @Test fun gapsListMissingFields() {
        val empty = Book("2", "العمى", "ساراماغو")
        assertEquals(listOf(Gap.ISBN, Gap.COVER, Gap.YEAR, Gap.PUBLISHER, Gap.LANGUAGE, Gap.PAGES), empty.gaps { true })
        // Cover file referenced but missing on disk counts as gap; remote cover URL alone is fine.
        assertEquals(listOf(Gap.COVER), full.gaps { false })
        assertEquals(emptyList<Gap>(), full.copy(cover = "", coverUrl = "https://x/c.jpg").gaps { false })
        // "August 1, 1993" still contains a year.
        assertEquals(emptyList<Gap>(), full.copy(published = "August 1, 1993").gaps { true })
    }

    @Test fun auditReportsDuplicatesMissingFilesAndCoreGapsOnly() {
        val dup = full.copy(id = "3")
        val onlyPages = full.copy(id = "4", isbn = "9783446173478", title = "Anderes Buch", pages = 0, publisher = "")
        val incomplete = Book("5", "قل وداعاً", "فهد العودة")
        val findings = auditLibrary(listOf(full, dup, onlyPages, incomplete)) { it != "missing.jpg" }
        assertTrue(findings.any { it.kind == AuditKind.DUPLICATE && it.bookId == "1" && it.otherId == "3" })
        assertTrue(findings.none { it.bookId == "4" }) // Verlag/Seiten allein sind kein Hinweis
        assertTrue(findings.any { it.kind == AuditKind.INCOMPLETE && it.bookId == "5" })
        val missing = auditLibrary(listOf(full.copy(cover = "missing.jpg"))) { it != "missing.jpg" }
        assertTrue(missing.any { it.kind == AuditKind.MISSING_COVER_FILE })
    }

    @Test fun citationsAreNotQuotes() {
        assertTrue(de.mo.bibliothek.data.KnowledgeLookup.isCitation("Der Hexenhammer, zum ersten Male ins Deutsche übertragen von J.W.R. Schmidt, Erster Teil, Berlin 1906, S. 99"))
        assertTrue(de.mo.bibliothek.data.KnowledgeLookup.isCitation("Haben oder Sein, dtv, München 1979, Seite 12"))
        assertTrue(!de.mo.bibliothek.data.KnowledgeLookup.isCitation("Also schlecht ist das Weib von Natur, da es schneller am Glauben zweifelt."))
        assertTrue(!de.mo.bibliothek.data.KnowledgeLookup.isCitation("Man sieht nur mit dem Herzen gut. Das Wesentliche ist für die Augen unsichtbar."))
    }
}
