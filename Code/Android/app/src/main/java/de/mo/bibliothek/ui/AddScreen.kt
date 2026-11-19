package de.mo.bibliothek.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.FormatQuote
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.PhotoCamera
import de.mo.bibliothek.data.sameWork
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mo.bibliothek.data.Book
import de.mo.bibliothek.data.Found
import de.mo.bibliothek.data.ReadStatus
import kotlinx.coroutines.launch

data class AddActions(
    val camera: () -> Unit,
    val editions: suspend (Found, List<Long>, Int) -> Result<de.mo.bibliothek.data.EditionPage>,
    val coverFile: (String) -> java.io.File,
    val search: suspend (String) -> Result<List<Found>>,
    val owned: (Found) -> Book?,
    val add: (Found, ReadStatus) -> Unit,
    val scan: () -> Unit,
    val manual: () -> Unit,
    val openOwned: (String) -> Unit,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScreen(
    actions: AddActions,
    contentPadding: PaddingValues,
    initialQuery: String = "",
    searchOnOpen: Boolean = false,
    photoHashes: List<Long> = emptyList(),
    recentBooks: List<Book> = emptyList(),
    enrichTitle: String? = null,
    onEnrich: ((Found) -> Unit)? = null,
    onClose: (() -> Unit)? = null,
    lensPhoto: java.io.File? = null,
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var query by rememberSaveable { mutableStateOf(initialQuery) }
    var results by remember { mutableStateOf<List<Found>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var searched by remember { mutableStateOf(false) }
    var preview by remember { mutableStateOf<Found?>(null) }
    var work by remember { mutableStateOf<Found?>(null) }
    var searchJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val offline = tr("Keine Verbindung zur Buchsuche.", "لا يوجد اتصال بالبحث.")

    fun run() {
        val q = query.trim()
        if (q.isEmpty()) return
        focus.clearFocus()
        loading = true; error = null
        searchJob?.cancel()
        searchJob = scope.launch {
            val result = actions.search(q)
            loading = false; searched = true
            result.onSuccess { results = it }.onFailure { results = emptyList(); error = offline }
        }
    }

    androidx.compose.runtime.LaunchedEffect(Unit) { if ((enrichTitle != null || searchOnOpen) && initialQuery.isNotEmpty()) run() }

    LazyColumn(Modifier.fillMaxSize(), contentPadding = contentPadding) {
        item {
            Row(Modifier.padding(start = 24.dp, end = 8.dp, top = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        if (enrichTitle == null) tr("Hinzufügen", "إضافة") else tr("Ergänzen", "إكمال"),
                        style = MaterialTheme.typography.headlineLarge,
                    )
                    if (enrichTitle != null) Text(
                        tr("Treffer wählen für: ", "اختر نتيجة لـ: ") + enrichTitle,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (onClose != null) IconButton(onClick = onClose) { Icon(Icons.Outlined.Close, tr("Schließen", "إغلاق")) }
            }
        }

        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                singleLine = true,
                placeholder = { Text(tr("Titel, Autor oder ISBN suchen...", "ابحث بالعنوان أو الكاتب أو ISBN...")) },
                leadingIcon = { Icon(Icons.Outlined.Search, null) },
                trailingIcon = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                    if (query.isNotEmpty()) IconButton(onClick = { query = ""; results = emptyList(); searched = false }) {
                        Icon(Icons.Outlined.Close, tr("Leeren", "مسح"))
                    }
                    IconButton(onClick = { run() }, enabled = query.isNotBlank() && !loading) {
                        Icon(Icons.Outlined.Search, tr("Suchen", "بحث"), tint = if (query.isNotBlank() && !loading) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { run() }),
                shape = RoundedCornerShape(28.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
            )
        }

        if (enrichTitle == null && !searched && results.isEmpty()) {
            // Kamera ist der Hauptweg und steht deshalb zuerst.
            item {
                Button(onClick = actions.camera, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp).heightIn(min = 56.dp)) {
                    Icon(Icons.Outlined.PhotoCamera, null); Spacer(Modifier.width(10.dp)); Text(tr("Buch mit Kamera erkennen", "التعرف على كتاب بالكاميرا"))
                }
            }
            item {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    EntryCard(
                        Icons.Outlined.QrCodeScanner,
                        tr("ISBN scannen", "مسح الرمز"),
                        tr("Barcode hinten", "الباركود"),
                        actions.scan,
                        Modifier.weight(1f),
                    )
                    EntryCard(
                        Icons.Outlined.EditNote,
                        tr("Manuell", "يدوي"),
                        tr("Selbst eintragen", "إدخال بنفسي"),
                        actions.manual,
                        Modifier.weight(1f),
                    )
                }
            }

            // Zuletzt hinzugefügte Bücher
            item {
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 24.dp)
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        Text(tr("Zuletzt hinzugefügt", "أُضيفت مؤخراً"), style = MaterialTheme.typography.titleMedium)
                        recentBooks.takeLast(3).reversed().forEach { book ->
                            Row(Modifier.fillMaxWidth().clickable { actions.openOwned(book.id) }.padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                                BookCover(book.title, book.author, coverModel(book, actions.coverFile), Modifier.width(42.dp).height(62.dp), titleSize = 8)
                                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                    Text(book.title, maxLines = 2, overflow = TextOverflow.Ellipsis)
                                    if (book.author.isNotBlank()) Text(book.author, style = MaterialTheme.typography.bodySmall, maxLines = 1, overflow = TextOverflow.Ellipsis)
                                }
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Text(
                            tr(
                                "Scanne Barcodes, suche nach Titel/Autor oder erfasse alte Schätze von Hand. Geladene Cover werden lokal gespeichert.",
                                "امسح الباركود، أو ابحث عن العنوان والمؤلف، أو أضف كتبك يدوياً. تُحفظ الأغلفة المحمّلة محلياً."
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Start
                        )
                    }
                }
            }
        }

        if (loading) item {
            Box(Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }

        error?.let { msg ->
            item {
                Text(msg, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(24.dp))
            }
        }

        // Camera photo can be looked up in Google Lens on request (sent to Google only by this tap).
        if (lensPhoto != null && searched && !loading) item {
            androidx.compose.material3.OutlinedButton(onClick = { openInLens(context, lensPhoto) }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
                Icon(Icons.Outlined.PhotoCamera, null); Spacer(Modifier.width(8.dp))
                Text(if (results.isEmpty()) tr("Nicht gefunden? Mit Google Lens suchen", "لم يُعثر عليه؟ ابحث عبر Google Lens") else tr("Mit Google Lens suchen", "ابحث عبر Google Lens"))
            }
        }

        if (searched && !loading && query.isNotBlank()) item {
            val unavailable = tr("Amazon konnte nicht geöffnet werden.", "تعذر فتح Amazon.")
            androidx.compose.material3.OutlinedButton(onClick = {
                val uri = android.net.Uri.Builder().scheme("https").authority("www.amazon.de")
                    .path("/s").appendQueryParameter("k", query.trim()).build()
                runCatching { context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, uri)) }
                    .onFailure { android.widget.Toast.makeText(context, unavailable, android.widget.Toast.LENGTH_SHORT).show() }
            }, modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 4.dp)) {
                Text(tr("Auf Amazon suchen", "البحث على Amazon"))
            }
        }

        if (searched && results.isEmpty() && !loading && error == null) item {
            Column(Modifier.padding(24.dp)) {
                Text(tr("Keine passenden Treffer in den Buchkatalogen gefunden.", "لم يتم العثور على نتائج مناسبة في فهارس الكتب."))
                Spacer(Modifier.height(8.dp))
                Button(onClick = actions.manual) { Text(tr("Buch manuell erfassen", "إدخال الكتاب يدوياً")) }
            }
        }

        items(results.fold(mutableListOf<Found>()) { works, found -> works.apply { if (none { sameWork(it, found) }) add(found) } },
            key = { it.workKey + it.title + it.author }) { found ->
            val owned = actions.owned(found)
            FoundRow(found, owned, onOpen = { actions.openOwned(it.id) }, onChoose = {
                work = found
            })
        }
    }

    work?.let { selected -> EditionSheet(selected, results, photoHashes, actions.editions,
        { found -> work = null; preview = found }, { work = null },
        onAdd = if (onEnrich != null) null else { found -> actions.add(found, ReadStatus.UNREAD); work = null }) }

    preview?.let { found ->
        PreviewSheet(found, enriching = onEnrich != null, onDismiss = { preview = null }, onAdd = { status ->
            if (onEnrich != null) onEnrich(found) else actions.add(found, status)
            preview = null
        })
    }
}

