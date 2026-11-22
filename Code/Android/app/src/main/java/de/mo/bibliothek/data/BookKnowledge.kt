package de.mo.bibliothek.data

import android.text.Html
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

data class WebSource(val title: String, val url: String)
/** [literal] = wörtliches Zitat aus der Werkseite; sonst eine aus der Beschreibung entnommene Kernaussage. */
data class BookQuote(val text: String, val source: WebSource, val literal: Boolean = true)
data class BookKnowledge(val summary: String = "", val source: WebSource? = null, val quotes: List<BookQuote> = emptyList())

private val SENTENCE_END = Regex("(?<=[.!?؟…])\\s+")
private val SPOILER = Regex("(?i)\\b(ending|ends with|at the end|in the end|eventually|dies|is killed|am ende|zum schluss|stirbt|getötet|tod von)\\b|في النهاية|في نهاية|نهاية الرواية|يموت|تموت|مقتل|ينتحر|تنتحر")
private val THESIS = Regex("(?i)\\b(this book|the book|explores|examines|argues|deals with|themes?|dieses buch|das buch|behandelt|thematisiert|untersucht|handelt von)\\b|هذا الكتاب|يتناول|يناقش|يطرح|يستعرض|يكشف|يرصد")

fun sentences(text: String): List<String> = text.replace(Regex("\\s+"), " ").trim().split(SENTENCE_END).filter { it.isNotBlank() }

/** Kurzer Klappentext: die ersten Sätze bis ca. 320 Zeichen, Sätze mit Handlungsende-Hinweisen fallen weg. */
fun shortBlurb(text: String, limit: Int = 320): String {
    val parts = sentences(text).filterNot { SPOILER.containsMatchIn(it) }
    val result = StringBuilder()
    for(sentence in parts) {
        if(result.isEmpty() && sentence.length > limit) {
            val cut = sentence.take(limit); val at = cut.indexOfLast { it == '،' || it == ',' || it == '؛' || it == ';' }
            return (if(at > limit / 2) cut.substring(0, at) else cut.substringBeforeLast(' ')).trim() + " …"
        }
        if(result.length + sentence.length > limit) break
        if(result.isNotEmpty()) result.append(' ')
        result.append(sentence)
    }
    return result.toString()
}

/** Ein Satz der Beschreibung, der das Thema benennt und nicht schon im Kurztext steht. Kein Autorzitat. */
fun keyIdea(text: String): String? {
    val shown = shortBlurb(text)
    val candidates = sentences(text).filter { it.length in 40..300 && !shown.contains(it) && !SPOILER.containsMatchIn(it) && THESIS.containsMatchIn(it) }
    // A sentence naming the book itself states the thesis most directly.
    return candidates.firstOrNull { THIS_BOOK.containsMatchIn(it) } ?: candidates.firstOrNull()
}

private val THIS_BOOK = Regex("(?i)\\b(this book|dieses buch)\\b|هذا الكتاب")

private fun hindawiId(book: Book): String =
    runCatching { JSONObject(book.extra).optString("workKey") }.getOrDefault("").removePrefix("hindawi:").takeIf { it.matches(Regex("[0-9]+")) }
        ?: Regex("/270x360/([0-9]+)\\.svg").find(book.coverUrl)?.groupValues?.get(1).orEmpty()

/** No generated quotations: excerpts are only read from the linked work's Wikiquote page. */
object KnowledgeLookup {
    private fun encode(value: String) = URLEncoder.encode(value, "UTF-8")

    /** Lokale Hindawi-Beschreibung hat Vorrang (verlagseigener Klappentext, auch offline); Zitate kommen nur online. */
    fun fetch(book: Book): BookKnowledge {
        val local = ArabicCatalog.blurb(book.title, book.author, hindawiId(book))?.let { BookKnowledge(it.text, WebSource("Hindawi", it.url)) }
        val online = runCatching { fetchOnline(book) }
        val found = online.getOrNull() ?: return local ?: throw online.exceptionOrNull()!!
        return if(local != null) local.copy(quotes = found.quotes) else found
    }

