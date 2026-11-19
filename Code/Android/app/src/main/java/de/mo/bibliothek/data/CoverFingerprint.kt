package de.mo.bibliothek.data

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import java.io.File

/** Small perceptual fingerprints help order editions; they never override bibliographic identity. */
object CoverFingerprint {
    private fun hash(image:Bitmap):Long {
        val small=Bitmap.createScaledBitmap(image,9,8,true)
        try {
            var bits=0L
            fun light(x:Int,y:Int):Int {val c=small.getPixel(x,y);return Color.red(c)*3+Color.green(c)*6+Color.blue(c)}
            for(y in 0..7)for(x in 0..7)if(light(x,y)>light(x+1,y))bits=bits or (1L shl(y*8+x))
            return bits
        } finally {if(small!==image)small.recycle()}
    }
    fun photo(image:Bitmap):List<Long> = listOf(1f,.85f,.7f,.55f).map{fraction->
        val width=(image.width*fraction).toInt().coerceAtLeast(1)
        val height=minOf(image.height,(width/.67f).toInt())
        val crop=Bitmap.createBitmap(image,(image.width-width)/2,(image.height-height)/2,width,height)
        try{hash(crop)}finally{if(crop!==image)crop.recycle()}
    }
    fun distance(photo:List<Long>,file:File):Int? {
        val image=BitmapFactory.decodeFile(file.path)?:return null
        try{val candidate=hash(image);return photo.minOfOrNull{java.lang.Long.bitCount(it xor candidate)}}finally{image.recycle()}
    }
}
