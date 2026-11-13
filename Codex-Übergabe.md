# Codex-Übergabe – 06.11.2026, Warraq 3.4.7 installiert

Dieser oberste Abschnitt ersetzt den Stand vom 05.11. Mo hat den KI-Ausbau für Beschreibungen/Zitate freigegeben und WLAN-Debugging aktiviert. Kein REA-/UI-Neuaufbau in dieser Runde.

## Installiert und geprüft

- **Warraq 3.4.7 / versionCode 20**, APK `Ausgaben/Signierte-Zwischenstaende/Warraq-3.4.7.apk`, SHA256 `c4ac5416b5fd4934de419cc5698553eda647e1865e07012de4fe2a5146d4ef8a`, bestehendes Zertifikat `440335587995d04852b97be898d2462389e5fdc5f5eed89583c93f74f8296d42`. Über bereits autorisiertes WLAN-ADB installiert, keine neue Kopplung oder Schlüsselabfrage nötig.
- **Allams Lesehilfe** in Buchdetails: automatischer kurzer Klappentext aus vorhandener Beschreibung/Quellentext; bei belegten Originalzitaten Auswahl eines vorhandenen Zitats und getrennte Erklärung. Gibt es keine verwendbare Auswahl, kann eine klar beschriftete sinngemäße Kernaussage erscheinen. Originaltexte/Quellen bleiben sichtbar, vorhandene Buchdaten unverändert.
- KI liefert nur `quoteIndex`, niemals den übernommenen Zitattext oder dessen URL. Parser akzeptiert nur einen gültigen Index auf vorgegebene wörtliche Zitate mit HTTPS-Quellenlink; Quellobjekt bleibt identisch. Ohne Beschreibung werden KI-Klappentext/Kernaussage verworfen. Das prüft Datenherkunft und Grenzen, garantiert keine fehlerfreie KI-Paraphrase; Texte tragen „KI · kann irren“.
- Mit aktivem optionalem Schlüssel automatisch nach Laden der Buchquellen; ohne Quelle kein KI-Abschnitt, ohne Schlüssel bisheriger Ablauf. Erfolgreiche Ergebnisse im ViewModel-Sitzungscache nach Buch/Titel/Autor/Quelltext/Zitaten/Oberflächensprache, bei Schlüsseländerung geleert. Keine KI-Texte in library.json oder ZIP-Sicherungen geschrieben. Bei Fehler bleiben Quellen sichtbar, „Erneut versuchen“ erreichbar.
- Bildkarte der KI-Kernaussage mit explizitem Label „sinngemäß · KI · kein Zitat“, ohne Zitatzeichen und mit Quellengrundlage. Bestehende Originalzitatkarten bleiben unverändert. Profilhinweis zu automatisch erstellten Lesehilfen und übertragenen Beschreibungen/Zitaten ergänzt. Keine Notizen/Fotos oder Geschmacksbibliothek an diese neue Funktion gesendet.

## Prüfergebnisse

- `testReleaseUnitTest assembleRelease` erfolgreich: **34 JUnit-Einträge**, darunter fünf neue BookNotesTest (Originalobjekt statt erfundenem Zitat/URL, keine Beschreibung → keine generierte Beschreibung/Idee, ungültiger Index → keine Erklärung, keine Paraphrasen/unbelegten Zitate als Originale, keine privaten Notizen/Listen/IDs im Lesehilfe-Prompt). Ein vorhandener optionaler Bildtest ohne Bildpfad ist weiterhin ein No-op; 33 weitere Checks ausgeführt.
- Echter S24-/Ollama-Test: **„أبي الذي أكره“** erzeugt KI-Klappentext und sinngemäße Kernaussage auf Arabisch und Deutsch. Grundlage Alrewaq; Quellentext blieb darüber sichtbar. Arabische Bildkarte erstellt, lokal geprüft, Teilen-Auswahl abgebrochen, **nichts versendet**.
- **„Fahrenheit 451“**: drei über Wikiquote gefundene englische Originalauszüge; Allam wählt einen unverändert aus und erklärt ihn auf Arabisch. Quellenlink bleibt Wikiquote, Beschreibung beruht auf Wikipedia AR. Screenshots zeigen Original und Erklärung getrennt.
- **15 Bücher und 12 Cover bytegleich** vor/nach Update und nach sämtlichen Prüfungen. Frische Sicherung `Sicherungen/Geraet-vor-Codex-3.4.7-20261106`, Bibliotheks-SHA256 `d29c9eb7d7042e3789a1ba50b05e6c63d629406ff22a432d7323d9b0cd2bee58`. Keine Bücher, Bewertungen, Notizen, Genres oder Status geändert. Zuletzt Bibliothek auf Arabisch.
- Eigene neue Testreste `/sdcard/warraq-codex-ui.xml` und `cache/share/warraq-quote.png` nach lokalem Beleg entfernt, Abwesenheit nachgeprüft. Andere Apps/Downloads unverändert.

