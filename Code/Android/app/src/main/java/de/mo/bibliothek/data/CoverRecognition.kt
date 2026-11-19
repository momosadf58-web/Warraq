package de.mo.bibliothek.data

import android.content.Context
import android.graphics.*
import android.media.ExifInterface
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.File

data class CoverText(val text:String,val title:String,val author:String,val confidence:Int,val hashes:List<Long> = emptyList())

/** Size of the camera preview the photo was taken from; the guide frame is centred, 78 % wide, aspect 0.67. */
data class PreviewFrame(val width:Int,val height:Int)

class CoverRecognition(private val context:Context) {
    /** ML Kit reads Latin script robustly on photos; Tesseract only handles Arabic, on a contrast-prepared image. */
    @Synchronized fun recognize(file:File,frame:PreviewFrame?=null):CoverText {
        // Up to 3.2 German/English Tesseract models were copied to the phone; ML Kit replaced them (≈5.6 MB freed).
        for(old in listOf("deu","eng"))File(context.filesDir,"ocr/tessdata/$old.traineddata").delete()
        val bitmap=prepare(file,frame)
        try {
            val latin=runCatching{latinLines(bitmap)}.onFailure{android.util.Log.w("WarraqOcr","ML Kit",it)}.getOrDefault(emptyList())
            android.util.Log.i("WarraqOcr","image=${bitmap.width}x${bitmap.height} latin=${latin.map{"${it.text}@${it.top}/${it.height}/${it.confidence.toInt()}"}}")
            val latinText=latin.joinToString("\n"){it.text}
            val latinParsed=if(latin.isEmpty())null else parseCoverLines(latinText,latin,100)
            // A confidently read Latin title needs no Arabic pass (saves seconds). Arabic script read by the
            // Latin model comes out as low-confidence gibberish, so that case still gets the Arabic pass.
            val sure=latin.filter{line->latinParsed?.title?.contains(line.text)==true}.map{it.confidence}.average().takeIf{!it.isNaN()}?:0.0
            if(latinParsed!=null&&latinParsed.title.count{it.isLetter()}>=4&&sure>=65)
                return latinParsed.copy(hashes=CoverFingerprint.photo(bitmap))
            // OCR engines also "find" Arabic fragments in pictures and Latin type; only real words count.
            fun strong(lines:List<OcrLine>)=lines.filter{line->line.confidence>=60f&&line.text.split(' ').any{w->w.count{ARABIC.matches(it.toString())}>=4}}
            val paddle=runCatching{paddle(context).lines(Pixels(bitmap.width,bitmap.height,IntArray(bitmap.width*bitmap.height).also{bitmap.getPixels(it,0,bitmap.width,0,0,bitmap.width,bitmap.height)}))}
                .onFailure{android.util.Log.w("WarraqOcr","PaddleOCR",it)}.getOrDefault(emptyList())
            android.util.Log.i("WarraqOcr","paddle=${paddle.map{"${it.text}@${it.top}/${it.height}/${it.confidence.toInt()}"}}")
            // Tesseract stays as fallback when the neural model finds no confident words.
            val arabic=if(strong(paddle).isNotEmpty())paddle else arabicLines(bitmap).let{if(strong(it).size>strong(paddle).size)it else paddle}
            val words=strong(arabic)
            val arabicParsed=if(words.isEmpty())null else parseCoverLines(arabic.joinToString("\n"){it.text},arabic,0)
            val inCatalog=arabicParsed!=null&&ArabicCatalog.exact(arabicParsed.title,arabicParsed.author)!=null
            val arabicWins=arabicParsed!=null&&(inCatalog||(words.size>=2&&words.map{it.confidence}.average()>sure)||latinParsed==null)
            android.util.Log.i("WarraqOcr","latinSure=$sure arabicWords=${words.map{it.text}} catalog=$inCatalog arabicWins=$arabicWins")
            val parsed=when {
                arabicWins -> arabicParsed!!
                latinParsed!=null -> latinParsed
                else -> CoverText(latinText,"","",0)
            }
            return parsed.copy(hashes=CoverFingerprint.photo(bitmap))
        } finally {bitmap.recycle()}
    }

