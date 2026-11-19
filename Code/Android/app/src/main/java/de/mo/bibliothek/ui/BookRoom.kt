package de.mo.bibliothek.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.webkit.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewAssetLoader
import de.mo.bibliothek.data.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.Calendar

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun BookRoom(books:List<Book>,goal:Int,coverFile:(String)->File,onOpen:(String)->Unit,onClose:()->Unit) {
    val context=LocalContext.current;val arabic=LocalArabic.current
    val read=books.filter{it.readStatus==ReadStatus.READ}
    val year=Calendar.getInstance().get(Calendar.YEAR)
    fun bookJson(book:Book)=JSONObject().put("id",book.id).put("title",book.title).put("author",book.author)
        .put("genre",book.genre).put("year",readingYear(book.readAt)?:0).put("kind",book.kind().name)
        .put("cover",if(book.cover.isNotBlank())"https://appassets.androidplatform.net/covers/${book.cover}" else "")
    val payload=remember(books,goal,arabic){JSONObject().put("arabic",arabic).put("year",year).put("goal",goal)
        .put("books",JSONArray().also{a->read.forEach{a.put(bookJson(it))}})
        .put("yearBooks",JSONArray().also{a->booksReadInYear(books,year).forEach{a.put(bookJson(it))}}).toString()}
    val currentRead by rememberUpdatedState(read.map{it.id}.toSet());val open by rememberUpdatedState(onOpen);val close by rememberUpdatedState(onClose)
    // MATCH_PARENT: with the default WRAP_CONTENT the page's layout viewport stays 0 px high (empty room, no HUD).
    val web=remember{WebView(context).apply{layoutParams=android.view.ViewGroup.LayoutParams(-1,-1)}}
    var ready by remember{mutableStateOf(false)};var failed by remember{mutableStateOf(false)}
    DisposableEffect(web) {
        val loader=WebViewAssetLoader.Builder().addPathHandler("/assets/",WebViewAssetLoader.AssetsPathHandler(context))
            .addPathHandler("/covers/"){name->
                runCatching {
                    if(!name.matches(Regex("[a-f0-9-]{36}\\.jpg")))return@runCatching null
                    val file=coverFile(name);val opts=BitmapFactory.Options().apply{inSampleSize=4}
                    val image=BitmapFactory.decodeFile(file.path,opts)?:return@runCatching null
                    val bytes=try{ByteArrayOutputStream().also{image.compress(Bitmap.CompressFormat.JPEG,85,it)}.toByteArray()}finally{image.recycle()}
                    WebResourceResponse("image/jpeg",null,ByteArrayInputStream(bytes))
                }.getOrNull()
            }.build()
        web.settings.apply {javaScriptEnabled=true;domStorageEnabled=false;allowFileAccess=false;allowContentAccess=false;javaScriptCanOpenWindowsAutomatically=false;setSupportMultipleWindows(false)}
        web.setBackgroundColor(android.graphics.Color.rgb(38,31,24))
        web.webViewClient=object:WebViewClient(){
            override fun shouldOverrideUrlLoading(view:WebView,request:WebResourceRequest)=true
            override fun shouldInterceptRequest(view:WebView,request:WebResourceRequest):WebResourceResponse? {
                return if(request.url.host=="appassets.androidplatform.net")loader.shouldInterceptRequest(request.url)
                else WebResourceResponse("text/plain","UTF-8",ByteArrayInputStream(ByteArray(0)))
            }
        }
        web.webChromeClient=object:WebChromeClient(){override fun onConsoleMessage(message:ConsoleMessage):Boolean {
            if(message.messageLevel()==ConsoleMessage.MessageLevel.ERROR)android.util.Log.e("WarraqRoom",message.message())
            return true
        }}
        web.addJavascriptInterface(object {
            @JavascriptInterface fun ready(){web.post{ready=true}}
            @JavascriptInterface fun sceneReady(){android.util.Log.i("WarraqRoom","scene-ready; real-books="+currentRead.size)}
            @JavascriptInterface fun openBook(id:String){web.post{if(id in currentRead)open(id)}}
            @JavascriptInterface fun closeRoom(){web.post{close()}}
        },"Warraq")
        web.loadUrl("https://appassets.androidplatform.net/assets/room/index.html")
        onDispose{web.removeJavascriptInterface("Warraq");web.stopLoading();web.destroy()}
    }
    LaunchedEffect(ready,payload){if(ready)web.evaluateJavascript("window.updateRoom($payload)",null)}
    LaunchedEffect(Unit){kotlinx.coroutines.delay(15000);if(!ready)failed=true}
    Box(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
        AndroidView({web},Modifier.fillMaxSize())
        if(failed)Surface {Column {Text(tr("3D-Ansicht konnte nicht gestartet werden.", "تعذر بدء العرض ثلاثي الأبعاد."));TextButton(onClick=onClose){Text(tr("Zur Statistik", "إلى الإحصاءات"))}}}
    }
}