    private fun fetchOnline(book: Book): BookKnowledge {
        var connected = false
        val languages = if(book.isArabic) listOf("ar", "de", "en") else listOf("de", "en", "ar")
        for(lang in languages) {
            val query = listOf(mainTitle(book.title), book.author).filter { it.isNotBlank() }.joinToString(" ")
            val search = runCatching { json("https://$lang.wikipedia.org/w/api.php?action=query&list=search&srlimit=3&format=json&srsearch=${encode(query)}") }
            if(search.isSuccess) connected = true
            val found = search.getOrNull()?.optJSONObject("query")?.optJSONArray("search") ?: continue
            // Exact title first; otherwise a title whose word stems all match (e.g. „Herz eines Hundes“ → „Hundeherz“).
            val hits = (0 until found.length()).map { found.getJSONObject(it) }
                .filter { titlesMatch(book.title, it.optString("title")) || titlesRelated(book.title, it.optString("title")) }
                .sortedBy { if(titlesMatch(book.title, it.optString("title"))) 0 else 1 }
            for(hit in hits) {
                val page = runCatching {
                    val response = json("https://$lang.wikipedia.org/w/api.php?action=query&prop=extracts%7Cinfo%7Cpageprops&exintro=1&explaintext=1&inprop=url&format=json&pageids=${hit.optLong("pageid")}")
                    val pages = response.getJSONObject("query").getJSONObject("pages")
                    pages.getJSONObject(pages.keys().next())
                }.getOrNull() ?: continue
                val extract = page.optString("extract").trim()
                if(page.optJSONObject("pageprops")?.has("disambiguation") == true) continue
                val item = page.optJSONObject("pageprops")?.optString("wikibase_item").orEmpty()
                // Author written in another script than the article (e.g. „García Márquez“ in an Arabic article): ask Wikidata.
                if(!isBookArticle(book, extract) && !(BOOK_WORD.containsMatchIn(extract) && authorNamesMatch(wikidataAuthors(item), book.author))) continue
                val source = WebSource("Wikipedia · ${lang.uppercase(Locale.ROOT)}", page.optString("fullurl"))
                val quotes = linkedQuotes(item, languages)
                return BookKnowledge(extract.take(16000), source, quotes)
            }
        }
        // The ISBN resolves an edition to its exact work, without guessing from a title.
        if(isbnKey(book.isbn).length in listOf(10,13)) {
            val result = runCatching {
                val edition = json("https://openlibrary.org/isbn/${isbnKey(book.isbn)}.json")
                connected = true
                val key = edition.optJSONArray("works")?.optJSONObject(0)?.optString("key").orEmpty()
                if(!key.matches(Regex("/works/OL[0-9]+W"))) return@runCatching null
                val work = json("https://openlibrary.org$key.json")
                val description = when(val raw = work.opt("description")) {
                    is String -> raw
                    is JSONObject -> raw.optString("value")
                    else -> ""
                }
                description.takeIf { it.isNotBlank() }?.let { BookKnowledge(it.take(16000), WebSource("Open Library", "https://openlibrary.org$key")) }
            }.getOrNull()
            if(result != null) return result
        }
        if(!connected) throw IOException("Keine Verbindung zu den Buchquellen.")
        return BookKnowledge()
    }

    private fun linkedQuotes(item: String, languages: List<String>): List<BookQuote> {
        if(!item.matches(Regex("Q[0-9]+"))) return emptyList()
        return runCatching {
            val sites = json("https://www.wikidata.org/w/api.php?action=wbgetentities&ids=$item&props=sitelinks&format=json")
                .getJSONObject("entities").getJSONObject(item).getJSONObject("sitelinks")
            for(lang in languages) {
                val title = sites.optJSONObject("${lang}wikiquote")?.optString("title") ?: continue
                val url = "https://$lang.wikiquote.org/wiki/" + encode(title.replace(' ', '_')).replace("+", "%20")
                val html = json("https://$lang.wikiquote.org/w/api.php?action=parse&prop=text&format=json&page=${encode(title)}")
                    .getJSONObject("parse").getJSONObject("text").getString("*")
                val quotes = parseQuotes(html, WebSource("Wikiquote · ${lang.uppercase(Locale.ROOT)}", url))
                if(quotes.isNotEmpty()) return@runCatching quotes
            }
            emptyList()
        }.getOrDefault(emptyList())
    }

    /** Main title without subtitle („: …“, „ -- …“) and bracket notes. */
    internal fun mainTitle(value: String) = value.split(Regex(":|\\s[-–—]{1,2}\\s")).first().replace(Regex("\\([^)]*\\)"), "").trim()

    private fun titleKey(value: String) = searchKey(mainTitle(value)).replace(Regex("^(the|a|an|der|die|das|ein|eine) "), "")

    internal fun titlesMatch(bookTitle: String, articleTitle: String): Boolean {
        val a = titleKey(bookTitle); val b = titleKey(articleTitle)
        return a.length >= 3 && a == b
    }

    private val STOP = setOf("eines", "einer", "einem", "einen", "the", "and", "und", "with", "from", "von", "der", "die", "das", "des", "dem", "den", "في", "من", "على")

    /** Every significant word stem (first 4 letters) of the book's main title occurs in the article title. */
    internal fun titlesRelated(bookTitle: String, articleTitle: String): Boolean {
        val stems = titleKey(bookTitle).split(' ').filter { it.length >= 4 && it !in STOP }.map { it.take(4) }
        val article = titleKey(articleTitle).replace(" ", "")
        return stems.isNotEmpty() && stems.all { article.contains(it) }
    }

    private val BOOK_WORD = Regex("(?i)\\b(novel|novella|book|roman|erzählung|sachbuch)\\b|رواية|كتاب")

