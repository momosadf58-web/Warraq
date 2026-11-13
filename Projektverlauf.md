---
type: project
status: archived
updated: 2026-10-30
---

# Warraq (früher bookie) – persönliche virtuelle Bibliothek

> Historischer Verlauf bis zur zentralen Sicherung am 30.10.2026. Frühere Formulierungen wie „aktueller Auftrag“ sind überholt. Maßgeblich sind [[Projekte/Waraq/Start]], [[Projekte/Waraq/Anforderungen]] und [[Projekte/Waraq/Stand]]. Das Projekt ist pausiert.


## 2026-10-30 – Weiterentwicklung und erneute Klärung

Mo beauftragt die Fortsetzung und betont anschließend: Alle bisherigen Wünsche bleiben relevant, einschließlich der Wünsche im eingefügten Anti-Gravity-Verlauf. Funktionalität und UX gehören genauso zum Auftrag wie Gestaltung. Arabische Bücher, Autoren und präzise Titel-/Coverzuordnung sind wesentlich.

**Neu konkretisiert:** Kamera auf ein Buch richten; Titel, Autor und sichtbares Cover erkennen; passende Bücher im Internet suchen; Werk auswählen, anschließend die Ausgabe wählen und hinzufügen. Kleine lokale Modelle dürfen dafür verwendet werden. Online-Bilderkennung ist erlaubt, wenn sie genauer ist und kein kostenpflichtiges Abo erfordert. Auf die Frage Live-Erkennung oder Foto antwortet Mo „das beste“; Live-Vorschau mit automatischer stabiler Aufnahme und Fotoknopf wurde empfohlen, aber noch nicht abschließend bestätigt.

**Zweite Fragerunde bestätigt:** Nur einzelne Bücher erkennen; keine Erkennung ganzer Regale. Das eigene Foto dient ausschließlich der Erkennung, übernommen wird das passende Internet-Cover. Der eingefügte Anti-Gravity-Verlauf enthält alle weiteren Wünsche.

**Dritte Fragerunde bestätigt:** Online-KI mit eigenem Schlüssel bleibt optional; die App soll auch ohne Online-KI gut funktionieren. Bereits gespeicherte Bücher ebenfalls auf fehlende oder falsche Daten und Cover prüfen, aber nur Vorschläge machen; jede Änderung bestätigt Mo selbst. Bücherraum: Option 2 gewählt, also ein begehbarer 3D-Raum mit frei beweglicher Kamera. Gestaltung ausdrücklich als chaotische, syrisch inspirierte Bibliothek, „kein ki slop“. Die bisher bearbeitete scrollbare Regalübersicht erfüllt diesen Raumwunsch noch nicht.

**Vierte Fragerunde bestätigt:** Ein enger Buchladen wie in einem alten syrischen Souk: volle Regale, schmale Gänge und gestapelte Bücher. Freies Gehen und Umsehen mit einfacher Touchsteuerung. Im 3D-Raum ausschließlich gelesene Bücher zeigen; Antippen öffnet die Buchdetails. Der gesamte Bestand bleibt in der normalen Bibliothek und Statistik berücksichtigt.

**Fünfte Fragerunde bestätigt:** Wachsender Jahresziel-Stapel ausschließlich im 3D-Buchladen, mit sichtbarer Zielanzeige. Nur kurze Klappentexte ohne Spoiler. Falls Originalzitate fehlen, zusätzlich eine klar getrennt beschriftete sinngemäße Kernaussage anbieten; niemals als Originalzitat ausgeben.

**Sechste Fragerunde bestätigt:** Anonyme Kulissenbücher dürfen den Laden füllen; Mos tatsächlich gelesene Bücher müssen klar erkennbar und antippbar sein. Atmosphärisch gemischte Anordnung; Filter heben passende echte Bücher nach Genre, Lesejahr oder Buchart hervor. Kulissenbücher zählen niemals zur Bibliothek oder zum Jahresziel.

**Aktueller Auftrag:** Die entscheidenden Abläufe wurden in sechs Fragerunden geklärt. Auf Grundlage dieser Antworten weiter umsetzen; alle Wünsche aus dem eingefügten Anti-Gravity-Verlauf bleiben im Umfang. Keine zusätzliche Freigaberunde für bereits beauftragte Umsetzung erforderlich.

**Technischer Zwischenstand:** 3.1 (versionCode 5) als gleich signiertes Update auf dem S24 installiert; aktuelle Bibliothek enthält 13 Bücher und neun referenzierte lokale Cover. Nach den Tests ist library.json bytegleich erhalten. 11 Regressionstests mit isolierten Testdaten bestanden; separater Online-Abruf für Fahrenheit 451 funktioniert. Bildprüfung fand noch einen Inhaltsverzeichnis-Eintrag unter den Zitaten; Parser und Test daraufhin korrigiert und neu gebaut, letzte Korrektur noch nicht erneut am Gerät geprüft oder installiert. Bücherraum, Orakel, Jahresstapel, Wissensquellen und Bildkarten sind bearbeitet; vollständige Sichtprüfung noch offen. Keine Kameraerkennung fertiggestellt und kein endgültiger Release behaupten.

