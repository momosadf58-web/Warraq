package de.mo.bibliothek.data

import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

private val ARABIC_SCRIPT = Regex("[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF]")

/** Ein Online-Treffer, noch nicht in der Bibliothek. */
data class Found(
    val title: String,
    val author: String,
    val isbn: String,
    val coverUrl: String,
    val publisher: String,
    val published: String,
    val pages: Int,
    val genre: String,
    val language: String,
    val description: String,
    val source: String,
    val sourceUrl: String = "",
    val workKey: String = "",
    val coverDistance: Int? = null,
    /** Werkcover, falls die Ausgabe selbst online kein Cover hat (z. B. DNB-Ausgaben). */
    val fallbackCover: String = "",
) {
    val isWorkResult: Boolean get() = sourceUrl.matches(Regex("https://openlibrary.org/works/OL[0-9]+W"))

    fun toBook(id: String, now: Long) = Book(
        id = id, title = title, author = author, isbn = isbn, coverUrl = coverUrl, publisher = publisher,
        published = published, pages = pages, genre = genre, language = language, description = description,
        addedAt = now, extra = JSONObject().put("source", source).put("sourceUrl", sourceUrl).put("workKey", workKey).toString(),
    )
}

data class EditionPage(val books: List<Found>, val nextOffset: Int? = null)

/** Google Books zuerst (mehr deutsche Ausgaben), Open Library als Ergänzung und Ersatz. */
object BookLookup {

    /** Bei null Treffern (typisch: einzelne OCR-Lesefehler) Paare der längsten Wörter versuchen. */
    fun search(query: String, expectedTitle: String = ""): List<Found> {
        val found = searchExact(query).filter { matchesExpectedTitle(expectedTitle, it) }
        if (found.isNotEmpty()) return found
        if (expectedTitle.isNotBlank()) {
            val byTitle = searchExact(expectedTitle).filter { matchesExpectedTitle(expectedTitle, it) }
            if (byTitle.isNotEmpty()) return byTitle
        }
        // A known camera title remains mandatory; author-only retry pairs are not book evidence.
        val retryQuery = expectedTitle.ifBlank { query }
        val words = retryQuery.split(Regex("\\s+")).filter { it.length >= 4 }.distinct().sortedByDescending { it.length }.take(4)
        val pairs = words.indices.flatMap { i -> (i + 1 until words.size).map { j -> "${words[i]} ${words[j]}" } }
        // A pair only counts where both of its words occur in the hit; one shared word is no evidence.
        val retries = pairs.map { pair -> java.util.concurrent.CompletableFuture.supplyAsync {
            val words = searchKey(pair).split(' ')
            runCatching { searchExact(pair) }.getOrDefault(emptyList()).filter { found ->
                val text = searchKey(found.title + " " + found.author)
                words.all { text.contains(it) } && matchesExpectedTitle(expectedTitle, found)
            }
        } }
        return retries.map { it.get() }.firstOrNull { it.isNotEmpty() }?.sortedByDescending { matchScore(query, it) } ?: found
    }

    internal fun matchesExpectedTitle(expectedTitle: String, found: Found): Boolean =
        expectedTitle.isBlank() || KnowledgeLookup.titlesMatch(expectedTitle, found.title) ||
            KnowledgeLookup.titlesRelated(expectedTitle, found.title)