## Fortsetzung und Dateien

Belege `Pruefnachweise/Codex-3.4.7-20261106`: Build, sechs Test-XMLs, Signatur, Update-/Endvergleich, Screenshots der deutschen/arabischen Lesehilfe, Zitat-Erklärung, Bildkarte. Produktionsänderungen: `data/WarraqAi.kt`, `BookieViewModel.kt`, `MainActivity.kt`, `ui/KnowledgePanel.kt`, neues `ui/ReadingHelp.kt`, DetailActions und Profilhinweis. Keine neue Bibliothek/kein anderes Modell eingebaut. Vorherige Dateien unter `Historie/vor-3.4.7-20261106`.

`S24-Pruefung.py` unterstützt jetzt `WARAQ_ADB_SERIAL` für die jeweils tatsächlich entdeckte WLAN-Adresse; bei fehlender Variable weiterhin USB-Seriennummer. Erst `adb mdns services`/`devices -l` prüfen, keinen alten Port ungeprüft übernehmen. `WARAQ_CHECK_BUILD=3.4.7` wählt aktuelle Hash-/Sicherungsordner. `Signieren.py` prüft aktuell 3.4.7/20 und nutzt den bestehenden externen Schlüssel. Schlüsseldateien im App-Bereich niemals auslesen.

Nicht neu geprüft: echter lateinischer Kameratest, Fehlerfall mit absichtlich ungültigem Schlüssel oder abgeschaltetem Internet. Vorhandene Schlüssel dafür nicht verändern. Frühere Grenzen zu Google Books/Amazon gelten weiter. Aktueller KI-Auftrag umgesetzt; die früheren UI-/Funktionsstände unten sind historisch.

---

# Historisch – 05.11.2026, Warraq 3.4.6 installiert

Dieser oberste Abschnitt hat Vorrang. Vor der Arbeit aktuelle Claude-Übergabe und Code gelesen: 3.4.5 mit DNB, Allam und Ein-Klick-Import war die Ausgangsbasis; ältere Codex-Arbeit nicht darüberkopiert.

## Erledigt: UI, Motion und Handy-Bereinigung

