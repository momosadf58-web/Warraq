---
type: project
status: active
updated: 2026-11-01
---

# Metadaten- und Cover-Import – Plan

## Festlegung (Mo)

- Ziel des ersten Umfangs: **Titel + Autor** als Vorschlag, kein Pflichtfeld.
- Cover wird **nicht automatisch** übernommen: Vorschlag anzeigen, Nutzer wählt.
- Quellen: **kostenlos** und nutzungsrechtsgeklärt.
- Alle bisherigen Daten bleiben geschützt; keine stille Überschreibung.

## Status quo im Code

- Titel/Autor können schon erkannt und bearbeitet werden.
- `BookLookup.search(...)` sucht in internen Katalogen und Wikipedia; strenger Titelabgleich, um falsche Werke desselben Autors zu vermeiden.
- Hindawi-Blurbs sind bereits offline angebunden.
- Amazon ist externer Suchzugang (Knopf), **kein** Import.
- Ausgaben-/Coverfluss hat bereits Ausgabenwahl, Cover-Fingerabdruck und Vorschau.

## Vorgeschlagene Reihenfolge der Quellen

1. **Hindawi** für arabische Werke/Daten, sofern vorhanden (quasi lokal, offline).
2. **Open Library** als allgemeine, kostenlose Metadaten-/Coverquelle.
3. **Google Books** als Fallback, mit API-Regeln und Hinweisen.
4. **Amazon** bleibt Suchknopf; keine Seitenextraktion, kein Scraping.

## Funktionsprinzip (Vorschlag, noch nicht implementiert)

1. Nach einer Titel-/Autorkennung oder im Detailbildschirm kann Mo „Metadaten/Cover vorschlagen“ auslösen.
2. Warraq fragt nacheinander Hindawi → Open Library → Google Books.
3. Ergebnisse werden **als Vorschlag** angezeigt: Quelle, Titel, Autor, Coverminiatur.
4. Nutzer bestätigt eine Quelle erst dann:
   - Titel/Autor können ins Formular übernommen werden.
   - Cover kann manuell gewählt werden.
5. Ohne passenden Treffer gibt es eine klare Meldung; vorhandene Amazon-Suche bleibt.

## Rechtliche/technische Leitplanken

- **Open Library:** keine Crawler; normale, nutzergetriebene Einzelabfragen; Attribution zu Open Library/Internet Archive; bei Coverzugriff nicht im großen Stil laden. Rate Limits berücksichtigen (z. B. 100 Anfragen je 5 Minuten für bestimmte IDs).
- **Google Books:** API-Nutzungsbedingungen, Attribution und Branding berücksichtigen; nicht als Ersatz für kommerzielle Dienste behandeln.
- Kein Bulk-Download von Metadaten oder Covern.
- Keine stillen Änderungen an bestehenden Büchern.
- Offline-first erhalten: Hindawi/local zuerst.

## Technische Vorschläge für spätere Umsetzung

- Neuer Adapter z. B. `data/BookEnrichment.kt` oder Erweiterung von `BookLookup`.
- Gemeinsame Schnittstelle: `MetadatenVorschlag(quelle, titel, autor, coverUrl, confidence)`.
- Anfragezähler/Rückoff für offene Webquellen.
- Keine permanente Speicherung von Fremdcovern ohne Nutzerauswahl; ausgewähltes Cover wie bisher lokal.
- Tests für Quellen-Ranking und Titelübereinstimmung (insbesondere Arabisch, gleicher Autor, ähnliche Titel).
- Keine Verschlechterung des Werktitel-Abgleichs in `BookLookup`.

## Offene klare Fragen vor Code

- Soll bei Open Library zuerst ISBN gesucht werden, wenn in Warraq schon eine ISBN vorhanden ist?
- Soll bei Google Books das Anzeige-Bild nur als Link/Vorschau dienen oder kann es gespeichert werden?
- Braucht die App einen von Mo gesetzten Kontakt-/Agent-Hinweis für Open Library-Abfragen?

## Nächster Schritt

Offline zuerst: Adapter-Skizze und kleine, abgrenzbare Implementierung hinter einem „Vorschläge laden“-Knopf, ohne vorhandene Suchlogik zu verändern. Erst danach am S24 testen (Gerät aktuell nicht angeschlossen).
---
[[Projekte/Waraq/Codex-Übergabe|Aktueller Abbruchpunkt]] | [[Projekte/Waraq/Anforderungen|Alle bestätigten Wünsche]] | [[Projekte/Waraq/Stand|Prüfergebnisse]]