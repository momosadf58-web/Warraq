package de.mo.bibliothek.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mo.bibliothek.data.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditionSheet(work:Found,known:List<Found>,photoHashes:List<Long>,fetch:suspend(Found,List<Long>,Int)->Result<EditionPage>,onChoose:(Found)->Unit,onClose:()->Unit,onAdd:((Found)->Unit)?=null) {
    val matching = remember(work,known) { known.filter { sameWork(work,it) }.ifEmpty { listOf(work) } }
    val catalogueWork = remember(work,matching) { matching.firstOrNull { it.workKey.matches(Regex("/works/OL[0-9]+W")) } ?: work }
    var editions by remember(work){mutableStateOf(matching.filterNot { it.isWorkResult })}
    var loading by remember(work){mutableStateOf(false)}
    var failed by remember(work){mutableStateOf(false)}
    var nextOffset by remember(work){mutableStateOf<Int?>(0)}
    val scope = rememberCoroutineScope()
    suspend fun loadPage() {
        if (loading) return
        val offset = nextOffset ?: return
        loading = true; failed = false
        val result = fetch(catalogueWork,photoHashes,offset)
        result.onSuccess { page ->
            editions = (editions + page.books.filterNot { it.isWorkResult })
                .groupBy { it.isbn.ifEmpty { it.sourceUrl + it.publisher + it.published } }.values
                .map { copies -> copies.firstOrNull { it.coverDistance != null } ?: copies.first() }
            nextOffset = page.nextOffset
        }
        failed = result.isFailure; loading = false
    }
    LaunchedEffect(work){loadPage()}
    ModalBottomSheet(onDismissRequest=onClose) {
        Column(Modifier.fillMaxWidth().padding(horizontal=24.dp)) {
            Text(tr("Ausgabe auswählen", "اختر الطبعة"),style=MaterialTheme.typography.headlineSmall)
            Text(work.title,style=MaterialTheme.typography.titleMedium)
            if(photoHashes.isNotEmpty())Text(tr("Ausgaben mit Coververgleich zum Foto", "طبعات مع مقارنة الغلاف بالصورة"),style=MaterialTheme.typography.bodySmall)
            if(loading)LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical=12.dp))
            if(failed)Text(tr("Weitere Ausgaben gerade nicht erreichbar. Verfügbare Treffer:", "تعذر جلب طبعات إضافية. النتائج المتاحة:"),style=MaterialTheme.typography.bodySmall)
        }
        // 64-bit difference hash: unrelated covers differ in ~32 bits, the same cover photographed in well under 15.
        val closest=editions.filter{it.coverDistance!=null}.minByOrNull{it.coverDistance!!}?.takeIf{it.coverDistance!!<=14}
        LazyColumn(Modifier.fillMaxWidth().heightIn(max=520.dp),contentPadding=PaddingValues(20.dp)) {
            if (!loading && editions.isEmpty()) item {
                Text(tr("Noch keine konkrete Ausgabe verfügbar. Du kannst die Suche ändern oder das Buch manuell erfassen.", "لا تتوفر طبعة محددة بعد. يمكنك تعديل البحث أو إدخال الكتاب يدوياً."))
            }
            items(editions){found->
                Row(Modifier.fillMaxWidth().clickable{onChoose(found)}.padding(vertical=10.dp),verticalAlignment=Alignment.CenterVertically) {
                    // DNB-Ausgaben haben online selten ein eigenes Cover; dann das Werkcover zeigen.
                    BookCover(found.title,found.author,found.fallbackCover.ifBlank{found.coverUrl}.ifBlank{null},Modifier.width(66.dp).height(96.dp),titleSize=9)
                    Column(Modifier.weight(1f).padding(start=16.dp)) {
                        if(found===closest)Text(tr("Cover ähnelt deinem Foto", "الغلاف يشبه صورتك"),style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.tertiary)
                        Text(found.title,style=MaterialTheme.typography.titleSmall)
                        Text(listOf(found.publisher,found.published,found.language).filter{it.isNotBlank()}.joinToString(" · "),style=MaterialTheme.typography.bodySmall)
                        if(found.isbn.isNotBlank())Text("ISBN ${found.isbn}",style=MaterialTheme.typography.labelSmall)
                        Text(found.source,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.primary)
                        if(onAdd!=null) {
                            Spacer(Modifier.height(8.dp))
                            FilledTonalButton(onClick={onAdd(found)}) { Text(tr("Hinzufügen", "إضافة")) }
                        }
                    }
                }
                HorizontalDivider(color=MaterialTheme.colorScheme.outlineVariant.copy(alpha=0.5f))
            }
            if (nextOffset != null && !loading) item {
                OutlinedButton(onClick = { scope.launch { loadPage() } }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (failed) tr("Erneut versuchen", "إعادة المحاولة") else tr("Weitere Ausgaben laden", "تحميل طبعات إضافية"))
                }
            }
        }
    }
}
