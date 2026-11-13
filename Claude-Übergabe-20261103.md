# Claude-Übergabe – 03.11.2026, Warraq 3.3.5 installiert, KI-Ausbau begonnen

## Aufräumen 04.11.2026

- PC: Build-Ausgaben, kopierte bookie/READO-APKs (REA-Erkenntnisse bleiben in `Pruefnachweise/Recherche-20261031`), Hilfsskripte und alter Codex-Arbeitsordner gelöscht. Vorher daraus `before-files.tar` und `FORTSETZEN.md` byte-gleich nach `Historie/Codex-Arbeitsordner-20261030` gesichert.
- Handy: 36 UI-Mitschnitte der Agenten in `/sdcard`, Agenten-Sicherung `Download/warraq-backup-before-update.tar`, Testfotos im App-Cache, drei Codex-Testdateien und das verwaiste Testcover gelöscht. Die drei Download-Sicherungen liegen byte-gleich in `Historie/Handy-Download-Sicherungen`; die zwei `.zip` (App-Exportformat, evtl. von Mo) bleiben zusätzlich auf dem Handy.
- Danach: 15 Bücher, Bibliothek identisch mit Sicherung vor 3.4.5, alle verwendeten Cover vorhanden (12 Dateien).

## Stand 04.11.2026 nachts – Warraq 3.4.5: ruhigeres UI und neues App-Symbol

**Installiert: Warraq 3.4.5 / versionCode 18**, `Ausgaben/Signierte-Zwischenstaende/Warraq-3.4.5.apk`, SHA-256 `ce24890ded4a904609654a50df1b94895d294869265a8cbdb72649bc72ab1f4b`, Zertifikat unverändert. Sicherung `Sicherungen/Geraet-vor-Claude-3.4.5-20261104`; Endvergleich 15 Bücher/13 Cover bytegleich. Belege vorher/nachher: `Pruefnachweise/Claude-3.4.4-20261104/ui-vorher-*.png`, `Pruefnachweise/Claude-3.4.5-20261104/ui-nachher-*.png`.

Mos Auftrag: Erscheinungsbild und Symbol verbessern, keine großen Umstellungen, „kein KI slop“. Mo wählte das ausgeschriebene Wort in Oliv mit Regal; UI-Punkte „das was passt“.
- **App-Symbol:** „وراق“ in Aref Ruqaa (Papierfarbe) über Messing-Regalbrett auf Oliv (`mipmap-*/ic_launcher_foreground.png`, Monochrom-Variante für Android-Designsymbole, `mipmap-anydpi-v26/ic_launcher.xml`). Gesetzt mit WPF/DirectWrite. **Schriftfehler:** Aref Ruqaa zeichnet das alleinstehende End-ق ohne Punkt-Strich; der Strich wurde aus dem ق in „قلم“ derselben Schrift übernommen und über das ق gesetzt. Altes Vektorsymbol unter `Historie/Icon-vor-3.4.5`. Erzeugungsskripte lagen nur temporär; bei Änderung neu setzen und Punkt-Strich prüfen.
- **Bibliothek-Kopfzeile:** goldenes Funkel-Symbol in Blase ersetzt durch schlichten Würfel (Orakel) ohne Hintergrund.
- **Status unter den Regal-Covern:** im Regal ohne Rahmen, leise, Kurzlabel in einer Zeile („Lese ich“ / „أقرأه“, `ReadStatus.shortLabel()`); Menü und Bedienung unverändert, Touchfläche 48 dp.
- **Hinzufügen:** Kamera-Knopf als Hauptaktion zuerst; Untertitel „Barcode hinten“ / „Selbst eintragen“ statt abgeschnittener Texte.
- **Statistik:** „3D-Buchladen betreten“ als ruhiger umrandeter Knopf statt heller Vollfläche.

---

## Stand 04.11.2026 abends – Warraq 3.4.4 installiert, KI „Allam / علام“ am S24 geprüft

**Installiert: Warraq 3.4.4 / versionCode 17**, `Ausgaben/Signierte-Zwischenstaende/Warraq-3.4.4.apk`, SHA-256 `f7aa82cdfe60c52741780fe625e879aa0922206fe489c3e9b7fe054366b9bd8b`, Zertifikat unverändert `4403…6d42`. Zwischenstand 3.4.3 (`83677816…`). Vor jedem Update gesichert; Endvergleich: 15 Bücher, 13 Coverdateien bytegleich mit `Sicherungen/Geraet-vor-Claude-3.4.4-20261104` (`Pruefnachweise/Claude-3.4.4-20261104/final-data-check.json`).

