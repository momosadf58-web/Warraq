package de.mo.bibliothek

import de.mo.bibliothek.data.Found
import de.mo.bibliothek.data.KnowledgeLookup
import de.mo.bibliothek.data.OcrLine
import de.mo.bibliothek.data.PaddleArabic
import de.mo.bibliothek.data.keyIdea
import de.mo.bibliothek.data.parseCoverLines
import de.mo.bibliothek.data.shortBlurb
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Pure logic of 3.3 that runs without a phone. */
class LogicTest {
    @Test fun matchingAuthorCannotReplaceRecognizedBookTitle() {
        val wrong = Found("أحببت وغدا", "عثمان، عماد رشاد", "", "", "", "", 0, "", "Arabisch", "", "Open Library")
        assertFalse(de.mo.bibliothek.data.BookLookup.matchesExpectedTitle("أبي الذي أكره", wrong))
        assertFalse(de.mo.bibliothek.data.BookLookup.matchesExpectedTitle("أبي الذي أكره", wrong.copy(source = "Google Books")))
        assertTrue(de.mo.bibliothek.data.BookLookup.matchesExpectedTitle("أبي الذي أكره", wrong.copy(title = "أبي الذي أكره: تأملات حول التعافي")))
    }

    @Test fun recognizedTitleDoesNotBreakAuthorSearchOrTitleVariants() {
        val found = Found("The Dictionary of Obscure Sorrows", "John Koenig", "", "", "", "", 0, "", "Englisch", "", "Open Library")
        assertTrue(de.mo.bibliothek.data.BookLookup.matchesExpectedTitle("DICTIONARY OF OBSCURE SORROWS", found))
        assertTrue(de.mo.bibliothek.data.BookLookup.matchesExpectedTitle("", found))
        assertFalse(de.mo.bibliothek.data.BookLookup.matchesExpectedTitle("The Book of John", found))
    }

    @Test fun coverTitleIsLargestTypeNotFirstLine() {
        // Mo's real photo: advert line on top, small "THE", large title, author in a box below.
        val lines = listOf(OcrLine("NEW YORK TIMES BESTSELLER", 40, 20), OcrLine("THE", 120, 18),
            OcrLine("DICTIONARY OF", 150, 40), OcrLine("OBSCURE", 200, 80), OcrLine("SORROWS", 285, 80),
            OcrLine("JOHN KOENIG", 470, 35))
        val parsed = parseCoverLines(lines.joinToString("\n") { it.text }, lines)
        assertEquals("DICTIONARY OF OBSCURE SORROWS", parsed.title)
        assertEquals("JOHN KOENIG", parsed.author)
    }

    @Test fun realS24ReadingOfObscureSorrows() {
        // ML Kit lines from Mo's live camera test on the S24 (3.3): lead-in line above, author box far below.
        val lines = listOf(OcrLine("NEW YORK TIMES BESTSELLER", 30, 30), OcrLine("THE", 330, 32), OcrLine("DICTIONARY OF", 380, 52),
            OcrLine("OBSCURE", 470, 120), OcrLine("SORROWS", 600, 120), OcrLine("JOHN KOENIG", 900, 48))
        val parsed = parseCoverLines("", lines)
        assertEquals("DICTIONARY OF OBSCURE SORROWS", parsed.title)
        assertEquals("JOHN KOENIG", parsed.author)
    }

    @Test fun realS24ArabicCoverKeepsSubtitleOutOfAuthor() {
        // PaddleOCR lines from Mo's live test (3.3.1): author on top, title, two-line subtitle, publisher fragment.
        val lines = listOf(OcrLine("د.عماد رشاد عثمان", 120, 40, 95f), OcrLine("أبي الذي أكره", 300, 110, 97f),
            OcrLine("تأملات حول التعافي من إساءات", 560, 45, 90f), OcrLine("الأيوين وصدماتالنشاة", 612, 45, 85f),
            OcrLine("ار والتوزي", 1450, 25, 60f))
        val parsed = parseCoverLines("", lines)
        assertEquals("أبي الذي أكره", parsed.title)
        assertEquals("عماد رشاد عثمان", parsed.author)
    }

