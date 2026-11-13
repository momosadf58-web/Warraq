# Stand – 06.11.2026: Warraq 3.4.7 installiert

Allams Lesehilfe in den Buchdetails ergänzt: kurzer KI-Klappentext aus vorhandenen Quellen, unverändertes ausgewähltes Originalzitat mit getrennter KI-Erklärung, andernfalls als KI/sinngemäß/kein Zitat beschriftete Kernaussage und Bildkarte. Deutsch/Arabisch mit echtem Dienst am S24 über WLAN geprüft, u. a. „أبي الذي أكره“ und „Fahrenheit 451“. 34 JUnit-Einträge ohne Fehler (ein optionaler Bildtest ohne Eingabe); 15 Bücher/12 Cover vor/nach Update und Test bytegleich, Testreste entfernt. [[Projekte/Waraq/Codex-Übergabe]].

# Stand – 05.11.2026: Warraq 3.4.6 installiert

Bestehenden Oliv-/Papierstil mit besserer Hierarchie, beschrifteten Ansichtsaktionen, sichtbarer Suche und breiteren Ausgabendaten verfeinert. Neue Buchdruck-/Cover-/Tab-/Listenanimationen, RTL berücksichtigt. 29 JUnit-Einträge ohne Fehler (ein optionaler Bildtest ohne Eingabe), gezielte UI-Prüfung am S24 in Deutsch/Arabisch. 15 Bücher/12 Cover vor und nach Update/Tests/Bereinigung bytegleich. Drei eigene Handy-Testdateien entfernt; keine alten Installer gefunden. Details und Grenzen: [[Projekte/Waraq/Codex-Übergabe]].

# Stand – 04.11.2026 nachts: Warraq 3.4.5 installiert

Neues App-Symbol (وراق in Ruqaa, Oliv, Messing-Regalbrett) und kleine UI-Beruhigung (Würfel statt Funkeln, leise Regal-Status, Kamera zuerst, ruhiger 3D-Knopf). 15 Bücher bytegleich. Details: [[Projekte/Waraq/Claude-Übergabe-20261103]].

# Stand – 04.11.2026: Warraq 3.4.4 installiert

KI „Allam / علام“ (Ollama `beam`) eingebaut und am S24 geprüft: Orakel, Lesetipps, Bibliothek fragen, Genres (nicht übernommen), Jahresrückblick, Buchfrage, arabische Oberfläche, zweiter Suchversuch bei null Treffern („Kafkka am Straand Murakamii“ → „Kafka am Strand“; Kataloge allein 0 Treffer). 403-Fehler kam vom Android-User-Agent, behoben. 15 Bücher/13 Cover bytegleich. Details: [[Projekte/Waraq/Claude-Übergabe-20261103]].

# Stand – 03.11.2026: Warraq 3.3.5 installiert

Neue DNB-Quelle für deutsche Ausgaben; Titelsuche mit Ausgabenliste und Ein-Klick-Import (Cover, ISBN, Verlag, Jahr, Seiten) am S24 bestätigt. 15 Bücher erhalten. Lateinische Kameraerkennung am echten Buch offen. Details: [[Projekte/Waraq/Codex-Übergabe]].

## Frühere Stände

# Stand – 31.10.2026: Warraq 3.3.2 installiert

Build, gezielte Suchregressionen und S24-Test erfolgreich. Titel/Autor richtig, keine Scheintreffer im arabischen Prüffall, manuelle Vorbelegung und Amazon-Suchknopf bestätigt; kein Kompatibilitätsdialog bei zwei Starts. 12 Bücher/9 Coverdateien bytegleich erhalten. Vollständige aktuelle Ergebnisse und Grenzen: [[Projekte/Waraq/Codex-Übergabe]].

## Historische Stände bis 30.10.2026

Die folgenden Abbruchpunkte und Arbeitsaufträge sind überholt; aktuelle Übergabe oben verwenden.

# Stand – 30.10.2026, nach Codex pausiert

**Arbeit auf Mos Auftrag beendet.** Maßgeblicher aktueller Abbruchpunkt: [[Projekte/Waraq/Codex-Übergabe]].

