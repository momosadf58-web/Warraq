# Fortsetzung für einen anderen Agenten

**Am 31.10.2026 fortgesetzt: 3.3.2 erfolgreich gebaut und am S24 geprüft/installiert. Aktueller Stand und nächste Schritte in [[Projekte/Waraq/Codex-Übergabe]].** Keine weitere Klärungsrunde für bereits beantwortete Fragen beginnen; Anforderungen aus allen sechs Runden zusammen berücksichtigen. Neue echte Unklarheiten gezielt fragen, keine bisherigen Wünsche ersetzen.

## Maßgebliche Ablage

`C:\Users\49157\OneDrive\Desktop\KI MO\AI Brain\Projekte\Waraq`

Aktueller Quellcode: `Code/Android`. Die alte Kopie unter `C:\Users\49157\Documents\Codex\2026-10-29\new-chat\outputs\bookie3` wurde während dieser Arbeit nicht geändert und ist historisch. Der bisherige Arbeitsordner unter `.codex/visualizations/.../warraq-update` ist jetzt ebenfalls ein älterer Arbeitsort, nicht die maßgebliche Quelle. Kein altes Synchronisationsskript blind ausführen.

1. `Start.md`, `Anforderungen.md`, `Stand.md` und `Technik und Quellen.md` lesen.
2. `Dateimanifest.json` und die konkreten Quellen ansehen. Code und signierte/installierte APK entsprechen 3.3.2.
3. Eine erste gezielte Kompilierung des aktuellen Quellstands machen. Nicht jede frühere Testserie wiederholen.
4. Die neuen Abläufe fertigstellen und am S24 prüfen; Prioritäten siehe unten.
5. Erst bei erfolgreichem Stand signieren und als Update installieren. Keine Deinstallation und keine Wiederherstellung in die echte Nutzerbibliothek zu Testzwecken.

## Codekarte

Alle folgenden Pfade relativ zu `Code/Android/app/src/main`:

| Datei/Bereich | Verantwortung |
|---|---|
| `java/de/mo/bibliothek/MainActivity.kt` | Tabs und Ebenen: Bibliothek, Hinzufügen, Statistik, Profil; Kamera, Erkennung, Suche, 3D, Details, Editieren, Ergänzen; Vorschlagsdialog |
| `BookieViewModel.kt` | Gesicherte Änderungen, Suche, OCR, Ausgaben und Coververgleich; nur bestätigte Feldänderungen übernehmen |
| `data/Book.kt` | Kompatibles `library.json`, Status und Datum, unbekannte Felder, arabische Suchnormalisierung |
| `data/LibraryRepository.kt` | Atomare Speicherung, lokale Cover, strenge URL-/ZIP-Prüfung, SVG-Rasterung für Hindawi, Backup/Rückrollschutz |
| `data/BookLookup.kt` | Google Books/Open Library, Quellen, Ausgaben, Ranking; unterschiedliche ISBNs nicht zu einer Ausgabe vermischen |
| `data/ArabicCatalog.kt` | Lokale Hindawi-Metadaten, Werk- und Tokenabgleich |
| `data/CoverRecognition.kt` | Tesseract, Modelle, Fotoauswertung, Titel-/Autorvorschlag |
| `data/CoverFingerprint.kt` | Lokaler einfacher Coververgleich für die Ausgabenauswahl |
| `data/BookKnowledge.kt` | Wikipedia mit Titel-/Autorprüfung, Wikidata-Verknüpfung zu Wikiquote, Quellen und echte Listenzitate |
| `data/ReadingStats.kt` | Lesejahr/Erscheinungsjahr, Buchart, Orakel-Kandidaten und Sprachnamen |
| `ui/CameraScreen.kt`, `RecognitionScreen.kt` | Live-Kamera/ruhige Aufnahme und editierbare Erkennungsergebnisse |
| `ui/AddScreen.kt`, `EditionSheet.kt` | Werk finden, dann Ausgabe wählen, Vorschau und Hinzufügen |
| `ui/MetadataReview.kt` | Explizite Auswahl abweichender Felder; keine stillen Datenkorrekturen |
| `ui/BookRoom.kt` | Lokale WebView-Assets, validierte Buch-IDs, Daten-/Coverübergabe an 3D; fremde Navigation/Requests blockiert |
| `assets/room/index.html`, `room.js` | Räumlicher Souk-Buchladen, Bewegung, Kulisse, echte Cover, Filter und Jahresstapel |
| `ui/StatsRoom.kt` | Normale Statistik für alle Bücher und Einstieg zum 3D-Laden; kein Jahresstapel mehr hier |
| `ui/KnowledgePanel.kt`, `QuoteCard.kt` | Buchinhalte, Quellen, Bildkarte; kurze spoilerarme Anzeige/Kernaussage seit 3.2 angebunden |
| `assets/tessdata`, `assets/paddle` | Tesseract Arabisch als Rückfall und PaddleOCR-Modelle; Latein über ML Kit; Manifeste mit Quellen/Hashes |
| `assets/hindawi-catalog.json` | 1.745 Einträge, Titel/Autor/Genre/Edition-Jahr und Hindawi-Coverpfad |
| `assets/hindawi-blurbs.json` | 3.066 Buchbeschreibungen, seit 3.2 angebunden |
| `../androidTest/java/de/mo/bibliothek` | Frühere gezielte Regressionen und getrennte Online-Probe |