- **3.4.6 / 19 installiert**, APK `Ausgaben/Signierte-Zwischenstaende/Warraq-3.4.6.apk`, SHA256 `107061f777426db2af475c3eadbbd70a522d0ed76f6d8baa932fad548453a749`. Bestehendes Zertifikat `440335587995d04852b97be898d2462389e5fdc5f5eed89583c93f74f8296d42`; Update ohne Deinstallation.
- Bibliothekskopf entzerrt: Orakel/Allam beim Schriftzug; Anzahl und beschriftete Ansicht/Sortierung in eigener Zeile. Bestehende Filter, drei Ansichten und Sortierarten erhalten.
- Buchdruck mit leichter Skalierung und Kippung, Buchcover beim Öffnen mit kurzem Eintritt. Tab-Übergänge folgen der Navigationsrichtung, bei RTL gespiegelt. Native Listenanimation bei Sortierung/Filtern. Keine Dauerschleifen; Compose berücksichtigt Android-Animationsskala, am Gerät war diese 1.
- Hinzufügen: sichtbarer Suchknopf zusätzlich zur Tastatur; ruhigere Überschrift, Aktionskarten wachsen mit Inhalt, zuletzt hinzugefügte Bücher links-/leserichtungsbündig, erklärender Text kontrastreicher. Ausgabenliste: Knopf unter den Metadaten statt daneben, damit ISBN/Verlag/Jahr mehr Breite haben. Profilüberschrift auf dasselbe Größenmaß abgestimmt.
- **Produktionsänderungen nur UI + Versionsnummer.** Suche, OCR, KI, Persistenz, Modelle, Raum und Datenformate unverändert. Keine neue Laufzeitabhängigkeit.
- `testReleaseUnitTest assembleRelease` erfolgreich: **29 JUnit-Einträge**, davon optionaler `preparedImages` ohne Bildpfad ohne OCR-Durchlauf; übrige 28 Checks ausgeführt. Keine neue OCR-/KI-Qualitätsprüfung behaupten.
- S24: Regal → Raster → Liste → Regal, Buchdetail/Zurück, Haupttabs, sichtbarer Suchknopf mit „Der Alchimist Coelho“, echte Ausgabenliste (DNB/Diogenes, ISBN/Jahr), deutsche und arabische Oberfläche geprüft. Kein Buch hinzugefügt/gelöscht, kein Status oder Genre geändert. Zuletzt Bibliothek auf Arabisch. Ein zwischenzeitlicher Sprachwechsel wurde beobachtet; beim gezielten erneuten Arabisch-Test blieb die Sprache über Tabs und Detailnavigation erhalten. Ursache des einzelnen Wechsels nicht belegt.
- **15 Bücher / 12 Coverdateien bytegleich** beim Update und nach allen Tests/Bereinigung. Frische Sicherung `Sicherungen/Geraet-vor-Codex-3.4.6-20261105`; Bibliotheks-SHA256 `d29c9eb7d7042e3789a1ba50b05e6c63d629406ff22a432d7323d9b0cd2bee58`. Frühere 13. Coverdatei war schon zuvor bereinigter Testrest.
- Handy auf ausdrückliche Auswahl nur projektbezogen bereinigt: `/sdcard/warraq-codex-ui.xml` und zwei eigene Motion-Testvideos entfernt (5.623.131 Bytes). Fertiger Clip lokal gesichert und SHA256 vor Entfernen verglichen. Keine alten Warraq-/bookie-Installer im Download-Ordner gefunden; keine persönlichen Downloads, ZIP-Sicherungen, Apps oder Schlüssel entfernt. Frühere Bereinigung vom 04.11. bleibt dokumentiert.

## Belege und Fortsetzung

`Pruefnachweise/Codex-3.4.6-20261105`: Build, fünf JUnit-XMLs, Signatur, `update-result.json`, `final-data-check.json`, `cleanup.json`, Screenshots und `motion.mp4`. `gfxinfo.txt` ist eine kurze Gerätestichprobe unter UI-/Aufnahmebetrieb, kein vergleichender Leistungsbenchmark. Source-Snapshot der veränderten Dateien unter `Historie/UI-vor-3.4.6-20261105`.

Mos letzte Fragen: KI ist bereits für Orakel, Tipps, Bibliotheks-/Buchfragen, Genres, Jahresrückblick und Suchvarianten integriert. **Beschreibung und Zitate haben keine eigene KI-Aufbereitung**: vorhandene Beschreibung/Quellentext wird regelbasiert gekürzt, Zitate aus verknüpften Wikiquote-Werkseiten, sinngemäße Kernaussage regelbasiert und getrennt beschriftet. Als mögliche Erweiterung erläutert: quellengebundene spoilerarme KI-Klappentexte, Erklärung/Auswahl belegter Zitate und explizit sinngemäße Aussagen. Noch kein Auftrag zur Implementierung dieser Ergänzungen. Keine erfundenen Originalzitate.

Weiter offen aus früherem Umfang: echter lateinischer Kameratest; Google Books ohne eigenen Schlüssel gedrosselt; Amazon nur externer Suchzugang. Aktueller UI-Auftrag umgesetzt, keine neue Produktfunktion ungefragt begonnen. Projekt-/Schlüsselregeln in `AGENTS.md` beachten.

---

# Historisch: Codex-Übergabe – 31.10.2026, Warraq 3.3.2 installiert

## Stand 03.11.2026 (Claude Code) – Warraq 3.3.5 installiert