| Bereich | Tatsächlicher Stand |
|---|---|
| Gerät | Neuester gespeicherter 3.3.1-Bau installiert, SHA-256 `f256a5414251bff9eabe410bf0c130910203eabcb429ed36d5cb464f4b61a33d` |
| Daten | Beim Update 13 Bücher und alle 12 Coverdateien bytegleich; Sicherung `Sicherungen/Geraet-vor-Codex-3.3.1-20261030`. Nach Such-/Lens-Test kein abschließender erneuter Vergleich, kein Buch gespeichert |
| Arabische Kamera | „أبي الذي أكره“ und „عماد رشاد عثمان“ am echten Buch richtig erkannt |
| Werksuche 3.3.1 | Weiterhin falsches anderes Werk desselben Autors gefunden: „أحببت وغدا“. Fehler bestätigt |
| Lens | Google Lens mit Foto tatsächlich geöffnet; passende Buchtreffer einschließlich Amazon sichtbar. Keine Übernahme in Warraq |
| Code 3.3.2 / 9 | Strukturierter erwarteter Titel für Kamerasuche und ONNX Runtime Android/JVM 1.23.0 ergänzt; Debugfähigkeit erhalten |
| Letzter Build | `testReleaseUnitTest assembleRelease` fehlgeschlagen: neue Tests, `LogicTest.kt:18/:25`, fehlender Import `de.mo.bibliothek.data.Found`. Produktions-Kotlin kompiliert, kein abschließender erfolgreicher APK-Build; 3.3.2 nicht signiert/installiert |
| Tests | Zwei neue Regressionen im Code, insgesamt 20 vorgesehen; kein bestandener 20-Test-Lauf. Ältere Angabe „18 grün“ ohne gefundenen zugehörigen JUnit-Bericht |
| Startdialog | 3.3.1 zeigt ihn weiterhin; PT_LOAD-Fehler im neuen ORT-Artefakt behoben, Wirkung auf tatsächlichen Dialog noch offen |
| Amazon | Neuer Nutzerwunsch und offizielle Quellen gespeichert; noch nicht integriert |

Belege: `Pruefnachweise/Codex-3.3.1-20261030`. Nächster technischer Schritt nach neuem Auftrag: fehlenden Test-Import ergänzen, gezielt bauen und erst danach signiertes Update mit Sicherung prüfen. Manuelle Vorbelegung, neuer Suchfilter und Startdialog bleiben am neuen Bau zu prüfen.

Die folgenden Abschnitte sind **historische Aufzeichnungen**. Ihre damaligen Aussagen „3.2 installiert“, „3.3 noch nicht installiert“ oder „nächster Schritt installieren“ ersetzen nicht den aktuellen Stand oben.

## Historische Meilensteine vor Codex – überholte Versionsstände

Diese Datei beschreibt den tatsächlichen Stand; Anforderungen sind keine Fertigstellungsnachweise.

**Neuester Stand (ca. 22 Uhr):** 3.3 und ein Zwischenbau 3.3.1 wurden am S24 installiert und live getestet (Souk 120 fps, Antippen, Hundeherz, Kamera lateinisch und arabisch). Der neueste 3.3.1-Bau (`F256A541…`) mit den letzten Erkennungs-, Such- und Lens-Korrekturen ist signiert, aber wegen gesperrtem Handy noch nicht installiert. Abbruchpunkt und nächste Schritte: [Codex-Übergabe](Codex-Übergabe.md).

**Kurz:** Auf dem S24 läuft 3.2 (versionCode 6) inklusive des Kamera-Umbaus mit ML Kit. **Warraq 3.3 (versionCode 7)** ist fertig gebaut und gleich signiert (`Ausgaben/Signierte-Zwischenstaende/Warraq-3.3.apk`, 73,7 MB, SHA-256 `A9C306D5…36B5`, finaler Bau nach allen PC-Prüfungen; größte Teile: ONNX Runtime 17,6 MB, ML Kit 11,1 MB, arabische Modelle, unkomprimierte native Bibliotheken. Ein früherer 3.3-Zwischenbau war 68,6 MB groß; die Ursache des Unterschieds ließ sich nicht mehr vergleichen, weil die Datei überschrieben wurde.), aber **noch nicht installiert**, weil Mo das Handy getrennt hat. 3.3 enthält zusätzlich den neuen 3D-Souk und die arabische Texterkennungs-KI. Nächster Schritt mit Handy: Daten sichern, `adb install -r Warraq-3.3.apk`, Hash von `library.json` vergleichen, dann Kamera (lateinisch und arabisch) und 3D am Gerät abnehmen.

