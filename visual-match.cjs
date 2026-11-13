const fs=require('node:fs'),path=require('node:path');const root=path.join(__dirname,'project'),base='app/src/main/java/de/mo/bibliothek/';
function edit(name,fn){const p=path.join(root,name),s=fs.readFileSync(p,'utf8').replace(/\r\n/g,'\n'),n=fn(s);if(s===n)throw Error(name);fs.writeFileSync(p,n);}
edit(base+'data/CoverRecognition.kt',s=>s.replace('val confidence:Int)','val confidence:Int,val hashes:List<Long> = emptyList())')
 .replace('            return parseCoverText(text,confidence)','            return parseCoverText(text,confidence).copy(hashes=CoverFingerprint.photo(bitmap))'));
edit(base+'data/BookLookup.kt',s=>s.replace('    val workKey: String = "",','    val workKey: String = "",\n    val coverDistance: Int? = null,'));
edit(base+'BookieViewModel.kt',s=>s.replace('import kotlinx.coroutines.Dispatchers','import kotlinx.coroutines.Dispatchers\nimport kotlinx.coroutines.async\nimport kotlinx.coroutines.awaitAll\nimport kotlinx.coroutines.coroutineScope')
 .replace('    suspend fun editions(work: Found): Result<List<Found>> = withContext(Dispatchers.IO) { runCatching { BookLookup.editions(work) } }',
 `    suspend fun editions(work: Found, hashes: List<Long> = emptyList()): Result<List<Found>> = withContext(Dispatchers.IO) { runCatching {
        val editions = BookLookup.editions(work)
        if (hashes.isEmpty()) return@runCatching editions
        val compared = coroutineScope { editions.take(8).map { found -> async {
            if (found.coverUrl.isBlank()) return@async found
            val name = runCatching { repository.downloadCover(found.coverUrl) }.getOrNull() ?: return@async found
            try { found.copy(coverDistance = de.mo.bibliothek.data.CoverFingerprint.distance(hashes, repository.coverFile(name))) }
            finally { repository.coverFile(name).delete() }
        } }.awaitAll() }
        compared.sortedBy { it.coverDistance ?: Int.MAX_VALUE } + editions.drop(8)
    } }`));
edit(base+'ui/RecognitionScreen.kt',s=>s.replace('onSearch:(String,String)->Unit','onSearch:(String,String,List<Long>)->Unit')
 .replace('onSearch(title.trim(),author.trim())','onSearch(title.trim(),author.trim(),result?.getOrNull()?.hashes.orEmpty())'));
edit(base+'MainActivity.kt',s=>s.replace('data class Search(val query: String) : Layer','data class Search(val query: String, val hashes: List<Long> = emptyList()) : Layer')
 .replace('onSearch = { title, author -> layers = layers.dropLast(1) + Layer.Search(listOf(title, author).filter { it.isNotBlank() }.joinToString(" ")) }',
 'onSearch = { title, author, hashes -> layers = layers.dropLast(1) + Layer.Search(listOf(title, author).filter { it.isNotBlank() }.joinToString(" "), hashes) }')
 .replace('initialQuery = layer.query, onClose = ::pop, searchOnOpen = true','initialQuery = layer.query, photoHashes = layer.hashes, onClose = ::pop, searchOnOpen = true'));
edit(base+'ui/AddScreen.kt',s=>s.replace('val editions: suspend (Found) -> Result<List<Found>>','val editions: suspend (Found, List<Long>) -> Result<List<Found>>')
 .replace('    searchOnOpen: Boolean = false,','    searchOnOpen: Boolean = false,\n    photoHashes: List<Long> = emptyList(),')
 .replace('EditionSheet(selected, results, actions.editions,','EditionSheet(selected, results, photoHashes, actions.editions,'));
edit(base+'ui/EditionSheet.kt',s=>s.replace('known:List<Found>,fetch:suspend(Found)->Result<List<Found>>','known:List<Found>,photoHashes:List<Long>,fetch:suspend(Found,List<Long>)->Result<List<Found>>')
 .replace('val result=fetch(work);result.onSuccess{more->editions=(editions+more).distinctBy','val result=fetch(work,photoHashes);result.onSuccess{more->editions=(more+editions).distinctBy')
 .replace('            if(loading)LinearProgressIndicator','            if(photoHashes.isNotEmpty())Text(tr("Ausgaben mit Coververgleich zum Foto", "طبعات مع مقارنة الغلاف بالصورة"),style=MaterialTheme.typography.bodySmall)\n            if(loading)LinearProgressIndicator'));
const file=path.join(root,'app/src/main/assets/hindawi-catalog.json');const source=fs.readFileSync(path.join(__dirname,'evidence/metadata.tsv'),'utf8').trim().split(/\r?\n/);const keys=source.shift().split('\t');const byId=new Map(source.map(line=>{let cols=line.split('\t');return[cols[0],cols[keys.indexOf('pubdate')].slice(0,4)]}));const catalog=JSON.parse(fs.readFileSync(file));catalog.forEach(b=>{b.year=byId.get(b.id)||b.year});fs.writeFileSync(file,JSON.stringify(catalog));
console.log('Lokalen Coververgleich und Hindawi-Ausgabedaten ergänzt.');
