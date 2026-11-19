package de.mo.bibliothek.ui

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import androidx.core.content.FileProvider
import de.mo.bibliothek.data.Book
import de.mo.bibliothek.data.BookQuote
import java.io.File

/** Android's text layout shapes Arabic and preserves RTL in the exported image.
 *  A non-literal [quote] is printed with [ideaLabel] and without quotation marks. */
fun createQuoteCard(context: Context, book: Book, quote: BookQuote, cover: File?, ideaLabel: String = ""): File {
    val bitmap=Bitmap.createBitmap(1080,1350,Bitmap.Config.ARGB_8888)
    try {
        val canvas=Canvas(bitmap); canvas.drawColor(Color.rgb(243,236,220))
        val line=Paint(Paint.ANTI_ALIAS_FLAG).apply { color=Color.rgb(162,133,77);style=Paint.Style.STROKE;strokeWidth=3f }
        canvas.drawRoundRect(RectF(36f,36f,1044f,1314f),22f,22f,line)
        fun text(value:String,y:Float,size:Float,color:Int=Color.rgb(37,46,32),width:Int=888,align:Layout.Alignment=Layout.Alignment.ALIGN_NORMAL,maxLines:Int=2):Int {
            val paint=TextPaint(Paint.ANTI_ALIAS_FLAG).apply {textSize=size;this.color=color;typeface=Typeface.create("serif",Typeface.NORMAL)}
            val layout=StaticLayout.Builder.obtain(value,0,value.length,paint,width).setAlignment(align).setIncludePad(false)
                .setMaxLines(maxLines).setEllipsize(android.text.TextUtils.TruncateAt.END).build()
            canvas.save();canvas.translate(96f,y);layout.draw(canvas);canvas.restore();return layout.height
        }
        text("Warraq  ·  الورّاق",88f,32f)
        var top=190f
        val image=cover?.takeIf {it.isFile}?.let {BitmapFactory.decodeFile(it.path)}
        if(image!=null) {
            val h=230f;val w=h*image.width/image.height
            canvas.drawBitmap(image,null,RectF(96f,top,96f+w,top+h),Paint(Paint.ANTI_ALIAS_FLAG))
            image.recycle();top+=268f
        }
        if(!quote.literal)top+=text(ideaLabel,top,24f,Color.rgb(122,98,56))+22f
        val body=if(quote.literal)"« ${quote.text} »" else quote.text
        top+=text(body,top,if(quote.text.length>350)29f else 37f,maxLines=12)+38f
        top+=text(book.title,top,27f)+16f
        text(book.author,top,23f)
        text(quote.source.title,1180f,21f)
        if(quote.source.url.isNotBlank())text(quote.source.url,1220f,17f)
        val dir=File(context.cacheDir,"share").apply {mkdirs()}
        val output=File(dir,"warraq-quote.png")
        output.outputStream().use {bitmap.compress(Bitmap.CompressFormat.PNG,100,it)}
        return output
    } finally {bitmap.recycle()}
}

fun shareQuoteCard(context:Context,file:File) {
    val uri=FileProvider.getUriForFile(context,"de.mo.bibliothek.files",file)
    val intent=Intent(Intent.ACTION_SEND).apply {
        type="image/png";putExtra(Intent.EXTRA_STREAM,uri)
        clipData=ClipData.newRawUri("Warraq",uri);addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent,"Warraq"))
}