## Installiert und gesichert

- Gerät: Samsung Galaxy S24 Ultra, SM-S928B, ADB-Kennung `R5CX157KZGK`.
- Installiert: **Warraq 3.2 / versionCode 6** als gleich signiertes Update (`adb install -r`), Paket `de.mo.bibliothek`. Nutzer-App nie deinstalliert (`firstInstallTime` weiterhin 29.10.2026 15:17).
- APK: `Ausgaben/Signierte-Zwischenstaende/Warraq-3.2.apk`, entspricht dem aktuellen Quellstand `Code/Android`. Zertifikat SHA-256 `4403…6d42` wie bisher.
- Vor dem Update frisch gesichert: `Sicherungen/Geraet-vor-Update-3.2` mit `library.json`, allen 12 Coverdateien und `bookie-prefs.xml`. 13 Bücher, 9 referenzierte lokale Cover, alle vorhanden.
- Nach jeder Installation und nach dem Gerätetest war `library.json` bytegleich zur Sicherung (SHA-256 `A7B965C2…7551`), 12 Coverdateien erhalten.
- Ältere Sicherungen (`Warraq-Bibliothek-vor-Arbeit.zip`, `Android-Dateien-bereinigt.zip`) bleiben unverändert erhalten.

## In 3.2 neu und am S24 geprüft

| Bereich | Ergebnis am Gerät |
|---|---|
| Hindawi-Beschreibungen | `hindawi-blurbs.json` angebunden (Hindawi-ID oder gleicher Werktitel mit passendem Autor, sonst nichts). „ماجدولين“ und „التفكير السريع والبطيء“ zeigen jetzt den Hindawi-Klappentext mit Quelle; vorher fand „ماجدولين“ nichts. Funktioniert auch offline. Katalogtreffer bei der Suche bringen die Beschreibung mit. |
| Kurzer Klappentext | Nur noch erste Sätze bis ca. 320 Zeichen, Sätze mit Handlungsende-Hinweisen fallen weg, kein „Weiterlesen“ mehr; Quelle verlinkt. Geprüft an Hindawi- und Wikipedia-AR-Text (فهرنهايت 451). |
| Kernaussage | Nur wenn keine belegten Zitate da sind: ein themenbenennender Satz der Beschreibung, beschriftet „Kernaussage laut Beschreibung · kein Zitat“, ohne Anführungszeichen, mit Quelle. Geprüft an Kahneman. |
| Bildkarten | Zitatkarte (Fahrenheit 451, Wikiquote EN, keine Inhaltsverzeichnis-Zeilen mehr) und Kernaussagekarte (Arabisch RTL korrekt, Beschriftung statt « ») im echten Android-Teilen-Dialog geprüft; jeweils ohne Senden geschlossen. |
| 3D-Buchladen | War vorher **leer**: WebView ohne `MATCH_PARENT` gab der Seite 0 px Höhe (kein Bild, kein Steuerkreuz). Behoben, rendert jetzt. Dunkleres Licht mit warmen Hängelampen, Nebel, dunkleres Holz. Breiterer Blickwinkel im Hochformat. Eigene gelesene Bücher stehen gemischt, aber zuerst in den vom Eingang sichtbaren Regalfeldern. Antippen eines eigenen Covers öffnete die Details von „Herz eines Hundes“. Jahresziel-Anzeige „2026: 3 / 15“ und Stapel auf dem Tisch sichtbar. Keine JS-Fehler im Log. |
| Skalierung 3D | Jedes eigene Buch hat einen eigenen Platz (2 Seiten × 5 Felder × 5 Böden × ≥4), bei über 200 Büchern werden Cover schmaler statt überlagert. Jahresstapel verteilt sich auf zwei Tischstapel und zwei Bodenstapel. Nur im Code, nicht mit großer Testbibliothek am Gerät geprüft. |
| Doppeltes `updateRoom` | Generationssperre eingebaut (nur der letzte Aufruf baut die Szene). |
| Buildskript | `Werkzeuge/Build.ps1` lief vorher nicht (PowerShell-5.1-BOM im Gradle-Init-Skript, Abbruch an Gradle-Stderr, Task-Name in Zeichen zerlegt). Alle drei Fehler behoben, Debug und Release bauen. |