    @Test fun realS24ArabicCoverExactGeometry() {
        // Exact PaddleOCR output of the second live test (text@top/height/confidence from the log).
        val lines = listOf(OcrLine("د.عماد رشاد عثمان", 850, 101, 95f), OcrLine("أبي الذي أكره", 996, 207, 97f),
            OcrLine("تأملات حول التعافي من إساءات", 1211, 89, 98f), OcrLine("بن وصدمات اش", 1266, 142, 72f),
            OcrLine("الرواق للتشر والتوزيع", 1448, 33, 94f))
        val parsed = parseCoverLines("", lines)
        assertEquals("أبي الذي أكره", parsed.title)
        assertEquals("عماد رشاد عثمان", parsed.author)
    }

    @Test fun smallerSubtitleUnderTitleIsNotJoined() {
        // Third live test: subtitle line under the title is about half its size.
        val lines = listOf(OcrLine("د.عماد رشاد عثمان", 830, 90, 97f), OcrLine("أبي الذي أكره", 965, 250, 96f),
            OcrLine("تأملات حول التعافي من إساءات", 1230, 125, 95f))
        val parsed = parseCoverLines("", lines)
        assertEquals("أبي الذي أكره", parsed.title)
        assertEquals("عماد رشاد عثمان", parsed.author)
    }

    @Test fun oneSharedWordIsNoCatalogMatch() {
        fun hindawi(title: String, author: String) = de.mo.bibliothek.data.Found(title, author, "", "", "", "", 0, "", "Arabisch", "", "Hindawi")
        val query = "أبي الذي أكره عماد رشاد عثمان"
        assertFalse(de.mo.bibliothek.data.relevant(query, hindawi("رأي في أبي العلاء: الرجل الذي وجد نفسه", "أمين الخولي")))
        assertTrue(de.mo.bibliothek.data.relevant("ماجدولين مصطفى لطفي المنفلوطي", hindawi("ماجدولين", "مصطفى لطفي المنفلوطي")))
        assertTrue(de.mo.bibliothek.data.relevant("ماجدولين", hindawi("ماجدولين", "مصطفى لطفي المنفلوطي")))
    }

    @Test fun authorAboveTitleIsNotPartOfIt() {
        val lines = listOf(OcrLine("RAY BRADBURY", 40, 60), OcrLine("Fahrenheit 451", 120, 110))
        val parsed = parseCoverLines("", lines)
        assertEquals("Fahrenheit 451", parsed.title)
        assertEquals("RAY BRADBURY", parsed.author)
    }

    @Test fun genreAndPromotionLinesAreNeverTitles() {
        val lines = listOf(OcrLine("Roman", 10, 90), OcrLine("SPIEGEL Bestseller", 120, 60),
            OcrLine("Die Wand", 300, 70), OcrLine("Marlen Haushofer", 420, 30))
        val parsed = parseCoverLines("", lines)
        assertEquals("Die Wand", parsed.title)
        assertEquals("Marlen Haushofer", parsed.author)
    }

    @Test fun markedAuthorLineWins() {
        val lines = listOf(OcrLine("الأيام", 100, 90), OcrLine("تأليف: طه حسين", 400, 30), OcrLine("دار المعارف", 700, 40))
        val parsed = parseCoverLines("", lines)
        assertEquals("الأيام", parsed.title)
        assertEquals("طه حسين", parsed.author)
    }

    @Test fun lowConfidenceGibberishIsIgnored() {
        val lines = listOf(OcrLine("yatagläinll yrábl nábnn", 50, 80, 30f), OcrLine("Fahrenheit 451", 300, 60, 95f))
        assertEquals("Fahrenheit 451", parseCoverLines("", lines).title)
    }