Mo hat „beende endlich diesen projekt“ beauftragt (Android Studio und JADX installiert, S24 angeschlossen). Dieser Abschnitt hat Vorrang vor den älteren Angaben weiter unten.

**Installiert und am S24 geprüft: Warraq 3.3.5 / versionCode 12**, `Ausgaben/Signierte-Zwischenstaende/Warraq-3.3.5.apk`, SHA-256 `5268055437c3b5dc904dda3a3d93cb3601f4c38022517e9b62a80a26b0741d0e`, Zertifikat unverändert `440335…296d42`. Zwischenstand 3.3.4 (`c686a016…6b82de`) war kurz installiert. Belege: `Pruefnachweise/Claude-3.3.4-20261103`, `Pruefnachweise/Claude-3.3.5-20261103`.

- **Ursache schwacher lateinischer Treffer:** Google Books antwortet ohne API-Schlüssel mit HTTP 429 (am PC reproduziert), Treffer kamen praktisch nur aus Open Library.
- **Neu: Deutsche Nationalbibliothek** (`data/DnbCatalog.kt`, SRU ohne Schlüssel). Jeder DNB-Datensatz ist eine konkrete Ausgabe mit Titel, Autor, ISBN, Verlag, Jahr, Seiten, Sprache, Genre. Freitext „Titel Autor“ wird als CQL `tit=… or (tit=Resttitel and per=Nachname)` gesendet; Resttitel ohne Wort ab 4 Buchstaben entfällt (sonst kamen beliebige Bücher des Autors). Läuft in `BookLookup.searchExact` für nichtarabische Suchen parallel zu Google/Open Library.
- **Cover:** DNB-Cover sitzen hinter einer Bot-Schranke (Anubis) und werden nicht umgangen. Ausgabe-Cover = Open-Library-Cover per ISBN; zusätzlich neues Feld `Found.fallbackCover` = Cover desselben Werks aus den übrigen Treffern. Ausgabenliste zeigt dieses Werkcover, `addFound` lädt erst das Ausgabe-Cover, sonst das Werkcover.
- **Am S24 geprüft:** Suche „Der Alchimist Coelho“ → Werk „Der Alchimist“ → Ausgabenliste mit Diogenes 2026/2023/2021 usw., je ISBN, Verlag, Jahr, Cover, Knopf „Hinzufügen“. Ein Klick übernahm Titel, Autor, ISBN 9783257072723, Diogenes, 2023, 217 Seiten, Deutsch, Belletristik und lokal gespeichertes Cover; Buchtext „Über das Buch“ erschien automatisch. Testbuch danach in der App gelöscht. Arabischer Verlagsimport (Alrewaq, „أبي الذي أكره“ mit Cover/ISBN/Jahr) war schon unter 3.3.3 per Klick bestätigt und ist Mos echtes Buch in der Bibliothek.
- **Daten:** Gerätebestand jetzt **15 Bücher**. Vor jedem Update gesichert (`Sicherungen/Geraet-vor-Claude-3.3.4-20261103`, `…-3.3.5-20261103`), Bibliothek und Cover nach Update bytegleich; nach dem Test Bibliothek wieder identisch mit der Sicherung (`final-data-check.json`). Eine Coverdatei des gelöschten Testbuchs bleibt bis zur 6-Stunden-Schonfrist von `deleteUnusedCovers` liegen und wird danach automatisch entfernt.
- **Tests:** `testReleaseUnitTest` grün inkl. zwei neuer `DnbCatalogTest` (echte DNB-Antwort als Fixture `test/resources/dnb-alchimist.xml`).
- **Build-Hinweis:** Gradle-Daemon/Ausgaben landen auf diesem PC unter `C:\Users\49157\AppData\Local\Packages\OpenAI.Codex_2p2nqsd0c76g0\LocalCache\Local\Waraq\Build\outputs\app\outputs\apk\release` (Codex-Sandbox-Umleitung), nicht unter `%LOCALAPPDATA%\Waraq\Build`. Immer `output-metadata.json` (versionCode) prüfen, bevor signiert wird.
- `Werkzeuge/S24-Pruefung.py` kennt 3.3.4/3.3.5; ab 3.3.4 heißen Beleg-/Sicherungsordner `Claude-…`.