    internal fun isBookArticle(book: Book, extract: String): Boolean {
        if(!BOOK_WORD.containsMatchIn(extract)) return false
        val surname = searchKey(book.author).split(' ').lastOrNull { it.length >= 3 }
        return surname == null || searchKey(extract).contains(surname)
    }

    /** True if the book's author surname is one of the words in any Wikidata label/alias of the work's authors. */
    internal fun authorNamesMatch(names: List<String>, author: String): Boolean {
        val surname = searchKey(author).split(' ').lastOrNull { it.length >= 3 } ?: return false
        return names.any { surname in searchKey(it).split(' ') }
    }

    /** Labels and aliases (all languages) of the work's authors (P50). */
    private fun wikidataAuthors(item: String): List<String> {
        if(!item.matches(Regex("Q[0-9]+"))) return emptyList()
        return runCatching {
            val claims = json("https://www.wikidata.org/w/api.php?action=wbgetentities&ids=$item&props=claims&format=json")
                .getJSONObject("entities").getJSONObject(item).optJSONObject("claims")?.optJSONArray("P50") ?: return@runCatching emptyList()
            val ids = (0 until claims.length()).mapNotNull {
                claims.optJSONObject(it)?.optJSONObject("mainsnak")?.optJSONObject("datavalue")?.optJSONObject("value")?.optString("id")?.takeIf { id -> id.matches(Regex("Q[0-9]+")) }
            }.take(5)
            if(ids.isEmpty()) return@runCatching emptyList()
            val entities = json("https://www.wikidata.org/w/api.php?action=wbgetentities&ids=${ids.joinToString("%7C")}&props=labels%7Caliases&format=json").getJSONObject("entities")
            val names = mutableListOf<String>()
            for(id in entities.keys()) {
                val entity = entities.getJSONObject(id)
                entity.optJSONObject("labels")?.let { labels -> for(lang in labels.keys()) names += labels.getJSONObject(lang).optString("value") }
                entity.optJSONObject("aliases")?.let { aliases -> for(lang in aliases.keys()) { val list = aliases.getJSONArray(lang); for(i in 0 until list.length()) names += list.getJSONObject(i).optString("value") } }
            }
            names
        }.getOrDefault(emptyList())
    }

    /** Quellenangaben (Ausgabe, Übersetzer, Ort/Jahr, Seite) sind keine Zitate. */
    private val CITATION = Regex("(?i)(übertragen von|übersetzt von|übers\\.|hrsg\\.|herausgegeben von|translated by|edited by|\\b(S\\.|Seite|p\\.|pp\\.|Bd\\.|Band|Kapitel|chapter)\\s*[0-9IVX]|\\b(Berlin|München|Leipzig|Frankfurt|Wien|Zürich|Stuttgart|Hamburg|London|New York|Paris)[ ,]+[0-9]{4}|ترجمة|ص\\.\\s*[0-9])")

    internal fun isCitation(text: String): Boolean = CITATION.containsMatchIn(text)

    internal fun parseQuotes(html: String, source: WebSource): List<BookQuote> {
        // Stop before discussion, external links, or quotations ABOUT the work.
        val clean = html.replace(Regex("(?is)<table\\b.*?</table>"), "")
        val stop = Regex("(?is)<h[23][^>]*>(?:(?!</h[23]>).)*?(?:About|External links|See also|Über das Werk|Weblinks|وصلات خارجية)(?:(?!</h[23]>).)*?</h[23]>").find(clean)
        val body = if(stop == null) clean else clean.substring(0, stop.range.first)
        return Regex("(?is)<li(?:\\s[^>]*)?>(.*?)</li>").findAll(body).mapNotNull { hit ->
            val raw = hit.groupValues[1]
            if(raw.contains("mw-editsection", true) || raw.contains("tocnumber", true) || raw.contains("toctext", true)) return@mapNotNull null
            val textPart = raw.substringBefore("<ul").substringBefore("<dl")
                .replace(Regex("(?is)<sup.*?</sup>"), "")
            val text = Html.fromHtml(textPart, Html.FROM_HTML_MODE_LEGACY).toString().trim().replace(Regex("\\s+"), " ")
            if(text.length !in 35..700 || text.startsWith("ISBN") || text.contains("http") || isCitation(text)) null else BookQuote(text, source)
        }.distinctBy { it.text }.take(3).toList()
    }

    private fun json(url: String): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connectTimeout = 4000; connection.readTimeout = 5000
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", LibraryRepository.USER_AGENT)
        try {
            if(connection.responseCode != 200) throw IOException("HTTP ${connection.responseCode}")
            return JSONObject(connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                val result = StringBuilder(); val buffer = CharArray(8192)
                while(true) { val n=reader.read(buffer); if(n<0) break; result.append(buffer,0,n); if(result.length>2_000_000)throw IOException("Quelle zu groß.") }
                result.toString()
            })
        } finally { connection.disconnect() }
    }
}
