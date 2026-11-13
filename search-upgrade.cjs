const fs=require('node:fs'),path=require('node:path');const root=path.join(__dirname,'project'),base='app/src/main/java/de/mo/bibliothek/';
function edit(name,fn){const p=path.join(root,name),s=fs.readFileSync(p,'utf8').replace(/\r\n/g,'\n');const next=fn(s);if(next===s)throw Error(name);fs.writeFileSync(p,next);}
edit(base+'data/Book.kt',s=>s.replace('        .lowercase(Locale.ROOT)','        .lowercase(Locale.ROOT)\n        .replace("ـ", "").replace(\'ى\', \'ي\').replace(\'ی\', \'ي\').replace(\'ک\', \'ك\')'));
edit(base+'data/BookLookup.kt',s=>s.replace('    val source: String,','    val source: String,\n    val sourceUrl: String = "",\n    val workKey: String = "",')
 .replace('        addedAt = now,','        addedAt = now, extra = JSONObject().put("source", source).put("sourceUrl", sourceUrl).put("workKey", workKey).toString(),')
 .replace('        val google = runCatching { google(if (asIsbn) "isbn:$isbn" else q) }\n        val openLibrary = runCatching { openLibrary(q) }',
 '        val gf = java.util.concurrent.CompletableFuture.supplyAsync { runCatching { google(if (asIsbn) "isbn:$isbn" else q) } }\n        val of = java.util.concurrent.CompletableFuture.supplyAsync { runCatching { openLibrary(q) } }\n        val local = ArabicCatalog.search(q)\n        val google = gf.get()\n        val openLibrary = of.get()')
 .replace('if (google.isFailure && openLibrary.isFailure)','if (google.isFailure && openLibrary.isFailure && local.isEmpty())')
 .replace('(google.getOrDefault(emptyList()) + openLibrary.getOrDefault(emptyList()))','(local + google.getOrDefault(emptyList()) + openLibrary.getOrDefault(emptyList()))')
 .replace('            else merged[duplicate] = fill(merged[duplicate], found)','            else if (merged[duplicate].isbn == found.isbn) merged[duplicate] = fill(merged[duplicate], found)')
 .replace('        return merged.take(40)','        return merged.sortedByDescending { matchScore(q, it) }.take(50)')
 .replace('        return searchKey(a.title) == searchKey(b.title) && searchKey(a.author) == searchKey(b.author)',
 '        if (a.isbn.isNotEmpty() && b.isbn.isNotEmpty()) return false\n        return searchKey(a.title) == searchKey(b.title) && searchKey(a.author) == searchKey(b.author) && a.publisher == b.publisher && a.published == b.published')
 .replace('                source = "Google Books",','                source = "Google Books",\n                sourceUrl = "https://books.google.com/books?id=" + items.getJSONObject(index).optString("id"),')
 .replace('fields=title,author_name','fields=key,title,author_name')
 .replace('                source = "Open Library",','                source = "Open Library",\n                sourceUrl = "https://openlibrary.org" + doc.optString("key"),\n                workKey = doc.optString("key"),')
 .replace('    private fun same(a: Found, b: Found): Boolean {',
 `    fun editions(work: Found): List<Found> {
        if (!work.workKey.matches(Regex("/works/OL[0-9]+W"))) return listOf(work)
        val entries = JSONObject(get("https://openlibrary.org" + work.workKey + "/editions.json?limit=40")).optJSONArray("entries") ?: return listOf(work)
        val editions = (0 until entries.length()).mapNotNull { i ->
            val e = entries.optJSONObject(i) ?: return@mapNotNull null
            val id = e.optString("key")
            val cover = e.optJSONArray("covers")?.optLong(0) ?: 0
            val code = e.optJSONArray("languages")?.optJSONObject(0)?.optString("key").orEmpty().substringAfterLast('/')
            work.copy(title = e.optString("title").ifBlank { work.title },
                isbn = e.optJSONArray("isbn_13")?.optString(0).orEmpty().ifBlank { e.optJSONArray("isbn_10")?.optString(0).orEmpty() },
                publisher = e.optJSONArray("publishers")?.optString(0).orEmpty(), published = e.optString("publish_date"),
                pages = e.optInt("number_of_pages"), language = languageName(code),
                coverUrl = if (cover > 0) "https://covers.openlibrary.org/b/id/$cover-L.jpg" else "",
                sourceUrl = "https://openlibrary.org$id")
        }
        return editions.ifEmpty { listOf(work) }
    }

    private fun same(a: Found, b: Found): Boolean {`));
edit(base+'BookieViewModel.kt',s=>s.replace('                    val current = repository.load()','                    de.mo.bibliothek.data.ArabicCatalog.load(application)\n                    val current = repository.load()')
 .replace('    suspend fun search(query: String)',
 '    suspend fun recognizeCover(uri: Uri): Result<de.mo.bibliothek.data.CoverText> = withContext(Dispatchers.IO) {\n        runCatching {\n            val name = repository.importCover(uri)\n            try { de.mo.bibliothek.data.CoverRecognition(getApplication()).recognize(repository.coverFile(name)) }\n            finally { repository.coverFile(name).delete() }\n        }\n    }\n    suspend fun editions(work: Found): Result<List<Found>> = withContext(Dispatchers.IO) { runCatching { BookLookup.editions(work) } }\n\n    suspend fun search(query: String)'));
console.log('Arabische Normalisierung, Quellen und Ausgabenabgleich ergänzt.');