**Verbindliche Fortsetzung:** Tatsächlicher Arbeitsstand und genaue Testgrenzen in `C:\Users\49157\.codex\visualizations\2026\10\30\01a18c33-7bc0-7411-976b-5a39fca5242e\warraq-update\FORTSETZEN.md`; bearbeiteter Code im benachbarten Unterordner `project`. Ursprünglicher Projektordner outputs/bookie3 bisher unverändert; Rückschreiben und abschließende Übergabe stehen aus. Drei kleine offizielle OCR-Sprachmodelle (Arabisch/Deutsch/Englisch, zusammen ca. 18,2 MB) bereits im Arbeitsstand vorhanden, Erkennung noch nicht integriert. Hindawi als zusätzliche Quelle recherchiert, noch nicht funktionierend angebunden.

## Aktueller Stand – Gesprächsverlauf übernommen am 30.10.2026

Quelle: von Mo eingefügter Warraq-Gesprächsverlauf mit UI-Review, eigenen Featureentscheidungen und einem Kotlin/Compose-Codeausschnitt. Das Gespräch selbst ist undatiert; 30.10.2026 ist das Übernahmedatum.

**Letzter dokumentierter Gerätetest:** Warraq 3.0 auf dem S24 Ultra, 29.10.2026, 22:50; Details und offene Fehler weiter unten. Der neue Verlauf belegt keine neu gebaute oder installierte Version und keinen abgeschlossenen Test der neuen Funktionen.

### Bestätigte Entscheidungen aus dem neuen Verlauf

- Die drei Statusknöpfe **Gelesen · Ungelesen · Lese ich gerade** behalten. Mos ausdrückliche Entscheidung ersetzt den Vorschlag, sie durch Lesezeichenbänder oder Siegel zu ersetzen. Abgeschnittene Texte und missverständliche Auswahl-Icons trotzdem beheben.
- **Al-Warraq-Orakel:** zufällig ein ungelesenes Buch auswählen und mit einer Animation hervorheben; Feature 2 ausdrücklich gewünscht. Geste, Ton und Haptik sind noch nicht im Detail festgelegt.
- **Große Statistik als Bücherraum:** alle Bücher in der Statistik berücksichtigen, mit Auswertungen nach Genre, Jahren und Roman/Sachbuch. Gelesene Bücher sollen in einer großen Visualisierung wie in einem Lager-/Bibliotheksraum sichtbar werden.
- **Jahresziel mit Bücherstapel:** die im Jahr gelesenen Bücher sichtbar und animiert aufstapeln; Feature 3 ausdrücklich gewünscht, zusätzlich zur großen Übersicht.
- **Zusammenfassungen und wichtige Zitate automatisch aus dem Internet beziehen.** Die Idee der Zitat-/Buchkarte zum Teilen (Feature 4) gefällt Mo; die Inhalte sollen nicht manuell eingepflegt werden müssen. Schwerpunkt ist Buch-Tracking, kein Journaling. Bestehende eigene Notizen sind dadurch nicht ausdrücklich zur Entfernung freigegeben.
- UI-Fehler und Gestaltung überarbeiten; bei offenen Einzelheiten darf zur Klärung nachgefragt werden. Die letzte Anweisung im eingefügten Verlauf lautet, Stand und Code im Projektordner zu sichern und die Arbeit zu beenden.

### UI-Review und sichtbarer Arbeitsansatz

Im Verlauf vorgeschlagen und zur Überarbeitung beauftragt: kompakterer Kopf mit Kalligrafie, eine horizontal scrollende Filterzeile, plastischeres Holzregal mit Schatten, besser lesbare Detailseite mit bibliophiler Metadatenkarte, sinnvoll gefüllter Hinzufügen-Tab, korrekte Einzahl in der Statistik sowie lesbare Systemleisten im Hell-/Dunkelmodus. Die Statusknöpfe bleiben gemäß Mos Korrektur erhalten.

Der beigefügte Codeausschnitt zeigt eine Zusammenfassungs-Karte mit Ladeindikator und Weiterlesen/Weniger-anzeigen sowie eine überarbeitete Karte für eigene Notizen. Alte und neue Layoutteile sind im Ausschnitt vermischt; daraus lässt sich kein vollständiger, kompilierbarer Quellstand ableiten. Online-Abruf, Zitate, Orakel, Bücherraum und Stapel sind damit nicht als fertig nachgewiesen.