### Offen / nicht geprüft

1. **Kamera mit lateinischem Buch** am echten Buch noch nicht geprüft (kein Buch griffbereit). Die Erkennung nutzt ML Kit Latein; danach greift dieselbe Suche inkl. DNB.
2. Englische/internationale Ausgaben weiter nur über Open Library (+ Google, falls nicht gedrosselt). Ein eigener kostenloser Google-Books-API-Schlüssel würde helfen; er darf **nicht** im Vault/APK-Archiv liegen, also nur mit externer Ablage (z. B. `~/.gradle/gradle.properties`) einbauen – nur nach Mos Zustimmung.
3. Amazon bleibt externer Suchknopf; kein automatischer Import (Lizenz/API, siehe Technik und Quellen).
4. Restliche Featureabnahme (Souk-Filter/Touch, Coververgleich mit Foto) nach Bedarf.

---

Mo hat „weiter“ beauftragt und das S24 angeschlossen. Die Pause vom 30.10.2026 ist aufgehoben. Dieser Stand ersetzt frühere Angaben zum fehlgeschlagenen Build und zur installierten 3.3.1.

## Erledigt und am S24 geprüft

- Fehlenden `de.mo.bibliothek.data.Found`-Import in den zwei neuen Regressionstests ergänzt. `testReleaseUnitTest assembleRelease` erfolgreich. JUnit meldet 20 Tests ohne Fehler; der optionale `preparedImages`-Test kehrt mangels Eingabebildern ohne OCR-Durchlauf zurück. Die neue ONNX-Version wurde anschließend mit einem echten arabischen Kamerafoto am S24 geprüft.
- **Warraq 3.3.2 / versionCode 9** signiert und als Update installiert. APK: `Ausgaben/Signierte-Zwischenstaende/Warraq-3.3.2.apk`, SHA-256 `214b6f6cf870c712959e5b07347404a2064ecc59bf8f558a3c14f228a9e05f39`. Installierter APK-Hash identisch. Bisheriges Zertifikat `440335587995d04852b97be898d2462389e5fdc5f5eed89583c93f74f8296d42` beibehalten; keine Deinstallation.
- **Aktueller Bestand vor UND nach Update/Test: 12 Bücher, 9 Coverdateien bytegleich.** Das ist der frisch vom Gerät gelesene Bestand; die ältere Sicherung mit 13 Büchern/12 Covern nicht darüber wiederherstellen. Bibliotheks-SHA-256 `b1967503689aa0c832f968dba0ceb397e82b11ffe5d3fff2ea93677adc852003`. Neue Sicherung: `Sicherungen/Geraet-vor-Codex-3.3.2-20261031`.
- Echtes Kamerabuch: **„أبي الذي أكره“ / „عماد رشاد عثمان“** richtig erkannt. Suche zeigt **keinen falschen anderen Titel desselben Autors mehr**. Ohne passenden Katalogtreffer bleiben Lens, Amazon und manuelle Erfassung erreichbar.
- Manuelle Erfassung enthält bereits erkannten Titel und Autor; anschließend **abgebrochen, kein Buch gespeichert**.
- Neuer Knopf **„Auf Amazon suchen“**, Deutsch/Arabisch, öffnet die installierte Amazon-App mit URL-kodierter Titel-/Autoranfrage. ACTION_VIEW mit korrekter arabischer Suchanfrage am S24 belegt. Kein Kauf, kein automatischer Import und kein neuer Amazon-Zugang eingerichtet. Leermeldung präzisiert: „Keine passenden Treffer in den Buchkatalogen gefunden.“
- ONNX Runtime Android/JVM auf **1.23.0**, Debugfähigkeit und direkte `run-as`-Sicherung bleiben erhalten. Alle neun nativen Dateien im fertigen APK haben 16-KB-PT_LOAD- und ZIP-Ausrichtung. **Kein Kompatibilitätsdialog nach Installation und nach erneutem Force-stop/Start.** Keine Unterdrückungsoption gewählt.
- Abschließender Datenvergleich erfolgreich, Lens-Fotokopie nach Verlassen der Suche entfernt, eigene Geräte-UI-Testdatei entfernt. App zuletzt in der Bibliothek geöffnet.

