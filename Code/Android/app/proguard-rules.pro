# Keine Reflection im App-Code; Bibliotheken bringen eigene Regeln mit.

-keepclassmembers class * { @android.webkit.JavascriptInterface <methods>; }

# ONNX Runtime ruft Java-Klassen aus nativem Code auf (arabische Texterkennung).
-keep class ai.onnxruntime.** { *; }
