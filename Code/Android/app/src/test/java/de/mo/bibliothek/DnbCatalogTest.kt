package de.mo.bibliothek

import de.mo.bibliothek.data.DnbCatalog
import de.mo.bibliothek.data.LibraryRepository
import de.mo.bibliothek.data.sameWork
import org.junit.Assert.*
import org.junit.Test

class DnbCatalogTest {
    private fun fixture() = javaClass.getResource("/dnb-alchimist.xml")!!.readText()

    @Test fun realDnbRecordsBecomeConcreteEditions() {
        val editions = DnbCatalog.parse(fixture())
        assertEquals(3, editions.size)
        val first = editions.first()
        assertEquals("Der Alchimist: Roman", first.title)
        assertEquals("Paulo Coelho", first.author)
        assertEquals("Diogenes", first.publisher)
        assertTrue(first.isbn.matches(Regex("97832572[0-9]{5}")))
        assertTrue(first.published.matches(Regex("[0-9]{4}")))
        assertTrue(first.pages > 0)
        assertEquals(first.coverUrl, LibraryRepository.allowedCoverUrl(first.coverUrl).toString())
        // Alle Ausgaben gehören zu einem Werk und werden in der Trefferliste zusammengefasst.
        assertTrue(editions.all { sameWork(first, it) })
    }

    @Test fun titleAndSurnameAreSentAsCql() {
        var requested = ""
        DnbCatalog.search("Der Alchimist Paulo Coelho") { requested = it; fixture() }
        val cql = java.net.URLDecoder.decode(requested.substringAfter("query="), "UTF-8")
        assertEquals("tit=Der Alchimist Paulo Coelho or (tit=Der Alchimist Paulo and per=Coelho) or (tit=Der Alchimist and per=Coelho)", cql)
        DnbCatalog.search("Der Alchimist Coelho") { requested = it; fixture() }
        assertEquals("tit=Der Alchimist Coelho or (tit=Der Alchimist and per=Coelho)",
            java.net.URLDecoder.decode(requested.substringAfter("query="), "UTF-8"))
    }
}