Belege: `Pruefnachweise/3_2-*.png`, `Pruefnachweise/Build-Protokolle/release-3.2.log`.

## Meilenstein Kamera/Erkennung (30.10.2026, ca. 20 Uhr, installiert)

Mos Praxistest mit „The Dictionary of Obscure Sorrows“ (dunkles Zimmer, Goldprägung) ergab mit dem alten Stand Unsinn („STSELLER/YORK TI“, „wer/gee…“). Ursachen: erste Zeile als Titel, Tesseract mit drei Sprachen gleichzeitig auf Goldschrift/Schwarz, Foto viel breiter als der Kamerarahmen.

Umgebaut: ML Kit Texterkennung (gebündelt, offline, ca. 4 MB) für lateinische Schrift; Tesseract nur noch Arabisch auf kontrastaufbereitetem, ggf. invertiertem Bild. Zuschnitt auf den Kamerarahmen, EXIF-Drehung, Vergrößerung kleiner Bilder. Titel = größte Schrift samt anschließenden Zeilen, Werbe-/Gattungszeilen („Bestseller“, „Roman“) ausgeschlossen. Arabisch gewinnt nur bei Katalogtreffer oder sicheren echten Wörtern. Suche probiert bei null Treffern parallel Wortpaare (Lesefehler wie „JORN KOENG“). Kamera: Qualitätsmodus, Licht-Knopf. Fotos werden nach der Erkennung gelöscht (lagen vorher im Cache). Neu: Coverfoto aus Galerie über „Teilen → Warraq“ erkennen (`singleTask`, damit keine zweite App-Instanz parallel speichert).

Am S24 mit echten Bildern geprüft: Mos Kamerafoto → „DICTIONARY OF OBSCURE SORROWS / JORN KOENIG“ in unter 1 s, Werksuche zeigt als ersten Treffer das richtige Buch mit Cover. Arabisches Cover „ماجدولين“ → „ماجدولين / مصطفى لطفي المنفلوطي“ (Hindawi-Katalog), ca. 4 s. Nichts hinzugefügt; `library.json` blieb bytegleich.

Noch offen: erneuter Live-Kameratest mit Rahmenzuschnitt und Licht-Knopf, ein echtes Foto eines arabischen Buchs, Ausgabenwahl nach dem Treffer.

## Meilenstein 3D-Souk (30.10.2026 abends, in 3.3, noch nicht installiert)

Mos Kritik: Raum „nicht authentisch und arabisch/wabi-sabi genug“. `room.js` neu aufgebaut als alter Damaszener Bücher-Souk: ein enger Gang (4,5 m Raum, Gehbereich ±0,95 m), walnussdunkle Regale mit ungleichen, leicht schiefen Böden, Lücken, angelehnten und liegenden Büchern, Lederrücken mit Goldbändern, Schriftrollen obenauf. Kalkputz mit Flecken, Rissen und Fehlstellen, Ablaq-Streifen (Basalt/Kalkstein) an Rückwand, Pfeilern und Bogen. Ausgetretene, unregelmäßige Steinplatten, verblichener Kelim mit Fransen. Vier durchbrochene Messinglaternen als einzige Lichtquellen mit leichtem Flackern, Staub im Licht. Hinten Maschrabiyya mit achtzackigen Sternen vor warmem Hoflicht, niedriger Tisch mit Messingtablett und Teeglas, Kissen, Tonkrüge. Holztafel „الورّاق“ in Goldschrift an Ketten. Eigene Bücher weiterhin mit echten Covern, nun mit kleinem Messingschild darunter. Jahresstapel auf dem Tisch und daneben. Pfeiltasten/WASD und Q/E als Zusatzsteuerung. Alles lokal erzeugt, keine Downloads.

