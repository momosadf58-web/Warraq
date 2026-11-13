# Waraq

Projekt aktiv. Stand 06.11.2026: Warraq 3.4.7 über WLAN installiert; KI-Lesehilfe für Beschreibungen und vorhandene Zitate am S24 geprüft. 15 Bücher/12 Cover bytegleich erhalten, Testreste entfernt.

Zuerst `Start.md` und den obersten Abschnitt von `Codex-Übergabe.md` (06.11.2026), danach `Anforderungen.md` und `Technik und Quellen.md` lesen. Die Claude-Übergabe dokumentiert die vorherigen Funktionsausbauten. Die allgemeinen AI-Brain-Regeln im Vault gelten weiter.

- Maßgeblicher Quellstand: `Code/Android`; nicht versehentlich den älteren Dokumente-Ordner oder ein APK als neuesten Stand behandeln.
- Alle Wünsche aus den sechs Fragerunden zusammen erfüllen; Kameraerkennung ersetzt weder 3D-Raum noch Orakel, Statistik, Buchinhalte oder Bildkarten.
- Keine Deinstallation der Nutzer-App. Bücher, Cover, unbekannte JSON-Felder, Status, Notizen und Bewertungen erhalten. Daten-/Coverkorrekturen nur nach bestätigter Auswahl.
- Paket und bestehende Signatur beibehalten. Private Schlüssel und Zugangsdaten niemals im Vault oder in Sicherungen ablegen.
- Arabische Bücher, Autoren, Texterkennung und RTL sind gleichwertige Kernanforderungen.
- Ponytail anwenden, gezielt prüfen und möglichst am S24 testen; keine unnötigen Testschleifen. Vor jeder Installationsbehauptung tatsächlichen Build, Signatur und Gerätestand prüfen.
- Ein früher bestandener Test ist kein Beleg für neu hinzugefügten Code. `Stand.md` ehrlich aktualisieren.
- Bestätigte neue Entscheidungen in den bestehenden Projektunterlagen pflegen, keine parallelen Projektordner oder Vaultkopien anlegen.
- Echte API-Schlüssel (Ollama, Google) niemals in Code, Vault, Prompts-Archiv oder Belege schreiben; Mo trägt sie selbst in der App ein (`noBackupFilesDir`). Schlüsseldatei nie auslesen.
- KI „Allam“ ist nie Faktenquelle: ISBN, Cover, Jahr kommen nur aus Katalogen. Keine privaten Profildaten in KI-Prompts.
- KI-Lesehilfe nutzt nur vorhandene Beschreibung/Zitate, schreibt keine Buchdaten um und erzeugt niemals Originalzitate. Quellenobjekte bleiben aus KnowledgeLookup; KI-Kernaussagen auch auf Bildkarten ausdrücklich als KI/sinngemäß/kein Zitat kennzeichnen.
