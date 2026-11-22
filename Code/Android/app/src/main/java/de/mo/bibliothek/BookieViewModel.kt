package de.mo.bibliothek

import android.app.Application
import android.content.Context
import android.net.Uri
import android.util.AtomicFile
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import de.mo.bibliothek.data.Book
import de.mo.bibliothek.data.BookLookup
import de.mo.bibliothek.data.BookKnowledge
import de.mo.bibliothek.data.KnowledgeLookup
import de.mo.bibliothek.data.Found
import de.mo.bibliothek.data.GeminiException
import de.mo.bibliothek.data.LibraryRepository
import de.mo.bibliothek.data.ReadStatus
import de.mo.bibliothek.data.sameAs
import de.mo.bibliothek.data.gaps
import de.mo.bibliothek.data.searchKey
import de.mo.bibliothek.data.WarraqAi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.UUID

enum class ViewMode { SHELF, GRID, LIST }
enum class SortMode { ADDED, TITLE, AUTHOR, STATUS }

data class Settings(
    val language: String = "system",
    val yearGoal: Int = 12,
    val viewMode: ViewMode = ViewMode.SHELF,
    val sort: SortMode = SortMode.ADDED,
)

class BookieViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LibraryRepository(application)
    private val prefs = application.getSharedPreferences("bookie", Context.MODE_PRIVATE)
    private val writes = Mutex()
    private val knowledgeCache = java.util.concurrent.ConcurrentHashMap<String, BookKnowledge>()
    private data class ReadingKey(val id: String, val title: String, val author: String, val text: String,
        val quotes: List<de.mo.bibliothek.data.BookQuote>, val arabic: Boolean)
    private val readingCache = java.util.concurrent.ConcurrentHashMap<ReadingKey, de.mo.bibliothek.data.AiBookNotes>()

    private val _books = MutableStateFlow<List<Book>>(emptyList())
    val books: StateFlow<List<Book>> = _books.asStateFlow()

    private val _loaded = MutableStateFlow(false)
    val loaded: StateFlow<Boolean> = _loaded.asStateFlow()

    private val _settings = MutableStateFlow(readSettings())
    val settings: StateFlow<Settings> = _settings.asStateFlow()

    /** Kurze Meldung für die Snackbar; [consumeMessage] leert sie. */
    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    init {
        viewModelScope.launch {
            val loaded = withContext(Dispatchers.IO) {
                runCatching {
                    de.mo.bibliothek.data.ArabicCatalog.load(application)
                    val current = repository.load()
                    val adopted = repository.adoptLegacyCovers(current)
                    if (adopted != null) repository.save(adopted)
                    adopted ?: current
                }
            }
            loaded.onSuccess { _books.value = it }
                .onFailure { _message.value = "Die Bibliothek konnte nicht gelesen werden. Es wurde nichts verändert." }
            _loaded.value = loaded.isSuccess
            if (loaded.isSuccess) checkAuditDue()
        }
        // Reste des entfernten Zitat-Widgets (3.5.0) einmalig löschen; Bibliothek und Cover bleiben unberührt.
        runCatching {
            java.io.File(application.filesDir, "widget-quotes.json").delete()
            application.deleteSharedPreferences("widget")
        }
    }

    // ---- Allam räumt auf: lokale Prüfung, ändert nichts ----
    private val week = 7L * 24 * 60 * 60 * 1000
    private val _auditCount = MutableStateFlow(0)
    /** Anzahl Hinweise des fälligen Wochenberichts; 0 = keine Karte zeigen. */
    val auditCount: StateFlow<Int> = _auditCount.asStateFlow()

    private fun coverExists(name: String) = repository.coverFile(name).isFile

    private fun checkAuditDue() {
        if (System.currentTimeMillis() - prefs.getLong("auditSeenAt", 0L) < week) return
        _auditCount.value = auditReport().size
    }

    fun auditReport(): List<de.mo.bibliothek.data.AuditFinding> = de.mo.bibliothek.data.auditLibrary(_books.value, ::coverExists)

    /** Bericht geöffnet oder weggeklickt: nächste Karte in einer Woche. */
    fun markAuditSeen() {
        prefs.edit().putLong("auditSeenAt", System.currentTimeMillis()).apply()
        _auditCount.value = 0
    }

    /** Lücken eines neuen Buches (bis 14 Tage nach dem Hinzufügen), sofern der Hinweis nicht ausgeblendet wurde. */
    fun newBookGaps(book: Book): List<de.mo.bibliothek.data.Gap> {
        val fresh = book.addedAt > 0 && System.currentTimeMillis() - book.addedAt < 2 * week
        if (!fresh || book.id in (prefs.getStringSet("gapsHidden", emptySet()) ?: emptySet())) return emptyList()
        return book.gaps(::coverExists)
    }

    fun hideGaps(id: String) {
        val hidden = (prefs.getStringSet("gapsHidden", emptySet()) ?: emptySet()) + id
        prefs.edit().putStringSet("gapsHidden", hidden).apply()
    }

    // Schlüssel nur im nicht gesicherten App-Bereich: nie in library.json, ZIP-Export oder Android-Backup.
    private val aiKeyFile = AtomicFile(java.io.File(application.noBackupFilesDir, "gemini.key"))
    private val _aiEnabled = MutableStateFlow(false)
    val aiEnabled: StateFlow<Boolean> = _aiEnabled.asStateFlow()
    init {
        WarraqAi.geminiKeyFile = aiKeyFile
        _aiEnabled.value = WarraqAi.enabled
    }

    fun setAiKey(key: String) {
        val clean = key.trim()
        val saved = runCatching {
            synchronized(aiKeyFile) {
                if (clean.isEmpty()) {
                    aiKeyFile.delete()
                    check(!aiKeyFile.baseFile.exists())
                } else {
                    val output = aiKeyFile.startWrite()
                    try {
                        output.write(clean.toByteArray(Charsets.UTF_8))
                        aiKeyFile.finishWrite(output)
                    } catch (e: java.io.IOException) {
                        aiKeyFile.failWrite(output)
                        throw e
                    }
                }
            }
        }
        if (saved.isFailure) {
            _message.value = "Gemini-Schlüssel konnte nicht gespeichert oder gelöscht werden."
            return
        }
        readingCache.clear()
        _aiEnabled.value = WarraqAi.enabled
        _message.value = if (clean.isEmpty()) "KI ausgeschaltet." else "Gemini-Schlüssel gespeichert."
    }

    private suspend fun <T> ai(block: () -> T): Result<T> = withContext(Dispatchers.IO) { runCatching(block) }
    suspend fun aiOracle(wish: String, arabic: Boolean) = ai { WarraqAi.oracle(_books.value, wish, arabic) }
    suspend fun aiTips(wish: String, arabic: Boolean) = ai { WarraqAi.tips(_books.value, wish, arabic) }
    suspend fun aiAsk(question: String, arabic: Boolean) = ai { WarraqAi.askLibrary(_books.value, question, arabic) }
    suspend fun aiGenres() = ai { WarraqAi.genres(_books.value) }
    suspend fun aiYear(year: Int, arabic: Boolean) = ai { WarraqAi.yearReview(_books.value, year, arabic) }
    suspend fun aiAskBook(book: Book, question: String, arabic: Boolean) = ai { WarraqAi.askBook(book, question, arabic, _books.value) }
    suspend fun aiReadingNotes(book: Book, description: String, quotes: List<de.mo.bibliothek.data.BookQuote>, arabic: Boolean) = ai {
        check(WarraqAi.enabled) { "Kein Gemini-Schlüssel." }
        val key = ReadingKey(book.id, book.title, book.author, description, quotes, arabic)
        readingCache[key] ?: WarraqAi.bookNotes(book, description, quotes, arabic).also { readingCache[key] = it }
    }

    /** Übernimmt bestätigte Genre-Vorschläge nur in noch leere Genrefelder. */
    fun applyGenres(choices: Map<String, String>) =
        commit("Genres nicht gespeichert.") { list -> list.map { b -> choices[b.id]?.takeIf { b.genre.isBlank() }?.let { b.copy(genre = it) } ?: b } }

    /** Ohne Katalogtreffer: KI bereinigt die Eingabe und liefert Original-/Übersetzungstitel als weitere Suchen. */
    private fun withAiVariants(query: String, direct: List<Found>): List<Found> {
        if (direct.isNotEmpty() || !WarraqAi.enabled) return direct
        val variants = runCatching { WarraqAi.searchVariants(query) }.getOrElse {
            if (it is GeminiException) throw it
            emptyList()
        }
            .filter { searchKey(it) != searchKey(query) }
        return variants.flatMap { v -> runCatching { BookLookup.search(v) }.getOrDefault(emptyList()).take(10) }
            .distinctBy { it.isbn.ifEmpty { it.sourceUrl + it.title } }
    }

    fun consumeMessage() { _message.value = null }
    fun say(text: String) { _message.value = text }

    /** Jede Änderung wird erst übernommen, wenn sie gespeichert ist. */
    private fun commit(failure: String, onSaved: () -> Unit = {}, change: (List<Book>) -> List<Book>) {
        if (!_loaded.value) return
        viewModelScope.launch {
            writes.withLock {
                val next = change(_books.value)
                val saved = withContext(Dispatchers.IO) { runCatching { repository.save(next) } }
                if (saved.isFailure) {
                    _message.value = failure
                    return@launch
                }
                _books.value = next
            }
            onSaved()
        }
    }

    fun book(id: String): Book? = _books.value.firstOrNull { it.id == id }

    fun setStatus(id: String, status: ReadStatus) =
        commit("Status nicht gespeichert.") { list -> list.map { if (it.id == id) it.withStatus(status) else it } }

    fun setReadAt(id: String, millis: Long) =
        commit("Datum nicht gespeichert.") { list -> list.map { if (it.id == id) it.copy(readAt = millis) else it } }

    fun setRating(id: String, rating: Int) =
        commit("Bewertung nicht gespeichert.") { list -> list.map { if (it.id == id) it.copy(rating = rating.coerceIn(0, 5)) else it } }

    fun toggleFavorite(id: String) =
        commit("Nicht gespeichert.") { list -> list.map { if (it.id == id) it.copy(favorite = !it.favorite) else it } }

    fun setNotes(id: String, notes: String) =
        commit("Notiz nicht gespeichert.") { list -> list.map { if (it.id == id) it.copy(notes = notes.take(10000)) else it } }

    fun setLists(id: String, lists: List<String>) =
        commit("Listen nicht gespeichert.") { list -> list.map { if (it.id == id) it.copy(lists = lists) else it } }

    fun upsert(book: Book, onSaved: () -> Unit) {
        commit("Buch nicht gespeichert.", onSaved = {
            cleanupCovers()
            onSaved()
        }) { list ->
            val clean = book.copy(title = book.title.trim(), author = book.author.trim())
            if (list.any { it.id == clean.id }) list.map { if (it.id == clean.id) clean else it } else list + clean
        }
    }

    fun delete(id: String) {
        commit("Buch nicht gelöscht.") { list -> list.filterNot { it.id == id } }
        cleanupCovers()
    }

    /** Läuft nach der jeweiligen Speicherung (gleiche Sperre), entfernt nur nicht mehr referenzierte Coverdateien. */
    private fun cleanupCovers() = viewModelScope.launch {
        writes.withLock { withContext(Dispatchers.IO) { runCatching { repository.deleteUnusedCovers(_books.value) } } }
    }

    fun contains(found: Found): Book? {
        val probe = found.toBook("", 0)
        return _books.value.firstOrNull { it.sameAs(probe) }
    }

    /** Fügt einen Treffer hinzu und legt das Cover sofort lokal ab (offline sichtbar, in der Sicherung enthalten). */
    fun addFound(found: Found, status: ReadStatus, onDone: (String) -> Unit = {}) {
        if (!_loaded.value) return
        viewModelScope.launch {
            val id = writes.withLock {
                contains(found)?.let { onDone(it.id); return@launch }
                val book = found.toBook(UUID.randomUUID().toString(), System.currentTimeMillis()).withStatus(status)
                val next = _books.value + book
                val saved = withContext(Dispatchers.IO) { runCatching { repository.save(next) } }
                if (saved.isFailure) {
                    _message.value = "Buch nicht gespeichert."
                    return@launch
                }
                _books.value = next
                book.id
            }
            onDone(id)
            val name = withContext(Dispatchers.IO) {
                listOf(found.coverUrl, found.fallbackCover).filter { it.isNotEmpty() }.firstNotNullOfOrNull { url ->
                    runCatching { repository.downloadCover(url) }.getOrNull()
                }
            }
            if (name != null) commit("Cover nicht gespeichert.") { list ->
                list.map { if (it.id == id && it.cover.isEmpty()) it.copy(cover = name) else it }
            }
        }
    }

    /** Ergänzt nur leere Felder eines vorhandenen Buchs mit einem ausdrücklich gewählten Treffer. */
    fun enrich(id: String, found: Found) {
        commit("Nicht gespeichert.") { list ->
            list.map { b ->
                if (b.id != id) b else b.copy(
                    author = b.author.ifEmpty { found.author },
                    isbn = b.isbn.ifEmpty { found.isbn },
                    publisher = b.publisher.ifEmpty { found.publisher },
                    published = b.published.ifEmpty { found.published },
                    pages = if (b.pages > 0) b.pages else found.pages,
                    genre = b.genre.ifEmpty { found.genre },
                    language = b.language.ifEmpty { found.language },
                    description = b.description.ifEmpty { found.description },
                    coverUrl = if (b.cover.isEmpty() && found.coverUrl.isNotEmpty()) found.coverUrl else b.coverUrl,
                )
            }
        }
        val current = book(id)
        if (found.coverUrl.isNotEmpty() && (current == null || current.cover.isEmpty())) viewModelScope.launch {
            val name = withContext(Dispatchers.IO) { runCatching { repository.downloadCover(found.coverUrl) }.getOrNull() }
            if (name != null) commit("Cover nicht gespeichert.") { list ->
                list.map { if (it.id == id && it.cover.isEmpty()) it.copy(cover = name) else it }
            }
        }
    }

    fun applyMatch(id: String, found: Found, fields: Set<String>) = viewModelScope.launch {
        writes.withLock {
            val result = withContext(Dispatchers.IO) { runCatching {
                val b = _books.value.firstOrNull { it.id == id } ?: return@runCatching _books.value
                val cover = if ("cover" in fields) repository.downloadCover(found.coverUrl) else b.cover
                val next = b.copy(
                    title = if ("title" in fields) found.title.take(300) else b.title,
                    author = if ("author" in fields) found.author.take(300) else b.author,
                    isbn = if ("isbn" in fields) found.isbn else b.isbn,
                    publisher = if ("publisher" in fields) found.publisher else b.publisher,
                    published = if ("published" in fields) found.published else b.published,
                    language = if ("language" in fields) found.language else b.language,
                    genre = if ("genre" in fields) found.genre else b.genre,
                    pages = if ("pages" in fields) found.pages.coerceAtLeast(0) else b.pages,
                    description = if ("description" in fields) found.description.take(8000) else b.description,
                    cover = cover, coverUrl = if ("cover" in fields) found.coverUrl else b.coverUrl)
                val list = _books.value.map { if (it.id == id) next else it }; repository.save(list); list
            } }
            result.onSuccess { _books.value = it; _message.value = "Ausgewählte Angaben übernommen." }
                .onFailure { _message.value = "Angaben konnten nicht übernommen werden. Die Bibliothek bleibt erhalten." }
        }
    }

    /** Das Foto dient nur der Erkennung: Kopie und eigene Kameraaufnahme werden danach gelöscht. */
    suspend fun recognizeCover(uri: Uri, frame: de.mo.bibliothek.data.PreviewFrame? = null): Result<de.mo.bibliothek.data.CoverText> = withContext(Dispatchers.IO) {
        runCatching {
            val name = repository.importCover(uri)
            try {
                // One copy stays only while the search is open, for an optional Google Lens lookup the user starts.
                repository.coverFile(name).copyTo(lensPhoto(), overwrite = true)
                de.mo.bibliothek.data.CoverRecognition(getApplication()).recognize(repository.coverFile(name), frame)
            }
            finally {
                repository.coverFile(name).delete()
                if (uri.scheme == "file") uri.path?.let { java.io.File(it) }?.takeIf { it.parentFile?.name == "recognition" }?.delete()
            }
        }.onFailure { android.util.Log.w("WarraqOcr", "Erkennung fehlgeschlagen", it) }
    }
    fun lensPhoto() = java.io.File(getApplication<Application>().cacheDir, "lens").apply { mkdirs() }.resolve("cover.jpg")
    fun discardLensPhoto() { lensPhoto().delete() }

    suspend fun editions(work: Found, hashes: List<Long> = emptyList(), offset: Int = 0): Result<de.mo.bibliothek.data.EditionPage> = withContext(Dispatchers.IO) { runCatching {
        val page = BookLookup.editions(work, offset)
        val editions = page.books
        if (hashes.isEmpty()) return@runCatching page
        val compared = coroutineScope { editions.take(8).map { found -> async {
            if (found.coverUrl.isBlank()) return@async found
            val name = runCatching { repository.downloadCover(found.coverUrl) }.getOrNull() ?: return@async found
            try { found.copy(coverDistance = de.mo.bibliothek.data.CoverFingerprint.distance(hashes, repository.coverFile(name))) }
            finally { repository.coverFile(name).delete() }
        } }.awaitAll() }
        page.copy(books = compared.sortedBy { it.coverDistance ?: Int.MAX_VALUE } + editions.drop(8))
    } }

    suspend fun search(query: String): Result<List<Found>> =
        withContext(Dispatchers.IO) { runCatching { withAiVariants(query, BookLookup.search(query)) } }
    suspend fun searchBook(query: String, title: String): Result<List<Found>> =
        withContext(Dispatchers.IO) { runCatching { withAiVariants(query, BookLookup.search(query, title)) } }
    suspend fun lookupIsbn(isbn: String): Result<Found?> = withContext(Dispatchers.IO) { runCatching { BookLookup.byIsbn(isbn) } }
    suspend fun fetchKnowledge(book: Book): Result<BookKnowledge> = withContext(Dispatchers.IO) {
        val key = "${book.id}|${book.title}|${book.author}|${book.isbn}"
        knowledgeCache[key]?.let { return@withContext Result.success(it) }
        runCatching { KnowledgeLookup.fetch(book) }.onSuccess { knowledgeCache[key] = it }
    }
    suspend fun importCover(uri: Uri): String? = withContext(Dispatchers.IO) { runCatching { repository.importCover(uri) }.getOrNull() }
    suspend fun downloadCover(url: String): String? = withContext(Dispatchers.IO) { runCatching { repository.downloadCover(url) }.getOrNull() }
    fun coverFile(name: String) = repository.coverFile(name)

    fun exportBackup(uri: Uri) = viewModelScope.launch {
        val result = writes.withLock { withContext(Dispatchers.IO) { runCatching { repository.exportBackup(uri, _books.value) } } }
        _message.value = if (result.isSuccess) "Sicherung gespeichert." else "Sicherung fehlgeschlagen."
    }

    fun hasRecovery() = repository.hasRecovery()

    fun exportRecovery(uri: Uri) = viewModelScope.launch {
        val result = writes.withLock { withContext(Dispatchers.IO) { runCatching { repository.exportRecovery(uri) } } }
        _message.value = if (result.isSuccess) "Vorheriger Stand exportiert." else "Vorheriger Stand konnte nicht exportiert werden."
    }

    fun importBackup(uri: Uri) = viewModelScope.launch {
        val result = writes.withLock { withContext(Dispatchers.IO) { runCatching { repository.importBackup(uri) } } }
        result.onSuccess { _books.value = it; _loaded.value = true; _message.value = "${it.size} Bücher wiederhergestellt." }
            .onFailure { _message.value = it.message ?: "Wiederherstellung fehlgeschlagen. Die Bibliothek bleibt unverändert." }
    }

    fun updateSettings(change: (Settings) -> Settings) {
        _settings.update(change)
        val s = _settings.value
        prefs.edit().putString("language", s.language).putInt("yearGoal", s.yearGoal)
            .putString("viewMode", s.viewMode.name).putString("sort", s.sort.name).apply()
    }

    private fun readSettings() = Settings(
        language = prefs.getString("language", "system") ?: "system",
        yearGoal = prefs.getInt("yearGoal", 12),
        viewMode = runCatching { ViewMode.valueOf(prefs.getString("viewMode", "SHELF")!!) }.getOrDefault(ViewMode.SHELF),
        sort = runCatching { SortMode.valueOf(prefs.getString("sort", "ADDED")!!) }.getOrDefault(SortMode.ADDED),
    )
}
