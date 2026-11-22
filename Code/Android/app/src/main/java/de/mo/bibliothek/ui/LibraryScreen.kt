package de.mo.bibliothek.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mo.bibliothek.SortMode
import de.mo.bibliothek.ViewMode
import de.mo.bibliothek.data.Book
import de.mo.bibliothek.data.ReadStatus
import de.mo.bibliothek.data.oracleCandidates
import de.mo.bibliothek.data.selectLibraryBooks
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.togetherWith
import androidx.compose.animation.scaleIn
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import java.io.File
import java.text.Collator

class BookActions(
    val open: (String) -> Unit,
    val menu: (String) -> Unit,
    val status: (String, ReadStatus) -> Unit,
    val coverFile: (String) -> File,
)

/** Fälliger Wochenbericht für die Bibliothekskarte. */
class Cleanup(val count: Int, val open: () -> Unit, val dismiss: () -> Unit)

@Composable
fun LibraryScreen(
    books: List<Book>,
    viewMode: ViewMode,
    sort: SortMode,
    onViewMode: (ViewMode) -> Unit,
    onSort: (SortMode) -> Unit,
    actions: BookActions,
    onAdd: () -> Unit,
    contentPadding: PaddingValues,
    onAssistant: (() -> Unit)? = null,
    cleanup: Cleanup? = null,
) {
    var query by rememberSaveable { mutableStateOf("") }
    var filter by rememberSaveable { mutableStateOf<String?>(null) } // null = alle, sonst ReadStatus.name
    var lang by rememberSaveable { mutableStateOf<String?>(null) } // null, "de", "ar"
    var showOracleDialog by remember { mutableStateOf(false) }
    var oracleBook by remember { mutableStateOf<Book?>(null) }

    val haptics = LocalHapticFeedback.current
    var oracleEmpty by remember { mutableStateOf(false) }
    fun pickOracleBook() {
        val candidates = oracleCandidates(books)
        if (candidates.isEmpty()) { oracleEmpty = true; return }
        val options = candidates.filter { it.id != oracleBook?.id }.ifEmpty { candidates }
        oracleBook = options.random()
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        showOracleDialog = true
    }

    val selection = remember(books, query, filter, lang) { selectLibraryBooks(books, query, filter, lang) }
    val shown = remember(selection.books, sort) {
        val collator = Collator.getInstance()
        selection.books.withIndex()
            .let { found ->
                when (sort) {
                    SortMode.ADDED -> found.sortedByDescending { it.index }
                    SortMode.TITLE -> found.sortedWith(compareBy<IndexedValue<Book>, String>(collator) { it.value.title })
                    SortMode.AUTHOR -> found.sortedWith(compareBy<IndexedValue<Book>, String>(collator) { it.value.author.ifEmpty { "" } }.thenBy(collator) { it.value.title })
                    SortMode.STATUS -> found.sortedWith(compareBy<IndexedValue<Book>> { order(it.value.readStatus) }.thenByDescending { it.index })
                }
            }
            .map { it.value }
    }
    val columns = if (LocalConfiguration.current.screenWidthDp >= 600) 5 else 3

    Box(Modifier.fillMaxSize()) {
        LazyColumn(Modifier.fillMaxSize(), contentPadding = contentPadding) {
            item(key = "header") {
                HeaderCompact(
                    count = books.size,
                    viewMode = viewMode,
                    sort = sort,
                    onViewMode = onViewMode,
                    onSort = onSort,
                    onOracle = { pickOracleBook() },
                    onAssistant = onAssistant,
                )
            }
            item(key = "search") {
                SearchField(query) { query = it }
            }
            item(key = "unified_filters") {
                // Einzeilige, elegante und platzsparende Filterleiste
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    item {
                        FilterPill(
                            text = tr("Alle", "الكل") + " ${selection.allCount}",
                            selected = filter == null && lang == null,
                            onClick = { filter = null; lang = null }
                        )
                    }
                    items(ReadStatus.entries) { s ->
                        FilterPill(
                            text = "${s.label()} ${selection.statusCounts.getValue(s)}",
                            selected = filter == s.name,
                            onClick = { filter = if (filter == s.name) null else s.name }
                        )
                    }
                    item {
                        Box(
                            Modifier
                                .padding(horizontal = 4.dp)
                                .height(20.dp)
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                        )
                    }
                    item {
                        FilterPill(
                            text = "العربية ${selection.arabicCount}",
                            selected = lang == "ar",
                            subtle = true,
                            onClick = { lang = if (lang == "ar") null else "ar" }
                        )
                    }
                    item {
                        FilterPill(
                            text = "Deutsch ${selection.germanCount}",
                            selected = lang == "de",
                            subtle = true,
                            onClick = { lang = if (lang == "de") null else "de" }
                        )
                    }
                }
            }
            if (query.isNotBlank() || filter != null || lang != null) {
                item(key = "result_count") {
                    Text(
                        tr("${shown.size} von ${books.size} Büchern", "${shown.size} من أصل ${books.size} كتاب"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 6.dp),
                    )
                }
            }
            if (cleanup != null) item(key = "cleanup") { CleanupCard(cleanup.count, cleanup.open, cleanup.dismiss) }
            item(key = "gap") { Spacer(Modifier.height(8.dp)) }

            when {
                books.isEmpty() -> item(key = "empty") { EmptyLibrary(onAdd) }
                shown.isEmpty() -> item(key = "none") {
                    Text(
                        tr("Keine Bücher für diese Auswahl.", "لا توجد كتب لهذا الاختيار."),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 40.dp),
                    )
                }
                viewMode == ViewMode.SHELF -> {
                    val rows = shown.chunked(columns)
                    items(rows.size, key = { "shelf-" + rows[it].first().id }) { i ->
                        ShelfRow(rows[i], columns, actions, first = i == 0, last = i == rows.lastIndex, modifier = Modifier.animateItem())
                    }
                }
                viewMode == ViewMode.GRID -> {
                    val rows = shown.chunked(columns)
                    items(rows.size, key = { "grid-" + rows[it].first().id }) { i ->
                        GridRow(rows[i], columns, actions, Modifier.animateItem())
                    }
                }
                else -> items(shown, key = { "row-" + it.id }) { b -> ListRow(b, actions, Modifier.animateItem()) }
            }
            item(key = "end") { Spacer(Modifier.height(28.dp)) }
        }

        if (oracleEmpty) AlertDialog(onDismissRequest = { oracleEmpty = false },
            title = { Text(tr("Kein ungelesenes Buch", "لا توجد كتب غير مقروءة")) },
            text = { Text(tr("Das Orakel zieht nur ungelesene Bücher aus deiner Sammlung.", "تختار القرعة كتباً غير مقروءة فقط من مجموعتك.")) },
            confirmButton = { TextButton(onClick = { oracleEmpty = false }) { Text(tr("Schließen", "إغلاق")) } })

        // Al-Warraq Orakel Dialog
        if (showOracleDialog && oracleBook != null) {
            val book = oracleBook!!
            AlertDialog(
                onDismissRequest = { showOracleDialog = false },
                icon = {
                    Icon(
                        Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = Color(0xFFD9A441),
                        modifier = Modifier.size(36.dp)
                    )
                },
                title = {
                    Text(
                        tr("Al-Warraq Orakel", "قرعة الورّاق"),
                        style = MaterialTheme.typography.titleLarge,
                        fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                        textAlign = TextAlign.Center
                    )
                },
                text = {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            tr("Aus deinen ungelesenen Büchern gezogen:", "اختيار من كتبك غير المقروءة:"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))
                        AnimatedContent(book, transitionSpec = { (fadeIn(tween(350)) + scaleIn(initialScale = .85f)) togetherWith fadeOut(tween(150)) }, label = "oracle") { chosen ->
                        BookCover(
                            chosen.title,
                            chosen.author,
                            coverModel(chosen, actions.coverFile),
                            modifier = Modifier
                                .width(110.dp)
                                .aspectRatio(0.66f),
                            titleSize = 11,
                            elevation = 8.dp
                        )
                        }
                        Spacer(Modifier.height(12.dp))
                        Text(
                            book.title,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (book.author.isNotEmpty()) {
                            Text(
                                book.author,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showOracleDialog = false
                            actions.open(book.id)
                        }
                    ) {
                        Text(tr("Buch öffnen", "فتح الكتاب"))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { pickOracleBook() }) {
                        Text(tr("Nochmal würfeln", "اختيار آخر"))
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
    }
}

private fun order(s: ReadStatus) = when (s) { ReadStatus.READING -> 0; ReadStatus.UNREAD -> 1; ReadStatus.READ -> 2 }

@Composable
private fun HeaderCompact(
    count: Int,
    viewMode: ViewMode,
    sort: SortMode,
    onViewMode: (ViewMode) -> Unit,
    onSort: (SortMode) -> Unit,
    onOracle: () -> Unit,
    onAssistant: (() -> Unit)? = null,
) {
    var sortOpen by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 12.dp, top = 8.dp, bottom = 4.dp)
    ) {
        // Obere Aktionszeile: Logo, Orakel-Button, Ansichten, Sortierung
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.Bottom) {
                    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                        Text(
                            "مكتبتي",
                            fontFamily = Calligraphy,
                            fontSize = 38.sp,
                            lineHeight = 44.sp,
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        tr("Warraq", "الورّاق"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }

            // Orakel: Würfel statt Funkeln, gleiche ruhige Form wie die übrigen Symbole
            IconButton(onClick = onOracle) {
                Icon(Icons.Outlined.Casino, contentDescription = tr("Orakel / Zufälliges Buch", "قرعة الكتب"))
            }

            // KI-Buchhändler, nur mit Gemini-Schlüssel sichtbar
            if (onAssistant != null) IconButton(onClick = onAssistant) {
                Icon(Icons.Outlined.Forum, contentDescription = tr("Allam fragen", "اسأل علام"))
            }
        }

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(booksCount(count), style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))

            val next = when (viewMode) { ViewMode.SHELF -> ViewMode.GRID; ViewMode.GRID -> ViewMode.LIST; ViewMode.LIST -> ViewMode.SHELF }
            TextButton(onClick = { onViewMode(next) }) {
                Icon(
                    when (viewMode) {
                        ViewMode.SHELF -> Icons.AutoMirrored.Outlined.LibraryBooks
                        ViewMode.GRID -> Icons.Outlined.GridView
                        ViewMode.LIST -> Icons.AutoMirrored.Outlined.ViewList
                    },
                    contentDescription = tr("Ansicht wechseln", "تغيير العرض"),
                )
                Spacer(Modifier.width(6.dp))
                Text(when (viewMode) { ViewMode.SHELF -> tr("Regal", "رفوف"); ViewMode.GRID -> tr("Raster", "شبكة"); ViewMode.LIST -> tr("Liste", "قائمة") })
            }

            Box {
                TextButton(onClick = { sortOpen = true }) {
                    Icon(Icons.AutoMirrored.Outlined.Sort, tr("Sortieren", "ترتيب"))
                    Spacer(Modifier.width(6.dp))
                    Text(tr("Sortieren", "ترتيب"))
                }
                DropdownMenu(sortOpen, { sortOpen = false }, shape = RoundedCornerShape(16.dp)) {
                    listOf(
                        SortMode.ADDED to tr("Zuletzt hinzugefügt", "الأحدث إضافة"),
                        SortMode.TITLE to tr("Titel", "العنوان"),
                        SortMode.AUTHOR to tr("Autor", "المؤلف"),
                        SortMode.STATUS to tr("Status", "الحالة"),
                    ).forEach { (mode, label) ->
                        DropdownMenuItem(
                            text = { Text(label, color = if (mode == sort) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface) },
                            onClick = { sortOpen = false; onSort(mode) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchField(query: String, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onChange,
        singleLine = true,
        placeholder = { Text(tr("Titel, Autor oder ISBN", "العنوان أو المؤلف أو ISBN"), fontSize = 14.sp) },
        leadingIcon = { Icon(Icons.Outlined.Search, null, modifier = Modifier.size(20.dp)) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onChange("") }) {
                    Icon(Icons.Outlined.Close, tr("Leeren", "مسح"), modifier = Modifier.size(18.dp))
                }
            }
        },
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 6.dp),
    )
}

@Composable
private fun FilterPill(text: String, selected: Boolean, subtle: Boolean = false, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text, maxLines = 1, fontSize = 12.sp) },
        shape = RoundedCornerShape(50),
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = if (subtle) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primary,
            selectedLabelColor = if (subtle) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onPrimary,
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = selected,
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
        ),
    )
}

