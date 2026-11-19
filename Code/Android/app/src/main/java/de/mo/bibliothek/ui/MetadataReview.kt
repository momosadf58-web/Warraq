package de.mo.bibliothek.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mo.bibliothek.data.Book
import de.mo.bibliothek.data.Found

@Composable
fun MetadataReview(book:Book,found:Found,onApply:(Set<String>)->Unit,onClose:()->Unit) {
    val changes=listOf(
        Triple("title",tr("Titel", "العنوان"),book.title to found.title),
        Triple("author",tr("Autor", "المؤلف"),book.author to found.author),
        Triple("isbn","ISBN",book.isbn to found.isbn),
        Triple("cover",tr("Cover", "الغلاف"),(if(book.cover.isBlank())book.coverUrl else tr("Eigenes gespeichertes Cover", "غلاف محفوظ")) to found.coverUrl),
        Triple("publisher",tr("Verlag", "الناشر"),book.publisher to found.publisher),
        Triple("published",tr("Erschienen", "سنة النشر"),book.published to found.published),
        Triple("genre",tr("Genre", "النوع"),book.genre to found.genre),
        Triple("language",tr("Sprache", "اللغة"),book.language to found.language),
        Triple("pages",tr("Seiten", "الصفحات"),book.pages.takeIf{it>0}?.toString().orEmpty() to found.pages.takeIf{it>0}?.toString().orEmpty()),
        Triple("description",tr("Klappentext", "نبذة"),book.description to found.description)
    ).filter{it.third.second.isNotBlank()&&it.third.first!=it.third.second}
    var selected by remember(book.id,found){mutableStateOf(changes.filter{it.third.first.isBlank()}.map{it.first}.toSet())}
    AlertDialog(onDismissRequest=onClose,title={Text(tr("Änderungen prüfen", "مراجعة التغييرات"))},text={
        Column(Modifier.heightIn(max=480.dp).verticalScroll(rememberScrollState())) {
            Text(found.source+" · "+found.title)
            if(changes.isEmpty())Text(tr("Keine abweichenden Angaben gefunden.", "لا توجد بيانات مختلفة."))
            changes.forEach{(key,label,pair)->
                Row(Modifier.fillMaxWidth().padding(vertical=6.dp)) {
                    Checkbox(key in selected,{selected=if(it)selected+key else selected-key})
                    Column(Modifier.weight(1f)) {
                        Text(label,style=MaterialTheme.typography.titleSmall)
                        Text(pair.first.ifBlank{tr("Bisher leer", "فارغ سابقاً")}.take(180),style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("→ "+pair.second.take(if(key=="description")300 else 180),style=MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    },confirmButton={TextButton(onClick={onApply(selected)},enabled=selected.isNotEmpty()){Text(tr("Auswahl übernehmen", "تطبيق الاختيار"))}},
        dismissButton={TextButton(onClick=onClose){Text(tr("Abbrechen", "إلغاء"))}})
}
