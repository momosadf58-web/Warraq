# Technik, Build und Quellen

## Aktuell – 06.11.2026

Warraq **3.4.7 / 20** signiert und über WLAN am S24 installiert; SHA256 `c4ac5416b5fd4934de419cc5698553eda647e1865e07012de4fe2a5146d4ef8a`. Neue `WarraqAi.bookNotes` verwendet die bestehende Ollama-Anbindung: ausschließlich Titel/Autor, vorhandene Beschreibung (bis 6000 Zeichen) und maximal drei vorgegebene belegte Zitate. KI-Output kann nur ein bestehendes Zitat auswählen, keine neuen Zitattexte/URLs als Original liefern. Cache nur im Arbeitsspeicher, keine Änderungen an Bibliothek/ZIP. UI `ReadingHelp.kt`, Integration über `DetailActions.readingNotes`; ohne optionalen Schlüssel/Quellen kein KI-Aufruf. Profil-Datenschutzhinweis aktualisiert.

`WARAQ_CHECK_BUILD=3.4.7`, bei WLAN `WARAQ_ADB_SERIAL=<aktuell entdeckter Endpunkt>`; vorhandene Kopplung verwendet. `Signieren.py` erwartet aktuell Version 3.4.7/20 und prüft das bestehende Zertifikat. Erfolgreicher Build unter `C:\Users\49157\AppData\Local\Waraq\Build\outputs`. Vollständige aktuelle Belege in [[Projekte/Waraq/Codex-Übergabe]].

## Historisch – 05.11.2026

Warraq **3.4.6 / versionCode 19** gebaut, mit bestehendem Zertifikat signiert und als Update am S24 installiert. SHA256 `107061f777426db2af475c3eadbbd70a522d0ed76f6d8baa932fad548453a749`. Aktuelle Belege und Grenzen in [[Projekte/Waraq/Codex-Übergabe]]. Alte Versions-/Funktionsangaben weiter unten sind historisch.