/** Eine Regalreihe: Bücher stehen plastisch auf dem Holzbrett mit Schatten und Statusknöpfen. */
@Composable
private fun ShelfRow(row: List<Book>, columns: Int, actions: BookActions, first: Boolean, last: Boolean, modifier: Modifier = Modifier) {
    val wood = LocalWood.current
    val shape = RoundedCornerShape(
        topStart = if (first) 18.dp else 0.dp, topEnd = if (first) 18.dp else 0.dp,
        bottomStart = if (last) 18.dp else 0.dp, bottomEnd = if (last) 18.dp else 0.dp,
    )
    Column(
        modifier
            .padding(horizontal = 14.dp)
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    listOf(
                        wood.back,
                        wood.back.copy(alpha = 0.85f),
                        wood.back.copy(alpha = 0.70f)
                    )
                )
            )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(start = 14.dp, end = 14.dp, top = if (first) 20.dp else 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            row.forEach { book -> ShelfBook(book, actions, Modifier.weight(1f)) }
            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
        }

        // Weicher Buchschatten auf das Regalbrett
        Box(
            Modifier
                .fillMaxWidth()
                .height(4.dp)
                .background(Brush.verticalGradient(listOf(Color.Transparent, Color(0x38000000))))
        )

        // Plastisches Holzregalbrett mit Lichtkante & Tiefe
        Box(
            Modifier
                .fillMaxWidth()
                .height(16.dp)
                .shadow(6.dp, RoundedCornerShape(2.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color(0x33FFFFFF), // Lichtkante oben
                            wood.plankTop,
                            wood.plankBottom,
                            Color(0x22000000)  // Unterkante
                        )
                    )
                )
        )
        // Regalschatten nach unten
        Box(
            Modifier
                .fillMaxWidth()
                .height(10.dp)
                .background(Brush.verticalGradient(listOf(wood.shadow, Color.Transparent)))
        )

        // Statusknöpfe direkt unter den Büchern (Mo's gewünschte 3 Knöpfe)
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp)
                .padding(top = 2.dp, bottom = if (last) 18.dp else 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            row.forEach { book ->
                StatusPill(book.readStatus, book.title, { actions.status(book.id, it) }, Modifier.weight(1f), compact = true)
            }
            repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
        }
    }
}

