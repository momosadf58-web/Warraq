---
type: project
status: active
updated: 2026-11-06
aliases:
  - Warraq
  - Bookie
---

# Waraq – Projektstand und Übergabe

**Stand 06.11.2026: Warraq 3.4.7 / versionCode 20 über WLAN installiert und am S24 geprüft.** Ordnername **Waraq**, App **Warraq / الورّاق**, Paket `de.mo.bibliothek`.

## Zuerst lesen

1. **[[Projekte/Waraq/Codex-Übergabe|Aktuelle Übergabe]]**: oberster Abschnitt vom 06.11.2026 = KI-Lesehilfe, Klappentext, Zitat-Erklärung. UI/Motion vom Vortag darunter. Vorheriger KI-/Quellenausbau in [[Projekte/Waraq/Claude-Übergabe-20261103]].
2. [[Projekte/Waraq/Anforderungen|Alle bestätigten Wünsche und Entscheidungen]].
3. [[Projekte/Waraq/Technik und Quellen|Werkzeuge, Build, Quellen, Signierung, KI]].
4. [[Projekte/Waraq/Stand|Prüfergebnisse und historische Meilensteine]].
5. [[Projekte/Waraq/Übergabe|Codekarte und Fortsetzungsregeln]] · [[Projekte/Waraq/Codex-Übergabe|älterer Codex-Stand]].

## Was die App jetzt kann (am S24 geprüft)

- **Buch finden und mit einem Klick hinzufügen:** Titelsuche → Werk → alle Ausgaben mit Cover, ISBN, Verlag, Jahr → „Hinzufügen“. Quellen: Open Library, Deutsche Nationalbibliothek (deutsche Ausgaben, ohne Schlüssel), Alrewaq (arabischer Verlag), lokaler Hindawi-Katalog; Google Books ohne eigenen Schlüssel gedrosselt (HTTP 429); Amazon nur als Suchknopf.
- **Kamera:** arabischer Titel/Autor am echten Buch erkannt; lateinisch am echten Buch **noch nicht geprüft**.
- **KI „Allam / علام“** (optional, Ollama Cloud, Modell `beam`, Schlüssel trägt Mo selbst in der App ein): Orakel mit Begründung, Lesetipps → Katalogsuche, Bibliothek fragen, Genres vorschlagen (Übernahme nur nach Häkchen), Jahresrückblick, Buchfrage in den Details, zweiter Suchversuch bei null Katalogtreffern. Deutsch und arabische Oberfläche geprüft. Alle KI-Texte als „KI“ markiert.
- **Neu: Allams Lesehilfe in den Buchdetails.** Mit aktivem Schlüssel automatisch aus vorhandenen Quellen: kurzer Klappentext auf Deutsch/Arabisch; Auswahl und Erklärung eines vorhandenen Originalzitats mit unverändertem Quellenlink. Andernfalls separat markierte sinngemäße Kernaussage, auch als korrekt beschriftete Bildkarte. Keine KI-erzeugten Originalzitate. Ergebnisse nur im Sitzungscache, bestehende Buchdaten bleiben unverändert.
- **Erscheinungsbild (3.4.6):** Oliv-/Papierstil und App-Symbol erhalten. Entzerrte Kopfzeile mit beschrifteter Ansicht/Sortierung, anpassbare Aktionskarten, lesbarere Ausgabenliste und sichtbarer Suchknopf. Buchdruck mit leichter Skalierung/Kippung, Cover-Eintritt, gerichtete Tab-Übergänge (RTL gespiegelt) und animierte Listenänderungen. Android-Animationsskala wird berücksichtigt.
- Bisherige Funktionen bleiben: drei Statusknöpfe, Orakel, Statistik/Bücherraum, 3D-Souk, Buchwissen/Zitate/Bildkarte, Sicherung/Wiederherstellung.
- Gerätebestand **15 Bücher, 12 Coverdateien**, vor/nach 3.4.7 und allen KI-/Bildkartentests bytegleich. Neue Testreste entfernt; frühere Bereinigung vom 05.11. dokumentiert.

## Offen

- Kameratest mit einem lateinischen Buch.
- Optional eigener Google-Books-Schlüssel (nur außerhalb des Vaults).
- Mo sollte die im Chat gezeigten Ollama-Schlüssel auf ollama.com neu erzeugen und den neuen in der App eintragen.

## Ablage

| Bereich | Inhalt |
|---|---|
| `Code/Android` | Maßgeblicher Quellstand, Gradle-Wrapper, Modelle, Kataloge, 3D-Assets |
| `Ausgaben/Signierte-Zwischenstaende` | Signierte APKs bis 3.4.7 (installiert) |
| `Sicherungen` | Eigene Bibliothek/Cover, darunter die Sicherung vor Codex-Update |
| `Pruefnachweise/Codex-3.4.7-20261106` | Aktuelle KI-Lesehilfe-Belege: Build/Tests, Signatur, Datenvergleich, deutsche/arabische Screenshots, Bildkarte. UI/Motion unter Codex-3.4.6-20261105 |
| `Historie` | Ältere Quellen/APKs, Dateien vor 3.3.2 und bisherige Claude-Übergabe |
| `Werkzeuge` | Build-, Sicherungs- und Geräteprüfhilfen; historische Skripte nicht blind ausführen |
| `Quellen` | Eingefügter Anti-Gravity-Verlauf |
| `Dateimanifest.json` | Größen und SHA-256 der gesicherten Projektdateien |

Private Schlüssel und Zugangsdaten bleiben außerhalb des Vaults. Der bestehende Projektordner ist die einzige maßgebliche Ablage. Kopierbarer Weiterarbeitsauftrag: [Claude-Start.txt](Claude-Start.txt); nicht versendet.

Zurück: [[Projekte/Übersicht]]