### Fortsetzung und offene Nachweise

1. Im vorhandenen Projektordner den tatsächlich gespeicherten Quellstand und eine mögliche Übergabe prüfen. Dass die zuletzt verlangte Sicherung ausgeführt wurde, ist aus dem Verlauf nicht belegt.
2. Bei erneuter Entwicklungsarbeit offene Fehler aus dem letzten Gerätetest berücksichtigen; Bücher und Cover erhalten, Sicherung/Wiederherstellung mit Testdaten prüfen.
3. Vor Umsetzung der großen Statistik die Raumdarstellung, die Zuordnung nach Jahren (Lesejahr oder Erscheinungsjahr) und fehlende Genre-/Datumsangaben klären. Quellen und Verfügbarkeit automatisch bezogener Zusammenfassungen/Zitate prüfen.
4. Neue Funktionen erst nach erfolgreichem Build und passenden S24-Tests als umgesetzt markieren. Dieser Import setzt die App-Entwicklung nicht fort.

Bekannter Kotlin/Compose-Projektordner: `C:\Users\49157\Documents\Codex\2026-10-29\new-chat\outputs\bookie3`; Build-Skript: `C:\Users\49157\Documents\Codex\2026-10-29\new-chat\work\build-bookie3.ps1`. Beide Pfade stammen aus der bisherigen Projektnotiz und wurden bei diesem Import nicht erneut geprüft.

## Bestätigter Auftrag

Mo möchte eine persönliche virtuelle Bibliothek für eigene Bücher entwickeln. Die App soll keine Lese-App sein: EPUB- oder PDF-Inhalte werden nicht direkt geöffnet. Es gibt keine Anmeldung und keine Paywall. Bücher werden später von Mo selbst hinzugefügt.

Die App soll die nützlichen Ideen aus READO und bookie verbinden, aber als eigene persönliche Anwendung umgesetzt werden. Die App ist für den privaten Gebrauch gedacht; fremde Projektinhalte werden nicht in den AI Brain übernommen.

## Bereits untersucht

- Gerät: Samsung Galaxy S24 Ultra, per ADB erreichbar.
- Installierte Vergleichs-Apps: READO 67.0.5 und bookie 26.9.5.
- READO- und bookie-Ansichten sowie UI-Zustände wurden als Screenshots und UI-XML gesichert.
- Aus den Vergleichs-Apps wurden keine Zugangsdaten oder privaten Kontoinhalte übernommen.

## Arbeitsstände und Dateien – Java/2.0 (historisch)

- App-Ausgabe: `C:\Users\49157\Documents\Codex\2026-10-29\new-chat\outputs\bookie`
- Analyse, Screenshots, UI-XML und APK-Arbeitskopien: `C:\Users\49157\Documents\Codex\2026-10-29\new-chat\work`
- Fortsetzungsanweisung für Codex: `C:\Users\49157\Documents\Codex\2026-10-29\new-chat\work\BOOKIE-CONTINUE.md`
- Reproduzierbarer Build: `C:\Users\49157\Documents\Codex\2026-10-29\new-chat\work\build-bookie.ps1`
- Damalige Test-APK: `C:\Users\49157\Documents\Codex\2026-10-29\new-chat\outputs\bookie-2.0.apk`

## Produktentscheidungen – ursprünglicher Auftrag (historisch)

- Name ausdrücklich bookie; persönliche virtuelle Bibliothek, kein Reader.
- Keine Anmeldung und keine Paywall; arabische Bücher und eigene Bilder.
- Gestaltung nah an READO/bookie, schöner Bibliothekscharakter, einfache Bedienung ohne KI-Floskeln.
- Gelesen/Ungelesen mit einem Tipp direkt am Buch; schneller Wechsel zwischen Alle/Ungelesen/Gelesen.
- Vorhandene Bücher erhalten. Keine Timer-, Community- oder Statistikfunktionen zusätzlich erfinden.

## Nächster Arbeitsauftrag (historisch, ersetzt durch 2.0-Abschnitt)

Codex soll die bestehende Bookie-Arbeit im vorhandenen Arbeitsordner fortsetzen, zuerst die vorhandenen Analyseartefakte und diese Notiz lesen und danach einen kleinen, offline nutzbaren Bibliotheks-Prototypen ohne Login, Paywall und Reader-Funktion weiterentwickeln. Keine zusätzlichen Funktionen als endgültig festlegen, solange Mo sie nicht bestätigt.

## Stand 2026-10-29, S24-Test (historisch)

