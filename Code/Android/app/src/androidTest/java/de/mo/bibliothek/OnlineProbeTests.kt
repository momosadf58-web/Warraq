package de.mo.bibliothek

import android.test.InstrumentationTestCase
import de.mo.bibliothek.data.*
import de.mo.bibliothek.ui.createQuoteCard
import java.io.File

/** Separately invoked integration probe, excluded from deterministic regression checks. */
@Suppress("DEPRECATION")
class OnlineProbeTests: InstrumentationTestCase() {
    fun testLiveSourcesForKnownBook() {
        val book=Book("probe","Fahrenheit 451",author="Ray Bradbury")
        val result=KnowledgeLookup.fetch(book)
        assertTrue("No summary from the live source",result.summary.length>100)
        assertNotNull(result.source)
        assertTrue("No linked book quotes from Wikiquote",result.quotes.isNotEmpty())
        assertTrue(result.quotes.all{it.source.url.startsWith("https://")&&it.source.url.contains("wikiquote.org")})
        val context=instrumentation.targetContext
        val png=createQuoteCard(context,book,result.quotes.first(),null)
        png.copyTo(File(context.cacheDir,"live-quote-card.png"),overwrite=true)
        File(context.cacheDir,"live-result.txt").writeText("summary=${result.summary.length}; quotes=${result.quotes.size}; source=${result.source?.url}")
    }
}
