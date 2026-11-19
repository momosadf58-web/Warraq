package de.mo.bibliothek

import de.mo.bibliothek.data.AlrewaqCatalog
import de.mo.bibliothek.data.BookLookup
import de.mo.bibliothek.data.LibraryRepository
import org.junit.Assert.*
import org.junit.Test

class AlrewaqCatalogTest {
    private fun fixture() = javaClass.getResource("/alrewaq-book.json")!!.readText()

    @Test fun realPublisherRecordProvidesOneConsistentEdition() {
        val book = AlrewaqCatalog.parse(fixture()).single()
        assertEquals("أبي الذي أكره", book.title)
        assertEquals("عماد رشاد عثمان", book.author)
        assertEquals("9789778240788", book.isbn)
        assertEquals("2020", book.published)
        assertTrue(book.coverUrl.startsWith("https://d11v1jq8owgw3u.cloudfront.net/"))
        assertEquals(book.coverUrl, LibraryRepository.allowedCoverUrl(book.coverUrl).toString())
        assertTrue(BookLookup.matchesExpectedTitle("أبي الذي أكره", book))
        assertFalse(BookLookup.matchesExpectedTitle("أحببت وغدا", book))
    }

    @Test fun originalArabicTitleIsSentWithoutRemovingHamza() {
        var requested = ""
        AlrewaqCatalog.search("أبي الذي أكره") { requested = it; fixture() }
        val query = java.net.URLDecoder.decode(java.net.URI(requested).rawQuery, "UTF-8")
        assertEquals("search=أبي الذي أكره&per_page=20", query)
    }

    @Test fun unrelatedImageHostIsNeverImported() {
        val data = fixture().replace("d11v1jq8owgw3u.cloudfront.net", "untrusted.example")
        assertEquals("", AlrewaqCatalog.parse(data).single().coverUrl)
        assertEquals(0, AlrewaqCatalog.parse(fixture().replace("alrewaqpublishing.com", "untrusted.example")).size)
    }
}