- APK aus dem Java-Quellstand gebaut, mit lokalem Debug-Keystore signiert und mit `apksigner` (v2/v3) verifiziert.
- Auf dem angeschlossenen Samsung Galaxy S24 Ultra (SM-S928B, Android 17, 1080x2340, 450 dpi) installiert und gestartet.
- Sichtbar und ohne Absturz geprüft: persönliche Bibliotheksansicht, leerer Zustand, Suche, Regalfilter, Hinzufügen-Dialog, lokale App-Initialisierung.
- Der Prototyp speichert lokal in der App, verarbeitet Cover in eine bereinigte lokale JPEG-Kopie und besitzt ZIP-Sicherung/Wiederherstellung.
- Die Test-APK enthält noch keine Nutzerbibliothek; der misslungene ADB-Texttest wurde nicht gespeichert. Keine vorhandenen Nutzerdaten wurden gelöscht.

## Cover-Untersuchung und Stand 2026-10-29, späterer S24-Test (historisch)

- Die gesicherten READO- und bookie-Screenshots zeigen echte Coverbilder in Suche, Listen und Bibliothek; Platzhalter erscheinen nur bei fehlenden Bildern.
- Die APK-Ressourcen und das READO-JavaScript-Bundle zeigen serverseitige Bild-URLs und Bild-Ladekomponenten. Die Vergleichs-Apps laden Cover also online anhand von Buchmetadaten/ISBN und speichern nicht nur einen lokalen Dateinamen.
- Ursache im Prototyp: Open-Library-Suchergebnisse enthielten `cover_i`, aber `addRemoteBook()` verwarf diese Information und speicherte `cover` leer. Dadurch blieb nur die farbige Titel-Platzhalterkarte sichtbar.
- Behoben: Open-Library-Cover-URL `https://covers.openlibrary.org/b/id/<cover_i>-L.jpg` wird beim Hinzufügen heruntergeladen, in eine lokale bereinigte JPEG-Kopie umgewandelt und wie ein manuell importiertes Cover gerendert.
- Der S24-Test bestätigte danach mit „Dune“ ein sichtbares Cover in der lokalen Bibliothek nach dem Hinzufügen und erneutem Start. Belegdateien: `work\bookie-cover-results.xml`, `work\bookie-cover-final.xml`, `work\bookie-cover-screen.png`.
- Der Build-Keystore liegt nun dauerhaft unter `work\bookie-debug.keystore`; dadurch sind künftige Update-Installationen signaturkompatibel. Die aktuell installierte ältere Testsignatur musste einmalig deinstalliert werden; dabei wurden nur die von diesem Test erzeugten Daten entfernt.

## Remote-Cover und arabische Bücher – finaler S24-Stand (historisch)

- Die Online-Cover-Version verwendet `coverUrl` mit `https://covers.openlibrary.org/...`. Das Cover wird beim Anzeigen geladen und nicht dauerhaft heruntergeladen; lokale manuelle Cover bleiben weiterhin möglich.
- Auf dem S24 wurden über die Open-Library-Suche arabische Treffer gefunden. Erfolgreich zur persönlichen Bibliothek hinzugefügt und nach dem Schließen des Suchdialogs sichtbar: `Mahfouz Trilogy` und `ميرامار`, jeweils mit Autor `نجيب محفوظ`.
- Der abschließende UI-Nachweis zeigt `2 Bücher · nur für dich` sowie beide arabisch recherchierten Einträge mit `ImageView`/Cover-Beschreibung. Dateien: `work\bookie-arabic-clean-final.xml`, `work\bookie-arabic-clean-final.png`.
- Beim Test entstand kurzfristig ein doppelter `ميرامار`-Eintrag. Einer wurde über die normale Löschbestätigung entfernt. Zusätzlich verhindert `addRemoteBook()` jetzt doppelte Einträge über ISBN oder normalisierte Kombination aus Titel und Autor.
- Remote-Cover sind bei fehlender Internetverbindung nicht garantiert sichtbar; der Titel-Platzhalter bleibt als Fallback. Remote-Bilder werden nicht in ZIP-Backups übernommen, lokale Cover weiterhin schon.

## Testimport Desktop-Ordner `Bücher` – 2026-10-29 (historisch)

- Aus `C:\Users\49157\OneDrive\Desktop\Bücher` wurden 13 Dateien erkannt: EPUBs und PDFs mit deutschen und arabischen Titeln.
- Für den Test wurden nur Metadaten aus den Dateinamen in Bookie angelegt; keine EPUB-/PDF-Datei wurde kopiert, geöffnet oder als Reader-Inhalt importiert.
- Der S24-Teststand enthält jetzt 13 Bücher: die 2 bestehenden arabischen Bücher plus 11 neue Einträge aus dem Ordner. Alle neuen Einträge stehen zunächst auf `Noch nicht gelesen`.
- Erkannt wurden unter anderem Rafik Schami, Ahmad al-Buni, Daniel Kahneman, Ray Bradbury, Haruki Murakami, Gabriel García Márquez, مصطفى لطفي المنفلوطي und آلان دونو. Die Datei `978-3-658-41520-4.pdf` wurde zunächst mit dem Dateinamen als Titel und ISBN übernommen; der Titel kann später bearbeitet werden.
- Die temporäre Testimport-Aktion wurde nach der Übernahme aus dem App-Quellcode entfernt. Belege liegen unter `work\bookie-after-import.xml` und `work\bookie-after-import.png`.