    @Test fun blurbIsShortAndDropsEndingSpoilers() {
        val text = "A fireman burns books in a future city. He meets a curious young woman. " +
            "In the end he joins a group of exiles who memorise books. The novel explores censorship and conformity."
        val blurb = shortBlurb(text)
        assertTrue(blurb.length <= 320)
        assertTrue(blurb.startsWith("A fireman burns books"))
        assertFalse(blurb.contains("In the end"))
    }

    @Test fun overlongFirstSentenceIsCutAtAClause() {
        val text = "سيظل اسم القائد العربي مرتبطًا في الأذهان بأعظم الانتصارات، " + "وتعود أهمية تلك الانتصارات إلى كونها جاءت في خضم صراعات مفصلية ".repeat(6) + "."
        val blurb = shortBlurb(text)
        assertTrue(blurb.length <= 322)
        assertTrue(blurb.endsWith("…"))
    }

    @Test fun keyIdeaIsAThesisSentenceOutsideTheBlurbOrNothing() {
        // Long enough that the short blurb (≈320 chars) ends before the last sentence, as in the real Hindawi text.
        val text = "في هذا الكتاب يأخذنا عالم النفس المعروف في جولة سحرية داخل العقل البشري ويشرح لنا النظامين اللذين يحركان تفكيرنا. " +
            "النظام الأول سريع وحدسي وعاطفي، في حين أن النظام الثاني أبطأ وأكثر تدبرًا وأكثر عقلانية في معالجة المعلومات. " +
            "ويكشف المؤلف عن مواطن قوة كل نظام ومواطن ضعفه وعن التحيزات التي تؤثر في قراراتنا اليومية في العمل والحياة. " +
            "إن هذا الكتاب سيغير نظرتك إلى التفكير تغييرًا جذريًا."
        assertEquals("إن هذا الكتاب سيغير نظرتك إلى التفكير تغييرًا جذريًا.", keyIdea(text))
        // Everything already shown in the short blurb is not repeated as key idea.
        assertNull(keyIdea("Dieses Buch behandelt Erinnerung. Noch ein Satz."))
        // A thesis sentence that gives away the ending is not offered.
        assertNull(keyIdea("Short opening. Another short line. This book explores grief until the hero dies at the end of the war."))
    }

    @Test fun relatedWorkTitlesAreFoundButDifferentWorksAreNot() {
        assertTrue(KnowledgeLookup.titlesRelated("Herz Eines Hundes -- Sataniade", "Hundeherz"))
        assertTrue(KnowledgeLookup.titlesMatch("Herz eines Hundes: Erzählung", "Herz eines Hundes"))
        assertFalse(KnowledgeLookup.titlesRelated("The Trial", "The Castle"))
        assertFalse(KnowledgeLookup.titlesRelated("Fahrenheit 451", "Ray Bradbury"))
        assertFalse(KnowledgeLookup.titlesRelated("Das Mosaik der Frauen", "Rafik Schami"))
    }

    @Test fun authorInAnotherScriptIsConfirmedViaWikidataNames() {
        // Labels of Q5878 as Wikidata returns them; the book stores the Latin name, the article is Arabic.
        val labels = listOf("غابرييل غارثيا ماركيث", "Gabriel García Márquez", "Gabo")
        assertTrue(KnowledgeLookup.authorNamesMatch(labels, "Gabriel García Márquez"))
        assertTrue(KnowledgeLookup.authorNamesMatch(labels, "غابرييل غارثيا ماركيث"))
        assertFalse(KnowledgeLookup.authorNamesMatch(labels, "Michail Bulgakow"))
        assertFalse(KnowledgeLookup.authorNamesMatch(labels, ""))
    }

    @Test fun wordsOfOneLineAreReadTogether() {
        // „حسين“ and the short „طه“ beside it become one line; the title above stays separate.
        val boxes = listOf(PaddleArabic.Box(100, 600, 260, 640), PaddleArabic.Box(280, 605, 330, 640), PaddleArabic.Box(100, 300, 400, 380))
        val merged = PaddleArabic.mergeLines(boxes)
        assertEquals(2, merged.size)
        assertTrue(merged.contains(PaddleArabic.Box(100, 600, 330, 640)))
    }
}
