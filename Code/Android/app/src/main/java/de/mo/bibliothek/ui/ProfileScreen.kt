package de.mo.bibliothek.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.Key
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mo.bibliothek.data.Book
import de.mo.bibliothek.data.ReadStatus

@Composable
fun ProfileScreen(
    books: List<Book>,
    language: String,
    goal: Int,
    onLanguage: (String) -> Unit,
    onGoal: (Int) -> Unit,
    onExport: () -> Unit,
    onImport: () -> Unit,
    onExportRecovery: (() -> Unit)? = null,
    aiEnabled: Boolean = false,
    onAiKey: (String) -> Unit = {},
    onAssistant: () -> Unit = {},
    contentPadding: PaddingValues,
) {
    var confirmRestore by remember { mutableStateOf(false) }
    var editKey by remember { mutableStateOf(false) }
    var editGoal by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
            .padding(horizontal = 20.dp),
    ) {
        Text(
            tr("Profil & Einstellungen", "الملف الشخصي والإعدادات"),
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.padding(start = 4.dp, top = 12.dp)
        )
        Text(
            tr("Warraq · Deine persönliche virtuelle Bibliothek", "الورّاق · مكتبتك الخاصة بلا قيود"),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
        )

        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(Modifier.padding(20.dp)) {
                Stat(books.size, tr("Bücher", "كتب"), Modifier.weight(1f))
                Stat(books.count { it.readStatus == ReadStatus.READING }, tr("Lese ich", "أقرأ"), Modifier.weight(1f))
                Stat(books.count { it.readStatus == ReadStatus.READ }, tr("Gelesen", "قرأت"), Modifier.weight(1f))
            }
        }

        SectionLabel(tr("Sprache", "اللغة"), Modifier.padding(start = 4.dp, top = 26.dp, bottom = 8.dp))
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(vertical = 6.dp)) {
                listOf(
                    "system" to tr("Wie das Handy", "حسب الهاتف"),
                    "de" to "Deutsch",
                    "ar" to "العربية (RTL)"
                ).forEach { (code, label) ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .clickable { onLanguage(code) }
                            .padding(horizontal = 14.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = language == code, onClick = { onLanguage(code) })
                        Spacer(Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }

        SectionLabel(tr("Lesen", "القراءة"), Modifier.padding(start = 4.dp, top = 26.dp, bottom = 8.dp))
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Action(
                Icons.Outlined.Flag,
                tr("Jahresziel", "هدف السنة"),
                tr("$goal Bücher", "$goal كتاب")
            ) { editGoal = true }
        }

        SectionLabel(tr("Allam, KI-Buchhändler (optional)", "علام، الورّاق الذكي (اختياري)"), Modifier.padding(start = 4.dp, top = 26.dp, bottom = 8.dp))
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Action(
                    Icons.Outlined.Key,
                    tr("Ollama-Schlüssel", "مفتاح Ollama"),
                    if (aiEnabled) tr("Aktiv · Ollama Cloud · nur auf diesem Handy gespeichert", "مفعّل · محفوظ على هذا الهاتف فقط")
                    else tr("Aus · App funktioniert auch ohne", "غير مفعّل · التطبيق يعمل بدونه"),
                ) { editKey = true }
                if (aiEnabled) Action(Icons.Outlined.AutoAwesome, tr("Allam fragen", "اسأل علام"),
                    tr("Orakel, Tipps, Bibliothek fragen, Genres, Jahresrückblick", "القرعة، اقتراحات، أسئلة، تصنيفات، حصاد السنة"), onAssistant)
            }
        }
        Text(
            tr("Gesendet werden Titel, Autor, Genre, Sprache, Seiten, Status, Bewertung und Lesejahr. Für Buchfragen und Lesehilfen auch vorhandene Beschreibungen; für Zitat-Erklärungen die gefundenen Zitate. Mit aktivem Schlüssel werden Lesehilfen in den Buchdetails automatisch erstellt. Keine Notizen oder Fotos.",
                "تُرسل العناوين والمؤلفون والتصنيف واللغة والصفحات والحالة والتقييم وسنة القراءة. ولأسئلة الكتب والمساعدة في القراءة تُرسل الأوصاف المتوفرة، ولشرح الاقتباسات تُرسل الاقتباسات الموجودة. مع تفعيل المفتاح تُنشأ المساعدة تلقائياً في تفاصيل الكتاب. لا تُرسل ملاحظات أو صور."),
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, top = 6.dp)
        )

        SectionLabel(tr("Sicherung & Datenhoheit", "النسخ الاحتياطي والبيانات"), Modifier.padding(start = 4.dp, top = 26.dp, bottom = 8.dp))
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column {
                Action(
                    Icons.Outlined.Backup,
                    tr("Sicherung exportieren", "تصدير نسخة احتياطية"),
                    tr("ZIP mit allen Büchern und Offline-Covern", "ملف ZIP بكل الكتب والأغلفة محلياً"),
                    onExport
                )
                Action(
                    Icons.Outlined.Restore,
                    tr("Sicherung wiederherstellen", "استعادة نسخة احتياطية"),
                    tr("Ersetzt die aktuelle Bibliothek", "استيراد ملف ZIP سابق"),
                ) { confirmRestore = true }
                if (onExportRecovery != null) Action(Icons.Outlined.Backup,
                    tr("Vorherigen Stand exportieren", "تصدير الحالة السابقة"),
                    tr("Automatische Sicherung vor der letzten Wiederherstellung", "نسخة تلقائية قبل آخر استعادة"), onExportRecovery)
            }
        }

        Spacer(Modifier.height(36.dp))
    }

    if (confirmRestore) {
        AlertDialog(
            onDismissRequest = { confirmRestore = false },
            title = { Text(tr("Sicherung wiederherstellen?", "استعادة النسخة الاحتياطية؟")) },
            text = { Text(tr("Die Sicherung ersetzt deine Bibliothek. Vorher wird der aktuelle Stand automatisch gesichert. Diesen kannst du danach hier exportieren.", "ستحل النسخة محل المكتبة. تُحفظ الحالة الحالية تلقائياً ويمكن تصديرها من هنا.")) },
            confirmButton = {
                TextButton(onClick = { confirmRestore = false; onImport() }) {
                    Text(tr("Wiederherstellen", "استعادة"), color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmRestore = false }) { Text(tr("Abbrechen", "إلغاء")) }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (editKey) {
        var value by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { editKey = false },
            title = { Text(tr("Ollama-Schlüssel", "مفتاح Ollama")) },
            text = {
                Column {
                    Text(tr("Schlüssel von ollama.com einfügen. Leer lassen und speichern schaltet die KI aus.",
                        "الصق المفتاح من ollama.com. اتركه فارغاً لإيقاف الذكاء."), style = MaterialTheme.typography.bodySmall)
                    androidx.compose.material3.OutlinedTextField(value, { value = it.trim() }, singleLine = true,
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp))
                }
            },
            confirmButton = { TextButton(onClick = { onAiKey(value); editKey = false }) { Text(tr("Speichern", "حفظ")) } },
            dismissButton = { TextButton(onClick = { editKey = false }) { Text(tr("Abbrechen", "إلغاء")) } },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (editGoal) {
        GoalDialog(goal, { onGoal(it); editGoal = false }, { editGoal = false })
    }
}

@Composable
private fun Stat(value: Int, label: String, modifier: Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text("$value", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Action(icon: ImageVector, title: String, hint: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(24.dp))
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(hint, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun GoalDialog(current: Int, onSave: (Int) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(current.toString()) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(tr("Jahresziel festlegen", "تحديد هدف السنة")) },
        text = {
            Column {
                Text(tr("Wie viele Bücher möchtest du dieses Jahr lesen?", "كم كتاباً تود قراءته هذا العام؟"))
                Spacer(Modifier.height(8.dp))
                androidx.compose.material3.OutlinedTextField(
                    value = text,
                    onValueChange = { text = it.filter { c -> c.isDigit() }.take(4) },
                    singleLine = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(onClick = { onSave(text.toIntOrNull()?.coerceAtLeast(1) ?: current) }) {
                Text(tr("Speichern", "حفظ"))
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) { Text(tr("Abbrechen", "إلغاء")) }
        },
        shape = RoundedCornerShape(24.dp)
    )
}