## Cover-Ergänzung nach dem Testimport – 2026-10-29 (historisch)

- Für Bücher ohne Cover wurde die Menüfunktion `Fehlende Cover online ergänzen` ergänzt.
- Die App sucht anhand von Titel und Autor über Open Library und speichert nur die Remote-Adresse `coverUrl`. Die EPUB-/PDF-Dateien werden nicht geöffnet, kopiert oder als Reader-Inhalt verwendet.
- Die Funktion wurde auf dem S24 ausgeführt; die Cover werden beim Rendern als `ImageView` geladen. Ein Wiederholungstest nach der Installation erzeugte keine AndroidRuntime- oder FATAL-Fehler.
- Belege: `work\bookie-covers-added.xml`, `work\bookie-covers-added.png` und `work\bookie-cover-repeat.xml`.

## 2026-10-29 – Überarbeitung auf bookie 2.0

Mo beauftragt nach Unzufriedenheit mit dem Copilot-Stand eine gründliche Überarbeitung mit Ponytail, reverse-engineer-anything und Advisor. Die frühere Pause ist damit aufgehoben.

**Ergebnis:** bookie 2.0 als signaturkompatibles Update auf dem S24 Ultra installiert, Paket de.mo.bibliothek. Bibliothek mit Papier-/Olivgestaltung, Coverraster, vollständig sichtbaren Covern, direktem Gelesen/Ungelesen-Knopf, schnellen Filtern mit Zählern, eigener Suche/Regalen, Vollbilddetails und Editor. Keine Werbefloskeln in der Oberfläche. Eigene Coverauswahl und optionale Online-Suche erhalten.

**Datenerhalt geprüft:** Vorherige Bibliothek über eigene Exportfunktion gesichert. Nachher-Export mit vorher verglichen: alle 15 IDs und sämtliche alten Metadaten unverändert; Status-Test zurückgesetzt. Neue Sicherung umfasst zusätzlich fünf lokal gespeicherte Cover. Keine Deinstallation der Nutzer-App; temporäre isolierte Test-App anschließend entfernt.

**Technik/Tests:** Bilder und Speichervorgänge haben getrennte Warteschlangen; private atomare Coverkopien, vollständige Sicherung bereits geladener Bilder, strenge ZIP-/URL-/Typprüfung, Erhalt unbekannter Metadaten. OpenLibrary leitet tatsächlich über Archive.org/CDN weiter; dafür begrenzte geprüfte HTTPS-Weiterleitungen ergänzt. 48 isolierte Speichertests auf S24 bestanden. Statuswechsel/Filter/Neustart, arabischer Editor, abgebrochene Bildauswahl sowie endgültiger Update-Start geprüft. Advisor fand zusätzlich zwei seltene Status-/Entwurfsrennen; beide korrigiert und abschließend neu gebaut/installiert, exakte Prozessabbruch-Rennen nicht separat am Gerät erzwungen.

**REA-Grenzen:** Beide Original-APKs mit REA inventarisiert; Live-Paket-/Screenshotnachweise zusätzlich mit REA-CLI und prozesslokalem ADB-Pfad. READO-Gastbibliothek, Listen/Sortierung und neu Buchdetails/Verwalten mit Status-/Listen-/Premiumoptionen praktisch geprüft. bookie-Gastsuche/Details/manuelles Formular zugänglich, private Bibliothek/Feed/Profil weiterhin hinter Login. Keine vollständige Dekompilierung oder vollständige Prüfung beider Original-Apps behaupten; nichts eingereicht/gekauft und keine Anmeldung vorgenommen.

**Fortsetzung für andere KIs:** Zuerst den aktuellen Bericht lesen:
C:\Users\49157\Documents\Codex\2026-10-29\new-chat\outputs\bookie\UEBERGABE.md

- APK: C:\Users\49157\Documents\Codex\2026-10-29\new-chat\outputs\bookie-2.0.apk
- Sicherung: C:\Users\49157\Documents\Codex\2026-10-29\new-chat\outputs\bookie-Sicherung-2026-10-29.zip
- Vorschau/Quellstand/Tests/REA-Nachweise im Projektordner outputs\bookie.
- Build work\build-bookie.ps1 nutzt vorhandenen Signierschlüssel; diesen nicht ersetzen, veröffentlichen oder ins AI Brain kopieren.
- Vorherige Quellen/APK/Datenbackup unter work erhalten. Frühere Notizabschnitte sind historische Stände.

