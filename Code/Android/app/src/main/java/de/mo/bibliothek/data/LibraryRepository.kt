package de.mo.bibliothek.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.util.AtomicFile
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.util.Locale
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

/**
 * Lokale Bibliothek in files/library.json (gleiches Format und gleicher Ort wie 2.x).
 * Cover liegen als bereinigte JPEG-Kopien in files/covers. Nichts verlässt das Gerät außer Coverabrufen.
 */
class LibraryRepository(context: Context) {
    private val app = context.applicationContext
    private val covers = File(app.filesDir, "covers").apply { mkdirs() }
    private val legacyRemoteCovers = File(app.filesDir, "remote-covers")
    private val database = AtomicFile(File(app.filesDir, "library.json"))

    @Synchronized
    fun load(): List<Book> {
        val base = database.baseFile
        if (!base.exists() && !File("$base.bak").exists()) return emptyList()
        val text = database.openRead().use { String(readLimited(it, MAX_JSON), StandardCharsets.UTF_8) }
        val array = JSONArray(text)
        return (0 until array.length()).map { Book.fromJson(array.getJSONObject(it)) }
    }

    @Synchronized
    fun save(books: List<Book>) {
        val array = JSONArray()
        books.forEach { array.put(it.toJson()) }
        val bytes = array.toString().toByteArray(StandardCharsets.UTF_8)
        if (bytes.size > MAX_JSON) throw IOException("Die Bibliothek ist zu groß.")
        var output: FileOutputStream? = null
        try {
            output = database.startWrite()
            output.write(bytes)
            database.finishWrite(output)
        } catch (error: Exception) {
            output?.let { database.failWrite(it) }
            throw error
        }
    }

    /**
     * 2.x hielt geladene Online-Cover nur im Cache. Für 3.0 werden vorhandene Cachebilder einmalig
     * als lokale Cover übernommen, damit die Bibliothek offline vollständig bleibt.
     */
    fun adoptLegacyCovers(books: List<Book>): List<Book>? {
        if (!legacyRemoteCovers.isDirectory) return null
        var changed = false
        val result = books.map { book ->
            if (book.cover.isNotEmpty() || book.coverUrl.isEmpty()) return@map book
            val cached = File(legacyRemoteCovers, sha256(book.coverUrl) + ".jpg")
            if (!validCover(cached)) return@map book
            val name = UUID.randomUUID().toString() + ".jpg"
            cached.copyTo(coverFile(name))
            changed = true
            book.copy(cover = name)
        }
        return if (changed) result else null
    }

    fun coverFile(name: String): File {
        if (!name.matches(COVER_NAME)) throw IOException("Ungültiger Bildname.")
        return File(covers, name)
    }

    fun importCover(uri: Uri): String {
        val original = File.createTempFile("cover-", ".tmp", app.cacheDir)
        try {
            requireInput(uri).use { input -> FileOutputStream(original).use { copyLimited(input, it, 30L * 1024 * 1024) } }
            val name = UUID.randomUUID().toString() + ".jpg"
            normalizeCover(original, coverFile(name))
            return name
        } finally {
            original.delete()
        }
    }

    /** Lädt ein Online-Cover herunter und legt es als lokale Kopie ab. */
    fun downloadCover(url: String): String {
        val original = File.createTempFile("download-", ".tmp", app.cacheDir)
        try {
            var current = allowedCoverUrl(url)
            for (hop in 0..5) {
                val connection = current.openConnection() as HttpURLConnection
                connection.instanceFollowRedirects = false
                connection.connectTimeout = 7000
                connection.readTimeout = 10000
                connection.setRequestProperty("Accept", "image/*")
                connection.setRequestProperty("User-Agent", USER_AGENT)
                try {
                    val status = connection.responseCode
                    if (status in 300..399) {
                        val location = connection.getHeaderField("Location") ?: throw IOException("Cover nicht verfügbar.")
                        current = allowedCoverUrl(URL(current, location).toString())
                        continue
                    }
                    if (status != HttpURLConnection.HTTP_OK) throw IOException("Cover nicht verfügbar.")
                    connection.inputStream.use { input -> FileOutputStream(original).use { copyLimited(input, it, 10L * 1024 * 1024) } }
                    val name = UUID.randomUUID().toString() + ".jpg"
                    if (current.host == "downloads.hindawi.org" && current.path.endsWith(".svg")) {
                        val svg = original.inputStream().use { com.caverock.androidsvg.SVG.getFromInputStream(it) }
                        val bitmap = Bitmap.createBitmap(540, 720, Bitmap.Config.ARGB_8888)
                        try {
                            val canvas = Canvas(bitmap); canvas.drawColor(Color.WHITE)
                            svg.setDocumentWidth(540f); svg.setDocumentHeight(720f); svg.renderToCanvas(canvas)
                            FileOutputStream(original).use { bitmap.compress(Bitmap.CompressFormat.JPEG, 90, it) }
                        } finally { bitmap.recycle() }
                    }
                    normalizeCover(original, coverFile(name))
                    return name
                } finally {
                    connection.disconnect()
                }
            }
            throw IOException("Zu viele Weiterleitungen.")
        } finally {
            original.delete()
        }
    }

