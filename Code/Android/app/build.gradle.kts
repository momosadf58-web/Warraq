plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    useLibrary("android.test.runner", false)
    useLibrary("android.test.base", false)
    namespace = "de.mo.bibliothek"
    compileSdk = 35
    buildToolsVersion = "35.0.1"

    defaultConfig {
        applicationId = "de.mo.bibliothek"
        testInstrumentationRunner = "android.test.InstrumentationTestRunner"
        minSdk = 26
        targetSdk = 35
        versionCode = 22
        versionName = "3.5.0"
        // Nur das S24 und vergleichbare 64-Bit-GerÃ¤te: hÃ¤lt ONNX Runtime und Tesseract klein.
        ndk { abiFilters += "arm64-v8a" }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            // Wie 2.x: run-as bleibt mÃ¶glich, damit Sicherungen per ADB ohne Datenverlust prÃ¼fbar sind.
            isDebuggable = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    buildFeatures { compose = true }
    lint { checkReleaseBuilds = false }
    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }
}

dependencies {
    implementation("cz.adaptech.tesseract4android:tesseract4android:4.9.0")
    // Lateinische Schrift auf Fotos (GoldprÃ¤gung, dunkle Cover): gebÃ¼ndeltes Modell, offline, kein Konto.
    implementation("com.google.mlkit:text-recognition:16.0.1")
    // Arabische Schrift: PaddleOCR PP-OCRv3 (Erkennung + Textbereiche, zusammen ca. 11 MB) lokal Ã¼ber ONNX Runtime.
    implementation("com.microsoft.onnxruntime:onnxruntime-android:1.23.0")
    // PC-Test der arabischen Erkennung mit den echten Modellen (Desktop-Variante derselben ONNX-Runtime-Version).
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.json:json:20240303")
    testImplementation("com.microsoft.onnxruntime:onnxruntime:1.23.0")
    implementation("androidx.camera:camera-camera2:1.4.0")
    implementation("androidx.camera:camera-lifecycle:1.4.0")
    implementation("androidx.camera:camera-view:1.4.0")
    implementation("io.coil-kt:coil-svg:2.7.0")
    implementation("com.caverock:androidsvg-aar:1.4")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.palette:palette-ktx:1.0.0")
    implementation("com.google.android.gms:play-services-code-scanner:16.1.0")
}

// Im PC-Test nur die Desktop-Runtime, sonst kollidieren beide Varianten derselben Klassen.
configurations.matching { it.name.contains("UnitTest") }.configureEach {
    exclude(group = "com.microsoft.onnxruntime", module = "onnxruntime-android")
}

// Testbilder liegen auÃŸerhalb des Repos (eigene Fotos); Pfad per -PwaraqOcrImages=<Ordner>.
tasks.withType<Test>().configureEach {
    systemProperty("waraq.ocr.images", (project.findProperty("waraqOcrImages") ?: "").toString())
    systemProperty("waraq.assets", file("src/main/assets").absolutePath)
    testLogging { showStandardStreams = true; events("passed", "failed") }
}
