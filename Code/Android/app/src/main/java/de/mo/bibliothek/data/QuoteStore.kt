package de.mo.bibliothek.data

import android.content.Context
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

/** Belegtes Zitat für das Startbildschirm-Widget. Wortlaut und Quelle bleiben unverändert. */
data class StoredQuote(val bookId: String, val title: String, val author: String, val text: String, val source: String, val url: String)

/**
 * Privater Zitatvorrat für das Widget (nicht in library.json, nicht in der ZIP-Sicherung).
 * Nur wörtliche Wikiquote-Zitate auf Deutsch oder Arabisch; keine KI-Texte, keine Kernaussagen.
 */
class QuoteStore(context: Context) {
    private val file = AtomicFile(File(context.filesDir, "widget-quotes.json"))

    @Synchronized fun quotes(): List<StoredQuote> = read().quotes

    @Synchronized fun checkedAt(bookId: String): Long = read().checked[bookId] ?: 0L

    /** Ersetzt die Zitate eines Buches durch die neu geladenen; true, wenn sich der Zitatvorrat geändert hat. */
    @Synchronized fun record(book: Book, knowledge: BookKnowledge, now: Long = System.currentTimeMillis()): Boolean {
        val state = read()
        val fresh = widgetQuotes(book, knowledge)
        val old = state.quotes.filter { it.bookId == book.id }
        write(State(state.quotes.filterNot { it.bookId == book.id } + fresh, state.checked + (book.id to now)))
        return old != fresh
    }

    /** Entfernt Zitate gelöschter oder umbenannter Bücher. */
    @Synchronized fun prune(books: List<Book>): Boolean {
        val state = read()
        val valid = books.associateBy { it.id }
        val kept = state.quotes.filter { q -> valid[q.bookId]?.let { it.title == q.title && it.author == q.author } == true }
        val checked = state.checked.filterKeys { it in valid }
        if (kept.size == state.quotes.size && checked.size == state.checked.size) return false
        write(State(kept, checked))
        return true
    }

    private data class State(val quotes: List<StoredQuote>, val checked: Map<String, Long>)

    private fun read(): State = runCatching {
        val json = JSONObject(String(file.readFully(), Charsets.UTF_8))
        val quotes = json.optJSONArray("quotes") ?: JSONArray()
        val checked = json.optJSONObject("checked") ?: JSONObject()
        State(
            (0 until quotes.length()).mapNotNull { quotes.optJSONObject(it) }.map {
                StoredQuote(it.optString("bookId"), it.optString("title"), it.optString("author"), it.optString("text"), it.optString("source"), it.optString("url"))
            }.filter { it.bookId.isNotEmpty() && it.text.isNotEmpty() },
            checked.keys().asSequence().associateWith { checked.optLong(it) },
        )
    }.getOrDefault(State(emptyList(), emptyMap()))

    private fun write(state: State) {
        val json = JSONObject()
            .put("quotes", JSONArray().also { a -> state.quotes.forEach { q ->
                a.put(JSONObject().put("bookId", q.bookId).put("title", q.title).put("author", q.author).put("text", q.text).put("source", q.source).put("url", q.url))
            } })
            .put("checked", JSONObject().also { o -> state.checked.forEach { (k, v) -> o.put(k, v) } })
        val out = file.startWrite()
        try { out.write(json.toString().toByteArray(Charsets.UTF_8)); file.finishWrite(out) } catch (e: Exception) { file.failWrite(out); throw e }
    }
}

private val WIDGET_SOURCES = Regex("^Wikiquote · (DE|AR)$")

/** Nur wörtliche Wikiquote-Zitate auf Deutsch oder Arabisch (Mos Wunsch: kein Englisch, keine KI). */
internal fun widgetQuotes(book: Book, knowledge: BookKnowledge): List<StoredQuote> =
    knowledge.quotes.filter { it.literal && WIDGET_SOURCES.matches(it.source.title) && it.text.length in 20..400 }
        .map { StoredQuote(book.id, book.title, book.author, it.text, it.source.title, it.source.url) }

/** Zufälliges Zitat, möglichst nicht dasselbe wie zuletzt. */
internal fun pickQuote(quotes: List<StoredQuote>, last: String?, random: kotlin.random.Random = kotlin.random.Random): StoredQuote? {
    if (quotes.isEmpty()) return null
    val options = quotes.filter { it.text != last }.ifEmpty { quotes }
    return options[random.nextInt(options.size)]
}
