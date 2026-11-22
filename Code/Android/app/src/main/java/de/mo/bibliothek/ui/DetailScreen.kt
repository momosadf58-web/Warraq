package de.mo.bibliothek.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.ManageSearch
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mo.bibliothek.data.Book
import de.mo.bibliothek.data.ReadStatus
import de.mo.bibliothek.data.isArabic
import kotlinx.coroutines.launch
import java.io.File
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class DetailActions(
    val back: () -> Unit,
    val edit: () -> Unit,
    val enrich: () -> Unit,
    val delete: () -> Unit,
    val status: (ReadStatus) -> Unit,
    val readAt: (Long) -> Unit,
    val rating: (Int) -> Unit,
    val favorite: () -> Unit,
    val notes: (String) -> Unit,
    val lists: (List<String>) -> Unit,
    val fetchKnowledge: suspend (Book) -> Result<de.mo.bibliothek.data.BookKnowledge>,
    /** Frage an den KI-Buchhändler; null ohne Gemini-Schlüssel. */
    val askAi: (suspend (String, Boolean) -> Result<String>)? = null,
    val readingNotes: (suspend (Book, String, List<de.mo.bibliothek.data.BookQuote>, Boolean) -> Result<de.mo.bibliothek.data.AiBookNotes>)? = null,
    /** Fehlende Angaben eines neu hinzugefügten Buches (Allams Prüfung); leer = keine Karte. */
    val gaps: List<de.mo.bibliothek.data.Gap> = emptyList(),
    val hideGaps: () -> Unit = {},
)

@Composable
fun gapLabel(gap: de.mo.bibliothek.data.Gap): String = when (gap) {
    de.mo.bibliothek.data.Gap.ISBN -> "ISBN"
    de.mo.bibliothek.data.Gap.COVER -> tr("Cover", "الغلاف")
    de.mo.bibliothek.data.Gap.YEAR -> tr("Jahr", "سنة النشر")
    de.mo.bibliothek.data.Gap.PUBLISHER -> tr("Verlag", "الناشر")
    de.mo.bibliothek.data.Gap.LANGUAGE -> tr("Sprache", "اللغة")
    de.mo.bibliothek.data.Gap.PAGES -> tr("Seitenzahl", "عدد الصفحات")
}

