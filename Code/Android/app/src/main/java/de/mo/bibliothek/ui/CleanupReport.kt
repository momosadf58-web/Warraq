package de.mo.bibliothek.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import de.mo.bibliothek.data.AuditFinding
import de.mo.bibliothek.data.AuditKind
import de.mo.bibliothek.data.Book

/** Karte oben in der Bibliothek, wenn der Wochenbericht fällig ist. */
@Composable
fun CleanupCard(count: Int, open: () -> Unit, dismiss: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.secondaryContainer,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 6.dp).clickable(onClick = open),
    ) {
        Row(Modifier.padding(start = 16.dp, top = 12.dp, bottom = 12.dp, end = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(tr("Allam hat aufgeräumt", "علّام رتّب المكتبة"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                Text(
                    if (count == 0) tr("Alles in Ordnung – keine Hinweise diese Woche.", "كل شيء على ما يرام هذا الأسبوع.")
                    else tr("$count Hinweise zum Durchsehen", "$count ملاحظة للمراجعة"),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            androidx.compose.material3.TextButton(onClick = dismiss) { Text(tr("Später", "لاحقاً")) }
        }
    }
}

/** Wochenbericht: nur Hinweise, nichts wird automatisch geändert oder zusammengeführt. */
@Composable
fun CleanupReport(findings: List<AuditFinding>, books: List<Book>, open: (String) -> Unit, close: () -> Unit) {
    val byId = books.associateBy { it.id }
    val visible = findings.filter { byId.containsKey(it.bookId) }
    Column(Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.statusBars).windowInsetsPadding(WindowInsets.navigationBars)) {
        Row(Modifier.padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = close) { Icon(Icons.AutoMirrored.Filled.ArrowBack, tr("Zurück", "رجوع")) }
            Column {
                Text(tr("Aufräumbericht", "تقرير الترتيب"), style = MaterialTheme.typography.titleLarge, fontFamily = FontFamily.Serif)
                Text(tr("Allam · nur Hinweise, du entscheidest", "علّام · ملاحظات فقط، والقرار لك"),
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        if (visible.isEmpty()) {
            Text(tr("Keine Hinweise – deine Bibliothek ist ordentlich.", "لا ملاحظات، مكتبتك مرتبة."), Modifier.padding(24.dp))
            return
        }
        LazyColumn(contentPadding = PaddingValues(16.dp)) {
            items(visible, key = { it.kind.name + it.bookId + (it.otherId ?: "") }) { f ->
                val book = byId.getValue(f.bookId)
                val other = f.otherId?.let { byId[it] }
                val (headline, detail) = when (f.kind) {
                    AuditKind.DUPLICATE -> tr("Mögliche Dublette", "تكرار محتمل") to
                        "${book.title} ↔ ${other?.title.orEmpty()} " + tr("(prüfen, nichts wird zusammengeführt)", "(للمراجعة فقط)")
                    AuditKind.MISSING_COVER_FILE -> tr("Coverdatei fehlt", "ملف الغلاف مفقود") to book.title
                    AuditKind.INCOMPLETE -> book.title to tr("Es fehlen: ", "ينقص: ") + f.gaps.map { gapLabel(it) }.joinToString(", ")
                }
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { open(f.bookId) },
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(headline, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                        Text(detail, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}
