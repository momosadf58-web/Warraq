package de.mo.bibliothek.data

import android.content.Context
import org.json.JSONArray

data class Blurb(val text:String,val url:String)

/** Bibliographic snapshot: Hallberg (2025), Arabic E-Book Corpus, CC BY 4.0.
 *  Descriptions: Hindawi-Books-dataset (alielfilali01), CC BY-NC 4.0, only BookName/AuthorName/AboutBook/BookLink. */
object ArabicCatalog {
    private var entries: List<Found> = emptyList()
    private var assets: android.content.res.AssetManager? = null
    /** Loaded on first use (4.8 MB); keyed by Hindawi ID and by work title. */
    private val blurbs: Pair<Map<String,Blurb>,Map<String,List<Pair<String,Blurb>>>> by lazy {
        val json=JSONArray(assets!!.open("hindawi-blurbs.json").bufferedReader().use{it.readText()})
        val byId=HashMap<String,Blurb>();val byTitle=HashMap<String,MutableList<Pair<String,Blurb>>>()
        for(index in 0 until json.length()) {
            val r=json.getJSONObject(index);val url=r.optString("url");val text=r.optString("description").trim()
            if(text.isBlank())continue
            val blurb=Blurb(text,url)
            Regex("/books/([0-9]+)/").find(url)?.let{byId[it.groupValues[1]]=blurb}
            byTitle.getOrPut(workTitle(r.optString("title"))){mutableListOf()}+=searchKey(r.optString("author")) to blurb
        }
        byId to byTitle
    }
    /** Only a matching Hindawi ID, or the same work title with a compatible author, yields a description. */
    fun blurb(title:String,author:String,hindawiId:String=""):Blurb? {
        if(assets==null)return null
        if(hindawiId.isNotEmpty())blurbs.first[hindawiId]?.let{return it}
        val candidates=blurbs.second[workTitle(title)].orEmpty();val a=searchKey(author)
        if(a.isEmpty())return candidates.singleOrNull()?.second
        return candidates.firstOrNull{(other,_)->other.isNotEmpty()&&(other.contains(a)||a.contains(other))}?.second
    }
    @Synchronized fun load(context:Context) {
        assets=context.applicationContext.assets
        if(entries.isNotEmpty())return
        val json=JSONArray(context.assets.open("hindawi-catalog.json").bufferedReader().use{it.readText()})
        entries=(0 until json.length()).map{index->
            val r=json.getJSONObject(index);val id=r.getString("id")
            Found(r.getString("title"),r.getString("author"),"","https://downloads.hindawi.org/covers/svg/270x360/$id.svg?v=5",
                "مؤسسة هنداوي",r.optString("year").takeIf{it.matches(Regex("[0-9]{3,4}"))}.orEmpty(),0,
                when(r.optString("genre")){"novels"->"رواية";"philosophy"->"فلسفة";"history"->"تاريخ";"psychology"->"علم نفس";else->r.optString("genre")},
                "Arabisch","","Hindawi",sourceUrl="https://www.safahat.org/books/$id/",workKey="hindawi:$id")
        }
    }
    fun search(query:String):List<Found> = entries.map{it to matchScore(query,it)}.filter{it.second>0&&relevant(query,it.first)}
        .sortedByDescending{it.second}.take(18).map{(found,_)->
            found.copy(description=blurb(found.title,found.author,found.workKey.removePrefix("hindawi:"))?.text.orEmpty())
        }
    fun exact(title:String,author:String):Found? = entries.firstOrNull {
        workTitle(it.title)==workTitle(title) && (author.isBlank()||searchKey(it.author).contains(searchKey(author))||searchKey(author).contains(searchKey(it.author)))
    }
}

private val FILLERS=setOf("الذي","التي","الذين","في","من","على","عن","إلى","الى","و","او","أو","مع","the","of","and","der","die","das","und")

/** At least half of the meaningful query words occur in title/author: one shared word like „أبي“ is no match. */
fun relevant(query:String,found:Found):Boolean {
    if(isbnKey(query).length in listOf(10,13)&&isbnKey(query)==isbnKey(found.isbn))return true
    val tokens=searchKey(query).split(' ').filter{it.length>1&&it !in FILLERS}
    if(tokens.isEmpty())return false
    val combined="${searchKey(found.title)} ${searchKey(found.author)}"
    return tokens.count{combined.contains(it)}*2>=tokens.size
}

fun workTitle(value:String):String=searchKey(value.substringBefore(':').replace(Regex("\\([^)]*\\)"),""))
fun sameWork(a:Found,b:Found):Boolean = (a.workKey.isNotEmpty()&&a.workKey==b.workKey) ||
    (workTitle(a.title)==workTitle(b.title) && searchKey(a.author)==searchKey(b.author))

fun matchScore(query:String,found:Found):Int {
    val q=searchKey(query);if(q.isBlank())return 0
    val title=searchKey(found.title);val author=searchKey(found.author)
    if(isbnKey(query).length in listOf(10,13)&&isbnKey(query)==isbnKey(found.isbn))return 1000
    if(q==title)return 500
    if(q==author)return 450
    val tokens=q.split(' ').filter{it.length>1};if(tokens.isEmpty())return 0
    val combined="$title $author";val matched=tokens.count{combined.contains(it)}
    if(matched==0)return 0
    return 100*matched/tokens.size + (if(matched==tokens.size)100 else 0) + (if(title.startsWith(q))80 else 0)
}