@Composable
private fun ShelfBook(book: Book, actions: BookActions, modifier: Modifier) {
    Box(
        modifier
            .bookTap { actions.open(book.id) }
            .padding(horizontal = 2.dp)
    ) {
        BookCover(
            book.title, book.author, coverModel(book, actions.coverFile),
            Modifier
                .fillMaxWidth()
                .aspectRatio(0.66f),
            titleSize = 13,
            elevation = 8.dp
        )
        if (book.favorite) Icon(
            Icons.Filled.Favorite, null, tint = Color(0xFFE25D52),
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp)
                .size(16.dp),
        )
    }
}

@Composable
private fun GridRow(row: List<Book>, columns: Int, actions: BookActions, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        row.forEach { book ->
            Column(
                Modifier
                    .weight(1f)
                    .bookTap { actions.open(book.id) }
            ) {
                BookCover(book.title, book.author, coverModel(book, actions.coverFile), Modifier.fillMaxWidth().aspectRatio(0.66f), titleSize = 13)
                Spacer(Modifier.height(8.dp))
                Text(book.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                if (book.author.isNotEmpty()) Text(
                    book.author, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1, overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(8.dp))
                StatusPill(book.readStatus, book.title, { actions.status(book.id, it) }, compact = true)
            }
        }
        repeat(columns - row.size) { Spacer(Modifier.weight(1f)) }
    }
}

@Composable
private fun ListRow(book: Book, actions: BookActions, modifier: Modifier = Modifier) {
    Row(
        modifier
            .fillMaxWidth()
            .bookTap { actions.open(book.id) }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BookCover(book.title, book.author, coverModel(book, actions.coverFile), Modifier.width(58.dp).aspectRatio(0.66f), elevation = 4.dp, titleSize = 8)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(book.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (book.author.isNotEmpty()) Text(book.author, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            if (book.rating > 0) Text("★".repeat(book.rating), color = Color(0xFFD9A441), style = MaterialTheme.typography.bodySmall)
        }
        Spacer(Modifier.width(10.dp))
        StatusPill(book.readStatus, book.title, { actions.status(book.id, it) }, Modifier.width(132.dp), compact = true)
    }
}

@Composable
private fun EmptyLibrary(onAdd: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp, vertical = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(Icons.AutoMirrored.Outlined.LibraryBooks, null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(16.dp))
        Text(tr("Das Regal ist noch leer.", "الرف ما زال فارغاً."), style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onAdd) { Text(tr("Erstes Buch hinzufügen", "أضف أول كتاب")) }
    }
}