    /** Upright, cropped to the guide frame (with margin), scaled to about 1600 px (small web covers are enlarged). */
    private fun prepare(file:File,frame:PreviewFrame?):Bitmap {
        val decoded=BitmapFactory.decodeFile(file.path)?:error("Foto nicht lesbar.")
        val rotation=when(runCatching{ExifInterface(file.path).getAttributeInt(ExifInterface.TAG_ORIENTATION,ExifInterface.ORIENTATION_NORMAL)}.getOrDefault(1)) {
            ExifInterface.ORIENTATION_ROTATE_90->90f;ExifInterface.ORIENTATION_ROTATE_180->180f;ExifInterface.ORIENTATION_ROTATE_270->270f;else->0f
        }
        val upright=if(rotation==0f)decoded else Bitmap.createBitmap(decoded,0,0,decoded.width,decoded.height,Matrix().apply{postRotate(rotation)},true).also{decoded.recycle()}
        val crop=frame?.let{guideCrop(upright.width,upright.height,it)}?:Rect(0,0,upright.width,upright.height)
        val scale=minOf(4f,1600f/maxOf(crop.width(),crop.height()))
        val out=Bitmap.createBitmap((crop.width()*scale).toInt(),(crop.height()*scale).toInt(),Bitmap.Config.ARGB_8888)
        Canvas(out).drawBitmap(upright,crop,Rect(0,0,out.width,out.height),Paint(Paint.FILTER_BITMAP_FLAG))
        upright.recycle()
        return out
    }

    private fun latinLines(bitmap:Bitmap):List<OcrLine> {
        val recognizer=TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
        try {
            val text=Tasks.await(recognizer.process(InputImage.fromBitmap(bitmap,0)))
            return text.textBlocks.flatMap{it.lines}.mapNotNull{line->
                val box=line.boundingBox?:return@mapNotNull null
                OcrLine(line.text,box.top,box.height(),line.confidence*100f)
            }
        } finally {recognizer.close()}
    }

    private fun arabicLines(source:Bitmap):List<OcrLine> {
        val root=File(context.filesDir,"ocr");val data=File(root,"tessdata").apply{mkdirs()}
        val target=File(data,"ara.traineddata")
        if(!target.exists()) {
            val temp=File(data,"ara.tmp")
            context.assets.open("tessdata/ara.traineddata").use{input->temp.outputStream().use{input.copyTo(it)}}
            check(temp.renameTo(target)){"Sprachmodell konnte nicht vorbereitet werden."}
        }
        val bitmap=contrast(source)
        val engine=TessBaseAPI()
        try {
            check(engine.init(root.path,"ara")){"Texterkennung konnte nicht gestartet werden."}
            engine.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SPARSE_TEXT)
            engine.setImage(bitmap)
            engine.getUTF8Text()
            val lines=mutableListOf<OcrLine>()
            engine.resultIterator?.let{iterator->
                try {
                    iterator.begin()
                    do {
                        val line=iterator.getUTF8Text(TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE)?.trim().orEmpty()
                        val box=iterator.getBoundingRect(TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE)
                        if(line.isNotEmpty())lines+=OcrLine(line,box.top,box.height(),iterator.confidence(TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE))
                    } while(iterator.next(TessBaseAPI.PageIteratorLevel.RIL_TEXTLINE))
                } finally {iterator.delete()}
            }
            return lines
        } finally {engine.recycle();bitmap.recycle()}
    }
}

private val ARABIC=Regex("[\\u0600-\\u06FF]")

