package de.mo.bibliothek

import de.mo.bibliothek.data.Book
import de.mo.bibliothek.data.ReadStatus
import de.mo.bibliothek.data.isArabic
import de.mo.bibliothek.data.isGerman
import de.mo.bibliothek.data.sameAs
import de.mo.bibliothek.data.selectLibraryBooks
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class LibrarySelectionTest {
    @Test fun chipCountsUseSearchAndTheOtherFilter() {
        val germanRead = Book("1", "Geschichte A", language = "Deutsch", status = ReadStatus.READ.stored)
        val germanUnread = Book("2", "Geschichte B", language = "de")
        val arabicRead = Book("3", "Geschichte C", language = "Arabisch", status = ReadStatus.READ.stored)
        val otherRead = Book("4", "Gedichte", language = "Deutsch", status = ReadStatus.READ.stored)
        val books = listOf(germanRead, germanUnread, arabicRead, otherRead)

        val selection = selectLibraryBooks(books, "geschichte", ReadStatus.READ.name, "de")
        assertEquals(listOf(germanRead), selection.books)
        assertEquals(3, selection.allCount)
        assertEquals(mapOf(ReadStatus.UNREAD to 1, ReadStatus.READING to 0, ReadStatus.READ to 1), selection.statusCounts)
        assertEquals(1, selection.arabicCount)
        assertEquals(1, selection.germanCount)

        val reset = selectLibraryBooks(books, "geschichte", null, null)
        assertEquals(listOf(germanRead, germanUnread, arabicRead), reset.books)
        assertEquals(reset.books.size, reset.allCount)
        assertEquals(2, reset.statusCounts.getValue(ReadStatus.READ))
        assertEquals(2, reset.germanCount)
    }

    @Test fun isbnSearchMatchesWithOrWithoutSeparatorsAndHandlesIsbn10() {
        val compact = Book("1", "Die Wand", isbn = "9783548228007")
        val hyphenated = Book("2", "Fahrenheit 451", isbn = "978-1-4516-7331-9")
        val isbn10 = Book("3", "Ein Buch", isbn = "080442957X")
        val books = listOf(compact, hyphenated, isbn10)

        assertEquals(listOf(compact), selectLibraryBooks(books, " 978-3-548-22800-7 ", null, null).books)
        assertEquals(listOf(hyphenated), selectLibraryBooks(books, "9781451673319", null, null).books)
        assertEquals(listOf(isbn10), selectLibraryBooks(books, "0-8044-2957-x", null, null).books)
        assertTrue(selectLibraryBooks(books, "Buch 978-3-548-22800-7", null, null).books.isEmpty())
        assertEquals(listOf(hyphenated), selectLibraryBooks(books, "fahrenheit 451", null, null).books)
    }

    @Test fun twoPresentIsbnsDecideWhetherBooksMatch() {
        val book = Book("1", "Die Wand", author = "Marlen Haushofer", isbn = "9783548228007")
        assertFalse(book.sameAs(book.copy(id = "2", isbn = "9781451673319")))
        assertTrue(book.sameAs(book.copy(id = "3", title = "The Wall", isbn = "978-3-548-22800-7")))
        val withoutIsbn = book.copy(id = "4", title = "DIE WAND", isbn = "")
        assertTrue(book.sameAs(withoutIsbn))
        assertTrue(withoutIsbn.sameAs(book))
        assertFalse(book.sameAs(withoutIsbn.copy(author = "Andere Autorin")))
    }

    @Test fun explicitLanguageTakesPriorityOverTitleScript() {
        val arabicTitle = Book("1", "الأيام")
        val latinTitle = Book("2", "Die Wand")
        for (language in listOf("Deutsch", " de ")) {
            assertTrue(arabicTitle.copy(language = language).isGerman)
            assertFalse(arabicTitle.copy(language = language).isArabic)
        }
        assertTrue(latinTitle.copy(language = "Arabisch").isArabic)
        assertFalse(latinTitle.copy(language = "Arabisch").isGerman)
        for (language in listOf("Englisch", "Spanisch")) {
            for (book in listOf(arabicTitle, latinTitle)) {
                assertFalse(book.copy(language = language).isArabic)
                assertFalse(book.copy(language = language).isGerman)
            }
        }
        assertTrue(arabicTitle.isArabic)
        assertFalse(arabicTitle.isGerman)
        assertTrue(latinTitle.isGerman)
        assertFalse(latinTitle.isArabic)
    }
}
