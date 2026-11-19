package de.mo.bibliothek.data

import java.net.URLEncoder

/** Deutsche Nationalbibliothek (SRU, ohne Schlüssel): ein Datensatz ist eine konkrete Ausgabe mit ISBN/Verlag/Jahr. */
internal object DnbCatalog {
    private const val SRU = "https://services.dnb.de/sru/dnb?version=1.1&operation=searchRetrieve&recordSchema=oai_dc&maximumRecords=50&query="

    /** Freitext "Titel Autor": ganzer Text als Titel oder alles außer dem letzten Wort als Titel plus Nachname. */
    fun search(query: String, fetch: (String) -> String): List<Found> {
        val words = clean(query).split(' ').filter { it.isNotBlank() }
        if (words.isEmpty()) return emptyList()
        var cql = "tit=" + words.joinToString(" ")
        // Autor am Ende ("Nachname" oder "Vorname Nachname"): restliche Wörter als Titel.
        // Ein Resttitel ohne Wort ab 4 Buchstaben ("Der") trifft beliebige Bücher des Autors.
        for (k in 1..2) words.dropLast(k).takeIf { rest -> rest.any { it.length >= 4 } }?.let { rest ->
            cql += " or (tit=" + rest.joinToString(" ") + " and per=" + words.last() + ")"
        }
        return parse(fetch(SRU + URLEncoder.encode(cql, "UTF-8")))
    }

    /** Alle deutschen/hier verzeichneten Ausgaben eines Werks nach Titel und Nachname. */
    fun editions(title: String, author: String, fetch: (String) -> String): List<Found> {
        val t = clean(title.substringBefore(':'))
        if (t.isBlank()) return emptyList()
        val surname = clean(author.substringBefore(',')).split(' ').lastOrNull().orEmpty()
        val cql = "tit=$t" + if (surname.length >= 2) " and per=$surname" else ""
        return parse(fetch(SRU + URLEncoder.encode(cql, "UTF-8")))
    }

    // CQL-Sonderzeichen entfernen; Wörter bleiben, damit die DNB selbst nach Wortstamm sucht.
    private fun clean(value: String) = value.replace(Regex("[\"()=<>/\\\\*?]"), " ").replace(Regex("\\s+"), " ").trim()

    internal fun parse(xml: String): List<Found> =
        Regex("(?s)<record>(.*?)</record>").findAll(xml).mapNotNull { parseRecord(it.groupValues[1]) }.toList()

    private fun parseRecord(r: String): Found? {
        fun all(tag: String) = Regex("(?s)<dc:$tag\\b[^>]*>(.*?)</dc:$tag>").findAll(r).map { unescape(it.groupValues[1]).trim() }.toList()
        val rawTitle = all("title").firstOrNull() ?: return null
        // "[Einheitstitel] ; Titel : Untertitel / Verantwortlich ; Übersetzer" -> "Titel: Untertitel"
        val title = rawTitle.replace(Regex("^\\[[^]]*]\\s*;\\s*"), "").substringBefore(" / ")
            .substringBefore(" ; ").replace(Regex("\\s+:\\s+"), ": ").trim()
        if (title.isBlank()) return null
        val creators = all("creator")
        val author = (creators.firstOrNull { it.contains("[Verfasser]") } ?: creators.firstOrNull { !it.contains('[') }).orEmpty()
            .replace(Regex("\\s*\\[.*"), "").trim().let { name ->
                val parts = name.split(", ", limit = 2)
                if (parts.size == 2 && !parts[1].contains(' ')) "${parts[1]} ${parts[0]}" else name
            }
        val isbn = Regex("(?s)<dc:identifier[^>]*tel:ISBN\"[^>]*>(.*?)</dc:identifier>").findAll(r)
            .map { isbnKey(unescape(it.groupValues[1]).trim().substringBefore(' ')) }
            .firstOrNull { it.length == 13 || it.length == 10 }.orEmpty()
        val idn = Regex("(?s)<dc:identifier[^>]*dnb:IDN\"[^>]*>(\\d+)</dc:identifier>").find(r)?.groupValues?.get(1).orEmpty()
        val subjects = all("subject")
        val genre = if (subjects.any { it == "B Belletristik" }) "Belletristik"
            else subjects.firstOrNull { it.matches(Regex("\\d{3}\\s.+")) }?.drop(4).orEmpty()
        return Found(
            title = title, author = author, isbn = isbn,
            // DNB-Cover liegen hinter einer Bot-Schranke; Open Library liefert vorhandene Cover nach ISBN.
            coverUrl = if (isbn.isEmpty()) "" else "https://covers.openlibrary.org/b/isbn/$isbn-L.jpg?default=false",
            publisher = all("publisher").firstOrNull().orEmpty().substringAfterLast(" : ").trim(),
            published = Regex("\\d{4}").find(all("date").firstOrNull().orEmpty())?.value.orEmpty(),
            pages = Regex("(\\d+)\\s*(Seiten|S\\.)").find(all("format").joinToString(" "))?.groupValues?.get(1)?.toIntOrNull() ?: 0,
            genre = genre, language = languageName(all("language").firstOrNull().orEmpty()), description = "",
            source = "Deutsche Nationalbibliothek", sourceUrl = if (idn.isEmpty()) "" else "https://d-nb.info/$idn",
            workKey = if (idn.isEmpty()) "" else "dnb:$idn",
        )
    }

    private fun unescape(s: String) = s.replace("&lt;", "<").replace("&gt;", ">").replace("&quot;", "\"")
        .replace("&apos;", "'").replace("&amp;", "&")
}
