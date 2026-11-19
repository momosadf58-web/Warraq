package de.mo.bibliothek

import de.mo.bibliothek.data.BookLookup
import org.junit.Assert.*
import org.junit.Test

class EditionLookupTest {
    private val workJson = """{"docs":[{"key":"/works/OL25286715W","title":"The Dictionary of Obscure Sorrows","author_name":["John Koenig"],"isbn":["9781501153648","9781501153662"],"publisher":["Publisher A","Publisher B"],"first_publish_year":2010,"number_of_pages_median":200,"language":["eng","ger"],"cover_i":12373445}]}"""
    private fun work() = BookLookup.parseWorkSearch(workJson).single()

    @Test fun searchWorkNeverMixesMetadataFromDifferentEditions() {
        val work = work()
        assertTrue(work.isWorkResult)
        assertEquals("John Koenig", work.author)
        assertEquals("", work.isbn)
        assertEquals("", work.publisher)
        assertEquals("", work.published)
        assertEquals("", work.language)
        assertEquals(0, work.pages)
    }

    @Test fun editionContainsOnlyItsOwnDataAndOffersNextPage() {
        val page = BookLookup.parseEditionPage(work(), """{"size":42,"entries":[{"key":"/books/OL1M","title":"The Dictionary of Obscure Sorrows","isbn_13":["9781501153648"],"publishers":["Simon & Schuster"],"publish_date":"2021","number_of_pages":288,"languages":[{"key":"/languages/eng"}],"covers":[12373445]}]}""", 40)
        val edition = page.books.single()
        assertFalse(edition.isWorkResult)
        assertEquals("9781501153648", edition.isbn)
        assertEquals("Simon & Schuster", edition.publisher)
        assertEquals("2021", edition.published)
        assertEquals(288, edition.pages)
        assertEquals(41, page.nextOffset)
    }

    @Test fun lastPageStopsAndMissingEditionMetadataStaysUnknown() {
        val page = BookLookup.parseEditionPage(work(), """{"size":41,"entries":[{"key":"/books/OL2M","title":"Another edition"}]}""", 40)
        assertNull(page.nextOffset)
        assertEquals("", page.books.single().coverUrl)
        assertEquals("", page.books.single().isbn)
        assertEquals("", page.books.single().published)
    }

    @Test fun emptyPageCannotLoopEvenWithStaleTotal() {
        assertNull(BookLookup.parseEditionPage(work(), """{"size":90,"entries":[]}""", 40).nextOffset)
    }
}
