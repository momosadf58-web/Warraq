package de.mo.bibliothek.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mo.bibliothek.data.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.Calendar

class AssistantActions(
    val oracle: suspend (String, Boolean) -> Result<AiPick?>,
    val tips: suspend (String, Boolean) -> Result<List<AiTip>>,
    val ask: suspend (String, Boolean) -> Result<AiAnswer>,
    val genres: suspend () -> Result<List<AiGenre>>,
    val applyGenres: (Map<String, String>) -> Unit,
    val year: suspend (Int, Boolean) -> Result<String>,
    /** Öffnet ein eigenes Buch. */
    val open: (String) -> Unit,
    /** Sucht einen Tipp in den Katalogen, damit Cover/Ausgaben/Ein-Klick von dort kommen. */
    val search: (String) -> Unit,
    val close: () -> Unit,
)

private enum class Part { ORACLE, TIPS, ASK, GENRES, YEAR }

/** Alles Erzeugte ist als KI-Text markiert und nie eine Faktenquelle. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssistantScreen(books: List<Book>, coverFile: (String) -> File, actions: AssistantActions) {
    val arabic = LocalArabic.current
    var part by remember { mutableStateOf(Part.ORACLE) }
    var input by remember(part) { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }
    var error by remember(part) { mutableStateOf<String?>(null) }
    var failure by remember(part) { mutableStateOf<Throwable?>(null) }
    var pick by remember { mutableStateOf<AiPick?>(null) }
    var tips by remember { mutableStateOf<List<AiTip>>(emptyList()) }
    var answer by remember { mutableStateOf<AiAnswer?>(null) }
    var genres by remember { mutableStateOf<List<AiGenre>>(emptyList()) }
    var chosen by remember { mutableStateOf(setOf<String>()) }
    var review by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val year = remember { Calendar.getInstance().get(Calendar.YEAR) }
    val failText = aiNoReply()
    val noUnread = tr("Keine ungelesenen Bücher gefunden.", "لا توجد كتب غير مقروءة.")
    val noGenres = tr("Keine Vorschläge: alle Bücher haben schon ein Genre oder sind unbekannt.", "لا اقتراحات.")

    fun <T> run(call: suspend () -> Result<T>, done: (T) -> Unit) {
        if (loading) return
        loading = true; error = null; failure = null
        scope.launch {
            call().onSuccess(done).onFailure { failure = it }
            loading = false
        }
    }

    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        TopAppBar(
            title = { Column {
                Text(tr("Allam fragen", "اسأل علام"))
                Text(tr("Allam · KI-Buchhändler · kann irren", "علام · ورّاق ذكي · قد يخطئ"), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            } },
            navigationIcon = { IconButton(onClick = actions.close) { Icon(Icons.AutoMirrored.Filled.ArrowBack, tr("Zurück", "رجوع")) } },
        )
        ScrollableTabRow(selectedTabIndex = part.ordinal, edgePadding = 12.dp) {
            Part.entries.forEach { p ->
                Tab(selected = part == p, onClick = { if (!loading) part = p }, text = { Text(when (p) {
                    Part.ORACLE -> tr("Orakel", "القرعة")
                    Part.TIPS -> tr("Lesetipps", "اقتراحات")
                    Part.ASK -> tr("Bibliothek fragen", "اسأل مكتبتي")
                    Part.GENRES -> tr("Genres ordnen", "التصنيفات")
                    Part.YEAR -> tr("Jahresrückblick", "حصاد السنة")
                }) })
            }
        }
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            val hint = when (part) {
                Part.ORACLE -> tr("Stimmung oder Wunsch (optional), z. B. „kurz und leicht“", "مزاجك أو رغبتك (اختياري)")
                Part.TIPS -> tr("Wunsch (optional), z. B. „arabische Klassiker“", "رغبتك (اختياري)")
                Part.ASK -> tr("Frage an deine Bibliothek, z. B. „Welche Bücher handeln von Familie?“", "سؤالك عن مكتبتك")
                else -> ""
            }
            if (hint.isNotEmpty()) OutlinedTextField(input, { input = it }, Modifier.fillMaxWidth(), label = { Text(hint) },
                shape = RoundedCornerShape(16.dp), enabled = !loading)
            Button(
                enabled = !loading && (part != Part.ASK || input.isNotBlank()),
                onClick = { when (part) {
                    Part.ORACLE -> run({ actions.oracle(input, arabic) }) { pick = it; if (it == null) error = noUnread }
                    Part.TIPS -> run({ actions.tips(input, arabic) }) { tips = it; if (it.isEmpty()) error = failText }
                    Part.ASK -> run({ actions.ask(input, arabic) }) { answer = it }
                    Part.GENRES -> run({ actions.genres() }) { g -> genres = g; chosen = g.map { it.book.id }.toSet()
                        if (g.isEmpty()) error = noGenres }
                    Part.YEAR -> run({ actions.year(year, arabic) }) { review = it }
                } },
                modifier = Modifier.fillMaxWidth(),
            ) { Text(when (part) {
                Part.ORACLE -> tr("Buch für mich wählen", "اختر لي كتاباً")
                Part.TIPS -> tr("Tipps holen", "اقترح كتباً")
                Part.ASK -> tr("Fragen", "اسأل")
                Part.GENRES -> tr("Fehlende Genres vorschlagen", "اقترح التصنيفات الناقصة")
                Part.YEAR -> tr("Rückblick $year schreiben", "حصاد $year")
            }) }
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            val failureText = failure?.let { geminiErrorText(it) ?: failText }
            (error ?: failureText)?.let { Text(it, color = MaterialTheme.colorScheme.error) }

            when (part) {
                Part.ORACLE -> pick?.let { p ->
                    BookLine(p.book, coverFile) { actions.open(p.book.id) }
                    AiText(p.reason)
                }
                Part.TIPS -> tips.forEach { t ->
                    Card(Modifier.fillMaxWidth().clickable { actions.search(listOf(t.title, t.author).filter { it.isNotBlank() }.joinToString(" ")) },
                        shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(14.dp)) {
                            Text(t.title, style = MaterialTheme.typography.titleMedium)
                            if (t.author.isNotBlank()) Text(t.author, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            AiText(t.reason)
                            Text(tr("Antippen: in den Katalogen suchen und hinzufügen", "اضغط للبحث والإضافة"),
                                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
                Part.ASK -> answer?.let { a ->
                    AiText(a.text)
                    a.books.forEach { b -> BookLine(b, coverFile) { actions.open(b.id) } }
                }
                Part.GENRES -> if (genres.isNotEmpty()) {
                    genres.forEach { g ->
                        Row(Modifier.fillMaxWidth().clickable { chosen = if (g.book.id in chosen) chosen - g.book.id else chosen + g.book.id },
                            verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(g.book.id in chosen, { chosen = if (it) chosen + g.book.id else chosen - g.book.id })
                            Column(Modifier.weight(1f)) {
                                Text(g.book.title, style = MaterialTheme.typography.titleSmall)
                                Text(g.genre, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                    Text(tr("KI-Vorschläge. Nur leere Genrefelder werden gefüllt.", "اقتراحات ذكية، تُملأ الحقول الفارغة فقط."), style = MaterialTheme.typography.labelSmall)
                    Button(enabled = chosen.isNotEmpty(), onClick = {
                        actions.applyGenres(genres.filter { it.book.id in chosen }.associate { it.book.id to it.genre })
                        genres = emptyList(); chosen = emptySet()
                    }, modifier = Modifier.fillMaxWidth()) { Text(tr("Ausgewählte übernehmen", "اعتماد المختار")) }
                }
                Part.YEAR -> if (review.isNotBlank()) AiText(review)
            }
            Spacer(Modifier.height(24.dp))
            if (books.isEmpty()) Text(tr("Deine Bibliothek ist noch leer.", "مكتبتك فارغة."))
        }
    }
}

/** Karte in den Buchdetails: eine spoilerfreie Frage zu genau diesem Buch. */
@Composable
fun AskBookCard(bookId: String, ask: suspend (String, Boolean) -> Result<String>) {
    val arabic = LocalArabic.current
    var question by remember(bookId) { mutableStateOf("") }
    var answer by remember(bookId) { mutableStateOf("") }
    var loading by remember(bookId) { mutableStateOf(false) }
    var failure by remember(bookId) { mutableStateOf<Throwable?>(null) }
    val scope = rememberCoroutineScope()
    Surface(shape = RoundedCornerShape(18.dp), color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp)) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(tr("Allam fragen", "اسأل علام"), style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(question, { question = it }, Modifier.fillMaxWidth(), enabled = !loading,
                label = { Text(tr("z. B. „Passt das Buch zu mir?“", "مثلاً: هل يناسبني هذا الكتاب؟")) }, shape = RoundedCornerShape(14.dp))
            Button(enabled = question.isNotBlank() && !loading, onClick = {
                loading = true; failure = null
                scope.launch { ask(question, arabic).onSuccess { answer = it }.onFailure { failure = it }; loading = false }
            }) { Text(tr("Fragen", "اسأل")) }
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth())
            failure?.let { Text(geminiErrorText(it) ?: aiNoReply(), color = MaterialTheme.colorScheme.error) }
            AiText(answer)
        }
    }
}