## Belege

`Pruefnachweise/Codex-3.3.2-20261031`: `build.log`, beide JUnit-XML-Berichte, `signature.txt`, `native-alignment.json`, `update-result.json`, `final-data-check.json`, Screenshots/XML für Start, arabische Erkennung, Suche ohne Scheintreffer, manuelle Vorbelegung und erneuten Start; `amazon-intent.txt` belegt die Übergabe an Amazon.

Google Lens mit passendem Foto und passenden Internettreffern wurde zuvor unter 3.3.1 bestätigt (`Pruefnachweise/Codex-3.3.1-20261030/lens-result.json`). In 3.3.2 wurde der sichtbare Lens-Einstieg geprüft, die Google-Oberfläche nicht erneut durchlaufen.

## Umsetzung und Grenzen

`BookLookup.search(query, expectedTitle)` filtert Kameraergebnisse zusätzlich nach erkanntem Titel; Retry-Wortpaare stammen aus dem Titel. `BookieViewModel.searchBook` und `MainActivity` übergeben den strukturierten Titel. Allgemeine bzw. vom Nutzer geänderte Suchanfragen behalten die freie Suche. Zwei gezielte Regressionen verhindern die Verwechslung mit einem anderen Werk desselben Autors. Das ist keine Garantie für jedes unscharfe Foto oder jeden Katalogfehler.

Amazon ist jetzt ein **externer Suchzugang**, noch keine automatische Metadaten-/Coverquelle. Mos weitergehender Wunsch bleibt offen. Offizielle API-/Lizenzrecherche und mögliche nächste Schritte: [[Projekte/Waraq/Technik und Quellen#Amazon – Recherche vom 30.10.2026]]. Keine Amazon-Seitenextraktion implementiert.

## Weitere Arbeit

1. Falls Mo den automatischen Import weiterführen möchte: geeignete zusätzliche Metadaten-/Verlagsquellen oder vom Nutzer geteilte Buchlinks konkret untersuchen; Titel, Autor und Ausgabe zusammen prüfen. Zugang und Nutzungsrechte der jeweiligen Quelle berücksichtigen. Amazon-Suchknopf nicht als erfüllten Coverimport ausgeben.
2. Restliche Featureabnahme nach Bedarf: Ausgabenwahl/Coververgleich und Abbruchwege sowie Souk-Filter/Touchsteuerung. Bisherige Features nicht streichen; vollständiger Umfang in `Anforderungen.md`.
3. Bei neuen Erkennungsfehlern konkrete Beispiele untersuchen; keine beliebigen Autorentreffer als erkanntes Werk ausgeben. Datenkorrekturen bleiben bestätigungspflichtige Vorschläge.

## Sichere Fortsetzung

Maßgeblich `Code/Android`, S24 `R5CX157KZGK`, Paket `de.mo.bibliothek`. Vor Updates frisch sichern; Nutzer-App nie deinstallieren, Bibliothek niemals mit alten Test-/Sicherungsdaten überschreiben. Schlüssel extern halten. Nur frisch beobachtete UI-Elemente bedienen; keine blinden Koordinaten oder ungefragten Systempräferenzen.

`Werkzeuge/S24-Pruefung.py`: Umgebungsvariable `WARAQ_CHECK_BUILD=3.3.2` wählt aktuellen APK-Hash und Beleg-/Sicherungsordner; ohne Variable weiterhin historisch 3.3.1. Der einmalige `update`-Befehl verweigert bei bestehender Sicherung erneutes Überschreiben. Für neue Releases neue Version/Hash bewusst ergänzen.

Build: `Werkzeuge/Build.ps1`, tatsächliche Ausgaben zuletzt `C:\Users\49157\AppData\Local\Waraq\Build\outputs`. In Sandbox kann LOCALAPPDATA abweichen. Werkzeuge und externe Signierung in `Technik und Quellen.md`.

Historischer Abbruchpunkt vollständig erhalten unter `Historie/Codex-Pause-20261030.md`. Die Claude-Übergabe davor liegt ebenfalls in `Historie`.

Zurück: [[Projekte/Waraq/Start]]