@Volatile private var paddleInstance:PaddleArabic?=null
/** Loaded once (about 11 MB of models) and kept for further photos. */
private fun paddle(context:Context):PaddleArabic=paddleInstance?:synchronized(PaddleArabic::class.java){
    paddleInstance?:PaddleArabic(context.assets.open("paddle/det.onnx").use{it.readBytes()},context.assets.open("paddle/arabic_rec.onnx").use{it.readBytes()},
        context.assets.open("paddle/arabic_dict.txt").bufferedReader(Charsets.UTF_8).use{it.readLines()}.filter{it.isNotEmpty()}).also{paddleInstance=it}
}

fun guideCrop(imageW:Int,imageH:Int,frame:PreviewFrame):Rect {
    // PreviewView FILL_CENTER: the photo is scaled to cover the view and centred.
    val scale=maxOf(frame.width.toFloat()/imageW,frame.height.toFloat()/imageH)
    val guideW=frame.width*0.78f*1.15f;val guideH=minOf(frame.width*0.78f/0.67f*1.12f,frame.height.toFloat())
    val w=(guideW/scale).toInt().coerceAtMost(imageW);val h=(guideH/scale).toInt().coerceAtMost(imageH)
    val left=(imageW-w)/2;val top=(imageH-h)/2
    return Rect(left,top,left+w,top+h)
}

/** Grey, dark covers inverted, levels stretched: Tesseract wants dark text on light ground. */
private fun contrast(source:Bitmap):Bitmap {
    val w=source.width;val h=source.height;val pixels=IntArray(w*h);source.getPixels(pixels,0,w,0,0,w,h)
    val grey=IntArray(pixels.size){i->val p=pixels[i];(Color.red(p)*299+Color.green(p)*587+Color.blue(p)*114)/1000}
    val sorted=grey.copyOf().also{it.sort()}
    val low=sorted[sorted.size/50];val high=maxOf(low+1,sorted[sorted.size-1-sorted.size/50])
    val invert=sorted[sorted.size/2]<110
    for(i in grey.indices){var v=((grey[i]-low)*255/(high-low)).coerceIn(0,255);if(invert)v=255-v;pixels[i]=Color.rgb(v,v,v)}
    return Bitmap.createBitmap(pixels,w,h,Bitmap.Config.ARGB_8888)
}

data class OcrLine(val text:String,val top:Int,val height:Int,val confidence:Float=100f)

/** Werbe-, Verlags- und Gattungszeilen sind nie Titel oder Autor. */
private val NOISE=Regex("(?i)ISBN|copyright|www\\.|bests?el|seller|new york times|spiegel|nominiert|preis|prize|winner|award|auflage|edition|verlag|publishing|publishers|\\bpress\\b|الناشر|دار النشر|للنشر|للتشر|والتوزيع|التوزيع|حقوق|الطبعة|الأكثر مبيع")
private val GENRE_ONLY=Regex("(?i)^(roman|novel|a novel|ein roman|erzählung|erzählungen|stories|sachbuch|رواية|قصص|مجموعة قصصية)$")
private val BY=Regex("^(تأليف|بقلم|ترجمة|von|by)[:\\s]+",RegexOption.IGNORE_CASE)

private fun usable(line:String):Boolean {
    val letters=line.count{it.isLetter()}
    return letters>=3 && letters>=line.count{!it.isWhitespace()}*0.7 && !NOISE.containsMatchIn(line) && !GENRE_ONLY.matches(line.trim())
}

private fun catalogMatch(raw:String,lines:List<String>,confidence:Int):CoverText? {
    val found=lines.asSequence().flatMap{ArabicCatalog.search(it).asSequence()}.maxByOrNull{found->
        lines.maxOfOrNull{matchScore(it,found)}?:0
    }?.takeIf{found->lines.any{workTitle(it)==workTitle(found.title)}} ?: return null
    return CoverText(raw,found.title,found.author,confidence)
}