    fun deleteUnusedCovers(books: List<Book>) {
        val used = books.mapTo(HashSet()) { it.cover }
        // Frische Dateien können zu einem noch offenen Editor gehören.
        val cutoff = System.currentTimeMillis() - 6 * 60 * 60 * 1000L
        covers.listFiles()?.forEach { file ->
            if (file.name.matches(COVER_NAME) && file.name !in used && file.lastModified() < cutoff) file.delete()
        }
    }

    // ---------- Sicherung (Format "mos-buecher", lesbar für Version 1 und 2) ----------

    @Synchronized
    fun exportBackup(uri: Uri, books: List<Book>) {
        val coverFiles = books.map { it.cover }.filter { it.isNotEmpty() }.distinct()
            .associateWith(::coverFile).filterValues { it.isFile }
        val manifest = JSONObject().put("format", "mos-buecher").put("version", 2)
            .put("books", JSONArray().also { array ->
                books.forEach { book ->
                    val json = book.toJson()
                    if (book.cover !in coverFiles) json.put("cover", "")
                    array.put(json)
                }
            })
        val temporary = File.createTempFile("sicherung-", ".zip", app.cacheDir)
        try {
            ZipOutputStream(FileOutputStream(temporary).buffered()).use { zip ->
                zip.putNextEntry(ZipEntry("manifest.json"))
                zip.write(manifest.toString().toByteArray(StandardCharsets.UTF_8))
                zip.closeEntry()
                coverFiles.forEach { (name, file) ->
                    zip.putNextEntry(ZipEntry("covers/$name"))
                    FileInputStream(file).use { it.copyTo(zip) }
                    zip.closeEntry()
                }
            }
            val output = app.contentResolver.openOutputStream(uri, "wt") ?: throw IOException("Die Sicherung konnte nicht erstellt werden.")
            output.use { out -> FileInputStream(temporary).use { it.copyTo(out) } }
        } finally {
            temporary.delete()
        }
    }

    fun hasRecovery(): Boolean = File(app.filesDir, "pre-restore.zip").isFile

    @Synchronized
    fun exportRecovery(uri: Uri) {
        val file = File(app.filesDir, "pre-restore.zip")
        if (!file.isFile) throw IOException("Noch kein Wiederherstellungsstand vorhanden.")
        val output = app.contentResolver.openOutputStream(uri, "wt") ?: throw IOException("Sicherung nicht gespeichert.")
        output.use { out -> file.inputStream().use { it.copyTo(out) } }
    }

    /** Ersetzt die Bibliothek erst, wenn die gesamte Sicherung geprüft ist. */
    @Synchronized
    fun importBackup(uri: Uri): List<Book> {
        val staging = File(app.cacheDir, "restore-" + UUID.randomUUID()).apply { mkdirs() }
        val installed = mutableListOf<File>()
        var committed = false
        try {
            var manifest: ByteArray? = null
            val images = HashMap<String, File>()
            var total = 0L
            ZipInputStream(requireInput(uri).buffered()).use { zip ->
                val seen = HashSet<String>()
                while (true) {
                    val entry = zip.nextEntry ?: break
                    val name = entry.name
                    if (!seen.add(name) || entry.isDirectory || seen.size > MAX_BOOKS + 1) throw IOException("Ungültige Sicherung.")
                    when {
                        name == "manifest.json" -> manifest = readLimited(zip, MAX_JSON).also { total += it.size }
                        name.startsWith("covers/") && name.substring(7).matches(COVER_NAME) -> {
                            val image = File(staging, UUID.randomUUID().toString() + ".jpg")
                            FileOutputStream(image).use { total += copyLimited(zip, it, MAX_COVER.toLong()) }
                            if (!validCover(image)) throw IOException("Die Sicherung enthält ein beschädigtes Cover.")
                            images[name.substring(7)] = image
                        }
                        else -> throw IOException("Die Datei ist keine bookie-Sicherung.")
                    }
                    if (total > MAX_ARCHIVE) throw IOException("Die Sicherung ist zu groß.")
                }
            }
            val root = JSONObject(String(manifest ?: throw IOException("Die Buchliste fehlt."), StandardCharsets.UTF_8))
            if (root.optString("format") != "mos-buecher" || root.optInt("version") !in 1..2)
                throw IOException("Dieses Sicherungsformat wird nicht unterstützt.")
            val array = root.getJSONArray("books")
            if (array.length() > MAX_BOOKS) throw IOException("Zu viele Bücher.")
            val ids = HashSet<String>()
            val renamed = HashMap<String, String>()
            val restored = (0 until array.length()).map { index ->
                var book = Book.fromJson(array.getJSONObject(index))
                if (!book.id.matches(ID) || !ids.add(book.id) || book.title.isBlank()) throw IOException("Ungültiger Bucheintrag.")
                if (book.cover.isNotEmpty()) {
                    val image = images[book.cover] ?: throw IOException("Ein Cover fehlt. Die Bibliothek bleibt unverändert.")
                    val target = renamed.getOrPut(book.cover) {
                        val name = UUID.randomUUID().toString() + ".jpg"
                        image.copyTo(coverFile(name)).also { installed += it }
                        name
                    }
                    book = book.copy(cover = target)
                }
                book
            }
            // Create a self-contained recovery point only after the incoming archive passed validation.
            exportBackup(Uri.fromFile(File(app.filesDir, "pre-restore.zip")), load())
            save(restored)
            committed = true
            return restored
        } finally {
            if (!committed) installed.forEach { it.delete() }
            staging.deleteRecursively()
        }
    }

