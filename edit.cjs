const fs=require('node:fs');const path=require('node:path');
const root=path.join(__dirname,'project');
function edit(name,change){const p=path.join(root,name),before=fs.readFileSync(p,'utf8').replace(/\r\n/g,'\n');const after=change(before);if(after===before)throw Error('Keine Änderung: '+name);fs.writeFileSync(p,after);}
const base='app/src/main/java/de/mo/bibliothek/';
edit(base+'data/BookLookup.kt',s=>s.replace('        val wikipedia = if (asIsbn) Result.success(emptyList()) else runCatching { wikipedia(q) }\n','')
 .replace('google.isFailure && openLibrary.isFailure && wikipedia.isFailure','google.isFailure && openLibrary.isFailure')
 .replace(' + wikipedia.getOrDefault(emptyList())','')
 .replace(/    \/\*\*[\s\S]*?Schlägt eine Zusammenfassung[\s\S]*?    private fun same\(/,'    private fun same(')
 .replace(/    private fun languageName\(code: String\): String = when[\s\S]*?\n    private fun encode/,'    private fun encode')
 .replace('        val google = runCatching { google("isbn:$isbn") }.getOrDefault(emptyList()).firstOrNull()\n        val open = runCatching { openLibrary(isbn) }.getOrDefault(emptyList()).firstOrNull()',
 '        val googleResult = runCatching { google("isbn:$isbn") }\n        val openResult = runCatching { openLibrary(isbn) }\n        if (googleResult.isFailure && openResult.isFailure) throw IOException("Keine Verbindung zur Buchsuche.")\n        val google = googleResult.getOrDefault(emptyList()).firstOrNull()\n        val open = openResult.getOrDefault(emptyList()).firstOrNull()'));
edit(base+'BookieViewModel.kt',s=>s.replace('import de.mo.bibliothek.data.Found','import de.mo.bibliothek.data.BookKnowledge\nimport de.mo.bibliothek.data.KnowledgeLookup\nimport de.mo.bibliothek.data.Found')
 .replace('    private val writes = Mutex()','    private val writes = Mutex()\n    private val knowledgeCache = java.util.concurrent.ConcurrentHashMap<String, BookKnowledge>()')
 .replace(/    suspend fun lookupIsbn[\s\S]*?    suspend fun importCover/,
 '    suspend fun lookupIsbn(isbn: String): Result<Found?> = withContext(Dispatchers.IO) { runCatching { BookLookup.byIsbn(isbn) } }\n    suspend fun fetchKnowledge(book: Book): Result<BookKnowledge> = withContext(Dispatchers.IO) {\n        val key = "${book.id}|${book.title}|${book.author}|${book.isbn}"\n        knowledgeCache[key]?.let { return@withContext Result.success(it) }\n        runCatching { KnowledgeLookup.fetch(book) }.onSuccess { knowledgeCache[key] = it }\n    }\n    suspend fun importCover'));
edit(base+'ui/DetailScreen.kt',s=>s.replace('val fetchKnowledge: (suspend (String, String) -> Pair<String, String>?)? = null','val fetchKnowledge: suspend (Book) -> Result<de.mo.bibliothek.data.BookKnowledge>')
 .replace(/    \/\/ Automatische Wikipedia[\s\S]*?\n    Box\(/,'    Box(')
 .replace(/            \/\/ WOW-Feature 4:[\s\S]*?            \/\/ Eigene Notizen & Tracking/,
 '            KnowledgePanel(book, actions.fetchKnowledge, coverFile)\n\n            // Optionale eigene Notizen\n')
 .replace('activeContentColor = Color.White','activeContentColor = scheme.onSecondaryContainer')
 .replace('maxLines = 1,\n                            fontSize = 13.sp','maxLines = 2,\n                            textAlign = TextAlign.Center,\n                            fontSize = 12.sp')
 .replace('tr("Tippe auf das Stiftsymbol, um private Gedanken festzuhalten.", "اضغط على القلم لتدوين أفكار حول الكتاب.")','tr("Optional", "اختياري")'));
edit(base+'MainActivity.kt',s=>s.replace(/    \/\/ Schüttel-Erkennung[\s\S]*?\n    fun push/,'    fun push')
 .replace('                        oracleTrigger = shakeCount,\n','')
 .replace('Triple(Tab.STATS, tr("Lagerraum", "المستودع")','Triple(Tab.STATS, tr("Bücherraum", "غرفة الكتب")')
 .replace('                    val found = model.lookupIsbn(code)','                    val result = model.lookupIsbn(code)\n                    if (result.isFailure) { model.say(trScanOffline); return@launch }\n                    val found = result.getOrNull()')
 .replace('    val scanFailed =','    val trScanOffline = tr("Keine Verbindung zur Buchsuche. Versuche es erneut.", "لا يوجد اتصال بالبحث. حاول مجدداً.")\n    val scanFailed ='));
edit(base+'ui/LibraryScreen.kt',s=>s.replace('import de.mo.bibliothek.data.searchKey','import de.mo.bibliothek.data.searchKey\nimport de.mo.bibliothek.data.oracleCandidates\nimport androidx.compose.runtime.LaunchedEffect\nimport androidx.compose.animation.AnimatedContent\nimport androidx.compose.animation.togetherWith\nimport androidx.compose.animation.scaleIn\nimport androidx.compose.ui.platform.LocalHapticFeedback\nimport androidx.compose.ui.hapticfeedback.HapticFeedbackType')
 .replace('    oracleTrigger: Int = 0,\n','')
 .replace(/    \/\/ Reagiere auf Schütteln[\s\S]*?\n    val arabicCount/,
 '    val haptics = LocalHapticFeedback.current\n    var oracleEmpty by remember { mutableStateOf(false) }\n    fun pickOracleBook() {\n        val candidates = oracleCandidates(books)\n        if (candidates.isEmpty()) { oracleEmpty = true; return }\n        val options = candidates.filter { it.id != oracleBook?.id }.ifEmpty { candidates }\n        oracleBook = options.random()\n        haptics.performHapticFeedback(HapticFeedbackType.LongPress)\n        showOracleDialog = true\n    }\n\n    val arabicCount')
 .replace('        // Al-Warraq Orakel Dialog',
 '        if (oracleEmpty) AlertDialog(onDismissRequest = { oracleEmpty = false },\n            title = { Text(tr("Kein ungelesenes Buch", "لا توجد كتب غير مقروءة")) },\n            text = { Text(tr("Das Orakel zieht nur ungelesene Bücher aus deiner Sammlung.", "تختار القرعة كتباً غير مقروءة فقط من مجموعتك.")) },\n            confirmButton = { TextButton(onClick = { oracleEmpty = false }) { Text(tr("Schließen", "إغلاق")) } })\n\n        // Al-Warraq Orakel Dialog')
 .replace('                        BookCover(\n                            book.title,',
 '                        AnimatedContent(book, transitionSpec = { (fadeIn(tween(350)) + scaleIn(initialScale = .85f)) togetherWith fadeOut(tween(150)) }, label = "oracle") { chosen ->\n                        BookCover(\n                            chosen.title,')
 .replace('                            book.author,\n                            coverModel(book, actions.coverFile),','                            chosen.author,\n                            coverModel(chosen, actions.coverFile),')
 .replace('                            elevation = 8.dp\n                        )\n                        Spacer(Modifier.height(12.dp))','                            elevation = 8.dp\n                        )\n                        }\n                        Spacer(Modifier.height(12.dp))')
 .replace('tr("Das Orakel empfiehlt dir als Nächstes:", "الورّاق يقترح عليك هذا الكتاب:")','tr("Aus deinen ungelesenen Büchern gezogen:", "اختيار من كتبك غير المقروءة:")'));
edit(base+'ui/EditorScreen.kt',s=>s.replace('shape = SegmentedButtonDefaults.itemShape(i, ReadStatus.entries.size),','shape = SegmentedButtonDefaults.itemShape(i, ReadStatus.entries.size),\n                        icon = {},')
 .replace('Text(s.label(), maxLines = 1, overflow = TextOverflow.Ellipsis','Text(s.label(), maxLines = 2, textAlign = androidx.compose.ui.text.style.TextAlign.Center, overflow = TextOverflow.Ellipsis'));
edit(base+'ui/AddScreen.kt',s=>s.replace('Text(s.label(), maxLines = 1, fontSize = 12.sp)','Text(s.label(), maxLines = 2, textAlign = TextAlign.Center, fontSize = 12.sp)')
 .replace('Alle Cover werden offline gesichert.','Geladene Cover werden lokal gespeichert.')
 .replace('جميع الأغلفة تُحفظ محلياً.','تُحفظ الأغلفة المحمّلة محلياً.'));
edit('app/src/main/res/xml/file_paths.xml',s=>s.replace('    <cache-path name="camera" path="camera/" />','    <cache-path name="camera" path="camera/" />\n    <cache-path name="quotes" path="share/" />'));
edit('app/build.gradle.kts',s=>s.replace('versionCode = 4','versionCode = 5').replace('versionName = "3.0"','versionName = "3.1"')
 .replace('        minSdk = 26','        testInstrumentationRunner = "android.test.InstrumentationTestRunner"\n        minSdk = 26'));
console.log('Bestehende Aufrufer, Statusauswahl, Suche und Version angepasst.');
