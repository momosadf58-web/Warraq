package de.mo.bibliothek

import android.net.Uri

import android.app.Activity
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.AddCircleOutline
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import de.mo.bibliothek.data.Book
import de.mo.bibliothek.ui.AddActions
import de.mo.bibliothek.ui.BookRoom
import de.mo.bibliothek.ui.MetadataReview
import de.mo.bibliothek.ui.CameraScreen
import de.mo.bibliothek.ui.RecognitionScreen
import de.mo.bibliothek.ui.AddScreen
import de.mo.bibliothek.ui.BookActions
import de.mo.bibliothek.ui.BookieTheme
import de.mo.bibliothek.ui.DetailActions
import de.mo.bibliothek.ui.DetailScreen
import de.mo.bibliothek.ui.EditorScreen
import de.mo.bibliothek.ui.LibraryScreen
import de.mo.bibliothek.ui.LocalArabic
import de.mo.bibliothek.ui.ProfileScreen
import de.mo.bibliothek.ui.StatsScreen
import de.mo.bibliothek.ui.tr
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.sqrt

class MainActivity : ComponentActivity() {
    private val model: BookieViewModel by viewModels()
    /** Über „Teilen“ empfangenes Coverfoto; wird nur zur Erkennung gelesen. */
    private val sharedImage = kotlinx.coroutines.flow.MutableStateFlow<Uri?>(null)

    private fun takeShared(intent: android.content.Intent?) {
        if (intent?.action == android.content.Intent.ACTION_SEND && intent.type?.startsWith("image/") == true)
            sharedImage.value = androidx.core.content.IntentCompat.getParcelableExtra(intent, android.content.Intent.EXTRA_STREAM, Uri::class.java)
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        takeShared(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState == null) takeShared(intent)
        coil.Coil.setImageLoader(coil.ImageLoader.Builder(this).components { add(coil.decode.SvgDecoder.Factory()) }.build())
        enableEdgeToEdge()
        setContent {
            val settings by model.settings.collectAsStateWithLifecycle()
            val arabic = when (settings.language) {
                "ar" -> true
                "de" -> false
                else -> Locale.getDefault().language == "ar"
            }
            CompositionLocalProvider(
                LocalArabic provides arabic,
                LocalLayoutDirection provides if (arabic) LayoutDirection.Rtl else LayoutDirection.Ltr,
            ) {
                BookieTheme {
                    val view = LocalView.current
                    val darkTheme = isSystemInDarkTheme()
                    if (!view.isInEditMode) {
                        SideEffect {
                            val window = (view.context as Activity).window
                            val insets = WindowCompat.getInsetsController(window, view)
                            // Behebt Bug #6: Statusleisten-Symbole passen sich automatisch dem Hell/Dunkelmodus an
                            insets.isAppearanceLightStatusBars = !darkTheme
                            insets.isAppearanceLightNavigationBars = !darkTheme
                        }
                    }
                    App(model, sharedImage)
                }
            }
        }
    }
}

private enum class Tab { LIBRARY, ADD, STATS, PROFILE }

