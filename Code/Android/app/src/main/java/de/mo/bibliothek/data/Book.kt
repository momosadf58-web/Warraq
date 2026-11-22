package de.mo.bibliothek.data

import org.json.JSONArray
import org.json.JSONObject
import java.text.Normalizer
import java.util.Locale

enum class ReadStatus(val stored: String) {
    UNREAD("Noch nicht gelesen"),
    READING("Lese ich gerade"),
    READ("Gelesen");

    companion object {
        /** Liest auch historische Werte aus 1.x/2.x und fremden Importen. */
        fun of(raw: String): ReadStatus = when (raw.trim().lowercase(Locale.ROOT)) {
            "gelesen", "read", "completed", "finished" -> READ
            "lese ich gerade", "lese gerade", "reading", "currently-reading", "currently reading" -> READING
            else -> UNREAD
        }
    }
}

/**
 * Ein Buch im Format von library.json (2.x-kompatibel).
 * Unbekannte Felder bleiben in [extra] erhalten und werden beim Speichern zurückgeschrieben.
 */
data class Book(
    val id: String,
    val title: String,
    val author: String = "",
    val isbn: String = "",
    val language: String = "",
    val status: String = ReadStatus.UNREAD.stored,
    val cover: String = "",
    val coverUrl: String = "",
    val rating: Int = 0,
    val favorite: Boolean = false,
    val pages: Int = 0,
    val notes: String = "",
    val lists: List<String> = emptyList(),
    val publisher: String = "",
    val published: String = "",
    val genre: String = "",
    val description: String = "",
    val addedAt: Long = 0,
    val readAt: Long = 0,
    val extra: String = "{}",
) {
    val readStatus: ReadStatus get() = ReadStatus.of(status)

    fun withStatus(next: ReadStatus, now: Long = System.currentTimeMillis()): Book = copy(
        status = next.stored,
        readAt = when {
            next != ReadStatus.READ -> 0
            readAt > 0 -> readAt
            else -> now
        },
    )

    fun toJson(): JSONObject {
        val json = JSONObject(extra)
        json.put("id", id).put("title", title).put("author", author).put("isbn", isbn)
            .put("language", language).put("status", status).put("cover", cover).put("coverUrl", coverUrl)
            .put("rating", rating).put("favorite", favorite).put("pages", pages).put("notes", notes)
            // 2.x kannte nur ein Regal; das erste Element bleibt dort lesbar.
            .put("shelf", lists.firstOrNull() ?: "")
            .put("lists", JSONArray(lists))
            .put("publisher", publisher).put("published", published).put("genre", genre)
            .put("description", description).put("addedAt", addedAt).put("readAt", readAt)
        return json
    }

    companion object {
        private val KNOWN = setOf(
            "id", "title", "author", "isbn", "language", "status", "cover", "coverUrl", "rating", "favorite",
            "pages", "notes", "shelf", "lists", "publisher", "published", "genre", "description", "addedAt", "readAt",
        )

        fun fromJson(json: JSONObject): Book {
            val lists = buildList {
                json.optString("shelf").trim().takeIf { it.isNotEmpty() }?.let { add(it) }
                json.optJSONArray("lists")?.let { array ->
                    for (i in 0 until array.length()) {
                        val name = array.optString(i).trim()
                        if (name.isNotEmpty() && name !in this) add(name)
                    }
                }
            }
            val extra = JSONObject()
            json.keys().forEach { key -> if (key !in KNOWN) extra.put(key, json.get(key)) }
            return Book(
                id = json.optString("id"),
                title = json.optString("title"),
                author = json.optString("author"),
                isbn = json.optString("isbn"),
                language = json.optString("language"),
                status = json.optString("status").ifEmpty { ReadStatus.UNREAD.stored },
                cover = json.optString("cover"),
                coverUrl = json.optString("coverUrl"),
                rating = json.optInt("rating").coerceIn(0, 5),
                favorite = json.optBoolean("favorite"),
                pages = json.optInt("pages").coerceAtLeast(0),
                notes = json.optString("notes"),
                lists = lists,
                publisher = json.optString("publisher"),
                published = json.optString("published"),
                genre = json.optString("genre"),
                description = json.optString("description"),
                addedAt = json.optLong("addedAt"),
                readAt = json.optLong("readAt"),
                extra = extra.toString(),
            )
        }
    }
}

private val ARABIC_SCRIPT = Regex("[\\u0600-\\u06FF\\u0750-\\u077F\\u08A0-\\u08FF]")
private val LATIN = Regex("[A-Za-zÄÖÜäöüß]")

/** Arabisch nach Sprachangabe, sonst nach der Schrift des Titels. */
val Book.isArabic: Boolean
    get() = when (language.trim().lowercase(Locale.ROOT)) {
        "ar", "ara", "arabisch", "arabic", "العربية", "عربي" -> true
        "" -> ARABIC_SCRIPT.containsMatchIn(title)
        else -> false
    }

/** Deutsch nach Sprachangabe, sonst näherungsweise bei lateinischer Schrift im Titel. */
val Book.isGerman: Boolean
    get() = when (language.trim().lowercase(Locale.ROOT)) {
        "de", "deu", "ger", "deutsch", "german" -> true
        "" -> !isArabic && LATIN.containsMatchIn(title)
        else -> false
    }

/** Vergleichsschlüssel ohne Akzente, Satzzeichen und Groß-/Kleinschreibung. */
fun searchKey(value: String): String =
    Normalizer.normalize(value, Normalizer.Form.NFKD)
        .replace(Regex("\\p{Mn}+"), "")
        .lowercase(Locale.ROOT)
        .replace("ـ", "").replace('ى', 'ي').replace('ی', 'ي').replace('ک', 'ك')
        .replace(Regex("[^\\p{L}\\p{N}]+"), " ")
        .trim()

fun isbnKey(value: String): String = value.uppercase(Locale.ROOT).filter { it.isDigit() || it == 'X' }

fun Book.sameAs(other: Book): Boolean {
    if (isbn.isNotBlank() && other.isbn.isNotBlank()) {
        val key = isbnKey(isbn)
        return key.isNotEmpty() && key == isbnKey(other.isbn)
    }
    return searchKey(title) == searchKey(other.title) && searchKey(author) == searchKey(other.author)
}