Ausstehend ist Mos Rückmeldung zur neuen Gestaltung. Fehlende Buchcover bleiben mit Titelplatzhalter sichtbar; kein automatisches Zuordnen möglicherweise falscher Ersttreffer.

## 2026-10-29 abends – Neuauftrag bookie 3.0 (Klärungsphase)

**Auftrag:** App besser und schöner neu aufbauen; READO (com.read_o 67.0.5) und bookie (com.bookietheapp 26.9.5) per Reverse Engineering zu einer App vereinen. Mo beantwortet zuerst Klärungsfragen, bis das Projekt eindeutig ist.

**Ausgangslage geprüft (Claude Code, 29.10.2026):**
- S24 Ultra (SM-S928B) per ADB verbunden. Installiert: de.mo.bibliothek 2.1 (versionCode 3), com.read_o, com.bookietheapp.
- Aktueller Quellstand: native Java, `outputs\bookie\src\de\mo\bibliothek` (MainActivity ~48 KB, LibraryStore ~23 KB), APK ~45 KB.
- Bibliothek auf dem Gerät: 15 Bücher (14 ungelesen, 1 gelesen). Mehrere Bücher ohne Cover, ein Eintrag nur mit ISBN als Titel.
- Vorhandene Referenzscreens von READO/bookie unter `work\reado-*.png` und `work\bookie-*.png`.

**Harte Vorgaben bleiben:** keine Lese-App, kein Login, keine Paywall, Bibliotheksdaten nie löschen, Paket de.mo.bibliothek und Signierschlüssel beibehalten.

**Entscheidungen aus der Klärung (Runde 1, 29.10.2026):**
- Technik: Neuaufbau in Kotlin + Jetpack Compose; Bücher aus 2.1 übernehmen, gleiches Paket.
- Design: Hell (Papier/Oliv) und Dunkel (READO-artig) automatisch nach Systemeinstellung.
- Status: 3 Stufen – Ungelesen, Lese ich gerade, Gelesen.
- Funktionen: ISBN-Barcode-Scanner, große Buchdetailseite (READO-Stil), eigene Listen + Sortierung, eigene Bewertung + Notizen.
- Neu gewünscht: „coole Sachen wie Freunde adden und sehen, was sie lesen“. Widerspricht bisher „keine Anmeldung“ – Umsetzung wird in Runde 2 geklärt.

**Runde 2 (29.10.2026):**
- Freunde: kommen später (3.1), 3.0 konzentriert sich auf die Bibliothek. Kein Login in 3.0.
- Nutzerkreis: Mo plus ein paar Freunde mit Android, APK direkt weitergeben, kein Play Store.
- Navigation: untere Leiste mit Tabs wie READO.
- Statistik: jetzt erwünscht – Jahresziel und Übersicht (frühere Ausnahme aufgehoben).

**Runde 3 (29.10.2026):**
- Bibliotheksansicht: echtes Regal als Standard, umschaltbar auf Coverraster/Liste.
- Kein Lesefortschritt (Seiten); nur der Status.
- Buchdaten/Cover: Google Books zuerst, Open Library als Ersatz.
- App-Sprache: Deutsch und Arabisch umschaltbar, Arabisch mit RTL-Layout.

**Runde 4 (29.10.2026):**
- Statuswechsel: Tipp auf den Statusknopf öffnet ein kleines Auswahlmenü mit 3 Chips direkt am Buch.
- Tabs: Bibliothek · Hinzufügen (Suche, Scanner, manuell) · Statistik · Profil (Einstellungen, Sprache, Backup). Freunde später als eigener Tab.
- Installation: direkt als Update über 2.1, vorher Backup; alle 15 Bücher übernehmen.
- Eigene Cover: mit der Kamera fotografieren.

**Runde 5 (29.10.2026):**
- Statistik-Tab: Jahresziel-Ring (Ziel einstellbar). Gelesen-Datum wird beim Setzen auf Gelesen automatisch gespeichert.
- Cover: Kamera und Galerie.
- Neues adaptives App-Icon passend zum Design.
- Lieferung: alles auf einmal, sauber auf dem S24 getestet.

### Spezifikation bookie 3.0 (Stand Klärung abgeschlossen)

