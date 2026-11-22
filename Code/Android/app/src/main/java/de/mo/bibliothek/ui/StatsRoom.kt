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
        item { GoalRing(thisYear.size, goal, year) { editGoal = true } }
        item { YearBars(read) }
        item { LanguageBar(books) }
        item { TopGenres(books) }
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
private fun ChartCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surfaceContainerLow) {
        Column(Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

/** Jahresziel als Fortschrittsring; Antippen ändert das Ziel. */
@Composable
private fun GoalRing(done: Int, goal: Int, year: Int, onEdit: () -> Unit) {
    val progress by animateFloatAsState((done.toFloat() / goal.coerceAtLeast(1)).coerceIn(0f, 1f), tween(700), label = "goal")
    val track = MaterialTheme.colorScheme.surfaceContainerHighest
    val fill = MaterialTheme.colorScheme.primary
    ChartCard(tr("Jahresziel $year", "هدف سنة $year")) {
        Row(Modifier.fillMaxWidth().clickable(onClick = onEdit), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(96.dp), contentAlignment = Alignment.Center) {
                Canvas(Modifier.fillMaxSize()) {
                    val stroke = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                    val inset = stroke.width / 2
                    drawArc(track, -90f, 360f, false, Offset(inset, inset), Size(size.width - stroke.width, size.height - stroke.width), style = stroke)
                    drawArc(fill, -90f, 360f * progress, false, Offset(inset, inset), Size(size.width - stroke.width, size.height - stroke.width), style = stroke)
                }
                Text("$done/$goal", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(Modifier.width(18.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    if (done >= goal) tr("Ziel erreicht – stark!", "تحقق الهدف، أحسنت!")
                    else tr("Noch ${goal - done} Bücher bis zum Ziel.", "باقٍ ${goal - done} كتب للوصول إلى الهدف."),
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(tr("Antippen, um das Ziel zu ändern", "المس لتغيير الهدف"), style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
            }
        }
    }
}

/** Gelesene Bücher pro Jahr (nur Bücher mit Lesedatum), letzte sechs Jahre mit Einträgen. */
@Composable
private fun YearBars(read: List<Book>) {
    val perYear = remember(read) { read.mapNotNull { readingYear(it.readAt) }.groupingBy { it }.eachCount().toSortedMap().entries.toList().takeLast(6) }
    if (perYear.isEmpty()) return
    val max = perYear.maxOf { it.value }.coerceAtLeast(1)
    val color = MaterialTheme.colorScheme.primary
    ChartCard(tr("Gelesen pro Jahr", "المقروء في كل سنة")) {
        Row(Modifier.fillMaxWidth().height(140.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Bottom) {
            perYear.forEach { (y, n) ->
                Column(Modifier.weight(1f).fillMaxHeight(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Bottom) {
                    Text("$n", style = MaterialTheme.typography.labelMedium)
                    Box(Modifier.fillMaxWidth(0.7f).fillMaxHeight(0.75f * n / max).clip(RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)).background(color))
                    Text("$y", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 4.dp))
                }
            }
        }
        val undated = read.count { readingYear(it.readAt) == null }
        if (undated > 0) Text(tr("$undated gelesene Bücher ohne Lesedatum sind nicht enthalten.", "$undated كتب مقروءة بلا تاريخ غير محسوبة."),
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
    }
}

/** Anteil arabischer, deutscher und sonstiger Bücher als geteilter Balken. */
@Composable
private fun LanguageBar(books: List<Book>) {
    if (books.isEmpty()) return
    val ar = books.count { it.isArabic }
    val de = books.count { !it.isArabic && it.isGerman }
    val other = books.size - ar - de
    val parts = listOf(
        Triple("العربية", ar, MaterialTheme.colorScheme.primary),
        Triple("Deutsch", de, LocalWood.current.reading),
        Triple(tr("Andere", "أخرى"), other, MaterialTheme.colorScheme.outline),
    ).filter { it.second > 0 }
    ChartCard(tr("Sprachen", "اللغات")) {
        Row(Modifier.fillMaxWidth().height(18.dp).clip(RoundedCornerShape(9.dp))) {
            parts.forEach { (_, n, c) -> Box(Modifier.weight(n.toFloat()).fillMaxHeight().background(c)) }
        }
        Row(Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            parts.forEach { (label, n, c) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(c))
                    Text(" $label $n", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

/** Die fünf häufigsten Genres (nur Bücher mit Genre-Angabe). */
@Composable
private fun TopGenres(books: List<Book>) {
    // Nur Genres mit mindestens zwei Büchern: Einzelfälle sagen über die Sammlung nichts aus.
    val top = remember(books) { books.map { it.genre.trim() }.filter { it.isNotEmpty() }.groupingBy { it }.eachCount().entries.filter { it.value >= 2 }.sortedByDescending { it.value }.take(5) }
    val without = books.count { it.genre.isBlank() }
    if (top.isEmpty() && without == 0) return
    val max = top.firstOrNull()?.value ?: 1
    val color = LocalWood.current.read
    ChartCard(tr("Häufigste Genres", "الأنواع الأكثر حضوراً")) {
        top.forEach { (genre, n) ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                Text("⁨$genre⁩", style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(0.45f))
                Box(Modifier.weight(0.45f).height(10.dp)) {
                    Box(Modifier.fillMaxHeight().fillMaxWidth(n.toFloat() / max).clip(RoundedCornerShape(5.dp)).background(color))
                }
                Text("$n", style = MaterialTheme.typography.labelMedium, modifier = Modifier.weight(0.1f).padding(start = 8.dp))
            }
        }
        if (top.isEmpty()) Text(tr("Noch kein Genre mit mehreren Büchern.", "لا يوجد بعد نوع يضم أكثر من كتاب."), style = MaterialTheme.typography.bodyMedium)
        if (without > 0) Text(tr("$without Bücher ohne Genre – „Genres ordnen“ bei Allam hilft.", "$without كتب بلا نوع – „ترتيب الأنواع“ لدى علام يساعد."),
            style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(top = 8.dp))
    }
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