/** Opens the photo in Google Lens (Google app); falls back to the share sheet when Lens is not available. */
private fun openInLens(context: android.content.Context, photo: java.io.File) {
    val uri = androidx.core.content.FileProvider.getUriForFile(context, "de.mo.bibliothek.files", photo)
    val send = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
        type = "image/jpeg"; putExtra(android.content.Intent.EXTRA_STREAM, uri)
        clipData = android.content.ClipData.newRawUri("Warraq", uri); addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    runCatching { context.startActivity(android.content.Intent(send).setPackage("com.google.android.googlequicksearchbox")) }
        .onFailure { context.startActivity(android.content.Intent.createChooser(send, "Google Lens")) }
}

@Composable
private fun EntryCard(icon: ImageVector, title: String, hint: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        modifier = modifier.heightIn(min = 116.dp),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.primaryContainer, modifier = Modifier.size(40.dp)) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(22.dp))
                }
            }
            Column {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        }
    }
}

@Composable
private fun FoundRow(found: Found, owned: Book?, onOpen: (Book) -> Unit, onChoose: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { if (owned != null) onOpen(owned) else onChoose() }
            .padding(horizontal = 20.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        BookCover(found.title, found.author, found.coverUrl.ifEmpty { null }, Modifier.width(54.dp).height(80.dp), elevation = 3.dp, titleSize = 8)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(found.title, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            if (found.author.isNotEmpty()) Text(found.author, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
            val meta = listOfNotNull(found.publisher.ifEmpty { null }, found.published.ifEmpty { null }, found.language.ifEmpty { null }).joinToString(" · ")
            if (meta.isNotEmpty()) Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, maxLines = 1)
        }
        Spacer(Modifier.width(8.dp))
        if (owned != null) {
            Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceContainerHighest) {
                Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null, tint = LocalWood.current.read, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(tr("Im Regal", "في الرف"), style = MaterialTheme.typography.labelSmall)
                }
            }
        } else {
            Button(onClick = onChoose, shape = RoundedCornerShape(50)) {
                Icon(Icons.Outlined.BookmarkAdd, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(tr("Wählen", "اختيار"))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PreviewSheet(found: Found, enriching: Boolean = false, onDismiss: () -> Unit, onAdd: (ReadStatus) -> Unit) {
    var status by remember { mutableStateOf(ReadStatus.UNREAD) }
    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)) {
        Column(
            Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BookCover(found.title, found.author, found.coverUrl.ifEmpty { null }, Modifier.width(140.dp).height(200.dp), elevation = 8.dp)
            Spacer(Modifier.height(16.dp))
            Text(found.title, style = MaterialTheme.typography.headlineSmall, textAlign = TextAlign.Center)
            if (found.author.isNotEmpty()) Text(found.author, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val meta = listOfNotNull(
                found.publisher.ifEmpty { null },
                if (found.pages > 0) "${found.pages} " + tr("Seiten", "صفحة") else null,
                found.published.ifEmpty { null },
                found.source,
            ).joinToString(" · ")
            if (meta.isNotEmpty()) Text(meta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline, modifier = Modifier.padding(top = 4.dp))

            if (found.description.isNotEmpty()) {
                // Same short, spoiler-free blurb as on the detail page; the full text stays stored.
                Text(remember(found.description) { de.mo.bibliothek.data.shortBlurb(found.description) }, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 16.dp))
            }

            Spacer(Modifier.height(20.dp))
            if (!enriching) SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                ReadStatus.entries.forEachIndexed { i, s ->
                    SegmentedButton(
                        selected = status == s,
                        onClick = { status = s },
                        shape = SegmentedButtonDefaults.itemShape(i, ReadStatus.entries.size),
                        icon = {},
                    ) {
                        Text(s.label(), maxLines = 2, textAlign = TextAlign.Center, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))
            Button(onClick = { onAdd(status) }, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(24.dp)) {
                Text(if (enriching) tr("Vorschlag prüfen", "مراجعة الاقتراح") else tr("Zur Bibliothek hinzufügen", "إضافة إلى المكتبة"))
            }
        }
    }
}