    private fun searchExact(query: String): List<Found> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        val isbn = isbnKey(q)
        val asIsbn = (isbn.length == 10 || isbn.length == 13) && q.count { it.isLetter() && it != 'X' && it != 'x' } == 0
        val gf = java.util.concurrent.CompletableFuture.supplyAsync { runCatching { google(if (asIsbn) "isbn:$isbn" else q) } }
        val of = java.util.concurrent.CompletableFuture.supplyAsync { runCatching { openLibrary(q) } }
        val af = if (ARABIC_SCRIPT.containsMatchIn(q)) java.util.concurrent.CompletableFuture.supplyAsync {
            runCatching { AlrewaqCatalog.search(q, ::get) }
        } else null
        // Google Books antwortet ohne Schlüssel oft mit HTTP 429; die DNB liefert deutsche Ausgaben ohne Schlüssel.
        val df = if (!ARABIC_SCRIPT.containsMatchIn(q) && !asIsbn) java.util.concurrent.CompletableFuture.supplyAsync {
            runCatching { DnbCatalog.search(q, ::get) }
        } else null
        val local = ArabicCatalog.search(q)
        val google = gf.get()
        val openLibrary = of.get()
        val alrewaq = af?.get()
        val dnb = df?.get()
        if (google.isFailure && openLibrary.isFailure && (alrewaq == null || alrewaq.isFailure) && (dnb == null || dnb.isFailure) && local.isEmpty()) throw IOException("Keine Verbindung zur Buchsuche.")
        val others = local + google.getOrDefault(emptyList()) + alrewaq?.getOrDefault(emptyList()).orEmpty() + openLibrary.getOrDefault(emptyList())
        val dnbEditions = dnb?.getOrDefault(emptyList()).orEmpty().map { e ->
            e.copy(fallbackCover = others.firstOrNull { it.coverUrl.isNotEmpty() && sameWork(it, e) }?.coverUrl.orEmpty())
        }
        val merged = mutableListOf<Found>()
        (others + dnbEditions).forEach { found ->
            val duplicate = merged.indexOfFirst { same(it, found) }
            if (duplicate < 0) merged += found
            else if (merged[duplicate].isbn == found.isbn) merged[duplicate] = fill(merged[duplicate], found)
        }
        return merged.sortedByDescending { matchScore(q, it) }.take(50)
    }

    fun byIsbn(raw: String): Found? {
        val isbn = isbnKey(raw)
        if (isbn.isEmpty()) return null
        val googleResult = runCatching { google("isbn:$isbn") }
        val openResult = runCatching { openLibraryIsbn(isbn) }
        if (googleResult.isFailure && openResult.isFailure) throw IOException("Keine Verbindung zur Buchsuche.")
        val google = googleResult.getOrDefault(emptyList()).firstOrNull()
        val open = openResult.getOrNull()
        val found = when {
            google != null && open != null -> fill(google, open)
            else -> google ?: open
        } ?: return null
        val withIsbn = if (found.isbn.isEmpty()) found.copy(isbn = isbn) else found
        return if (withIsbn.coverUrl.isEmpty())
            withIsbn.copy(coverUrl = "https://covers.openlibrary.org/b/isbn/$isbn-L.jpg?default=false")
        else withIsbn
    }

    fun editions(work: Found, offset: Int = 0): EditionPage {
        require(offset >= 0)
        if (!work.workKey.matches(Regex("/works/OL[0-9]+W"))) return EditionPage(listOf(work))
        return parseEditionPage(work, get("https://openlibrary.org" + work.workKey + "/editions.json?limit=40&offset=$offset"), offset)
    }

    internal fun parseEditionPage(work: Found, json: String, offset: Int): EditionPage {
        val root = JSONObject(json)
        val entries = root.getJSONArray("entries")
        val editions = (0 until entries.length()).mapNotNull { i ->
            entries.optJSONObject(i)?.let { parseEdition(work, it) }
        }
        val next = offset + entries.length()
        val hasMore = entries.length() > 0 && if (root.has("size")) next < root.optInt("size") else entries.length() == 40
        return EditionPage(editions, next.takeIf { hasMore })
    }

    private fun parseEdition(work: Found, e: JSONObject): Found? {
        val id = e.optString("key")
        if (!id.matches(Regex("/books/OL[0-9]+M"))) return null
        val cover = e.optJSONArray("covers")?.optLong(0) ?: 0
        val code = e.optJSONArray("languages")?.optJSONObject(0)?.optString("key").orEmpty().substringAfterLast('/')
        val description = e.opt("description").let { if (it is JSONObject) it.optString("value") else it as? String }.orEmpty()
        return work.copy(title = e.optString("title").ifBlank { work.title },
            isbn = e.optJSONArray("isbn_13")?.optString(0).orEmpty().ifBlank { e.optJSONArray("isbn_10")?.optString(0).orEmpty() },
            publisher = e.optJSONArray("publishers")?.optString(0).orEmpty(), published = e.optString("publish_date"),
            pages = e.optInt("number_of_pages"), language = languageName(code), description = description,
            coverUrl = if (cover > 0) "https://covers.openlibrary.org/b/id/$cover-L.jpg" else "",
            source = "Open Library", sourceUrl = "https://openlibrary.org$id", coverDistance = null)
    }

    private fun openLibraryIsbn(isbn: String): Found? {
        val edition = JSONObject(get("https://openlibrary.org/isbn/$isbn.json"))
        val workKey = edition.optJSONArray("works")?.optJSONObject(0)?.optString("key").orEmpty()
        val work = if (workKey.matches(Regex("/works/OL[0-9]+W")))
            runCatching { JSONObject(get("https://openlibrary.org$workKey.json")) }.getOrNull() else null
        val authors = edition.optJSONArray("authors") ?: work?.optJSONArray("authors")
        val author = (0 until (authors?.length() ?: 0)).take(4).mapNotNull { index ->
            val a = authors?.optJSONObject(index) ?: return@mapNotNull null
            val key = a.optJSONObject("author")?.optString("key") ?: a.optString("key")
            if (!key.matches(Regex("/authors/OL[0-9]+A"))) return@mapNotNull null
            runCatching { JSONObject(get("https://openlibrary.org$key.json")).optString("name") }.getOrNull()
        }.joinToString(", ")
        val base = Found(edition.optString("title"), author, "", "", "", "", 0, "", "", "", "Open Library", workKey = workKey)
        return parseEdition(base, edition)?.copy(isbn = isbn)
    }

    private fun same(a: Found, b: Found): Boolean {
        val ia = isbnKey(a.isbn)
        if (ia.isNotEmpty() && ia == isbnKey(b.isbn)) return true
        if (a.isbn.isNotEmpty() && b.isbn.isNotEmpty()) return false
        return searchKey(a.title) == searchKey(b.title) && searchKey(a.author) == searchKey(b.author) && a.publisher == b.publisher && a.published == b.published
    }

    private fun fill(a: Found, b: Found) = a.copy(
        workKey = a.workKey.ifEmpty { b.workKey },
        isbn = a.isbn.ifEmpty { b.isbn },
        coverUrl = a.coverUrl.ifEmpty { b.coverUrl },
        publisher = a.publisher.ifEmpty { b.publisher },
        published = a.published.ifEmpty { b.published },
        pages = if (a.pages > 0) a.pages else b.pages,
        genre = a.genre.ifEmpty { b.genre },
        description = a.description.ifEmpty { b.description },
    )

    private fun google(query: String): List<Found> {
        val url = "https://www.googleapis.com/books/v1/volumes?maxResults=25&printType=books&q=" + encode(query)
        val items = JSONObject(get(url)).optJSONArray("items") ?: return emptyList()
        return (0 until items.length()).mapNotNull { index ->
            val info = items.optJSONObject(index)?.optJSONObject("volumeInfo") ?: return@mapNotNull null
            val title = listOf(info.optString("title"), info.optString("subtitle")).filter { it.isNotBlank() }.joinToString(": ")
            if (title.isBlank()) return@mapNotNull null
            val ids = info.optJSONArray("industryIdentifiers")
            var isbn13 = ""
            var isbn10 = ""
            if (ids != null) for (i in 0 until ids.length()) {
                val id = ids.optJSONObject(i) ?: continue
                when (id.optString("type")) {
                    "ISBN_13" -> isbn13 = id.optString("identifier")
                    "ISBN_10" -> isbn10 = id.optString("identifier")
                }
            }
            val images = info.optJSONObject("imageLinks")
            val thumbnail = images?.optString("thumbnail").orEmpty().ifEmpty { images?.optString("smallThumbnail").orEmpty() }
            val cover = if (thumbnail.isEmpty()) "" else
                thumbnail.replace("http://", "https://").replace("&edge=curl", "") + "&fife=w800"
            Found(
                title = title,
                author = info.optJSONArray("authors")?.let { a -> (0 until a.length()).joinToString(", ") { a.optString(it) } }.orEmpty(),
                isbn = isbn13.ifEmpty { isbn10 },
                coverUrl = cover,
                publisher = info.optString("publisher"),
                published = info.optString("publishedDate").take(4),
                pages = info.optInt("pageCount"),
                // Google-Kategorien sind englische Schlagwörter („Fiction“) – kein Genre übernehmen; Mo ordnet Genres selbst/mit Allam.
                genre = "",
                language = languageName(info.optString("language")),
                description = info.optString("description").replace(Regex("<[^>]+>"), "").trim(),
                source = "Google Books",
                sourceUrl = "https://books.google.com/books?id=" + items.getJSONObject(index).optString("id"),
            )
        }
    }

    private fun openLibrary(query: String): List<Found> {
        val isbn = isbnKey(query)
        if ((isbn.length == 10 || isbn.length == 13) && query.none { it.isLetter() && it.uppercaseChar() != 'X' })
            return listOfNotNull(openLibraryIsbn(isbn))
        return parseWorkSearch(get("https://openlibrary.org/search.json?limit=20&fields=key,title,author_name,cover_i,subject&q=" + encode(query)))
    }

    internal fun parseWorkSearch(json: String): List<Found> {
        val docs = JSONObject(json).optJSONArray("docs") ?: return emptyList()
        return (0 until docs.length()).mapNotNull { index ->
            val doc = docs.optJSONObject(index) ?: return@mapNotNull null
            val title = doc.optString("title")
            val key = doc.optString("key").let { if (it.startsWith("/works/")) it else "/works/$it" }
            if (title.isBlank() || !key.matches(Regex("/works/OL[0-9]+W"))) return@mapNotNull null
            val coverId = doc.optLong("cover_i")
            // Search aggregates describe a work, not one edition. ISBN/year/publisher must come from an edition record.
            Found(title = title, author = doc.optJSONArray("author_name")?.optString(0).orEmpty(),
                isbn = "", coverUrl = if (coverId > 0) "https://covers.openlibrary.org/b/id/$coverId-L.jpg" else "",
                // Open-Library-Subjects sind englische Schlagwörter („Storytelling, fiction“) – nicht als Genre übernehmen.
                publisher = "", published = "", pages = 0, genre = "",
                language = "", description = "", source = "Open Library",
                sourceUrl = "https://openlibrary.org$key", workKey = key)
        }
    }

    private fun encode(value: String) = URLEncoder.encode(value, "UTF-8")

    private fun get(url: String): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 7000
        connection.readTimeout = 10000
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", LibraryRepository.USER_AGENT)
        try {
            if (connection.responseCode != 200) throw IOException("HTTP ${connection.responseCode}")
            return connection.inputStream.use { String(it.readBytes(), StandardCharsets.UTF_8) }
        } finally {
            connection.disconnect()
        }
    }
}