@Composable
internal fun geminiErrorText(failure: Throwable?): String? = when ((failure as? GeminiException)?.failure) {
    GeminiFailure.KEY_REJECTED -> tr(
        "Gemini-Schlüssel abgelehnt. Unter aistudio.google.com/apikey einen gültigen Schlüssel erzeugen und neu eintragen.",
        "رُفض مفتاح Gemini. أنشئ مفتاحًا صالحًا على aistudio.google.com/apikey ثم أدخله من جديد.",
    )
    GeminiFailure.QUOTA_EXHAUSTED -> tr(
        "Tageskontingent von Gemini erreicht, morgen wieder verfügbar.",
        "بلغت الحدّ اليومي لاستخدام Gemini. سيكون متاحًا مجددًا غدًا.",
    )
    null -> null
}

@Composable
private fun aiNoReply() = tr("Keine Antwort. Netz und Gemini-Schlüssel prüfen.", "لا رد. تحقق من الشبكة ومفتاح Gemini.")

@Composable
private fun AiText(text: String) {
    if (text.isBlank()) return
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surfaceContainerLow, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp)) {
            Text(tr("KI", "ذكاء اصطناعي"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.tertiary)
            Text(text, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun BookLine(book: Book, coverFile: (String) -> File, onClick: () -> Unit) {
    Row(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
        BookCover(book.title, book.author, if (book.cover.isNotEmpty()) coverFile(book.cover) else book.coverUrl.ifEmpty { null },
            Modifier.width(48.dp).height(70.dp), elevation = 3.dp, titleSize = 8)
        Column(Modifier.weight(1f).padding(start = 14.dp)) {
            Text(book.title, style = MaterialTheme.typography.titleMedium)
            if (book.author.isNotBlank()) Text(book.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
