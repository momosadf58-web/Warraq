package de.mo.bibliothek.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import de.mo.bibliothek.data.*

@Composable
fun ReadingHelp(book:Book,description:String,quotes:List<BookQuote>,source:WebSource,
    fetch:suspend (Book,String,List<BookQuote>,Boolean)->Result<AiBookNotes>,
    share:(BookQuote,String)->Unit) {
    val arabic=LocalArabic.current
    var attempt by remember(book.id){mutableIntStateOf(0)}
    var result by remember(book.id,description,quotes,arabic){mutableStateOf<Result<AiBookNotes>?>(null)}
    val uri=LocalUriHandler.current
    LaunchedEffect(book.id,book.title,book.author,description,quotes,arabic,attempt) {
        result=null
        result=fetch(book,description,quotes,arabic)
    }
    HorizontalDivider(Modifier.padding(vertical=14.dp))
    Text(tr("Allams Lesehilfe · KI", "مساعدة علام في القراءة · ذكاء اصطناعي"),
        style=MaterialTheme.typography.titleMedium,color=MaterialTheme.colorScheme.primary)
    Text(tr("Aus den vorhandenen Quellen formuliert · kann irren", "صياغة من المصادر المتوفرة · قد تخطئ"),
        style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant)
    when {
        result==null -> {
            LinearProgressIndicator(Modifier.fillMaxWidth().padding(vertical=12.dp))
            Text(tr("Allam liest die Buchquellen …", "علام يقرأ المصادر …"),style=MaterialTheme.typography.bodySmall)
        }
        result?.isFailure==true -> {
            Text(geminiErrorText(result?.exceptionOrNull()) ?: tr("Allam konnte daraus gerade keine Lesehilfe erstellen. Die Quellentexte oben bleiben verfügbar.",
                "تعذر على علام إعداد المساعدة حالياً. تبقى نصوص المصادر أعلاه متاحة."),modifier=Modifier.padding(top=8.dp))
            TextButton(onClick={attempt++}){Text(tr("Erneut versuchen", "إعادة المحاولة"))}
        }
        else -> result?.getOrNull()?.let { notes ->
            if(notes.blurb.isNotBlank()) {
                Text(tr("Kurzbeschreibung · KI", "نبذة قصيرة · ذكاء اصطناعي"),style=MaterialTheme.typography.labelLarge,
                    modifier=Modifier.padding(top=12.dp))
                Text(notes.blurb,modifier=Modifier.padding(top=6.dp))
            }
            val quote=notes.quote
            if(quote!=null) {
                Text(tr("Ausgewähltes Originalzitat", "اقتباس أصلي مختار"),style=MaterialTheme.typography.labelLarge,
                    modifier=Modifier.padding(top=14.dp))
                // This is the original object from KnowledgeLookup, never a model-written quotation.
                Text("« ${quote.text} »",modifier=Modifier.padding(top=6.dp))
                TextButton(onClick={uri.openUri(quote.source.url)}){Text(quote.source.title)}
                Text(tr("Allams Erklärung · KI", "شرح علام · ذكاء اصطناعي"),style=MaterialTheme.typography.labelLarge)
                Text(notes.explanation,modifier=Modifier.padding(top=6.dp))
            } else if(notes.idea.isNotBlank()) {
                val label=tr("Sinngemäße Kernaussage · KI · kein Zitat", "الفكرة بصياغة أخرى · ذكاء اصطناعي · ليست اقتباساً")
                Text(label,style=MaterialTheme.typography.labelLarge,modifier=Modifier.padding(top=14.dp))
                Text(notes.idea,modifier=Modifier.padding(top=6.dp))
                val aiSource=WebSource("KI · ${source.title}",source.url)
                TextButton(onClick={share(BookQuote(notes.idea,aiSource,literal=false),label)}) {
                    Text(tr("Als Bild teilen", "مشاركة كصورة"))
                }
            }
            if(description.isNotBlank() && source.url.isNotBlank())TextButton(onClick={uri.openUri(source.url)}) {
                Text(tr("Grundlage: ", "المصدر: ")+source.title)
            } else if(description.isNotBlank()) Text(tr("Grundlage: gespeicherte Buchbeschreibung", "المصدر: وصف الكتاب المحفوظ"),
                style=MaterialTheme.typography.bodySmall,modifier=Modifier.padding(top=8.dp))
        }
    }
}
