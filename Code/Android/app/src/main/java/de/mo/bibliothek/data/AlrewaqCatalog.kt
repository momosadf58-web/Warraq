package de.mo.bibliothek.data

import org.json.JSONArray
import org.json.JSONObject
import java.net.URI
import java.net.URLEncoder

/** Public product metadata from the publisher; a product is one edition, not a global work record. */
internal object AlrewaqCatalog {
    private const val HOST = "alrewaqpublishing.com"
    private val imageHosts = setOf(HOST, "d11v1jq8owgw3u.cloudfront.net")
    private val arabicLetter = Regex("[\\u0621-\\u064A\\u066E-\\u06D3]")

    fun search(query: String, fetch: (String) -> String): List<Found> {
        val title = query.trim()
        if (!arabicLetter.containsMatchIn(title)) return emptyList()
        // The publisher does not match hamza-normalized text or title + author reliably.
        val encoded = URLEncoder.encode(title, "UTF-8")
        return parse(fetch("https://$HOST/wp-json/wc/store/v1/products?search=$encoded&per_page=20"))
    }

    internal fun parse(json: String): List<Found> {
        val products = JSONArray(json)
        return (0 until minOf(products.length(), 20)).mapNotNull { index ->
            val product = products.optJSONObject(index) ?: return@mapNotNull null
            val id = product.optLong("id")
            val title = product.text("name")
            val sourceUrl = safeUrl(product.optString("permalink"), setOf(HOST))
            if (id <= 0 || title.isBlank() || sourceUrl.isEmpty()) return@mapNotNull null

            val details = product.text("short_description")
            val attributes = product.optJSONArray("attributes") ?: JSONArray()
            val writers = (0 until attributes.length()).mapNotNull { attributes.optJSONObject(it) }
                .filter { it.optString("taxonomy") == "pa_writer" }
                .flatMap { attribute ->
                    val terms = attribute.optJSONArray("terms") ?: JSONArray()
                    (0 until terms.length()).mapNotNull { terms.optJSONObject(it)?.text("name") }
                }.filter { it.isNotBlank() }.distinct()
            val author = writers.joinToString(", ").ifBlank { field(details, "الكاتب|المؤلف") }
            val isbn = field(details, "ISBN(?:-1[03])?").mapNotNull { ch ->
                when {
                    ch == 'x' || ch == 'X' -> 'X'
                    ch.isDigit() -> Character.digit(ch, 10).takeIf { it >= 0 }?.let { '0' + it }
                    else -> null
                }
            }.joinToString("").takeIf { it.matches(Regex("(?:[0-9]{13}|[0-9]{9}[0-9X])")) }.orEmpty()
            val year = field(details, "سنة النشر|عام النشر").map { ch ->
                if (ch.isDigit()) Character.digit(ch, 10).takeIf { it >= 0 }?.let { '0' + it } ?: ch else ch
            }.joinToString("").let { Regex("(?<![0-9])[0-9]{4}(?![0-9])").find(it)?.value.orEmpty() }
            val images = product.optJSONArray("images")
            val cover = safeUrl(images?.optJSONObject(0)?.optString("src").orEmpty(), imageHosts)
            val genre = product.optJSONArray("categories")?.optJSONObject(0)?.text("name").orEmpty()

            Found(
                title = title, author = author, isbn = isbn, coverUrl = cover,
                publisher = "الرواق للنشر والتوزيع", published = year, pages = 0,
                genre = genre, language = "", description = product.text("description"),
                source = "Alrewaq", sourceUrl = sourceUrl, workKey = "alrewaq:$id",
            )
        }
    }

    private fun JSONObject.text(key: String): String =
        if (isNull(key)) "" else plainText(optString(key))

    private fun field(text: String, label: String): String =
        Regex("(?im)^\\s*(?:$label)\\s*[:：]\\s*([^\\n]*)").find(text)?.groupValues?.get(1)?.trim().orEmpty()

    private fun safeUrl(value: String, hosts: Set<String>): String = runCatching {
        val uri = URI(value.trim())
        if (uri.scheme.equals("https", ignoreCase = true) && uri.host?.lowercase()?.let(hosts::contains) == true &&
            uri.rawUserInfo == null && uri.port in setOf(-1, 443)) uri.toASCIIString() else ""
    }.getOrDefault("")

    /** Produces display text only: no WebView, HTML execution or resource loading. */
    private fun plainText(html: String): String {
        val withoutTags = html.replace(Regex("(?is)<(script|style)\\b[^>]*>.*?</\\1\\s*>"), "")
            .replace(Regex("(?i)<br\\b[^>]*>|</(?:p|div|li|h[1-6])\\s*>"), "\n")
            .replace(Regex("<[^>]*>"), " ")
        return Regex("&(#x[0-9a-fA-F]+|#[0-9]+|[a-zA-Z]+);").replace(withoutTags) { match ->
            val entity = match.groupValues[1]
            val code = when {
                entity.startsWith("#x") -> entity.drop(2).toIntOrNull(16)
                entity.startsWith('#') -> entity.drop(1).toIntOrNull()
                else -> null
            }
            if (code != null && Character.isValidCodePoint(code) && code !in 0xD800..0xDFFF && code > 0) {
                String(Character.toChars(code))
            } else when (entity) {
                "amp" -> "&"; "lt" -> "<"; "gt" -> ">"; "quot" -> "\""; "apos", "rsquo", "lsquo" -> "'"
                "nbsp" -> " "; "ndash" -> "–"; "mdash" -> "—"; "hellip" -> "…"; "laquo" -> "«"; "raquo" -> "»"
                else -> match.value
            }
        }.replace(Regex("[\\t\\x0B\\f\\r ]+"), " ").replace(Regex(" *\\n *"), "\n")
            .replace(Regex("\\n{3,}"), "\n\n").trim()
    }
}