/** Vollbildebenen über den Tabs. */
private sealed interface Layer {
    data object Room : Layer
    data object Assistant : Layer
    data object Camera : Layer
    data class Recognition(val uri: Uri, val frame: de.mo.bibliothek.data.PreviewFrame? = null) : Layer
    data class Search(val query: String, val hashes: List<Long> = emptyList(), val title: String = "", val author: String = "") : Layer
    data class Detail(val id: String) : Layer
    data class Edit(val book: Book, val isNew: Boolean) : Layer
    data class Enrich(val id: String) : Layer
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun App(model: BookieViewModel, sharedImage: kotlinx.coroutines.flow.MutableStateFlow<Uri?>) {
    val books by model.books.collectAsStateWithLifecycle()
    val settings by model.settings.collectAsStateWithLifecycle()
    val message by model.message.collectAsStateWithLifecycle()
    val aiEnabled by model.aiEnabled.collectAsStateWithLifecycle()
    var tab by rememberSaveable { mutableStateOf(Tab.LIBRARY) }
    var layers by remember { mutableStateOf(listOf<Layer>()) }
    var menuFor by remember { mutableStateOf<String?>(null) }
    var reviewFor by remember { mutableStateOf<Pair<String, de.mo.bibliothek.data.Found>?>(null) }
    var deleteFor by remember { mutableStateOf<String?>(null) }
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val direction = if (LocalLayoutDirection.current == LayoutDirection.Rtl) -1 else 1

    fun push(layer: Layer) { layers = layers + layer }
    fun pop() { layers = layers.dropLast(1) }

    val shared by sharedImage.collectAsStateWithLifecycle()
    LaunchedEffect(shared) {
        shared?.let { layers = layers + Layer.Recognition(it); sharedImage.value = null }
    }

    LaunchedEffect(message) {
        message?.let { snackbar.showSnackbar(it); model.consumeMessage() }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) model.exportBackup(uri)
    }
    val recoveryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) model.exportRecovery(uri)
    }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) model.importBackup(uri)
    }

    val notFound = tr("Kein Treffer für diese ISBN. Bitte manuell ergänzen.", "لا توجد نتيجة لهذا الرقم. أكمل يدوياً.")
    val trScanOffline = tr("Keine Verbindung zur Buchsuche. Versuche es erneut.", "لا يوجد اتصال بالبحث. حاول مجدداً.")
    val scanFailed = tr("Scanner nicht verfügbar.", "الماسح غير متاح.")
    val added = tr("Zur Bibliothek hinzugefügt.", "أضيف إلى المكتبة.")
    val coverFailed = tr("Bild konnte nicht übernommen werden.", "تعذر حفظ الصورة.")

    fun newBook(isbn: String = "") = Book(id = UUID.randomUUID().toString(), title = "", isbn = isbn, addedAt = System.currentTimeMillis())

    fun scan() {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_EAN_13, Barcode.FORMAT_EAN_8, Barcode.FORMAT_UPC_A)
            .enableAutoZoom()
            .build()
        GmsBarcodeScanning.getClient(context, options).startScan()
            .addOnSuccessListener { barcode ->
                val code = barcode.rawValue.orEmpty()
                scope.launch {
                    val result = model.lookupIsbn(code)
                    if (result.isFailure) { model.say(trScanOffline); return@launch }
                    val found = result.getOrNull()
                    if (found == null) {
                        model.say(notFound)
                        push(Layer.Edit(newBook(code), true))
                    } else {
                        val owned = model.contains(found)
                        if (owned != null) push(Layer.Detail(owned.id))
                        else model.addFound(found, de.mo.bibliothek.data.ReadStatus.UNREAD) { id -> model.say(added); push(Layer.Detail(id)) }
                    }
                }
            }
            .addOnCanceledListener {
                // Behebt Bug #3: Kein Fehler beim Schließen des Scanners
            }
            .addOnFailureListener { e ->
                if (e is MlKitException && e.errorCode == MlKitException.CODE_SCANNER_CANCELLED) {
                    // Abbruch ignorieren
                } else {
                    model.say(scanFailed)
                }
            }
    }

    val bookActions = BookActions(
        open = { push(Layer.Detail(it)) },
        menu = { menuFor = it },
        status = { id, s -> model.setStatus(id, s) },
        coverFile = model::coverFile,
    )
    val addActions = AddActions(
        camera = { push(Layer.Camera) },
        editions = model::editions,
        coverFile = model::coverFile,
        search = { model.search(it) },
        owned = { model.contains(it) },
        add = { found, status -> model.addFound(found, status) { model.say(added) } },
        scan = ::scan,
        manual = { push(Layer.Edit(newBook(), true)) },
        openOwned = { push(Layer.Detail(it)) },
    )

    BackHandler(enabled = layers.isNotEmpty()) { pop() }
    BackHandler(enabled = layers.isEmpty() && tab != Tab.LIBRARY) { tab = Tab.LIBRARY }

    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Scaffold(
            snackbarHost = { SnackbarHost(snackbar) },
            containerColor = MaterialTheme.colorScheme.background,
            bottomBar = {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surfaceContainerLow, tonalElevation = 0.dp) {
                    listOf(
                        Triple(Tab.LIBRARY, tr("Bibliothek", "المكتبة"), Icons.AutoMirrored.Filled.LibraryBooks to Icons.AutoMirrored.Outlined.LibraryBooks),
                        Triple(Tab.ADD, tr("Hinzufügen", "إضافة"), Icons.Filled.AddCircle to Icons.Outlined.AddCircleOutline),
                        Triple(Tab.STATS, tr("Statistik", "الإحصاءات"), Icons.Filled.BarChart to Icons.Outlined.BarChart),
                        Triple(Tab.PROFILE, tr("Profil", "الملف"), Icons.Filled.Person to Icons.Outlined.Person),
                    ).forEach { (t, label, icons) ->
                        NavigationBarItem(
                            selected = tab == t,
                            onClick = { tab = t },
                            icon = { Icon(if (tab == t) icons.first else icons.second, null) },
                            label = { Text(label) },
                            colors = NavigationBarItemDefaults.colors(indicatorColor = MaterialTheme.colorScheme.primaryContainer),
                        )
                    }
                }
            },
        ) { padding ->
            AnimatedContent(tab, transitionSpec = {
                val sign = (if (targetState.ordinal > initialState.ordinal) 1 else -1) * direction
                ((slideInHorizontally(tween(200)) { sign * it / 18 } + fadeIn(tween(180))) togetherWith
                    (slideOutHorizontally(tween(160)) { -sign * it / 18 } + fadeOut(tween(120)))).using(null)
            }, label = "tab") { current ->
                when (current) {
                    Tab.LIBRARY -> LibraryScreen(
                        books, settings.viewMode, settings.sort,
                        onViewMode = { mode -> model.updateSettings { it.copy(viewMode = mode) } },
                        onSort = { mode -> model.updateSettings { it.copy(sort = mode) } },
                        actions = bookActions,
                        onAdd = { tab = Tab.ADD },
                        contentPadding = padding,
                        onAssistant = if (aiEnabled) ({ push(Layer.Assistant) }) else null,
                    )
                    Tab.ADD -> AddScreen(addActions, padding, recentBooks = books)
                    Tab.STATS -> StatsScreen(
                        books, settings.yearGoal,
                        onGoal = { g -> model.updateSettings { it.copy(yearGoal = g) } },
                        onOpen = { push(Layer.Detail(it)) },
                        coverFile = model::coverFile,
                        onRoom = { push(Layer.Room) },
                        contentPadding = padding,
                    )
                    Tab.PROFILE -> ProfileScreen(
                        books, settings.language, settings.yearGoal,
                        onLanguage = { l -> model.updateSettings { it.copy(language = l) } },
                        onGoal = { g -> model.updateSettings { it.copy(yearGoal = g) } },
                        onExport = { exportLauncher.launch("Warraq-Sicherung-" + SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date()) + ".zip") },
                        onImport = { importLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
                        onExportRecovery = if (model.hasRecovery()) ({ recoveryLauncher.launch("Warraq-vor-Wiederherstellung.zip") }) else null,
                        aiEnabled = aiEnabled,
                        onAiKey = model::setAiKey,
                        onAssistant = { push(Layer.Assistant) },
                        contentPadding = padding,
                    )
                }
            }
        }

        val top = layers.lastOrNull()
        AnimatedContent(
            top,
            transitionSpec = { (slideInVertically { it / 12 } + fadeIn()) togetherWith fadeOut() },
            label = "layer",
        ) { layer ->
            when (layer) {
                null -> Unit
                Layer.Room -> Surface(Modifier.fillMaxSize()) { BookRoom(books, settings.yearGoal, model::coverFile, { push(Layer.Detail(it)) }, ::pop) }
                Layer.Assistant -> Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    de.mo.bibliothek.ui.AssistantScreen(books, model::coverFile, de.mo.bibliothek.ui.AssistantActions(
                        oracle = model::aiOracle, tips = model::aiTips, ask = model::aiAsk, genres = model::aiGenres,
                        applyGenres = { model.applyGenres(it) }, year = model::aiYear,
                        open = { push(Layer.Detail(it)) }, search = { push(Layer.Search(it)) }, close = ::pop))
                }
                Layer.Camera -> Surface(Modifier.fillMaxSize()) { CameraScreen(
                    onCaptured = { uri, frame -> layers = layers.dropLast(1) + Layer.Recognition(uri, frame) }, onClose = ::pop) }
                is Layer.Recognition -> Surface(Modifier.fillMaxSize()) { RecognitionScreen(layer.uri, { model.recognizeCover(it, layer.frame) },
                    onSearch = { title, author, hashes -> layers = layers.dropLast(1) + Layer.Search(listOf(title, author).filter { it.isNotBlank() }.joinToString(" "), hashes, title, author) },
                    onBack = { layers = layers.dropLast(1) + Layer.Camera }) }
                // Without an online match, manual entry starts with what the camera read (no cover photo is kept).
                is Layer.Search -> Surface(Modifier.fillMaxSize()) {
                    // The Lens copy of the camera photo lives only as long as this search.
                    DisposableEffect(layer) { onDispose { model.discardLensPhoto() } }
                    AddScreen(addActions.copy(
                        search = { query ->
                            if (de.mo.bibliothek.data.searchKey(query) == de.mo.bibliothek.data.searchKey(layer.query)) model.searchBook(query, layer.title)
                            else model.search(query)
                        },
                        manual = { push(Layer.Edit(newBook().copy(title = layer.title, author = layer.author), true)) }),
                        androidx.compose.foundation.layout.PaddingValues(), initialQuery = layer.query, photoHashes = layer.hashes, onClose = ::pop, searchOnOpen = true,
                        lensPhoto = model.lensPhoto().takeIf { layer.hashes.isNotEmpty() && it.isFile })
                }
                is Layer.Detail -> {
                    val book = books.firstOrNull { it.id == layer.id }
                    if (book == null) LaunchedEffect(layer) { pop() } else Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        DetailScreen(
                            book, books.flatMap { it.lists }.distinct().sorted(), model::coverFile,
                            DetailActions(
                                back = ::pop,
                                edit = { push(Layer.Edit(book, false)) },
                                enrich = { push(Layer.Enrich(book.id)) },
                                delete = { deleteFor = book.id },
                                status = { model.setStatus(book.id, it) },
                                readAt = { model.setReadAt(book.id, it) },
                                rating = { model.setRating(book.id, it) },
                                favorite = { model.toggleFavorite(book.id) },
                                notes = { model.setNotes(book.id, it) },
                                lists = { model.setLists(book.id, it) },
                                fetchKnowledge = model::fetchKnowledge,
                                readingNotes = if (aiEnabled) model::aiReadingNotes else null,
                                askAi = if (aiEnabled) ({ question, arabic -> model.aiAskBook(book, question, arabic) }) else null,
                            ),
                        )
                    }
                }
                is Layer.Edit -> Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    EditorScreen(
                        initial = layer.book, isNew = layer.isNew, coverFile = model::coverFile,
                        importCover = { model.importCover(it) },
                        onCoverFailed = { model.say(coverFailed) },
                        onCancel = ::pop,
                        onSave = { saved ->
                            model.upsert(saved)
                            layers = layers.dropLast(1).let { rest -> if (layer.isNew) rest + Layer.Detail(saved.id) else rest }
                        },
                    )
                }
                is Layer.Enrich -> {
                    val book = books.firstOrNull { it.id == layer.id }
                    Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                        Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
                            AddScreen(
                                addActions, padding,
                                initialQuery = book?.title.orEmpty(),
                                enrichTitle = book?.title.orEmpty(),
                                onEnrich = { found -> reviewFor = layer.id to found; pop() },
                                onClose = ::pop,
                            )
                        }
                    }
                }
            }
        }
    }

    menuFor?.let { id ->
        val book = books.firstOrNull { it.id == id }
        if (book == null) menuFor = null else ModalBottomSheet(onDismissRequest = { menuFor = null }) {
            Text(book.title, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp))
            DropdownMenuItem(
                text = { Text(tr("Bearbeiten", "تعديل")) }, leadingIcon = { Icon(Icons.Outlined.Edit, null) },
                onClick = { menuFor = null; push(Layer.Edit(book, false)) },
                modifier = Modifier.padding(horizontal = 8.dp),
            )
            DropdownMenuItem(
                text = { Text(tr("Löschen", "حذف"), color = MaterialTheme.colorScheme.error) },
                leadingIcon = { Icon(Icons.Outlined.Delete, null, tint = MaterialTheme.colorScheme.error) },
                onClick = { menuFor = null; deleteFor = id },
                modifier = Modifier.padding(horizontal = 8.dp).padding(bottom = 28.dp),
            )
        }
    }

    reviewFor?.let { (id, found) -> books.firstOrNull { it.id == id }?.let { book ->
        MetadataReview(book, found, { fields -> model.applyMatch(id, found, fields); reviewFor = null }, { reviewFor = null })
    } }

    deleteFor?.let { id ->
        val book = books.firstOrNull { it.id == id }
        AlertDialog(
            onDismissRequest = { deleteFor = null },
            shape = RoundedCornerShape(28.dp),
            title = { Text(tr("Buch löschen?", "حذف الكتاب؟")) },
            text = { Text(book?.title.orEmpty()) },
            confirmButton = {
                TextButton(onClick = {
                    deleteFor = null
                    layers = layers.filterNot { (it is Layer.Detail && it.id == id) || (it is Layer.Enrich && it.id == id) }
                    model.delete(id)
                }) { Text(tr("Löschen", "حذف"), color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = { TextButton(onClick = { deleteFor = null }) { Text(tr("Abbrechen", "إلغاء")) } },
        )
    }
}
