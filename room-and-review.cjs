const fs=require('node:fs'),path=require('node:path');const root=path.join(__dirname,'project'),base='app/src/main/java/de/mo/bibliothek/';
function edit(name,fn){const p=path.join(root,name),s=fs.readFileSync(p,'utf8').replace(/\r\n/g,'\n'),n=fn(s);if(s===n)throw Error(name);fs.writeFileSync(p,n);}
edit(base+'ui/StatsRoom.kt',s=>s.replace('coverFile: (String) -> File, contentPadding: PaddingValues)', 'coverFile: (String) -> File, contentPadding: PaddingValues, onRoom: () -> Unit)')
 .replace('Text(tr("Dein Bücherraum", "غرفة كتبك")','Text(tr("Statistik", "الإحصاءات")')
 .replace('        item { YearGoal(thisYear,goal,year,{editGoal=true},onOpen) }',
 '        item { Button(onClick=onRoom,modifier=Modifier.fillMaxWidth().padding(horizontal=20.dp,vertical=8.dp).height(56.dp)) { Text(tr("3D-Buchladen betreten", "دخول مكتبة ثلاثية الأبعاد")) } }')
 .replace('tr("Im Raum", "في الغرفة")','tr("Deine Sammlung", "مجموعتك")')
 .replace(/@Composable\nprivate fun YearGoal\([\s\S]*?\n@Composable\nprivate fun Metric/,'@Composable\nprivate fun Metric'));
edit(base+'BookieViewModel.kt',s=>s.replace('    suspend fun recognizeCover(uri: Uri)',
`    fun applyMatch(id: String, found: Found, fields: Set<String>) = viewModelScope.launch {
        writes.withLock {
            val result = withContext(Dispatchers.IO) { runCatching {
                val b = _books.value.firstOrNull { it.id == id } ?: return@runCatching _books.value
                val cover = if ("cover" in fields) repository.downloadCover(found.coverUrl) else b.cover
                val next = b.copy(
                    title = if ("title" in fields) found.title.take(300) else b.title,
                    author = if ("author" in fields) found.author.take(300) else b.author,
                    isbn = if ("isbn" in fields) found.isbn else b.isbn,
                    publisher = if ("publisher" in fields) found.publisher else b.publisher,
                    published = if ("published" in fields) found.published else b.published,
                    language = if ("language" in fields) found.language else b.language,
                    genre = if ("genre" in fields) found.genre else b.genre,
                    pages = if ("pages" in fields) found.pages.coerceAtLeast(0) else b.pages,
                    description = if ("description" in fields) found.description.take(8000) else b.description,
                    cover = cover, coverUrl = if ("cover" in fields) found.coverUrl else b.coverUrl)
                val list = _books.value.map { if (it.id == id) next else it }; repository.save(list); list
            } }
            result.onSuccess { _books.value = it; _message.value = "Ausgewählte Angaben übernommen." }
                .onFailure { _message.value = "Angaben konnten nicht übernommen werden. Die Bibliothek bleibt erhalten." }
        }
    }

    suspend fun recognizeCover(uri: Uri)`));
edit(base+'MainActivity.kt',s=>s.replace('import de.mo.bibliothek.ui.CameraScreen','import de.mo.bibliothek.ui.BookRoom\nimport de.mo.bibliothek.ui.MetadataReview\nimport de.mo.bibliothek.ui.CameraScreen')
 .replace('    data object Camera : Layer','    data object Room : Layer\n    data object Camera : Layer')
 .replace('    var deleteFor by remember','    var reviewFor by remember { mutableStateOf<Pair<String, de.mo.bibliothek.data.Found>?>(null) }\n    var deleteFor by remember')
 .replace('Triple(Tab.STATS, tr("Bücherraum", "غرفة الكتب")','Triple(Tab.STATS, tr("Statistik", "الإحصاءات")')
 .replace('                        coverFile = model::coverFile,\n                        contentPadding = padding,','                        coverFile = model::coverFile,\n                        onRoom = { push(Layer.Room) },\n                        contentPadding = padding,')
 .replace('                Layer.Camera ->','                Layer.Room -> Surface(Modifier.fillMaxSize()) { BookRoom(books, settings.yearGoal, model::coverFile, { push(Layer.Detail(it)) }, ::pop) }\n                Layer.Camera ->')
 .replace('                                onEnrich = { found -> model.enrich(layer.id, found); pop() },','                                onEnrich = { found -> reviewFor = layer.id to found; pop() },')
 .replace('initialQuery = listOfNotNull(book?.title, book?.author).joinToString(" ").trim()','initialQuery = book?.title.orEmpty()')
 .replace('    deleteFor?.let { id ->','    reviewFor?.let { (id, found) -> books.firstOrNull { it.id == id }?.let { book ->\n        MetadataReview(book, found, { fields -> model.applyMatch(id, found, fields); reviewFor = null }, { reviewFor = null })\n    } }\n\n    deleteFor?.let { id ->'));
edit(base+'ui/AddScreen.kt',s=>s.replace('PreviewSheet(found, onDismiss','PreviewSheet(found, enriching = onEnrich != null, onDismiss')
 .replace('private fun PreviewSheet(found: Found, onDismiss','private fun PreviewSheet(found: Found, enriching: Boolean = false, onDismiss')
 .replace('            SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {','            if (!enriching) SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {')
 .replace('Text(tr("Zur Bibliothek hinzufügen", "إضافة إلى المكتبة"))','Text(if (enriching) tr("Vorschlag prüfen", "مراجعة الاقتراح") else tr("Zur Bibliothek hinzufügen", "إضافة إلى المكتبة"))'));
edit('app/proguard-rules.pro',s=>s+'\n-keepclassmembers class * { @android.webkit.JavascriptInterface <methods>; }\n');
console.log('3D-Raum, Statistik und bestätigte Metadatenänderungen verbunden.');
