package de.mo.bibliothek

import de.mo.bibliothek.data.*
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test

class BookNotesTest {
    private val quote=BookQuote("Ein vorhandenes Zitat über die Freiheit des Menschen.",WebSource("Wikiquote","https://de.wikiquote.org/wiki/Werk"))

    @Test fun modelCannotSupplyQuotationTextOrSource() {
        val json=JSONObject().put("blurb","Das Buch behandelt Freiheit.").put("quoteIndex",0)
            .put("explanation","Der Satz betont die Selbstbestimmung.").put("quote","Erfunden")
            .put("source","https://untrusted.example")
        val result=WarraqAi.parseBookNotes(json,"Eine Beschreibung über Freiheit.",listOf(quote))
        assertSame(quote,result.quote)
        assertEquals(quote.source,result.quote!!.source)
    }

    @Test fun missingDescriptionCannotProduceBlurbOrIdea() {
        val result=WarraqAi.parseBookNotes(JSONObject().put("blurb","Erfunden").put("idea","Erfunden")
            .put("quoteIndex",0).put("explanation","Eine Erklärung."),"",listOf(quote))
        assertEquals("",result.blurb);assertEquals("",result.idea)
        assertSame(quote,result.quote)
    }

    @Test fun invalidQuoteIndexCannotProduceExplanation() {
        for(index in listOf(-1,42)) {
            val result=WarraqAi.parseBookNotes(JSONObject().put("idea","Das Thema ist Selbstbestimmung.")
                .put("quoteIndex",index).put("explanation","Erfundener Kontext"),"Vorhandener Quellentext",listOf(quote))
            assertNull(result.quote);assertEquals("",result.explanation)
        }
    }

    @Test fun paraphrasesAndUnlinkedQuotesCannotBecomeOriginalQuotes() {
        val supplied=listOf(quote.copy(literal=false),quote.copy(source=WebSource("Keine Quelle","")))
        val result=WarraqAi.parseBookNotes(JSONObject().put("idea","Eine sinngemäße Aussage.")
            .put("quoteIndex",0).put("explanation","Kein Beleg"),"Quellentext",supplied)
        assertNull(result.quote);assertEquals("",result.explanation)
    }

    @Test fun readingPromptExcludesPrivateNotesAndLibraryTaste() {
        val book=Book("private-id","Titel","Autor",notes="PRIVATE_NOTE",rating=5,lists=listOf("PRIVATE_LIST"))
        val prompt=WarraqAi.readingPrompt(book,"QUELLENTEXT",listOf(quote),true)
        assertTrue(prompt.contains("QUELLENTEXT"));assertTrue(prompt.contains(quote.text))
        assertTrue(prompt.contains("Antworte auf Arabisch"))
        assertFalse(prompt.contains("PRIVATE_NOTE"));assertFalse(prompt.contains("PRIVATE_LIST"))
        assertFalse(prompt.contains("private-id"));assertFalse(prompt.contains("5/5"))
    }
}
