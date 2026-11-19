package de.mo.bibliothek.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.mo.bibliothek.data.*
import java.io.File
import java.util.Calendar

private enum class RoomGrouping { STATUS, GENRE, READ_YEAR, PUBLISHED, TYPE }

@Composable
fun StatsScreen(books: List<Book>, goal: Int, onGoal: (Int) -> Unit, onOpen: (String) -> Unit,
                coverFile: (String) -> File, contentPadding: PaddingValues, onRoom: () -> Unit) {
    val year = Calendar.getInstance().get(Calendar.YEAR)
    val read = remember(books) { books.filter { it.readStatus == ReadStatus.READ } }
    val thisYear = remember(books, year) { booksReadInYear(books, year) }
    var grouping by rememberSaveable { mutableStateOf(RoomGrouping.STATUS) }
    var status by rememberSaveable { mutableStateOf<ReadStatus?>(null) }
    var editGoal by remember { mutableStateOf(false) }
    val selected = remember(books, status) { books.filter { status == null || it.readStatus == status } }
    val columns = if (LocalConfiguration.current.screenWidthDp >= 600) 5 else 3
    val unknown = tr("Ohne Angabe", "غير محدد")
    val groups = when (grouping) {
        RoomGrouping.STATUS -> ReadStatus.entries.associate { s -> s.label() to selected.filter { it.readStatus == s } }
        RoomGrouping.GENRE -> selected.groupBy { it.genre.trim().ifEmpty { unknown } }.toSortedMap()
        RoomGrouping.READ_YEAR -> selected.groupBy {
            if (it.readStatus != ReadStatus.READ) tr("Noch nicht abgeschlossen", "لم تُكتمل قراءته")
            else readingYear(it.readAt)?.toString() ?: tr("Gelesen ohne Datum", "مقروء بلا تاريخ")
        }.toSortedMap(reverseOrder())
        RoomGrouping.PUBLISHED -> selected.groupBy { it.publicationYear()?.toString() ?: unknown }.toSortedMap(reverseOrder())
        RoomGrouping.TYPE -> selected.groupBy { b -> when(b.kind()) {
            BookKind.NOVEL -> tr("Romane / Fiktion", "روايات وأدب خيالي")
            BookKind.NONFICTION -> tr("Sachbücher", "كتب غير روائية")
            BookKind.OTHER -> tr("Weitere Genres", "أنواع أخرى")
            BookKind.UNKNOWN -> tr("Ohne Zuordnung", "بلا تصنيف")
        } }
    }.filterValues { it.isNotEmpty() }
    LazyColumn(Modifier.fillMaxSize(), contentPadding = contentPadding) {
        item {
            Column(Modifier.padding(horizontal=24.dp, vertical=16.dp)) {
                Text(tr("Statistik", "الإحصاءات"), style=MaterialTheme.typography.headlineLarge)
                Text(tr("Deine Sammlung, Regal für Regal.", "مجموعتك، رفاً بعد رف."),color=MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
        // Ruhiger Eintrag statt greller Vollflächen-Pille; der Raum bleibt ein Ort, kein Werbebanner.
        item { OutlinedButton(onClick=onRoom,modifier=Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=8.dp).height(52.dp)) { Text(tr("3D-Buchladen betreten", "دخول مكتبة ثلاثية الأبعاد")) } }
        item {
            Row(Modifier.padding(horizontal=20.dp,vertical=16.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)) {
                Metric("${books.size}",tr("Bücher gesamt", "كل الكتب"),Modifier.weight(1f))
                Metric("${read.size}",tr("Gelesen", "مقروءة"),Modifier.weight(1f))
                Metric("${read.sumOf { it.pages.toLong() }}",tr("Seiten gelesen*", "صفحات مقروءة*"),Modifier.weight(1f))
            }
            Text(tr("* Seitenumfang abgeschlossener Bücher mit Seitenangabe.", "* مجموع صفحات الكتب المكتملة ذات عدد صفحات معروف."),
                style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(horizontal=24.dp))
        }
        item {
            Text(tr("Deine Sammlung", "مجموعتك")+" · "+booksCount(selected.size),style=MaterialTheme.typography.titleLarge,
                modifier=Modifier.padding(start=24.dp,top=24.dp,bottom=8.dp))
            LazyRow(contentPadding=PaddingValues(horizontal=20.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                item { FilterChip(status==null,{status=null},{Text(tr("Alle", "الكل"))}) }
                items(ReadStatus.entries.toList()) { s -> FilterChip(status==s,{status=s},{Text(s.label())}) }
            }
            LazyRow(contentPadding=PaddingValues(horizontal=20.dp),horizontalArrangement=Arrangement.spacedBy(8.dp)) {
                items(RoomGrouping.entries.toList()) { g -> FilterChip(grouping==g,{grouping=g},{Text(when(g) {
                    RoomGrouping.STATUS -> tr("Lesestatus", "حالة القراءة")
                    RoomGrouping.GENRE -> tr("Genre", "النوع")
                    RoomGrouping.READ_YEAR -> tr("Lesejahr", "سنة القراءة")
                    RoomGrouping.PUBLISHED -> tr("Erscheinungsjahr", "سنة النشر")
                    RoomGrouping.TYPE -> tr("Roman / Sachbuch", "رواية / غير روائي")
                })}) }
            }
            if(grouping==RoomGrouping.TYPE) Text(tr("Zuordnung nach Genre. Fehlende Angaben bleiben offen.", "التصنيف حسب النوع؛ البيانات الناقصة تبقى غير محددة."),
                style=MaterialTheme.typography.bodySmall,color=MaterialTheme.colorScheme.onSurfaceVariant,modifier=Modifier.padding(horizontal=24.dp,vertical=8.dp))
        }
        if(selected.isEmpty()) item { Text(tr("Hier stehen noch keine Bücher.", "لا توجد كتب هنا بعد."),modifier=Modifier.padding(24.dp)) }
        groups.forEach { (name,group) ->
            item(key="heading:$name") {
                Row(Modifier.padding(start=24.dp,end=24.dp,top=24.dp,bottom=12.dp),verticalAlignment=Alignment.CenterVertically) {
                    Text(name,style=MaterialTheme.typography.titleMedium,modifier=Modifier.weight(1f))
                    Text(booksCount(group.size),style=MaterialTheme.typography.labelMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            items(group.chunked(columns),key={row->"shelf:$name:${row.first().id}"}) {row->RoomShelf(row,columns,onOpen,coverFile)}
        }
        item { Spacer(Modifier.height(32.dp)) }
    }
    if(editGoal) GoalDialog(goal,{onGoal(it);editGoal=false},{editGoal=false})
}

@Composable
private fun Metric(value:String,title:String,modifier:Modifier) {
    Surface(modifier,shape=RoundedCornerShape(16.dp),color=MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(12.dp)){Text(value,style=MaterialTheme.typography.headlineSmall)
            Text(title,style=MaterialTheme.typography.labelSmall,color=MaterialTheme.colorScheme.onSurfaceVariant)}
    }
}

@Composable
private fun RoomShelf(books:List<Book>,columns:Int,onOpen:(String)->Unit,coverFile:(String)->File) {
    val wood=LocalWood.current
    Column(Modifier.padding(horizontal=16.dp).clip(RoundedCornerShape(5.dp)).background(Brush.verticalGradient(listOf(wood.shadow,wood.back,wood.back)))) {
        Row(Modifier.fillMaxWidth().padding(start=14.dp,end=14.dp,top=18.dp),horizontalArrangement=Arrangement.spacedBy(12.dp),verticalAlignment=Alignment.Bottom) {
            books.forEach{book->
                Column(Modifier.weight(1f).clickable{onOpen(book.id)}) {
                    BookCover(book.title,book.author,coverModel(book,coverFile),Modifier.fillMaxWidth().aspectRatio(.66f),titleSize=11,elevation=8.dp)
                    Text(book.title,maxLines=2,overflow=TextOverflow.Ellipsis,style=MaterialTheme.typography.labelSmall,
                        modifier=Modifier.heightIn(min=32.dp).padding(top=6.dp))
                    Text(book.readStatus.label(),style=MaterialTheme.typography.labelSmall,color=statusColor(book.readStatus),
                        modifier=Modifier.padding(bottom=10.dp),maxLines=2)
                }
            }
            repeat(columns-books.size){Spacer(Modifier.weight(1f))}
        }
        Canvas(Modifier.fillMaxWidth().height(15.dp)) {
            drawRect(Brush.verticalGradient(listOf(wood.plankTop,wood.plankBottom)))
            drawLine(Color.White.copy(alpha=.15f),Offset.Zero,Offset(size.width,0f),2f)
            for(i in 1..4)drawLine(Color.Black.copy(alpha=.08f),Offset(0f,size.height*i/5),Offset(size.width,size.height*i/5+1f),1f)
        }
        Box(Modifier.fillMaxWidth().height(12.dp).background(Brush.verticalGradient(listOf(wood.shadow,Color.Transparent))))
    }
}

@Composable
private fun GoalDialog(current:Int,onSave:(Int)->Unit,onDismiss:()->Unit) {
    var text by remember{mutableStateOf(current.toString())}
    AlertDialog(onDismissRequest=onDismiss,title={Text(tr("Jahresziel", "هدف السنة"))},text={
        OutlinedTextField(text,{text=it.filter(Char::isDigit).take(4)},label={Text(tr("Bücher pro Jahr", "كتب في السنة"))},singleLine=true,
            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number))
    },confirmButton={TextButton(onClick={onSave(text.toIntOrNull()?.coerceIn(1,9999)?:current)}){Text(tr("Speichern", "حفظ"))}},
        dismissButton={TextButton(onClick=onDismiss){Text(tr("Abbrechen", "إلغاء"))}})
}