    private fun requireInput(uri: Uri): InputStream =
        app.contentResolver.openInputStream(uri) ?: throw IOException("Die Datei konnte nicht geöffnet werden.")

    companion object {
        const val USER_AGENT = "Warraq/3.3 (private personal library; Android)"
        private const val MAX_BOOKS = 5000
        private const val MAX_JSON = 8 * 1024 * 1024
        private const val MAX_COVER = 5 * 1024 * 1024
        private const val MAX_ARCHIVE = 200L * 1024 * 1024
        private val COVER_NAME = Regex("[a-f0-9-]{36}\\.jpg")
        private val ID = Regex("[a-f0-9-]{36}")
        private val COVER_HOSTS = Regex(
            "covers\\.openlibrary\\.org|archive\\.org|ia[0-9]+\\.(us|eu)\\.archive\\.org|" +
                "books\\.google\\.com|books\\.googleusercontent\\.com|[a-z0-9-]+\\.googleusercontent\\.com|downloads\\.hindawi\\.org|" +
                "alrewaqpublishing\\.com|d11v1jq8owgw3u\\.cloudfront\\.net"
        )

        fun allowedCoverUrl(value: String): URL {
            val uri = URI(value)
            val host = uri.host?.lowercase(Locale.ROOT)
            if (!"https".equals(uri.scheme, true) || host == null || uri.rawUserInfo != null || uri.port != -1 ||
                !host.matches(COVER_HOSTS)
            ) throw IOException("Nicht erlaubte Coverquelle.")
            return uri.toURL()
        }

        fun sha256(value: String): String =
            MessageDigest.getInstance("SHA-256").digest(value.toByteArray(StandardCharsets.UTF_8))
                .joinToString("") { "%02x".format(Locale.ROOT, it.toInt() and 255) }

        fun validCover(file: File): Boolean {
            if (!file.isFile || file.length() == 0L || file.length() > MAX_COVER) return false
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(file.path, bounds)
            return bounds.outWidth in 1..2000 && bounds.outHeight in 1..2000
        }

        private fun normalizeCover(original: File, target: File) {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            BitmapFactory.decodeFile(original.path, bounds)
            if (bounds.outWidth !in 1..50000 || bounds.outHeight !in 1..50000) throw IOException("Das Bild konnte nicht geöffnet werden.")
            var sample = 1
            while (maxOf(bounds.outWidth, bounds.outHeight) / sample > 2400) sample *= 2
            var bitmap = BitmapFactory.decodeFile(original.path, BitmapFactory.Options().apply { inSampleSize = sample })
                ?: throw IOException("Das Bild konnte nicht geöffnet werden.")
            try {
                val transform = Matrix()
                runCatching {
                    when (ExifInterface(original.path).getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)) {
                        2 -> transform.setScale(-1f, 1f)
                        3 -> transform.setRotate(180f)
                        4 -> transform.setScale(1f, -1f)
                        5 -> { transform.setRotate(90f); transform.postScale(-1f, 1f) }
                        6 -> transform.setRotate(90f)
                        7 -> { transform.setRotate(270f); transform.postScale(-1f, 1f) }
                        8 -> transform.setRotate(270f)
                    }
                }
                val scale = minOf(1f, 1400f / maxOf(bitmap.width, bitmap.height))
                transform.postScale(scale, scale)
                val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, transform, true)
                if (rotated !== bitmap) { bitmap.recycle(); bitmap = rotated }
                val flat = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                Canvas(flat).apply { drawColor(Color.WHITE); drawBitmap(bitmap, 0f, 0f, null) }
                bitmap.recycle(); bitmap = flat
                val destination = AtomicFile(target)
                val output = destination.startWrite()
                try {
                    if (!bitmap.compress(Bitmap.CompressFormat.JPEG, 90, output)) throw IOException("Bild konnte nicht gespeichert werden.")
                    destination.finishWrite(output)
                } catch (error: Exception) {
                    destination.failWrite(output)
                    throw error
                }
            } finally {
                bitmap.recycle()
            }
        }

        private fun readLimited(input: InputStream, limit: Int): ByteArray =
            ByteArrayOutputStream().also { copyLimited(input, it, limit.toLong()) }.toByteArray()

        private fun copyLimited(input: InputStream, output: OutputStream, limit: Long): Long {
            val buffer = ByteArray(32768)
            var count = 0L
            while (true) {
                val read = input.read(buffer)
                if (read == -1) break
                count += read
                if (count > limit) throw IOException("Die Datei ist zu groß.")
                output.write(buffer, 0, read)
            }
            return count
        }
    }
}
