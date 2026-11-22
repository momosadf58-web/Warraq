package de.mo.bibliothek.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import de.mo.bibliothek.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
fun KnowledgePanel(book:Book,fetch:suspend (Book)->Result<BookKnowledge>,coverFile:(String)->File,
    readingNotes:(suspend (Book,String,List<BookQuote>,Boolean)->Result<AiBookNotes>)?=null) {
    var knowledge by remember(book.id){mutableStateOf<BookKnowledge?>(null)}
    var loading by remember(book.id){mutableStateOf(true)}
    var failed by remember(book.id){mutableStateOf(false)}
    var attempt by remember(book.id){mutableIntStateOf(0)}
    var shareError by remember(book.id){mutableStateOf(false)}
    val context=LocalContext.current;val uri=LocalUriHandler.current;val scope=rememberCoroutineScope()
    LaunchedEffect(book.id,book.title,book.author,attempt) {
        loading=true;failed=false
        val result=fetch(book)
        knowledge=result.getOrNull();failed=result.isFailure;loading=false
    }
    Surface(Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=8.dp),shape=RoundedCornerShape(18.dp),color=MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(18.dp)) {
            Text(tr("Über das Buch", "عن الكتاب"),style=MaterialTheme.typography.titleMedium)
            // Eigene Beschreibung des Buchs vor Fremdquelle; angezeigt wird nur ein kurzer Klappentext.
            val full=book.description.ifBlank{knowledge?.summary.orEmpty()}
            val source=if(book.description.isBlank())knowledge?.source else null
            val blurb=remember(full){shortBlurb(full)}
            if(blurb.isNotBlank()) {
                Text(blurb,modifier=Modifier.padding(top=10.dp))
                source?.let{TextButton(onClick={uri.openUri(it.url)}){Text(tr("Quelle: ", "المصدر: ")+it.title)}}
            } else if(!loading)Text(tr("Keine passende Zusammenfassung gefunden.", "لم يُعثر على ملخص مطابق."),modifier=Modifier.padding(top=8.dp))
            if(loading)LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical=12.dp))
            val shareFailure=tr("Bild konnte nicht erstellt werden.", "تعذر إنشاء الصورة.")
            val ideaLabel=tr("Kernaussage laut Beschreibung · kein Zitat", "الفكرة الأساسية حسب الوصف · ليست اقتباسًا")
            fun share(quote:BookQuote,label:String=ideaLabel)=scope.launch{
                val result=withContext(Dispatchers.IO){runCatching{createQuoteCard(context,book,quote,book.cover.takeIf{it.isNotBlank()}?.let(coverFile),label)}}
                result.onSuccess{runCatching{shareQuoteCard(context,it)}.onFailure{shareError=true}}.onFailure{shareError=true}
            }
            val quotes=knowledge?.quotes.orEmpty()
            val ownSource=WebSource(tr("Buchbeschreibung", "وصف الكتاب"),"")
            val idea=remember(full,quotes,ownSource){if(quotes.isEmpty())keyIdea(full)?.let{BookQuote(it,source?:ownSource,literal=false)} else null}
            if(quotes.isNotEmpty()) {
                HorizontalDivider(Modifier.padding(vertical=14.dp))
                Text(tr("Zitate zum Buch", "اقتباسات من الكتاب"),style=MaterialTheme.typography.titleMedium)
                quotes.forEach{quote->
                    Text("« ${quote.text} »",modifier=Modifier.padding(top=16.dp))
                    TextButton(onClick={uri.openUri(quote.source.url)}){Text(quote.source.title)}
                    TextButton(onClick={share(quote)}){Text(tr("Als Bild teilen", "مشاركة كصورة"))}
                }
            } else if(!loading && readingNotes==null) {
                // Ohne Allam die regelbasierte Kernaussage; mit Allam übernimmt seine Lesehilfe (keine Doppelung, keine Leer-Meldung).
                idea?.let{quote->
                    HorizontalDivider(Modifier.padding(vertical=14.dp))
                    Text(ideaLabel,style=MaterialTheme.typography.labelLarge,color=MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(quote.text,modifier=Modifier.padding(top=8.dp))
                    if(quote.source.url.isNotBlank())TextButton(onClick={uri.openUri(quote.source.url)}){Text(tr("Quelle: ", "المصدر: ")+quote.source.title)}
                    TextButton(onClick={share(quote)}){Text(tr("Als Bild teilen", "مشاركة كصورة"))}
                }
            }
            if(shareError)Text(shareFailure,color=MaterialTheme.colorScheme.error)
            if(!loading && readingNotes!=null && (full.isNotBlank() || WarraqAi.readingQuotes(quotes).isNotEmpty())) {
                ReadingHelp(book,full,quotes,source?:descriptionSource(book)?:ownSource,readingNotes) { quote,label -> share(quote,label) }
            }
            if(failed) {
                Text(tr("Buchquellen gerade nicht erreichbar.", "مصادر الكتب غير متاحة الآن."),style=MaterialTheme.typography.bodySmall)
                TextButton(onClick={attempt++}){Text(tr("Erneut laden", "إعادة المحاولة"))}
            }
        }
    }
}

private fun descriptionSource(book:Book):WebSource? = runCatching {
    val extra=org.json.JSONObject(book.extra)
    val name=extra.optString("source").ifBlank{return@runCatching null}
    val url=extra.optString("sourceUrl").takeIf{runCatching{java.net.URI(it).scheme=="https"}.getOrDefault(false)}.orEmpty()
    WebSource(name,url)
}.getOrNull()
