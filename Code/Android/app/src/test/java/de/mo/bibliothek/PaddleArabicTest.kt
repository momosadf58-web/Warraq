package de.mo.bibliothek

import de.mo.bibliothek.data.PaddleArabic
import de.mo.bibliothek.data.Pixels
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.DataInputStream
import java.io.File

/**
 * Runs the real PaddleOCR models on the PC. Images are prepared as .argb files (int width, int height, ARGB ints,
 * big endian) by Werkzeuge/OCR-Testbilder.ps1; pass the folder with -PwaraqOcrImages=<folder>.
 */
class PaddleArabicTest {
    private val assets = File(System.getProperty("waraq.assets"))
    private val ocr by lazy {
        PaddleArabic(File(assets, "paddle/det.onnx").readBytes(), File(assets, "paddle/arabic_rec.onnx").readBytes(),
            File(assets, "paddle/arabic_dict.txt").readLines(Charsets.UTF_8).filter { it.isNotEmpty() })
    }

    @Test fun logicalOrderKeepsLatinRuns() {
        assertEquals("ماجدولين 451", PaddleArabic.arabicOrder("451 نيلودجام"))
    }

    @Test fun preparedImages() {
        val folder = System.getProperty("waraq.ocr.images").orEmpty()
        if (folder.isBlank()) return
        val report = File(folder, "ergebnis.txt").apply { writeText("rec input ${ocr.recShape.toList()}\n", Charsets.UTF_8) }
        File(folder).listFiles { f -> f.extension == "argb" }.orEmpty().sorted().forEach { file ->
            val image = DataInputStream(file.inputStream().buffered()).use { input ->
                val w = input.readInt(); val h = input.readInt()
                Pixels(w, h, IntArray(w * h) { input.readInt() })
            }
            val start = System.currentTimeMillis()
            val lines = ocr.lines(image)
            report.appendText("${file.nameWithoutExtension} ${image.width}x${image.height} (${System.currentTimeMillis() - start} ms):\n" +
                lines.joinToString("") { "  ${it.text} [${it.confidence.toInt()}] top=${it.top} h=${it.height}\n" }, Charsets.UTF_8)
        }
    }
}