Geprüft nur im Desktop-Browser mit Platzhalterdaten über einen lokalen Testserver: keine Konsolenfehler, Ansichten unter `Pruefnachweise/3_3-souk-vorschau-*.jpg` (die Gang-Ansicht ist vor der letzten Licht- und Schild-Korrektur entstanden). Am S24 sind noch zu prüfen: Bildrate, Antippen der eigenen Bücher, echte Cover, Steuerung im engen Gang.

## Meilenstein arabische Texterkennungs-KI (30.10.2026 nachts, in 3.3, nicht installiert)

Auf Mos Angebot („Text-Erkennungs-KI bis 100 MB“): PaddleOCR PP-OCRv3 lokal über ONNX Runtime 1.20.0. Modelle aus [monkt/paddleocr-onnx](https://huggingface.co/monkt/paddleocr-onnx) (Apache 2.0, Revision `7b02d0a3…`): Textbereichserkennung `det.onnx` 2,4 MB und arabische Zeilenerkennung `arabic_rec.onnx` 9,0 MB, Herkunft und SHA-256 in `assets/paddle/manifest.json`. Code: `data/ArabicOcr.kt` (Android-frei, damit am PC testbar). Ablauf in der App: ML Kit für Latein; ist das unsicher, liest PaddleOCR Arabisch; Tesseract nur noch als Rückfallebene. App nur noch arm64 (S24), dadurch APK 68,6 MB statt 95,2 MB.

PC-Test mit den echten Modellen (`PaddleArabicTest`, Bilder über `Werkzeuge/OCR-Testbilder.ps1`, Ergebnis `Pruefnachweise/3_3-arabische-ki-pc-test.txt`): echtes Cover „ماجدولين“ → „ماجدولين“ 99 %, „أوتحت طلال الزيزفون“ 97 % (Leerzeichen fehlt), „مصطفى لطفي المنفلوطي“ 98 %, rund 0,25 s. Künstliches Gold-auf-Dunkel-Cover: „الأيام“ 99 %, Autor „ه حسين“ (das kurze „طه“ verliert einen Buchstaben). Lateinisches Foto liefert dort nur unsichere Bruchstücke und gewinnt in der App nicht gegen ML Kit. Am S24 noch nicht gelaufen.

## Feinschliff ohne Handy (30.10.2026 nachts, in der finalen 3.3)

- **12 PC-Tests**, alle grün (`Pruefnachweise/3_3-pc-tests.txt`): Titel = größte Schrift statt erster Zeile (Mos echtes Cover-Layout), Werbe-/Gattungszeilen nie Titel, markierte Autorzeile („تأليف“), unsicherer Kauderwelsch ignoriert, Klappentext kurz ohne Ende-Spoiler, Kernaussage bevorzugt „هذا الكتاب / dieses Buch“-Sätze und wiederholt nichts aus dem Klappentext, Zeilenzusammenführung, Wortstamm-Titelabgleich, arabische Leserichtung, PaddleOCR mit echten Modellen.
- Dabei behoben: Ein kleinerer Autorname direkt unter dem Titel wurde an den Titel gehängt („Die Wand Marlen Haushofer“). Anschlusszeilen brauchen jetzt mindestens 45 % der Titelhöhe und engen Abstand.
- **Werkzuordnung:** „Herz Eines Hundes -- Sataniade“ findet jetzt den Artikel „Hundeherz“ (Suche mit Haupttitel ohne Untertitel, zweiter Abgleich über Wortstämme, weiterhin nur bei „Erzählung/Roman/Buch“ und Autorname im Artikel; exakte Titel haben Vorrang). Live gegen Wikipedia bestätigt. Vollständige Gegenprobe über alle 13 Bücher mit nachgebildeter App-Logik (langsam, wegen Wikipedia-Drosselung): **kein falscher Treffer**; richtig gefunden „التفكير السريع والبطيء“, „فهرنهايت 451“, „نظام التفاهة“ (neu), „Hundeherz“ (neu), „The Man Who Mistook His Wife for a Hat“. Zusätzlich bestätigt die App den Autor jetzt über Wikidata (P50, alle Sprachen), wenn er im Artikel in anderer Schrift steht: „مئة عام من العزلة“ mit lateinisch gespeichertem „Gabriel García Márquez“ wird damit zugeordnet (Wikidata live geprüft, Namensvergleich als Test). Bei „كافكا على الشاطئ“ steht in Mos Daten „ماروكامي“ statt „موراكامي“; die App ändert das nicht still, Korrektur über die Vorschlagsprüfung. Für die übrigen Bücher gibt es keinen passenden Wikipedia-Artikel; dort greifen Hindawi oder die eigene Beschreibung.
- **Paket:** ungenutzte Tesseract-Modelle Deutsch/Englisch (5,6 MB) nach `Historie/tessdata-ab-3.3-ungenutzt` verschoben; die App löscht deren alte Kopien auf dem Handy bei der nächsten Erkennung.
- **3D-Souk im Browser mit echten Covern geprüft:** arabische Oberfläche (RTL, Filtertexte, Jahresziel), 230 Bücher ohne Überlappung, Antippen öffnet das richtige Buch, Genre-Filter dunkelt Nicht-Treffer ab. Der Jahresstapel türmte sich bei 40 Büchern zu einem 1,4-m-Turm mit überlappenden Stapeln: jetzt ein Stapel neben dem Teetablett plus vier Bodenstapel, Tonkrüge versetzt.
- Alte Gerätetests (`androidTest`) kompilieren weiterhin.
- **Ausgabenfluss durchgesehen:** Die beste Ausgabe trägt jetzt den Hinweis „Cover ähnelt deinem Foto“, wenn ihr Cover-Fingerabdruck höchstens 14 von 64 Bits abweicht (fremde Cover liegen um 32). Die Vorschau vor dem Hinzufügen zeigt nur noch den kurzen, spoilerfreien Klappentext; der volle Text bleibt gespeichert.
- **Kamera-Sackgasse behoben:** Nach abgelehntem Kamerazugriff gibt es einen Knopf „Kamera erlauben“, nach zweiter Ablehnung „In den Einstellungen erlauben“; beim Zurückkehren erkennt die Kamera das neue Recht.
- App-Kennung für Buchquellen auf „Warraq/3.3“ aktualisiert.

## Aufgeräumt (30.10.2026, auf Mos Wunsch)

- Nach `.trash/Waraq-Aufraeumen-2026-10-30` verschoben (wiederherstellbar, 42,8 MB): unsignierte/Debug-Zwischenbuilds vom Stand vor der Wiederaufnahme und zwei reine Test-APKs samt Signaturdateien. Behalten: signierte 3.1, 3.2 (installiert, Rückfall) und 3.3.
- Gelöscht außerhalb des Vaults: temporäre Testkopien (Mos Kamerafotos, Testcover, Testseiten, heruntergeladene Modellkopien). Auf dem Handy waren die eingespielten Testdateien schon vorher entfernt.
- `Historie` und `Pruefnachweise` bleiben vollständig; die doppelte Schriftdatei `aref` wird vom Build zweimal gebraucht (3D-Raum und App).
- Status: pausiert bis Mo das Handy anschließt; dann 3.3 installieren und abnehmen.

## Nicht erneut geprüft

- Live-Kamera nach dem Umbau, Ausgabenwahl mit Coververgleich, Scanner-/Foto-Abbruch.
- Die elf Regressionstests wurden nicht erneut ausgeführt (Instrumentierung würde eine Test-App neben der Nutzer-App installieren). Neue Funktionen `shortBlurb`/`keyIdea` sind nur am Gerät über die Oberfläche geprüft.
- 3D-Filter (Genre/Lesejahr/Buchart) und das Gehen per Steuerkreuz nur kurz bedient, nicht systematisch.

## Bekannte offene Punkte

1. 3.3 installieren und am S24 abnehmen: Souk (Bildrate, Antippen, Cover), Live-Kamera mit Zuschnitt/Licht, echtes arabisches Buch über PaddleOCR.
2. Ausgabenwahl nach Kameratreffer mit Coververgleich, Abbruchwege.
3. Werkzuordnung und enger Gang sind in 3.3 erledigt (siehe oben), am Gerät noch anzusehen.
4. Optionaler Online-KI-Dienst ist weiterhin nicht integriert und nicht Voraussetzung.