## Nächste Schritte

Die zuvor offenen Build-, Erkennungs-, Such-, Vorbelegungs- und Kompatibilitätsprüfungen wurden am 31.10.2026 erledigt. Weitere Quellenintegration und übrige gezielte Featureabnahme: [[Projekte/Waraq/Codex-Übergabe]].

## Bauen, signieren, installieren

Arabische KI ohne Handy testen: `Werkzeuge/OCR-Testbilder.ps1 -Quelle <Fotos> -Ziel <Ordner>`, dann `gradlew testReleaseUnitTest -PwaraqOcrImages=<Ordner>` (mit demselben Init-Skript wie `Build.ps1`); Ergebnis in `<Ordner>/ergebnis.txt`. Eingespielte Testdateien auf dem Handy danach immer löschen.

`Werkzeuge/Build.ps1 -Target Release` (läuft seit 3.2), danach mit `apksigner` und dem bestehenden externen Debug-Keystore signieren (Befehl wie im historischen Skript, Zertifikat muss `4403…6d42` ergeben), vorher `run-as`-Sicherung von `files/library.json` und `files/covers`, dann `adb install -r` und Hash von `library.json` vergleichen. Für 3D-Fehlersuche: WebView-Debugging über `adb forward tcp:9223 localabstract:webview_devtools_remote_<pid>` (App ist debuggable).

## Fallen aus dieser Sitzung

- Android-Instrumentation läuft mit der Identität der Ziel-App. Für isolierte Daten `ContextWrapper(instrumentation.targetContext)` mit eigenen `files`-/`cache`-Unterordnern verwenden; `applicationContext` ebenfalls überschreiben. Die echten `files/library.json` nie als Testdaten überschreiben.
- Tests gegen die passende Debug-Variante ausführen. Gegen die verkleinerte Release-Variante fehlten Kotlin-Fassaden und interne Methodennamen unterschieden sich (`app_debug`/Release). Das war ein Testaufbaufehler, kein bestandener Test.
- Beide APKs müssen für die Gerätetests passend signiert sein. Temporäre Test-App danach entfernen; **nur** `de.mo.bibliothek.test`, niemals die Nutzer-App.
- Google Books lieferte früher HTTP 429 ohne eigenen Schlüssel. Open Library und lokaler Katalog müssen als sinnvolle Quellen funktionieren, ohne API-Key-Zwang.
- Hindawi/Safahat-Seiten und die geprüfte EPUB-Adresse lieferten beim direkten Abruf 403; keine Schutzumgehung. Öffentliche Cover-SVGs funktionieren, ebenso der offene Metadatenkatalog. HF-Viewerfilter war langsam/Timeout; deshalb wurden Beschreibungen einmalig aus projizierten Parquet-Spalten vorbereitet.
- `Werkzeuge/Entwicklungsverlauf` sind historische, teilweise nicht idempotente Patchskripte mit alten Pfaden. Sie erklären die Entstehung, sind kein Build-Einstieg und dürfen nicht erneut über den fertigen Code laufen.

Zurück: [[Projekte/Waraq/Start]]