/** Allam prüft ein neues Buch: zeigt Lücken und führt zum bestehenden Ergänzen-Dialog (Übernahme nur nach Bestätigung). */
@Composable
private fun GapCard(gaps: List<de.mo.bibliothek.data.Gap>, enrich: () -> Unit, hide: () -> Unit) {
    var hidden by remember { mutableStateOf(false) }
    if (hidden) return
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp),
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(tr("Allam hat das Buch geprüft", "علّام راجع الكتاب"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            Text(
                tr("Es fehlen: ", "ينقص: ") + gaps.map { gapLabel(it) }.joinToString(", ") +
                    tr(". Ich suche passende Angaben in den Katalogen – übernommen wird nur, was du auswählst.",
                        ". سأبحث في الفهارس عن بيانات مناسبة، ولن يُعتمد إلا ما تختاره."),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 4.dp),
            )
            Row(Modifier.fillMaxWidth().padding(top = 8.dp), horizontalArrangement = Arrangement.End) {
                TextButton(onClick = { hidden = true; hide() }) { Text(tr("Ausblenden", "إخفاء")) }
                Button(onClick = enrich) { Text(tr("Ergänzen", "إكمال")) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    book: Book,
    allLists: List<String>,
    coverFile: (String) -> File,
    actions: DetailActions
) {
    val scheme = MaterialTheme.colorScheme
    val glowColor = clothColor(book.title)
    val glow by animateColorAsState(glowColor, tween(500), label = "glow")
    var revealed by remember(book.id) { mutableStateOf(false) }
    LaunchedEffect(book.id) { revealed = true }
    val reveal by animateFloatAsState(if (revealed) 1f else 0f, tween(200), label = "cover-reveal")
    var menu by remember { mutableStateOf(false) }
    var editNotes by remember { mutableStateOf(false) }
    var addList by remember { mutableStateOf(false) }
    var pickDate by remember { mutableStateOf(false) }
    var longText by remember { mutableStateOf(false) }

    Box(
        Modifier
            .fillMaxSize()
            .background(scheme.background)
    ) {
        // Weiches Umgebungslicht passend zum Buch
        Box(
            Modifier
                .fillMaxWidth()
                .height(480.dp)
                .background(Brush.verticalGradient(listOf(glow.copy(alpha = 0.50f), glow.copy(alpha = 0.15f), scheme.background)))
        )
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .windowInsetsPadding(WindowInsets.statusBars)
                .windowInsetsPadding(WindowInsets.navigationBars),
        ) {
            // Obere Leiste
            Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = actions.back) { Icon(Icons.AutoMirrored.Filled.ArrowBack, tr("Zurück", "رجوع")) }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = actions.favorite) {
                    Icon(
                        if (book.favorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        tr("Lieblingsbuch", "كتاب مفضل"),
                        tint = if (book.favorite) Color(0xFFE25D52) else scheme.onSurface,
                    )
                }
                Box {
                    IconButton(onClick = { menu = true }) { Icon(Icons.Outlined.MoreVert, tr("Mehr", "المزيد")) }
                    DropdownMenu(menu, { menu = false }, shape = RoundedCornerShape(16.dp)) {
                        DropdownMenuItem(text = { Text(tr("Bearbeiten", "تعديل")) }, leadingIcon = { Icon(Icons.Outlined.Edit, null) }, onClick = { menu = false; actions.edit() })
                        DropdownMenuItem(text = { Text(tr("Mit Online-Treffer ergänzen", "إكمال من نتيجة بحث")) }, leadingIcon = { Icon(Icons.Outlined.ManageSearch, null) }, onClick = { menu = false; actions.enrich() })
                        DropdownMenuItem(text = { Text(tr("Löschen", "حذف"), color = scheme.error) }, leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = scheme.error) }, onClick = { menu = false; actions.delete() })
                    }
                }
            }

            // Großes Buchcover
            BookCover(
                book.title, book.author, coverModel(book, coverFile),
                Modifier
                    .padding(top = 4.dp)
                    .align(Alignment.CenterHorizontally)
                    .width(180.dp)
                    .aspectRatio(0.66f)
                    .graphicsLayer {
                        alpha = reveal
                        scaleX = 0.94f + reveal * 0.06f; scaleY = scaleX
                        rotationY = (1f - reveal) * -8f
                    },
                titleSize = 16,
                elevation = 12.dp
            )

            // Titel & Autor
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(top = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (book.genre.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = scheme.surfaceContainerHigh,
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text(
                            book.genre,
                            style = MaterialTheme.typography.labelMedium,
                            color = scheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    book.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontFamily = FontFamily.Serif,
                    textAlign = TextAlign.Center,
                )
                if (book.author.isNotEmpty()) {
                    Text(
                        book.author,
                        style = MaterialTheme.typography.titleMedium,
                        color = scheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                // Kompakte Metadaten-Zeile
                val meta = listOfNotNull(
                    book.publisher.ifEmpty { null },
                    if (book.pages > 0) "${book.pages} " + tr("Seiten", "صفحة") else null,
                    book.published.ifEmpty { null },
                ).joinToString(" · ")
                if (meta.isNotEmpty()) {
                    Text(
                        meta,
                        style = MaterialTheme.typography.bodySmall,
                        color = scheme.outline,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }

                // Sterne-Bewertung
                Spacer(Modifier.height(14.dp))
                Stars(book.rating, actions.rating, size = 30.dp)
            }

            // Mo's 3 Status-Knöpfe (Ungelesen / Lese ich gerade / Gelesen)
            // Sauber ohne überflüssige Icons, damit kein Text abgeschnitten wird
            Spacer(Modifier.height(20.dp))
            SingleChoiceSegmentedButtonRow(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
            ) {
                ReadStatus.entries.forEachIndexed { index, status ->
                    val selected = book.readStatus == status
                    val active = when (status) { ReadStatus.READ -> LocalWood.current.read; ReadStatus.READING -> LocalWood.current.reading; ReadStatus.UNREAD -> scheme.secondaryContainer }
                    SegmentedButton(
                        selected = selected,
                        onClick = { actions.status(status) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = ReadStatus.entries.size),
                        icon = {}, // Keine störenden Icons, damit alle Texte sauber hineinpassen
                        colors = SegmentedButtonDefaults.colors(
                            activeContainerColor = when (status) {
                                ReadStatus.READ -> LocalWood.current.read
                                ReadStatus.READING -> LocalWood.current.reading
                                ReadStatus.UNREAD -> scheme.secondaryContainer
                            },
                            activeContentColor = if (active.luminance() > .4f) Color(0xFF172014) else Color.White,
                            inactiveContainerColor = Color.Transparent,
                        )
                    ) {
                        Text(
                            status.label(),
                            maxLines = 2,
                            textAlign = TextAlign.Center,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            // Gelesen am (Datum)
            if (book.readStatus == ReadStatus.READ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dateText = if (book.readAt > 0) {
                        tr("Gelesen: ", "تاريخ القراءة: ") + DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date(book.readAt))
                    } else {
                        tr("Gelesen-Datum eintragen", "حدد تاريخ القراءة")
                    }
                    AssistChip(
                        onClick = { pickDate = true },
                        label = { Text(dateText, fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Outlined.CalendarToday, null, modifier = Modifier.size(16.dp)) },
                        shape = RoundedCornerShape(50)
                    )
                }
            }

            if (actions.gaps.isNotEmpty()) GapCard(actions.gaps, actions.enrich, actions.hideGaps)
            KnowledgePanel(book, actions.fetchKnowledge, coverFile, actions.readingNotes)
            actions.askAi?.let { AskBookCard(book.id, it) }

            // Optionale eigene Notizen

            Surface(
                shape = RoundedCornerShape(18.dp),
                color = scheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            tr("Eigene Notizen", "ملاحظاتي"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { editNotes = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.Edit, tr("Notizen bearbeiten", "تعديل الملاحظات"), modifier = Modifier.size(16.dp))
                        }
                    }
                    if (book.notes.isEmpty()) {
                        Text(
                            tr("Optional", "اختياري"),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            book.notes,
                            style = MaterialTheme.typography.bodyMedium,
                            color = scheme.onSurface,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }

            // Listen & Regale
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = scheme.surfaceContainerLow,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 8.dp)
                    .padding(bottom = 36.dp)
            ) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            tr("Listen & Kategorien", "القوائم"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { addList = true }, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Outlined.Add, tr("Zu Liste hinzufügen", "إضافة إلى قائمة"), modifier = Modifier.size(18.dp))
                        }
                    }
                    if (book.lists.isEmpty()) {
                        Text(
                            tr("Noch keiner Liste zugeordnet.", "لم تتم إضافته إلى أي قائمة."),
                            style = MaterialTheme.typography.bodySmall,
                            color = scheme.outline,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            book.lists.forEach { listName ->
                                InputChip(
                                    selected = true,
                                    onClick = { actions.lists(book.lists - listName) },
                                    label = { Text(listName, fontSize = 12.sp) },
                                    trailingIcon = { Icon(Icons.Outlined.Close, null, modifier = Modifier.size(14.dp)) },
                                    shape = RoundedCornerShape(50)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialog: Notizen bearbeiten
    if (editNotes) {
        var draft by remember { mutableStateOf(book.notes) }
        AlertDialog(
            onDismissRequest = { editNotes = false },
            title = { Text(tr("Notizen", "الملاحظات")) },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it },
                    modifier = Modifier.fillMaxWidth().height(160.dp),
                    shape = RoundedCornerShape(16.dp),
                    placeholder = { Text(tr("Gedanken, Erkenntnisse oder Zitate...", "أفكار أو اقتباسات...")) }
                )
            },
            confirmButton = {
                Button(onClick = { actions.notes(draft); editNotes = false }) { Text(tr("Speichern", "حفظ")) }
            },
            dismissButton = {
                TextButton(onClick = { editNotes = false }) { Text(tr("Abbrechen", "إلغاء")) }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Dialog: Zu Liste hinzufügen
    if (addList) {
        var newListName by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { addList = false },
            title = { Text(tr("Zu Liste hinzufügen", "إضافة إلى قائمة")) },
            text = {
                Column {
                    OutlinedTextField(
                        value = newListName,
                        onValueChange = { newListName = it },
                        singleLine = true,
                        placeholder = { Text(tr("Neue oder bestehende Liste...", "اسم القائمة...")) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )
                    if (allLists.isNotEmpty()) {
                        Text(
                            tr("Vorhandene Listen:", "القوائم الحالية:"),
                            style = MaterialTheme.typography.labelSmall,
                            color = scheme.outline,
                            modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
                        )
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            allLists.filter { it !in book.lists }.forEach { name ->
                                AssistChip(
                                    onClick = { actions.lists(book.lists + name); addList = false },
                                    label = { Text(name, fontSize = 11.sp) },
                                    shape = RoundedCornerShape(50)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newListName.trim()
                        if (trimmed.isNotEmpty() && trimmed !in book.lists) {
                            actions.lists(book.lists + trimmed)
                        }
                        addList = false
                    }
                ) { Text(tr("Hinzufügen", "إضافة")) }
            },
            dismissButton = { TextButton(onClick = { addList = false }) { Text(tr("Abbrechen", "إلغاء")) } },
            shape = RoundedCornerShape(24.dp)
        )
    }

    // Dialog: Gelesen-Datum wählen
    if (pickDate) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = if (book.readAt > 0) book.readAt else System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { pickDate = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { actions.readAt(it) }
                        pickDate = false
                    }
                ) { Text(tr("Auswählen", "تحديد")) }
            },
            dismissButton = { TextButton(onClick = { pickDate = false }) { Text(tr("Abbrechen", "إلغاء")) } }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
