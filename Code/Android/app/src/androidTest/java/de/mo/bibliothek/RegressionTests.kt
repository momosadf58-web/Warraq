package de.mo.bibliothek

import android.content.Context
import android.content.ContextWrapper
import android.graphics.BitmapFactory
import android.net.Uri
import android.test.InstrumentationTestCase
import de.mo.bibliothek.data.*
import de.mo.bibliothek.ui.createQuoteCard
import org.json.JSONObject
import java.io.File
import java.util.Calendar
import java.util.UUID
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

@Suppress("DEPRECATION")
class RegressionTests: InstrumentationTestCase() {
    private lateinit var isolated: Context
    private lateinit var root: File
    override fun setUp() {
        super.setUp()
        root=File(instrumentation.targetContext.cacheDir,"regression-"+UUID.randomUUID()).apply{check(mkdirs())}
        isolated=object:ContextWrapper(instrumentation.targetContext) {
            override fun getApplicationContext():Context=this
            override fun getFilesDir()=File(root,"files").apply{mkdirs()}
            override fun getCacheDir()=File(root,"cache").apply{mkdirs()}
        }
    }
    override fun tearDown(){root.deleteRecursively();super.tearDown()}
    private fun book(title:String="Test",genre:String="")=Book(UUID.randomUUID().toString(),title,genre=genre)
    private fun date(year:Int)=Calendar.getInstance().apply{set(year,Calendar.JUNE,15,12,0,0)}.timeInMillis

    fun testOnlyDatedCompletedBooksCountForYear() {
        val current=book().withStatus(ReadStatus.READ,date(2026))
        val old=book().withStatus(ReadStatus.READ,date(2025))
        val undated=book().copy(status=ReadStatus.READ.stored)
        val reading=book().copy(status=ReadStatus.READING.stored,readAt=date(2026))
        assertEquals(listOf(current),booksReadInYear(listOf(current,old,undated,reading),2026))
        assertTrue(booksReadInYear(listOf(old,undated),2026).isEmpty())
        assertNull(readingYear(0))
    }
    fun testGenreDoesNotInventNovelFromMissingMetadata() {
        assertEquals(BookKind.UNKNOWN,book("Geschichte eines Romans").kind())
        assertEquals(BookKind.NONFICTION,book(genre="Nonfiction / Psychology").kind())
        assertEquals(BookKind.NOVEL,book(genre="Roman").kind())
        assertEquals(BookKind.NOVEL,book(genre="رواية").kind())
        assertEquals(BookKind.OTHER,book(genre="Poesie").kind())
    }
    fun testPublicationYearIsIndependentOfReadingYear() {
        val b=book().copy(published="1953-10-19",readAt=date(2026),status=ReadStatus.READ.stored)
        assertEquals(1953,b.publicationYear())
        assertEquals(2026,readingYear(b.readAt))
        assertNull(book().publicationYear())
    }
    fun testOracleNeverSelectsReadingOrCompletedBooks() {
        val unread=book();val reading=book().withStatus(ReadStatus.READING);val read=book().withStatus(ReadStatus.READ)
        assertEquals(listOf(unread),oracleCandidates(listOf(read,unread,reading)))
        assertTrue(oracleCandidates(listOf(read,reading)).isEmpty())
    }
    fun testUnknownFieldsAndOldStatusSurviveRoundTrip() {
        val json=JSONObject().put("id",UUID.randomUUID().toString()).put("title","كتاب")
            .put("status","completed").put("custom",JSONObject().put("preserve",true))
        val parsed=Book.fromJson(json)
        assertEquals(ReadStatus.READ,parsed.readStatus)
        assertTrue(parsed.toJson().getJSONObject("custom").getBoolean("preserve"))
    }
    fun testArticleMatchingRejectsAuthorBiographyAndDifferentWorks() {
        assertTrue(KnowledgeLookup.titlesMatch("Fahrenheit 451","Fahrenheit 451 (Roman)"))
        assertFalse(KnowledgeLookup.titlesMatch("Fahrenheit 451","Ray Bradbury"))
        assertFalse(KnowledgeLookup.titlesMatch("The Trial","The Castle"))
        assertTrue(KnowledgeLookup.isBookArticle(book().copy(author="Ray Bradbury"),"A novel by Ray Bradbury."))
        assertFalse(KnowledgeLookup.isBookArticle(book().copy(author="Ray Bradbury"),"A book by Franz Kafka."))
    }
    fun testQuotesAreSourceLinkedAndNotArticleSentences() {
        val source=WebSource("Wikiquote","https://en.wikiquote.org/wiki/Fixture")
        val html="<ul><li><span class=\"tocnumber\">1</span><span class=\"toctext\">A chapter heading that must not be treated as a quotation</span></li></ul>"+
            "<ul><li>A fictional quotation long enough for a deterministic parser test.<ul><li>Chapter 1</li></ul></li></ul>"+
            "<h2>About the work</h2><ul><li>This commentary about the work must never be returned as a quotation.</li></ul>"
        val quotes=KnowledgeLookup.parseQuotes(html,source)
        assertEquals(1,quotes.size)
        assertEquals(source,quotes[0].source)
        assertFalse(quotes[0].text.contains("Chapter"))
        assertTrue(KnowledgeLookup.parseQuotes("<p>A Wikipedia summary is not a book quotation.</p>",source).isEmpty())
    }
    fun testBackupRoundTripKeepsPreviousLibraryRecoverable() {
        val repo=LibraryRepository(isolated)
        val old=book("Before").copy(extra="{\"custom\":42}")
        repo.save(listOf(old))
        val zip=File(root,"backup.zip");repo.exportBackup(Uri.fromFile(zip),listOf(old))
        val current=book("Current");repo.save(listOf(current))
        val restored=repo.importBackup(Uri.fromFile(zip))
        assertEquals(old.id,restored.single().id)
        val safety=File(isolated.filesDir,"pre-restore.zip")
        assertTrue(safety.isFile)
        val previous=repo.importBackup(Uri.fromFile(safety))
        assertEquals(current.id,previous.single().id)
    }
    fun testRejectedBackupKeepsUserDataAndRejectsTraversal() {
        val repo=LibraryRepository(isolated);val original=book("Untouched");repo.save(listOf(original))
        val file=File(root,"bad.zip")
        ZipOutputStream(file.outputStream()).use {zip->zip.putNextEntry(ZipEntry("../library.json"));zip.write("[]".toByteArray());zip.closeEntry()}
        assertTrue(runCatching{repo.importBackup(Uri.fromFile(file))}.isFailure)
        assertEquals(original.id,repo.load().single().id)
        assertFalse(File(isolated.filesDir,"pre-restore.zip").exists())
    }
    fun testArabicQuoteCardIsAValidShareablePng() {
        val b=book("كتاب تجريبي").copy(author="مؤلف تجريبي")
        val file=createQuoteCard(isolated,b,BookQuote("نص تجريبي لا يُنسب إلى كتاب حقيقي أو شخص حقيقي.",WebSource("Test","https://example.org")),null)
        val bounds=BitmapFactory.Options().apply{inJustDecodeBounds=true};BitmapFactory.decodeFile(file.path,bounds)
        assertEquals(1080,bounds.outWidth);assertEquals(1350,bounds.outHeight)
        assertTrue(file.canonicalPath.startsWith(File(isolated.cacheDir,"share").canonicalPath))
    }
    fun testLanguageCodesAreReadable() {assertEquals("Portugiesisch",languageName("por"));assertEquals("Arabisch",languageName("ara"))}
}