/** Titel = größte Schrift samt direkt anschließender Zeilen; Autor = größte übrige namensartige Zeile. */
fun parseCoverLines(raw:String,ocr:List<OcrLine>,confidence:Int=0):CoverText {
    val lines=ocr.map{it.copy(text=it.text.replace(Regex("\\s+")," ").trim())}
        .filter{it.confidence>=35f&&usable(it.text)}.sortedBy{it.top}
    if(lines.isEmpty())return parseCoverText(raw,confidence)
    catalogMatch(raw,lines.map{it.text},confidence)?.let{return it}
    lines.firstOrNull{BY.containsMatchIn(it.text)}?.let{marked->
        val rest=lines-marked
        val title=rest.maxByOrNull{it.height}?.text.orEmpty()
        return CoverText(raw,title,marked.text.replace(BY,""),confidence)
    }
    val anchor=lines.indexOf(lines.maxBy{it.height});val big=lines[anchor].height
    var from=anchor;var to=anchor
    fun gap(upper:OcrLine,lower:OcrLine)=lower.top-(upper.top+upper.height)
    // Smaller lead-in lines above the main title („THE DICTIONARY OF“) belong to it; a person's name never does.
    while(from>0&&lines[from-1].height>=big*0.3f&&gap(lines[from-1],lines[from])<big&&!nameLike(lines[from-1].text))from--
    // Below the title only near-equal type continues it (two-line titles); subtitles and author names are smaller.
    while(to<lines.lastIndex&&lines[to+1].height>=big*0.7f&&gap(lines[to],lines[to+1])<big*0.6f&&!nameLike(lines[to+1].text))to++
    val titleLines=lines.subList(from,to+1)
    val title=titleLines.joinToString(" "){it.text}
    // A line that continues a neighbouring line of similar size is part of a block (e.g. a two-line subtitle), not a name.
    fun sameBlock(a:OcrLine,b:OcrLine)=gap(a,b)<0||(gap(a,b)<maxOf(a.height,b.height)*0.6f&&minOf(a.height,b.height)>=maxOf(a.height,b.height)*0.75f)
    val inBlock=lines.indices.filter{i->(i>0&&sameBlock(lines[i-1],lines[i]))||(i<lines.lastIndex&&sameBlock(lines[i],lines[i+1]))}.map{lines[it]}.toSet()
    val rest=(lines-titleLines.toSet()).filter{it.text.split(' ').size in 1..5}
    val author=(rest.filter{nameLike(it.text)&&it !in inBlock}.maxByOrNull{it.height}?:rest.filter{nameLike(it.text)}.maxByOrNull{it.height}
        ?:rest.maxByOrNull{it.height})?.text.orEmpty()
    return CoverText(raw,title,author.replace(HONORIFIC,"").trim(),confidence)
}

/** Academic titles in front of names hurt the catalogue search. */
private val HONORIFIC=Regex("^(د\\.\\s*|دكتور\\s+|الدكتور\\s+|Dr\\.\\s*|Prof\\.\\s*)",RegexOption.IGNORE_CASE)

private val NOT_IN_NAMES=setOf("of","the","and","a","an","to","der","die","das","des","dem","den","und","von","vom","zum","zur","im","in","el",
    "من","في","على","عن","إلى","و")
/** 2–4 words of letters without articles/prepositions: looks like a person's name, not a title fragment. */
internal fun nameLike(text:String):Boolean {
    val words=text.trim().split(Regex("\\s+"))
    return words.size in 2..4 && words.none{it.lowercase() in NOT_IN_NAMES} && words.all{w->w.all{it.isLetter()||it=='.'||it=='-'||it=='\''}}
}

fun parseCoverText(raw:String,confidence:Int=0):CoverText {
    val lines=raw.lines().map{it.trim().replace(Regex("\\s+")," ")}.filter(::usable).distinct().take(12)
    catalogMatch(raw,lines,confidence)?.let{return it}
    val authorLine=lines.firstOrNull{BY.containsMatchIn(it)}
    val cleanAuthor=authorLine?.replace(BY,"").orEmpty()
    val title=lines.firstOrNull{it!=authorLine}.orEmpty()
    val author=cleanAuthor.ifEmpty{lines.drop(1).firstOrNull().orEmpty()}
    return CoverText(raw,title,author,confidence)
}
