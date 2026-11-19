package de.mo.bibliothek.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
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
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.HideImage
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import de.mo.bibliothek.data.Book
import de.mo.bibliothek.data.ReadStatus
import kotlinx.coroutines.launch
import java.io.File

/**
 * Anlegen und Bearbeiten. Änderungen gelten erst mit „Speichern“.
 * [importCover] legt ein Foto/Bild als lokale Kopie ab und gibt den Dateinamen zurück.
 */
@Composable
fun EditorScreen(
    initial: Book,
    isNew: Boolean,
    coverFile: (String) -> File,
    importCover: suspend (Uri) -> String?,
    onCoverFailed: () -> Unit,
    onCancel: () -> Unit,
    onSave: (Book) -> Unit,
) {
    var draft by remember { mutableStateOf(initial) }
    var pagesText by remember { mutableStateOf(if (initial.pages > 0) initial.pages.toString() else "") }
    var listsText by remember { mutableStateOf(initial.lists.joinToString(", ")) }
    var busy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    fun take(uri: Uri?) {
        if (uri == null) return
        busy = true
        scope.launch {
            val name = importCover(uri)
            busy = false
            if (name != null) draft = draft.copy(cover = name) else onCoverFailed()
        }
    }

    val gallery = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { take(it) }
    var cameraUri by rememberSaveable { mutableStateOf<Uri?>(null) }
    val camera = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok -> if (ok) take(cameraUri) }

    Column(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.statusBars)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .imePadding(),
    ) {
        Row(Modifier.padding(horizontal = 4.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onCancel) { Icon(Icons.Outlined.Close, tr("Abbrechen", "إلغاء")) }
            Text(
                if (isNew) tr("Neues Buch", "كتاب جديد") else tr("Bearbeiten", "تعديل"),
                style = MaterialTheme.typography.headlineSmall, modifier = Modifier.weight(1f),
            )
            Button(
                onClick = {
                    onSave(
                        draft.copy(
                            pages = pagesText.filter { it.isDigit() }.take(6).toIntOrNull() ?: 0,
                            lists = listsText.split(',', '،').map { it.trim() }.filter { it.isNotEmpty() }.distinct(),
                        )
                    )
                },
                enabled = draft.title.isNotBlank() && !busy,
                modifier = Modifier.padding(end = 12.dp),
            ) { Text(tr("Speichern", "حفظ")) }
        }

        Column(
            Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 12.dp)) {
                Box(Modifier.width(110.dp).height(166.dp), contentAlignment = Alignment.Center) {
                    BookCover(
                        draft.title.ifBlank { "?" }, draft.author, coverModel(draft, coverFile),
                        Modifier.fillMaxSize(), titleSize = 12,
                    )
                    if (busy) CircularProgressIndicator(Modifier.size(32.dp))
                }
                Spacer(Modifier.width(18.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(onClick = {
                        val dir = File(context.cacheDir, "camera").apply { mkdirs() }
                        val file = File(dir, "foto-${System.currentTimeMillis()}.jpg")
                        val uri = FileProvider.getUriForFile(context, "de.mo.bibliothek.files", file)
                        cameraUri = uri
                        camera.launch(uri)
                    }, enabled = !busy) {
                        Icon(Icons.Outlined.PhotoCamera, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(tr("Foto", "تصوير"))
                    }
                    FilledTonalButton(onClick = {
                        gallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }, enabled = !busy) {
                        Icon(Icons.Outlined.Image, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(tr("Galerie", "المعرض"))
                    }
                    if (draft.cover.isNotEmpty() || draft.coverUrl.isNotEmpty()) TextButton(onClick = { draft = draft.copy(cover = "", coverUrl = "") }) {
                        Icon(Icons.Outlined.HideImage, null, Modifier.size(18.dp)); Spacer(Modifier.width(8.dp)); Text(tr("Cover entfernen", "إزالة الغلاف"), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
            }

            Field(tr("Titel", "العنوان"), draft.title) { draft = draft.copy(title = it.take(300)) }
            Field(tr("Autor", "المؤلف"), draft.author) { draft = draft.copy(author = it.take(300)) }

            Spacer(Modifier.height(14.dp))
            SectionLabel(tr("Status", "الحالة"))
            Spacer(Modifier.height(8.dp))
            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
                ReadStatus.entries.forEachIndexed { i, s ->
                    SegmentedButton(
                        selected = draft.readStatus == s,
                        onClick = { if (draft.readStatus != s) draft = draft.withStatus(s) },
                        shape = SegmentedButtonDefaults.itemShape(i, ReadStatus.entries.size),
                        icon = {},
                    ) { Text(s.label(), maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.labelMedium) }
                }
            }

            Field(tr("Listen (mit Komma trennen)", "القوائم (افصل بفاصلة)"), listsText) { listsText = it.take(400) }
            Field(tr("Genre", "النوع"), draft.genre) { draft = draft.copy(genre = it.take(100)) }
            Field("ISBN", draft.isbn, KeyboardType.Ascii) { draft = draft.copy(isbn = it.take(32)) }
            Field(tr("Sprache", "اللغة"), draft.language) { draft = draft.copy(language = it.take(80)) }
            Field(tr("Verlag", "الناشر"), draft.publisher) { draft = draft.copy(publisher = it.take(200)) }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(Modifier.weight(1f)) { Field(tr("Seiten", "الصفحات"), pagesText, KeyboardType.Number) { pagesText = it.filter(Char::isDigit).take(6) } }
                Box(Modifier.weight(1f)) { Field(tr("Erschienen", "سنة النشر"), draft.published, KeyboardType.Number) { draft = draft.copy(published = it.take(10)) } }
            }
            Field(tr("Klappentext", "نبذة"), draft.description, multiline = true) { draft = draft.copy(description = it.take(8000)) }
            Field(tr("Notiz", "ملاحظة"), draft.notes, multiline = true) { draft = draft.copy(notes = it.take(10000)) }
            Spacer(Modifier.height(32.dp))
        }
    }
}

@Composable
private fun Field(label: String, value: String, keyboard: KeyboardType = KeyboardType.Text, multiline: Boolean = false, onChange: (String) -> Unit) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        label = { Text(label) },
        singleLine = !multiline,
        minLines = if (multiline) 3 else 1,
        shape = RoundedCornerShape(14.dp),
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboard,
            capitalization = if (keyboard == KeyboardType.Text) KeyboardCapitalization.Sentences else KeyboardCapitalization.None,
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp),
    )
}