UI bleibt Jetpack Compose/Material3; keine Web-UI oder neue Laufzeitabhängigkeit eingebaut. [UI Skills Baseline](https://github.com/ibelick/ui-skills/blob/main/skills/baseline-ui/SKILL.md) sowie Motion-Kategorie und `emilkowalski/apple-design` über vorhandenes Node/npm gelesen. Web-spezifische Vorgaben sinngemäß auf native Komponenten angewendet. Kein zusätzlicher MCP-Server registriert. Motion verwendet vorhandene Compose-Animationen; Android-Animationsskala bleibt maßgeblich.

`Werkzeuge/Signieren.py` prüft APK-Metadaten für 3.4.6/19, nutzt ausschließlich die bestehende externe Signierkonfiguration, übergibt Kennwörter per Prozessumgebung und prüft das bisherige Zertifikat. Keine Zugangsdaten im Skript/Vault. `S24-Pruefung.py`: `WARAQ_CHECK_BUILD=3.4.6`; `update` mit frischer Sicherung, `final` für abschließenden Datenvergleich. Tatsächliche Build-Ausgabe dieses Laufs: `C:\Users\49157\AppData\Local\Waraq\Build\outputs`.

KI Allam ist inzwischen optional integriert (Ollama Cloud); Schlüssel ausschließlich in der App unter `noBackupFilesDir`, nie auslesen. Buchbeschreibungen und Zitate verwenden weiterhin Quellen-/Regellogik, keine eigene KI-Aufbereitung. Erweiterungsmöglichkeiten wurden erläutert, aber am 05.11. nicht beauftragt oder implementiert.

## Stack

- Kotlin 2.0.21 / Jetpack Compose; Gradle 8.9, Android Gradle Plugin 8.7.3.
- `applicationId` und Namespace `de.mo.bibliothek`; minSdk 26, compile-/targetSdk 35, Build Tools 35.0.1, Java 17.
- Quelltext und signierte/installierte APK: versionName 3.3.2 / versionCode 9; Build und S24-Prüfung am 31.10.2026 erfolgreich. Seit 3.3 nur `arm64-v8a`. Aktueller Abbruchpunkt in `Codex-Übergabe.md`.
- ML Kit Text Recognition 16.0.1 (gebündelt, Latein), ONNX Runtime Android 1.23.0 im Quellstand (installiertes 3.3.2 ebenfalls 1.23.0) mit PaddleOCR PP-OCRv3 (Arabisch), CameraX 1.4.0, Tesseract4Android 4.9.0 (nur noch Rückfall für Arabisch; `deu`/`eng`-Modelle seit 3.3 aus dem Paket entfernt und unter `Historie/tessdata-ab-3.3-ungenutzt` aufbewahrt), Coil/Coil-SVG 2.7.0, AndroidSVG 1.4, AndroidX WebKit 1.12.1.
- Three.js r170 / 0.170.0 liegt lokal in den App-Assets; kein CDN-Aufruf zum Benutzen des Raums nötig. npm-Integritätswert beim Download geprüft. MIT-Lizenz vorhanden.
- Release ist wie die bisherigen privaten Versionen weiterhin debuggable. Deshalb keine vollständige R8-Optimierung/Obfuskierung behaupten; `run-as` war für Datensicherungsnachweise verwendbar.

## Lokale Werkzeuge

- JDK: `C:\Users\49157\Documents\Mos Oase\Android\Werkzeuge\Java\jdk-17.0.20.1+1`
- Android SDK: `C:\Users\49157\Documents\Mos Oase\Android\Werkzeuge\AndroidSDK`
- ADB: `...\AndroidSDK\platform-tools\adb.exe`
- Signierwerkzeug: `...\AndroidSDK\build-tools\35.0.1\apksigner.bat`
- Vorhandener privater Signierschlüssel ausschließlich außerhalb des Vaults: `C:\Users\49157\Documents\Codex\2026-10-29\new-chat\work\bookie-debug.keystore`
- Historisches externes Build-/Signierskript: `...\new-chat\work\build-bookie3.ps1`. Es zeigt die bisherige lokale Signierung, baut aber den alten Projektpfad und benennt die Ausgabe noch Warraq-3.0.apk. Nicht ungeprüft als aktuelles Buildskript verwenden, nicht samt Kennwörtern in diesen Ordner kopieren.
- Öffentliches SHA-256-Zertifikatsmerkmal der bisherigen Signatur: `440335587995d04852b97be898d2462389e5fdc5f5eed89583c93f74f8296d42`. Kein Ersatzschlüssel und keine Deinstallation zum Erzwingen eines Updates.

`Werkzeuge/Build.ps1` baut den maßgeblichen Code; Ausgaben unter `%LOCALAPPDATA%\Waraq\Build\outputs`, Caches außerhalb von OneDrive. Am 30.10.2026 korrigiert (BOM, Stderr-Abbruch, Task-Liste) und mit Debug und Release erfolgreich ausgeführt. Node ist lokal unter `C:\Users\49157\.cache\codex-runtimes\codex-primary-runtime\dependencies\node\bin\node.exe` verfügbar. Python: `C:\Users\49157\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe`. Die frühere Aussage, Node sei nicht installiert, ist überholt. `%LOCALAPPDATA%` kann in der Sandbox umgeleitet sein; Ausgabepfad pro Lauf tatsächlich prüfen. Signierung erfolgt separat mit der bestehenden lokalen Konfiguration; kein Kennwort wird im Vault gespeichert.

## Quellen und Lizenzen

| Ressource | Verwendung und Zustand |
|---|---|
| [Google Books API](https://developers.google.com/books/docs/v1/using) | Bibliografische Suche; zeitweilige 429-Antworten berücksichtigen. Kein API-Key hinterlegt. |
| [Open Library](https://openlibrary.org/developers/api) | Suche, Covers und konkrete Ausgaben über Werke/Editionen. |
| [Tesseract4Android](https://github.com/adaptech-cz/Tesseract4Android) | Kleine lokale OCR-Engine, Version 4.9.0 über beschränktes JitPack-Repository, Apache 2.0. |
| [Tessdata best 4.1.0](https://github.com/tesseract-ocr/tessdata_best/tree/4.1.0) | Arabisch: 12.603.724 Bytes. |
| [Tessdata fast 4.1.0](https://github.com/tesseract-ocr/tessdata_fast/tree/4.1.0) | Deutsch: 1.525.436 Bytes; Englisch: 4.113.088 Bytes. Zusammen mit Arabisch ca. 18,2 MB. SHA256/Original-URLs in `assets/tessdata/manifest.json`, Apache-2.0-Lizenz unter `LIZENZEN`. |
| [The Arabic E-Book Corpus, Andreas Hallberg / Universität Göteborg](https://researchdata.se/en/catalogue/dataset/2024-145/1) | Offener Metadaten-Snapshot zu 1.745 Hindawi-Büchern, CC BY 4.0. Nur Metadaten katalogisiert. `Pruefnachweise/metadata.tsv` und `corpus-README.md` erhalten. |
| [Hindawi-Books-dataset, alielfilali01](https://huggingface.co/datasets/alielfilali01/Hindawi-Books-dataset) | Dataset-Lizenz CC BY-NC 4.0; für dieses private Projekt. Nur BookName, AuthorName, AboutBook und BookLink aus drei Parquet-Dateien projiziert; 3.066 verschiedene Beschreibungen als JSON vorbereitet. Keine Kapiteltexte in der App. Herkunft und Lizenz auch in App-Lizenzhinweisen ergänzen. |
| [Hindawi / Safahat](https://www.safahat.org/books/) | Öffentlich sichtbarer Buchkatalog und Quellenverweise. Direkter HTML-/EPUB-Abruf lieferte 403; nicht umgehen. Cover über `https://downloads.hindawi.org/covers/svg/270x360/<ID>.svg?v=5` erfolgreich geprüft; vertrauenswürdige SVGs werden lokal zu JPEG gerastert, keine externe SVG-Dateiauflösung. |
| [MediaWiki TextExtracts](https://www.mediawiki.org/wiki/Extension:TextExtracts) | Zusammenfassungen mit Quellenlink; Buch-/Autorzuordnung prüfen, keine Biographien als Bücher. |
| [Wikiquote](https://en.wikiquote.org/wiki/Wikiquote:Sourcing) / Wikidata | Nur zur passenden Werkseite verknüpfte Originalzitate; Navigations-/Inhaltsverzeichniszeilen ausscheiden. |
| [PaddleOCR ONNX, monkt/paddleocr-onnx](https://huggingface.co/monkt/paddleocr-onnx) | PP-OCRv3 Textbereiche (`detection/v3/det.onnx`) und arabische Erkennung (`languages/arabic/rec.onnx` + `dict.txt`), Apache 2.0, aus [PaddleOCR](https://github.com/PaddlePaddle/PaddleOCR). Revision und SHA-256 in `assets/paddle/manifest.json`. |
| [ONNX Runtime Android](https://central.sonatype.com/artifact/com.microsoft.onnxruntime/onnxruntime-android) | 1.23.0 im aktuellen Quellstand für Android und JVM, MIT; installiertes 3.3.2 nutzt ebenfalls 1.23.0. |
| [Three.js](https://github.com/mrdoob/three.js/tree/r170) | Offline-WebGL-Raum, MIT. Eigene Raumgeometrie, Kulisse und Materialtexturen im Code erzeugt. |

Die App muss ohne Online-KI gut funktionieren. Es wurde kein Online-KI-Konto eingerichtet, kein Schlüssel gespeichert und kein kostenpflichtiges Abo beauftragt. Internet-Abfragen an Buchkataloge sind vom Nutzer gewünscht; das Foto wird im aktuellen Ansatz lokal verarbeitet.

## Sicherungsformat

`Warraq-Bibliothek-vor-Arbeit.zip`: `manifest.json` mit Format `mos-buecher`, Version 2, Bücherliste; dazu `covers/<UUID>.jpg`. Aus der früheren Datensicherung erstellt, keine neue Gerätesicherung bei der Pause. Niemals zu Testzwecken über eine aktuelle Nutzerbibliothek wiederherstellen.

Nicht übernommen: private Signierschlüssel, Kennwörter, Plattform-/MLKit-Konfiguration, persönliche Fremdprojekte, allgemeine Gradle-/Compiler-/Downloadcaches und installierte Python-Hilfspakete. Zur Reproduktion sind Quellskripte, Versionsangaben, Modelle, Metadaten, Lizenzen und Hashes vorhanden.

Die Beschreibungsaufbereitung verwendete Python 3.12 und DuckDB 1.4.1 als lokales Entwicklungswerkzeug; diese Pakete werden nicht auf dem Handy benötigt. Historisches Skript: `Werkzeuge/Entwicklungsverlauf/build-blurbs.py`. Bei erneuter Datenaufbereitung Arbeits-/Ausgabepfade an die neue Ablage anpassen; die fertige JSON-Datei ist bereits vollständig unter `Code/Android/app/src/main/assets/hindawi-blurbs.json` vorhanden.

Zurück: [[Projekte/Waraq/Start]]

## Kompatibilität – Untersuchung vom 30.10.2026

Mo entschied: **Bibliotheken gezielt aktualisieren und direkte ADB-Sicherungen behalten**. Android und JVM-ONNX-Abhängigkeit sind deshalb im Code auf 1.23.0 erhöht; `isDebuggable=true` bleibt. Die bisherige JNI-Datei hatte 4-KB-PT_LOAD-Alignment; das tatsächliche 1.23.0-Artefakt hat 16 KB. Alle neun alten APK-Bibliotheken waren bereits ZIP-seitig 16-KB-ausgerichtet. RELRO-/Dialogwirkung am S24 bleibt offen. Vollständige Binärprüfung und Primärquellen: `Pruefnachweise/Codex-3.3.1-20261030/kompatibilitaet.md`. Neuer Bau scheiterte zuerst an einem fehlenden Test-Import; kein Erfolg am Gerät behaupten.

## Amazon – Recherche vom 30.10.2026

**Nutzerwunsch:** Amazon-Treffer für richtiges Werk/Ausgabe, Titel, Autor, Internet-Cover und weitere Buchdaten nutzen. In der echten Lens-Ansicht zum arabischen Testbuch waren passende Amazon.de-Treffer sichtbar (`lens-result.json`). Noch keine Amazon-Integration im Code.

Für automatischen offiziellen Produktabruf verweist Amazon auf **Creators API**. Laut aktueller [Einführung](https://affiliate-program.amazon.com/creatorsapi/docs/en-us/introduction) und [Registrierung](https://affiliate-program.amazon.com/creatorsapi/docs/en-us/onboarding/register-for-creators-api) sind Associates-Aufnahme, Zugangsdaten und qualifizierende Verkäufe Voraussetzungen; die Einführung nennt zehn qualifizierende Verkäufe binnen 30 Tagen. PA-API 5 wird als [abgekündigt](https://affiliate-program.amazon.com/creatorsapi/docs/en-us/paapiv5-deprecation) beschrieben. Voraussetzungen bei späterer Umsetzung erneut prüfen.

Die [deutschen Programmrichtlinien einschließlich Mobile-App-Bedingungen](https://partnernet.amazon.de/help/operating/policies) beschränken Produktwerbungsinhalte auf Amazon-Verkaufsförderung. Mobile Partner-Apps brauchen gesonderte Zulassung und reguläre Store-Verfügbarkeit; dauerhafte lokale Amazon-Bildspeicherung ist unter der Standardlizenz nicht vorgesehen, Caching/URL-Nutzung unterliegt Vorgaben. Daraus folgt als technische Einschätzung: keine unkomplizierte anonyme Gratis-API für Warraqs private Offline-Coverbibliothek versprechen.

Mögliche kleine Erweiterung nach Fortsetzung: externer **„Auf Amazon suchen“**-Knopf mit erkanntem Titel/Autor; optional vom Nutzer geteilter Produktlink als Verweis. ISBN/bestätigte Ausgabedaten können anschließend bei passenden Bibliotheks-/Verlagsquellen gesucht werden. Das ist noch **nicht implementiert** und kein automatischer Amazon-Import. Eine direkte API-/Coverübernahme wäre gesondert auf Zugang und Nutzungsbedingungen zu prüfen. Keine Amazon-Konten erstellt, Schlüssel eingerichtet oder Produktseiten automatisiert extrahiert.

Zurück: [[Projekte/Waraq/Start]]


## Fortschritt 31.10.2026

Amazon-Suchknopf inzwischen implementiert und Übergabe an installierte Amazon-App am S24 bestätigt. Automatischer Import weiterhin offen. ONNX-Update erfolgreich gebaut/installiert; alle neun nativen APK-Dateien 16-KB-ausgerichtet, kein Startdialog bei zwei Starts. Ältere Aussagen „noch nicht implementiert“, „Build scheitert“ und „Wirkung offen“ oben geben den Recherche-/Abbruchstand vom 30.10.2026 wieder. Aktuell: [[Projekte/Waraq/Codex-Übergabe]].


## Stand 04.11.2026 – Quellen, KI, Build

- **Deutsche Nationalbibliothek** (`data/DnbCatalog.kt`): SRU `https://services.dnb.de/sru/dnb`, `recordSchema=oai_dc`, ohne Schlüssel. DNB-Cover hinter Bot-Schranke (Anubis), nicht umgehen; Cover über Open Library nach ISBN, sonst Werkcover (`Found.fallbackCover`).
- **Google Books** ohne Schlüssel HTTP 429; ein eigener Schlüssel wäre nur außerhalb des Vaults zulässig.
- **KI Allam** (`data/WarraqAi.kt`): `POST https://ollama.com/api/chat`, `Authorization: Bearer`, `format:"json"`, `think:false`. Modellreihenfolge `beam` → `deepseek-v4.1-flash` → `gemma4:31b` → `gpt-oss:120b` → `gpt-oss:20b` (bei 400/403/404). **Eigener User-Agent nötig**: Ollama weist `Dalvik/…` mit 403 ab. API-Schlüssel von ollama.com/settings/keys; direkt heißt das Modell `beam` (`beam:cloud` nur über die lokale Ollama-App). Schlüssel nur in `noBackupFilesDir/ollama.key`.
- **Build-Ausgabe** auf diesem PC: `C:\Users\49157\AppData\Local\Packages\OpenAI.Codex_2p2nqsd0c76g0\LocalCache\Local\Waraq\Build\outputs\app\outputs\apk\release\` (Sandbox-Umleitung). Vor dem Signieren `output-metadata.json` prüfen.
- `.kts`-Dateien ohne BOM schreiben (PowerShell 5.1 `Set-Content -Encoding utf8` setzt BOM). Arabische Literale nicht über PowerShell-Skripttext ersetzen (Kodierung), Edit-Werkzeug oder Python nutzen.
