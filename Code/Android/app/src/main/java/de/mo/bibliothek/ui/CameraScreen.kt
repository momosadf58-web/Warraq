package de.mo.bibliothek.ui

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.SystemClock
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import de.mo.bibliothek.data.PreviewFrame
import java.io.File
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.abs

@Composable
fun CameraScreen(onCaptured:(Uri,PreviewFrame)->Unit,onClose:()->Unit) {
    val context=LocalContext.current;val lifecycle=LocalLifecycleOwner.current
    var allowed by remember{mutableStateOf(ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED)}
    var denied by remember{mutableStateOf(0)}
    val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){allowed=it;if(!it)denied++}
    // After a second refusal Android no longer shows the dialog; then only the app settings can grant it.
    fun askAgain(){
        if(denied<2)permission.launch(Manifest.permission.CAMERA)
        else context.startActivity(android.content.Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.fromParts("package",context.packageName,null)))
    }
    var automatic by remember{mutableStateOf(true)};val auto by rememberUpdatedState(automatic)
    var error by remember{mutableStateOf(false)}
    val preview=remember{PreviewView(context)}
    val executor=remember{Executors.newSingleThreadExecutor()}
    // Quality mode: the S24 merges frames, which matters for dim rooms and glossy covers.
    val capture=remember{ImageCapture.Builder().setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY).build()}
    var camera by remember{mutableStateOf<Camera?>(null)};var torch by remember{mutableStateOf(false)}
    val capturing=remember{AtomicBoolean(false)}
    DisposableEffect(Unit) { onDispose { executor.shutdown() } }
    val currentOnCaptured by rememberUpdatedState(onCaptured)
    LaunchedEffect(Unit){if(!allowed)permission.launch(Manifest.permission.CAMERA)}
    DisposableEffect(lifecycle) {
        val observer=androidx.lifecycle.LifecycleEventObserver{_,event->
            if(event==androidx.lifecycle.Lifecycle.Event.ON_RESUME)allowed=ContextCompat.checkSelfPermission(context,Manifest.permission.CAMERA)==PackageManager.PERMISSION_GRANTED
        }
        lifecycle.lifecycle.addObserver(observer);onDispose{lifecycle.lifecycle.removeObserver(observer)}
    }
    fun take() {
        if(!capturing.compareAndSet(false,true))return
        val dir=File(context.cacheDir,"recognition").apply{mkdirs()};val file=File.createTempFile("book-",".jpg",dir)
        capture.takePicture(ImageCapture.OutputFileOptions.Builder(file).build(),ContextCompat.getMainExecutor(context),object:ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(output:ImageCapture.OutputFileResults){currentOnCaptured(Uri.fromFile(file),PreviewFrame(preview.width,preview.height))}
            override fun onError(exception:ImageCaptureException){file.delete();capturing.set(false);error=true}
        })
    }
    DisposableEffect(allowed,lifecycle) {
        var provider:ProcessCameraProvider?=null;var disposed=false
        if(allowed) {
            val future=ProcessCameraProvider.getInstance(context)
            future.addListener({
                if(disposed)return@addListener
                runCatching {
                    provider=future.get()
                    val cameraPreview=Preview.Builder().build().also{it.setSurfaceProvider(preview.surfaceProvider)}
                    val analyzer=ImageAnalysis.Builder().setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build()
                    var previous:IntArray?=null;var steadySince=SystemClock.elapsedRealtime()
                    analyzer.setAnalyzer(executor){image->
                        try {
                            val plane=image.planes[0];val bytes=plane.buffer;val values=IntArray(96)
                            for(i in values.indices){val x=image.width/5+(i%8)*image.width*3/5/8;val y=image.height/5+(i/8)*image.height*3/5/12
                                val pos=y*plane.rowStride+x*plane.pixelStride;values[i]=bytes.get(pos).toInt()and 255}
                            val before=previous;previous=values
                            val change=before?.let{values.indices.sumOf{i->abs(values[i]-it[i])}.toFloat()/values.size}?:100f
                            val mean=values.average();val contrast=values.sumOf{abs(it-mean)}/values.size
                            if(change>3.5f||contrast<16)steadySince=SystemClock.elapsedRealtime()
                            if(auto&&SystemClock.elapsedRealtime()-steadySince>1500)ContextCompat.getMainExecutor(context).execute{take()}
                        }finally{image.close()}
                    }
                    camera=provider?.bindToLifecycle(lifecycle,CameraSelector.DEFAULT_BACK_CAMERA,cameraPreview,capture,analyzer)
                }.onFailure{error=true}
            },ContextCompat.getMainExecutor(context))
        }
        onDispose{disposed=true;provider?.unbindAll()}
    }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if(allowed)AndroidView({preview},Modifier.fillMaxSize())
        else Column(Modifier.align(Alignment.Center).padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally) {
            Text(tr("Kamerazugriff ist für die Erkennung nötig.", "يلزم السماح بالكاميرا للتعرف على الكتاب."),color=Color.White)
            TextButton(onClick={askAgain()}){Text(if(denied<2)tr("Kamera erlauben", "السماح بالكاميرا") else tr("In den Einstellungen erlauben", "السماح من الإعدادات"),color=Color(0xFFE0C68E))}
        }
        Box(Modifier.align(Alignment.Center).fillMaxWidth(.78f).aspectRatio(.67f).border(2.dp,Color(0xFFE0C68E),RoundedCornerShape(12.dp)))
        TextButton(onClick=onClose,modifier=Modifier.align(Alignment.TopStart).statusBarsPadding().padding(12.dp)){Text(tr("Zurück", "رجوع"),color=Color.White)}
        if(camera?.cameraInfo?.hasFlashUnit()==true)TextButton(onClick={torch=!torch;camera?.cameraControl?.enableTorch(torch)},
            modifier=Modifier.align(Alignment.TopEnd).statusBarsPadding().padding(12.dp)){Text(if(torch)tr("Licht aus", "إطفاء الضوء") else tr("Licht an", "تشغيل الضوء"),color=Color.White)}
        Column(Modifier.align(Alignment.BottomCenter).navigationBarsPadding().background(Color(0xCC111812)).padding(20.dp),horizontalAlignment=Alignment.CenterHorizontally) {
            Text(tr("Ein Buch in den Rahmen halten", "ضع كتاباً واحداً داخل الإطار"),color=Color.White)
            Text(tr("Titel und Autor gut sichtbar halten. Das Bild bleibt auf deinem Handy.", "اجعل العنوان والمؤلف واضحين. تبقى الصورة على هاتفك."),color=Color.LightGray,style=MaterialTheme.typography.bodySmall)
            Row(verticalAlignment=Alignment.CenterVertically){Switch(automatic,{automatic=it});Spacer(Modifier.width(10.dp));Text(tr("Automatisch bei ruhigem Bild", "التقاط تلقائي عند ثبات الصورة"),color=Color.White)}
            if(error)Text(tr("Kamera nicht verfügbar. Erneut versuchen oder zurückgehen.", "الكاميرا غير متاحة. حاول مجدداً أو ارجع."),color=Color(0xFFFFB4A9))
            Button(onClick={take()},enabled=allowed){Text(tr("Buch erkennen", "التعرف على الكتاب"))}
        }
    }
}
