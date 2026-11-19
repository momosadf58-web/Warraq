package de.mo.bibliothek.data

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.TensorInfo
import java.nio.FloatBuffer
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/** Pixels as Android's Bitmap.getPixels delivers them (ARGB ints); keeps this class free of Android types. */
class Pixels(val width: Int, val height: Int, val argb: IntArray)

/**
 * PaddleOCR PP-OCRv3 (Apache 2.0) for Arabic covers, fully on the phone:
 * DB text detection finds line boxes, the Arabic recogniser reads each box (CTC).
 * Models: assets/paddle, see manifest.json there.
 */
class PaddleArabic(detModel: ByteArray, recModel: ByteArray, dictionary: List<String>) {
    private val env = OrtEnvironment.getEnvironment()
    private val det = env.createSession(detModel, OrtSession.SessionOptions())
    private val rec = env.createSession(recModel, OrtSession.SessionOptions())
    // Index 0 is the CTC blank; PaddleOCR appends a space as last class.
    private val chars = listOf("") + dictionary + " "
    val recShape: LongArray = (rec.inputInfo.values.first().info as TensorInfo).shape
    private val recHeight = recShape.getOrNull(2)?.takeIf { it > 0 }?.toInt() ?: 48

    data class Box(val left: Int, val top: Int, val right: Int, val bottom: Int)

    @Synchronized fun lines(image: Pixels): List<OcrLine> =
        detect(image).sortedBy { it.top }.mapNotNull { box ->
            val (text, confidence) = recognize(image, box) ?: return@mapNotNull null
            if (text.isBlank()) null else OcrLine(text.trim(), box.top, box.bottom - box.top, confidence * 100f)
        }

    /** DB post-processing simplified to axis-aligned boxes; enough for book covers. */
    fun detect(image: Pixels): List<Box> {
        val ratio = min(1f, 960f / max(image.width, image.height))
        val w = max(32, (image.width * ratio / 32f).roundToInt() * 32)
        val h = max(32, (image.height * ratio / 32f).roundToInt() * 32)
        val input = FloatBuffer.allocate(3 * w * h)
        val mean = floatArrayOf(.485f, .456f, .406f); val std = floatArrayOf(.229f, .224f, .225f)
        val plane = w * h
        for (y in 0 until h) for (x in 0 until w) {
            val p = sample(image, x * image.width / w, y * image.height / h)
            // PaddleOCR reads BGR and applies the ImageNet mean/std in that channel order.
            val bgr = intArrayOf(p and 255, (p shr 8) and 255, (p shr 16) and 255)
            for (c in 0..2) input.put(c * plane + y * w + x, (bgr[c] / 255f - mean[c]) / std[c])
        }
        val map = OnnxTensor.createTensor(env, input, longArrayOf(1, 3, h.toLong(), w.toLong())).use { tensor ->
            det.run(mapOf(det.inputNames.first() to tensor)).use { result ->
                val out = result[0].value
                @Suppress("UNCHECKED_CAST")
                (out as Array<Array<Array<FloatArray>>>)[0][0]
            }
        }
        val seen = BooleanArray(w * h); val boxes = mutableListOf<Box>(); val stack = IntArray(w * h)
        for (start in 0 until w * h) {
            if (seen[start] || map[start / w][start % w] < .3f) continue
            var top = 0; var minX = w; var minY = h; var maxX = 0; var maxY = 0; var sum = 0f; var count = 0
            stack[top++] = start; seen[start] = true
            while (top > 0) {
                val i = stack[--top]; val x = i % w; val y = i / w
                sum += map[y][x]; count++
                minX = min(minX, x); maxX = max(maxX, x); minY = min(minY, y); maxY = max(maxY, y)
                for ((dx, dy) in NEIGHBOURS) {
                    val nx = x + dx; val ny = y + dy
                    if (nx in 0 until w && ny in 0 until h) {
                        val n = ny * w + nx
                        if (!seen[n] && map[ny][nx] >= .3f) { seen[n] = true; stack[top++] = n }
                    }
                }
            }
            val bw = maxX - minX + 1; val bh = maxY - minY + 1
            if (bw < 2 || bh < 3 || sum / count < .5f) continue
            // Unclip like PaddleOCR (ratio 1.5): the shrunk text kernel grows back to the full line.
            val grow = (bw * bh * 1.5f / (2f * (bw + bh))).roundToInt()
            val sx = image.width / w.toFloat(); val sy = image.height / h.toFloat()
            boxes += Box(((minX - grow) * sx).toInt().coerceAtLeast(0), ((minY - grow) * sy).toInt().coerceAtLeast(0),
                ((maxX + grow) * sx).toInt().coerceAtMost(image.width), ((maxY + grow) * sy).toInt().coerceAtMost(image.height))
        }
        return mergeLines(boxes)
    }

