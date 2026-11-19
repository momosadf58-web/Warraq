package de.mo.bibliothek.ui

import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import de.mo.bibliothek.data.CoverText

@Composable
fun RecognitionScreen(uri:Uri,recognize:suspend(Uri)->Result<CoverText>,onSearch:(String,String,List<Long>)->Unit,onBack:()->Unit) {
    var result by remember{mutableStateOf<Result<CoverText>?>(null)}
    var title by remember{mutableStateOf("")};var author by remember{mutableStateOf("")};var showRaw by remember{mutableStateOf(false)}
    LaunchedEffect(uri){result=null;result=recognize(uri);result?.getOrNull()?.let{title=it.title;author=it.author}}
    Column(Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding().padding(24.dp),verticalArrangement=Arrangement.spacedBy(14.dp)) {
        TextButton(onClick=onBack){Text(tr("Zurück zur Kamera", "العودة إلى الكاميرا"))}
        Text(tr("Buch erkennen", "التعرف على الكتاب"),style=MaterialTheme.typography.headlineMedium)
        if(result==null) {
            CircularProgressIndicator();Text(tr("Arabischen und lateinischen Text auswerten …", "جارٍ تحليل النص العربي واللاتيني …"))
        } else {
            Text(tr("Erkannte Angaben prüfen, dann das passende Werk suchen.", "تحقق من النص المقروء ثم ابحث عن العمل المطابق."))
            OutlinedTextField(title,{title=it},label={Text(tr("Titel", "العنوان"))},modifier=Modifier.fillMaxWidth())
            OutlinedTextField(author,{author=it},label={Text(tr("Autor", "المؤلف"))},modifier=Modifier.fillMaxWidth())
            if(result?.isFailure==true||title.isBlank())Text(tr("Nicht sicher erkannt. Du kannst Titel oder Autor hier korrigieren.", "التعرف غير مؤكد. يمكنك تصحيح العنوان أو المؤلف هنا."))
            TextButton(onClick={showRaw=!showRaw}){Text(tr("Erkannten Text ansehen", "عرض النص المقروء"))}
            if(showRaw)Text(result?.getOrNull()?.text?.ifBlank{tr("Kein Text gefunden.", "لم يُعثر على نص.")}
                ?:(tr("Fehler: ", "خطأ: ")+result?.exceptionOrNull()?.message.orEmpty()),modifier=Modifier.weight(1f,false))
            Button(onClick={onSearch(title.trim(),author.trim(),result?.getOrNull()?.hashes.orEmpty())},enabled=title.isNotBlank()||author.isNotBlank(),modifier=Modifier.fillMaxWidth()) {
                Text(tr("Passendes Werk suchen", "البحث عن العمل المطابق"))
            }
        }
    }
}