| Bereich | Festgelegt |
|---|---|
| Technik | Kotlin + Jetpack Compose, Paket de.mo.bibliothek, bisheriger Signierschlüssel, Update über 2.1 |
| Design | Hell Papier/Oliv + Dunkel READO-artig, automatisch; Serifentitel |
| Tabs | Bibliothek · Hinzufügen · Statistik · Profil |
| Bibliothek | Regalansicht als Standard, umschaltbar Raster/Liste; Suche; Filter mit Zählern; eigene Listen; Sortierung |
| Status | Ungelesen · Lese ich gerade · Gelesen; Tipp öffnet 3 Chips am Buch; kein Seitenfortschritt |
| Buchdetail | großes Cover, Farbverlauf aus dem Cover, Klappentext, Genre, Seiten, Verlag; eigene Sterne, Lieblingsherz, Notizen |
| Hinzufügen | Google Books + Open Library, ISBN-Barcode-Scanner, manuell; Cover per Kamera oder Galerie |
| Statistik | Jahresziel-Ring |
| Profil | Sprache DE/AR (RTL), Jahresziel, Backup/Wiederherstellung (ZIP kompatibel mit 2.x) |
| Später (3.1) | Freunde hinzufügen und sehen, was sie lesen – braucht Server, aktuell kein Login |
| Weiter ausgeschlossen | Reader, Login, Paywall, Shop/Warenkorb |

**Technische Hinweise:** JDK 17 und Android SDK 35/36 unter `Dokumente\Mos Oase\Android\Werkzeuge` vorhanden. Kein Gradle installiert; Compose-Build lädt Gradle-Wrapper und Bibliotheken einmalig aus dem Internet. Migration muss das 2.x-Datenformat inklusive alter Statuswerte, unbekannter Felder und lokaler Cover übernehmen.

**Nächster Schritt (erledigt):** Mos Freigabe zum Bau, dann Umsetzung, Test auf dem S24 und Update über 2.1.

## 2026-10-29, 22:37 – bookie 3.0 gebaut und auf dem S24 installiert

**Stand:** bookie 3.0 (versionCode 4) als Update über 2.1 installiert, gleicher Signierschlüssel (SHA-256 4403…6d42), keine Deinstallation.

**Dateien:**
- Quellcode: `C:\Users\49157\Documents\Codex\2026-10-29\new-chat\outputs\bookie3` (Kotlin/Compose, Gradle 8.9, AGP 8.7.3)
- APK: `outputs\bookie-3.0.apk` (4,6 MB)
- Build: `work\build-bookie3.ps1` (Gradle, danach Signatur mit `work\bookie-debug.keystore`); Gradle liegt unter `work\tools\gradle-8.9`
- Sicherungen vor dem Update: `work\bookie-2.1-files-backup.tar` (komplette App-Dateien), `work\library-vor-3.0.json`

**Datenerhalt geprüft:** Vorher/Nachher-Vergleich von library.json: alle 15 Bücher, alle bisherigen Felder unverändert. Die 5 aus dem 2.x-Cache geladenen Online-Cover wurden als lokale Cover übernommen (jetzt offline sichtbar und in der Sicherung). Datenformat bleibt library.json; unbekannte Felder bleiben erhalten; `shelf` wird zu „Listen“ (mehrere möglich), das erste bleibt in `shelf` lesbar.

**Am Gerät geprüft:** Start ohne Absturz, Regalansicht im Dunkelmodus mit Covern und Statusknöpfen, Filterzähler, Profil, Umschalten auf Arabisch mit RTL-Layout (danach wieder auf „Wie das Handy“). Abgeschnittene Statusknöpfe gefunden und behoben.

**Noch nicht am Gerät geprüft:** Buchdetailseite, Statusmenü am Buch, Online-Suche/Hinzufügen, ISBN-Scanner, Kamera-/Galeriecover, Sicherung erstellen/wiederherstellen, Statistik-Ring, heller Modus. Diese Punkte zuerst testen bzw. Mos Rückmeldung abwarten.

**Bekannte Punkte:** Release-Build bleibt wie 2.x debuggable (run-as für Datensicherung per ADB); dadurch keine R8-Optimierung. Der Barcode-Scanner nutzt Google Play-Dienste (Code Scanner), keine eigene Kamera-Berechtigung. Gelesene Bücher ohne Datum (1 Buch aus 2.x) zählen nicht zum Jahresziel, bis ein Datum gesetzt wird.

**3.1 offen:** Freunde (braucht Server und Klärung Login).

## 2026-10-29, 22:42 – Personalisierung: neuer Name „Warraq“

Mo bat darum, die App anhand des AI Brain zu personalisieren und passende Namen zu finden. Grundlage nur belegte Fakten (Arabisch als Muttersprache, deutsch-arabische Bibliothek, Namensmuster „Mos Oase“); keine Deutungen aus Reflexionen verwendet.