    /** Arabic word ends (ط, ـه) often reach past the detected kernel: read with three margins, keep the surest. */
    fun recognize(image: Pixels, line: Box): Pair<String, Float>? =
        listOf(0, 8, 4).mapNotNull { div -> recognize(image, line, if (div == 0) 0 else (line.bottom - line.top) / div) }
            .maxByOrNull { it.second }

    private fun recognize(image: Pixels, line: Box, pad: Int): Pair<String, Float>? {
        val box = Box((line.left - pad).coerceAtLeast(0), (line.top - pad / 3).coerceAtLeast(0),
            (line.right + pad).coerceAtMost(image.width), (line.bottom + pad / 3).coerceAtMost(image.height))
        val bw = box.right - box.left; val bh = box.bottom - box.top
        if (bw < 4 || bh < 4) return null
        val w = (bw * recHeight / bh.toFloat()).roundToInt().coerceIn(16, 3200)
        val input = FloatBuffer.allocate(3 * w * recHeight); val plane = w * recHeight
        for (y in 0 until recHeight) for (x in 0 until w) {
            val p = sample(image, box.left + x * bw / w, box.top + y * bh / recHeight)
            val bgr = intArrayOf(p and 255, (p shr 8) and 255, (p shr 16) and 255)
            for (c in 0..2) input.put(c * plane + y * w + x, (bgr[c] / 255f - .5f) / .5f)
        }
        val probs = OnnxTensor.createTensor(env, input, longArrayOf(1, 3, recHeight.toLong(), w.toLong())).use { tensor ->
            rec.run(mapOf(rec.inputNames.first() to tensor)).use { result ->
                @Suppress("UNCHECKED_CAST")
                (result[0].value as Array<Array<FloatArray>>)[0]
            }
        }
        return decode(probs)
    }

    /** Greedy CTC: best class per step, repeats collapsed, blanks dropped. */
    fun decode(steps: Array<FloatArray>): Pair<String, Float> {
        val text = StringBuilder(); val scores = mutableListOf<Float>(); var last = -1
        for (raw in steps) {
            val row = if (raw.any { it < 0f || it > 1f }) softmax(raw) else raw
            var best = 0; for (i in row.indices) if (row[i] > row[best]) best = i
            if (best != 0 && best != last && best < chars.size) { text.append(chars[best]); scores += row[best] }
            last = best
        }
        return arabicOrder(text.toString()) to (if (scores.isEmpty()) 0f else scores.average().toFloat())
    }

    private fun sample(image: Pixels, x: Int, y: Int) =
        image.argb[y.coerceIn(0, image.height - 1) * image.width + x.coerceIn(0, image.width - 1)]

    companion object {
        private val NEIGHBOURS = listOf(1 to 0, -1 to 0, 0 to 1, 0 to -1)
        private val LATIN_RUN = Regex("[a-zA-Z0-9:*./%+-]")

        /** Words of one line (short ones like „طه“ are often separate kernels) are read together, as one line. */
        fun mergeLines(input: List<Box>): List<Box> {
            val boxes = input.sortedBy { it.left }.toMutableList()
            var merged = true
            while (merged) {
                merged = false
                loop@ for (i in boxes.indices) for (j in boxes.indices) {
                    if (i == j) continue
                    val a = boxes[i]; val b = boxes[j]
                    val overlap = min(a.bottom, b.bottom) - max(a.top, b.top)
                    val height = min(a.bottom - a.top, b.bottom - b.top)
                    val gap = max(a.left, b.left) - min(a.right, b.right)
                    if (overlap > height * .5f && gap < max(a.bottom - a.top, b.bottom - b.top) * 1.2f) {
                        boxes[i] = Box(min(a.left, b.left), min(a.top, b.top), max(a.right, b.right), max(a.bottom, b.bottom))
                        boxes.removeAt(j); merged = true; break@loop
                    }
                }
            }
            return boxes
        }

        private fun softmax(row: FloatArray): FloatArray {
            val top = row.max(); val e = FloatArray(row.size) { exp(row[it] - top) }; val s = e.sum()
            return FloatArray(row.size) { e[it] / s }
        }

        /**
         * The Arabic model reads the image left to right; like PaddleOCR's pred_reverse this restores logical order,
         * keeping Latin/digit runs intact. Unlike pred_reverse, spaces are separators, not part of a Latin run.
         */
        fun arabicOrder(visual: String): String {
            val parts = mutableListOf<String>(); var run = StringBuilder()
            for (ch in visual) {
                if (!LATIN_RUN.matches(ch.toString())) {
                    if (run.isNotEmpty()) { parts += run.toString(); run = StringBuilder() }
                    parts += ch.toString()
                } else run.append(ch)
            }
            if (run.isNotEmpty()) parts += run.toString()
            return parts.asReversed().joinToString("")
        }
    }
}