- **Ursache der 403-Fehler gefunden:** Ollama weist den Android-Standard-User-Agent `Dalvik/…` mit HTTP 403 ab, noch vor der Schlüsselprüfung (am PC reproduziert: Dalvik → 403, eigener UA → 401 ohne Schlüssel). Fix in 3.4.3: `WarraqAi` sendet `LibraryRepository.USER_AGENT`. **Schlüssel und Konto waren in Ordnung; `beam` funktioniert.** Der Modell-Rückfall aus 3.4.2 bleibt als Absicherung.
- **KI heißt jetzt Allam / علام** (Mos Wunsch). Persona im System-Prompt: Buchhändler in einem engen alten Bücherladen im Souk von Damaskus, direkt, warm, trockener Humor, ehrlich; Stil nach `Persönlich/Arbeitsweise.md` (keine Floskeln, keine Emojis, kein KI-Ton), Arabisch mit leichtem syrischem Ton, immer „du“. Bewusst **keine privaten Profildaten** im Prompt, der an Ollama geht. Oberflächentexte: „Allam fragen“ / „اسأل علام“, Profilabschnitt „Allam, KI-Buchhändler (optional)“.
- Interne Listennummern (#…) werden aus KI-Texten entfernt (`WarraqAi.clean`). Buchfrage bekommt bis zu 20 gelesene/bewertete Bücher als Geschmack mit.
- **Am S24 mit Mos Schlüssel geprüft (3.4.3/3.4.4):** Orakel mit Wunsch „kurz und leicht“ → eigenes ungelesenes Buch + Begründung; Lesetipps (6 echte Bücher, Antippen öffnet Katalogsuche mit Werk/Ausgaben); Bibliothek fragen („Familie“ → passende Antwort + 3 antippbare eigene Bücher); Genres ordnen (sinnvolle Vorschläge, **nicht übernommen**, weil das Mos Bibliothek ändern würde); Jahresrückblick 2026; Buchfrage in den Details (Du-Form, Bezug auf Gibran aus der Bibliothek). Belege: Screenshots `ki-*.png`, `allam-buchfrage.png` in `Pruefnachweise/Claude-3.4.3-20261104` und `…-3.4.4-20261104`.
- Schlüssel: Mo hat ihn selbst in der App eingetragen (`noBackupFilesDir/ollama.key`); Inhalt von Claude nie gelesen, nirgends im Vault. Mo hat zwei Schlüssel im Chat gezeigt, Empfehlung: nach Abschluss neu erzeugen.

Nachgeprüft am 04.11.2026:
- **Arabische Oberfläche:** Sprache kurz auf العربية, „اسأل علام“ → القرعة → Antwort auf natürlichem Arabisch mit Kennzeichnung „ذكاء اصطناعي“ (`allam-arabisch-orakel.png`). Danach wieder auf Deutsch gestellt (`language=de` geprüft).
- **Zweiter Suchversuch:** Suche „Kafkka am Straand Murakamii“ → „Kafka am Strand: Roman“ (DuMont 2011) samt Originaltitel 海辺のカフカ (`allam-suchvarianten.png`). Gegenprobe: Open Library und DNB liefern für die Eingabe allein 0 Treffer, also über Allams Varianten gefunden. Nichts gespeichert.
- Endvergleich danach: 15 Bücher, 13 Cover bytegleich mit der Sicherung vor 3.4.4.

Offen: lateinischer Kameratest am echten Buch.

---

## Nachtrag 04.11.2026 – 3.4.2 installiert, KI antwortet mit HTTP 403

- **Installiert: Warraq 3.4.2 / versionCode 15** (`Warraq-3.4.2.apk`, SHA-256 `466c66bc447dd752a32aecb18fdea6d0850b8186a3d9d05aad262586a797240e`). Zwischenstände 3.4.1 (`1a450ba9…`) und 3.4 (`f9a6f3d1…`). Je Update gesichert, 15 Bücher/13 Cover bytegleich. Belege: `Pruefnachweise/Claude-3.4.1-20261104`, `…-3.4.2-20261104`.
- Änderungen: Datenschutzhinweis im Profil korrekt (Titel, Autor, Genre, Sprache, Seiten, Status, Bewertung, Lesejahr, bei Buchfragen Verlagsbeschreibung). Fehlertexte unterscheiden 401/403. `WarraqAi` probiert bei 400/403/404 nacheinander `beam`, `deepseek-v4.1-flash`, `gemma4:31b`, `gpt-oss:120b`, `gpt-oss:20b` und merkt sich das erste funktionierende; Schlüsselwechsel setzt das zurück.
- **Befund am S24:** Mit dem eingetragenen Schlüssel liefern **alle** Modelle HTTP 403 (ohne Schlüssel 401, Modell `beam` existiert laut `/api/tags`). Ursache liegt also beim Schlüssel/Konto, nicht bei `beam`. Direkter API-Zugriff braucht einen API-Schlüssel von ollama.com/settings/keys (nicht Geräteschlüssel/`ollama signin`); Modellname direkt `beam`, `beam:cloud` nur über die lokale Ollama-App. Laut Preisseite zahlt Pay-as-you-go pro Anfrage mit Guthaben; ohne Guthaben/Abo vermutlich Ablehnung (Schlussfolgerung, nicht dokumentiert).
- Mo hat zweimal Schlüssel im Chat gesendet und um Eintrag gebeten; Claude trägt echte API-Schlüssel nicht selbst ein und speichert sie nirgends (auch nicht hier). Mo trägt neuen Schlüssel selbst in der App ein; danach KI-Funktionen am S24 testen.

---

## Stand 04.11.2026 – Warraq 3.4 installiert, KI-Buchhändler eingebaut

**Installiert: Warraq 3.4 / versionCode 13**, `Ausgaben/Signierte-Zwischenstaende/Warraq-3.4.apk`, SHA-256 `f9a6f3d1149b12102fd5bd80d3d259a21fcc5d7614959652a89bd3e7e940912b`, Zertifikat unverändert `4403…6d42`. Sicherung vorher: `Sicherungen/Geraet-vor-Claude-3.4-20261104`; 15 Bücher und 13 Coverdateien nach dem Update bytegleich. Belege: `Pruefnachweise/Claude-3.4-20261104` (build.log, signature.txt, update-result.json, profil-ki.png). `testReleaseUnitTest assembleRelease` grün.

Fertiggestellt (Schritte 1–5 unten):
- `ui/AssistantScreen.kt`: Vollbild „Warraq fragen“ mit Reitern Orakel (Wunsch → eigenes ungelesenes Buch + Begründung → Öffnen), Lesetipps (Antippen öffnet `Layer.Search("Titel Autor")` → Katalog, Ausgaben, Ein-Klick), Bibliothek fragen (Antwort + antippbare eigene Bücher), Genres ordnen (Häkchen → `applyGenres`, nur leere Felder), Jahresrückblick. Alle KI-Texte mit Kennzeichnung „KI“; Fehlertext bei Netz-/Schlüsselproblem.
- `AskBookCard` in den Buchdetails unter dem Wissensbereich (`DetailActions.askAi`, nur mit Schlüssel).
- `MainActivity`: `Layer.Assistant`, Profil-Anbindung (`aiEnabled`, `onAiKey`, `onAssistant`), Einstiegs-Symbol in der Bibliothek-Kopfzeile neben dem Orakel (nur mit Schlüssel).

Am S24 geprüft: Start, Profilbereich „KI-Buchhändler (optional)“ mit Status „Aus“, Schlüsseldialog öffnet/schließt. **Die KI-Funktionen selbst sind noch nicht am Gerät geprüft**, weil noch kein Schlüssel eingetragen ist. Mo wollte den Schlüssel von Claude eintragen lassen; Claude trägt echte API-Schlüssel nicht selbst ein und speichert sie nirgends. Mo trägt ihn selbst ein (Profil → Ollama-Schlüssel); empfohlen: auf ollama.com neu erzeugen, da er im Chat stand.

Kleine Ungenauigkeit offen: Der Hinweis im Profil nennt „Titel, Autor, Genre, Status und Bewertung“. Tatsächlich gehen auch Sprache, Seiten, Lesejahr und bei Buchfragen die Verlagsbeschreibung mit. Text beim nächsten Build korrigieren.

Nächster Schritt: Schlüssel in der App → jede KI-Funktion einmal am S24 auslösen (Orakel, Tipps → Suche, Bibliothek fragen, Genres übernehmen nur nach Bestätigung, Jahresrückblick, Buchfrage, Suche ohne Treffer mit KI-Varianten).

---

Diese Übergabe hat Vorrang vor `Codex-Übergabe.md`. Projektregeln: `AGENTS.md`, Wünsche: `Anforderungen.md`.

## Gerätestand (geprüft)

- S24 `R5CX157KZGK`, Paket `de.mo.bibliothek`: **Warraq 3.3.5 / versionCode 12** installiert, APK `Ausgaben/Signierte-Zwischenstaende/Warraq-3.3.5.apk`, SHA-256 `5268055437c3b5dc904dda3a3d93cb3601f4c38022517e9b62a80a26b0741d0e`, bisheriges Zertifikat `4403…6d42` beibehalten.
- Bestand: **15 Bücher, 12 Coverdateien**, beim Update bytegleich. Sicherungen: `Sicherungen/Geraet-vor-Claude-3.3.4-20261103` und `…-3.3.5-20261103`. Das unter Codex über die Verlagssuche hinzugefügte „أبي الذي أكره“ (mit Cover, ISBN, Jahr) ist ein echtes Buch von Mo und bleibt.
- Testbuch „Der Alchimist: Roman“ wurde am Gerät per Ein-Klick hinzugefügt und danach in der App wieder gelöscht; Bestand wieder 15. Der abschließende Datenvergleich nach dem Löschen wurde unterbrochen und nicht gespeichert, die Anzahl 15 ist erneut geprüft.

## Erledigt in 3.3.4 / 3.3.5

- **Deutsche Nationalbibliothek als Quelle** (`data/DnbCatalog.kt`, SRU ohne Schlüssel): Google Books liefert ohne Schlüssel HTTP 429, deshalb kamen lateinische Treffer fast nur von Open Library. DNB liefert konkrete Ausgaben mit ISBN, Verlag, Jahr, Seiten, Sprache, Genre. Nur für nicht-arabische Anfragen.
- Abfrage: `tit=<ganzer Text>` plus `(tit=<Rest> and per=<letztes Wort>)`, aber nur wenn der Resttitel ein Wort ab 4 Buchstaben hat (sonst fremde Titel des Autors).
- DNB-Cover liegen hinter einer Bot-Schranke (Anubis) – **nicht umgehen**. Cover: Open Library nach ISBN, sonst `Found.fallbackCover` = Werkcover; die Ausgabenliste zeigt das Werkcover, `addFound` lädt Ausgabencover und fällt sonst aufs Werkcover zurück.
- Am S24 geprüft: Suche „Der Alchimist Coelho“ → Werk → Ausgabenliste mit Covern, ISBN, Diogenes 2026/2023/2021 → **ein Klick „Hinzufügen“** übernahm Titel, Autor, ISBN, Verlag, Jahr, Seiten, Sprache, Genre und Cover. Belege: `Pruefnachweise/Claude-3.3.4-20261103`, `Pruefnachweise/Claude-3.3.5-20261103`.
- Tests: `DnbCatalogTest` (echte DNB-Antwort als Fixture `src/test/resources/dnb-alchimist.xml`) plus alle bisherigen Unit-Tests grün.

## Begonnen, NICHT gebaut: KI-Buchhändler mit Ollama Cloud „beam“

Mo will ausdrücklich alle KI-Funktionen. Modell `beam` über `https://ollama.com/api/chat` (Bearer-Schlüssel), getestet: nur Text, keine Bilder, Fähigkeiten completion/thinking/tools, mit `think:false` und `format:"json"` ca. 2 s. Bekannte Schwäche: korrigiert zu eifrig („Alchimist“ → „Alchemist“), daher **nie als Faktenquelle**, Katalog entscheidet.

**Schlüssel:** Mo hat ihn im Chat gezeigt; er steht absichtlich **nirgends** im Vault oder Code. Mo trägt ihn in der App unter Profil → „Ollama-Schlüssel“ ein; empfohlen, ihn auf ollama.com neu zu erzeugen. Speicherort in der App: `noBackupFilesDir/ollama.key` (nicht in ZIP-Export, library.json oder Android-Backup). Gesendet werden nur Titel, Autor, Genre, Sprache, Seiten, Status, Bewertung, Lesejahr – keine Notizen, keine Fotos.

Bereits geschrieben (ungebaut, Kompilierung ungeprüft):
- `data/WarraqAi.kt`: `chat`, `searchVariants`, `oracle`, `tips`, `askLibrary`, `genres` (Format „Roman: …“/„Sachbuch: …“, damit `Book.kind()` greift), `yearReview`, `askBook`. Persona: Buchhändler aus dem Souk von Damaskus, ohne Spoiler, Antwort Deutsch/Arabisch nach App-Sprache.
- `BookieViewModel.kt`: Schlüssel laden/speichern (`setAiKey`, `aiEnabled`), Wrapper `aiOracle/aiTips/aiAsk/aiGenres/aiYear/aiAskBook`, `applyGenres` (nur leere Genrefelder), `withAiVariants`: wenn eine Suche **null** Treffer hat und KI aktiv ist, werden bereinigte Eingabe und Original-/Übersetzungstitel zusätzlich gesucht.
- `ui/ProfileScreen.kt`: Abschnitt „KI-Buchhändler (optional)“ mit Schlüsseldialog und Eintrag „Warraq fragen“; neue Parameter `aiEnabled`, `onAiKey`, `onAssistant` (mit Standardwerten).

Noch zu tun:
1. `MainActivity.kt`: `ProfileScreen(..., aiEnabled = model.aiEnabled…, onAiKey = model::setAiKey, onAssistant = { push(Layer.Assistant) })`, neuen `Layer.Assistant`, und einen Einstieg in der Bibliothek-Kopfzeile neben dem Orakel (`LibraryScreen` Parameter `onAssistant`).
2. Neue `ui/AssistantScreen.kt` (Vollbild-Layer) mit fünf Bereichen: **Orakel mit Begründung** (Wunschfeld → Buch + Grund → Öffnen), **Lesetipps** (Liste; Tippen öffnet `Layer.Search(query = "Titel Autor")`, damit Cover/Ausgaben/Ein-Klick aus den Katalogen kommen), **Bibliothek fragen** (Antwort + antippbare Bücher), **Genres ordnen** (Vorschläge mit Häkchen → `applyGenres`), **Jahresrückblick** (Text, als KI-Text gekennzeichnet). Ladezustand und Fehlertexte bei fehlendem Netz/ungültigem Schlüssel.
3. `ui/DetailScreen.kt`: Karte „Warraq fragen“ unter `KnowledgePanel` (Frage → `aiAskBook`), nur wenn KI aktiv; `DetailActions` um eine optionale Funktion erweitern.
4. Alle KI-Texte sichtbar als „KI“ beschriften, getrennt von echten Zitaten/Verlagsbeschreibungen.
5. Version 3.4 / versionCode 13, bauen (`testReleaseUnitTest assembleRelease`), signieren, mit `Werkzeuge/S24-Pruefung.py` (Eintrag für 3.4 + Hash ergänzen) sichern und installieren, am S24 jede KI-Funktion einmal prüfen; Mo trägt den Schlüssel selbst ein.

## Offen außerhalb der KI

- **Kameratest mit einem lateinischen Buch** steht weiter aus (kein Buch griffbereit gewesen). Arabisch ist am echten Buch bestätigt.
- Optional: eigener Google-Books-Schlüssel würde Google wieder nutzbar machen; ebenfalls nur außerhalb des Vaults ablegen.
- Amazon bleibt externer Suchknopf, kein Import.

## Technik-Hinweise

- Build: Gradle-Daemon schreibt durch die Codex-Sandbox nach `C:\Users\49157\AppData\Local\Packages\OpenAI.Codex_2p2nqsd0c76g0\LocalCache\Local\Waraq\Build\outputs\app\outputs\apk\release\app-release-unsigned.apk` – nach jedem Build `output-metadata.json` (versionCode) prüfen, kein altes APK signieren. Das APK unter `%LOCALAPPDATA%\Waraq\Build\outputs` ist vom 30.10. und veraltet.
- Signieren mit dem bestehenden externen Schlüssel (Pfad in `Technik und Quellen.md`), nie in den Vault kopieren.
- `Werkzeuge/S24-Pruefung.py`: ab 3.3.4 Beleg-/Sicherungsordner mit Präfix `Claude-`; `update` verweigert Überschreiben bestehender Sicherungen.
- PowerShell 5.1 `Set-Content -Encoding utf8` schreibt ein BOM – für `.kts` mit `[IO.File]::WriteAllText(..., UTF8Encoding($false))` schreiben.
- REA/JADX ist eingerichtet (siehe `Pruefnachweise/Recherche-20261031/REA-Erkenntnisse.md`).

Zurück: [[Projekte/Waraq/Start]]