**Entscheidungen von Mo:**
- App-Name: **Warraq** (الورّاق, historische Buchhändler/Abschreiber in Bagdad). Ersetzt die frühere Vorgabe „Name ausdrücklich bookie“. Paket bleibt de.mo.bibliothek, Daten und Signatur unverändert.
- Kopfzeile: großer kalligrafischer Schriftzug „مكتبتي“ (Schrift Aref Ruqaa, OFL; Lizenz in `outputs\bookie3\LIZENZEN`), darunter „Meine Bibliothek“.
- Extra: Sprachfilter العربية / Deutsch. Erkennung: Angabe „Arabisch“/„Deutsch“ oder arabische Schrift im Titel; ohne Angabe zählt ein lateinischer Titel als Deutsch (Näherung, englische Titel landen dort bis zur Sprachangabe).
- Abgelehnt: Startlisten, Oase-Farben, neues Icon.

**Stand:** `outputs\Warraq-3.0.apk` auf dem S24 als Update installiert, Start ohne Absturz, Kopfzeile und Filter sichtbar geprüft.

## 2026-10-29, 22:50 – Gerätetest Warraq 3.0

Am S24 mit Testhilfe `work\phone.ps1` geprüft (Screens unter `work\test3`). Keine Abstürze. Testbedingte Änderungen wurden zurückgesetzt; Dunkelmodus wiederhergestellt.

**Funktioniert:** Buchdetail mit Statuswechsel (gespeichert und zurückgesetzt), Menü „Mit Online-Treffer ergänzen“ inkl. Suche, Hinzufügen-Suche mit Covern und Vorschau, ISBN-Scanner öffnet (Google Code Scanner), Editor mit Kamera (Abbruch sauber), Statistik-Ring (1 von 12), Sicherung erstellen: `Download\Warraq-Sicherung-2026-10-29.zip` mit 15 Büchern und 5 Covern geprüft, heller Modus.

**Beobachtung Daten:** „Nizam-Altafaha“ steht seit 22:42 auf Gelesen. Vermutlich von Mo selbst in der App gesetzt (nicht durch Test-Tipps); unverändert gelassen.

**Offene Fehler (nicht behoben, Mo hat Test beendet):**
1. Google Books antwortet ohne API-Schlüssel mit HTTP 429 (auch vom PC). Suche läuft nur über Open Library weiter. Abhilfe bräuchte einen eigenen Google-API-Schlüssel, den Mo selbst anlegen müsste.
2. Statusumschalter (Detail/Editor/Vorschau): Icons dauerhaft sichtbar, Häkchen bei „Gelesen“ wirkt ausgewählt; „Lese ich gerade“ abgeschnitten. Lösung: `icon`-Parameter entfernen.
3. Scanner schließen zeigt fälschlich „Scanner nicht verfügbar“ (Abbruch landet im Fehler-Listener).
4. Sprachcodes wie „por“ werden roh angezeigt; Lösung: `Locale(code).getDisplayLanguage`.
5. Statistik: „1 gelesene Bücher“ (Einzahl fehlt).
6. Statusleisten-Symbole passen sich beim Hell/Dunkel-Wechsel zur Laufzeit nicht an (weiß auf hell).
7. Wiederherstellen einer Sicherung nicht am Gerät getestet (bewusst nicht an der echten Bibliothek).

**Advisor-Prüfung (Opus, 29.10.2026), Reihenfolge vor Weitergabe an Freunde:**
1. Wiederherstellen: Im Code kein Datenverlust- oder Absturzfehler gefunden (erst prüfen, dann atomar speichern, bei Fehler zurückrollen). Risiko ist Bedienfehler: Wiederherstellen ersetzt die ganze Bibliothek ohne Rückgängig. Empfehlung: vor dem Wiederherstellen automatisch eine Sicherung `pre-restore.zip` anlegen; Rundlauf mit Testdaten am Gerät prüfen.
2. Scanner-Abbruch: `addOnCanceledListener` ergänzen und `MlKitException.CANCELLED` im Fehler-Listener ignorieren.
3. Statusumschalter: eigenes Icon entfernen bzw. `SegmentedButtonDefaults.Icon(selected)`.
4. Einzahl „1 gelesenes Buch“ und Sprachnamen über `Locale` – nur Feinschliff.
5. Google 429 unkritisch dank Open Library; offline meldet die ISBN-Suche aber irreführend „Kein Treffer“.
- Sicherung enthält keine Einstellungen (Sprache, Jahresziel).
- Keinen Absturzpfad gefunden.

**Nächster Arbeitsauftrag:** Punkte 1–3 beheben, Wiederherstellen mit Testdaten prüfen, dann als 3.0.1 installieren. Sicherungsdateien heißen jetzt `Warraq-Sicherung-JJJJ-MM-TT.zip` (Format unverändert). Build-Skript `work\build-bookie3.ps1` erzeugt die Warraq-APK.
