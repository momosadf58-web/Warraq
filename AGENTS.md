# Warraq – Regeln für KI-Agenten (Codex, Claude und andere)

Android-App „Warraq / الورّاق“, Paket `de.mo.bibliothek`, aktuell Version 3.4.7 (versionCode 20). Persönliche Bibliothek auf Deutsch und Arabisch. Was die App kann, steht in `README.md`.

## Einstieg

- Quellcode: `Code/Android` (Kotlin, Jetpack Compose, Gradle 8.9, JDK 17, SDK 35). Codekarte im README unter „Projektstruktur“.
- Bauen: `cd Code/Android && ./gradlew assembleDebug`. Tests: `./gradlew testDebugUnitTest`.
- **Auf Mos Laptop** liegen weitere interne Notizen, die nicht im Repo sind: `Start.md`, `Codex-Übergabe.md`, `Anforderungen.md`, `Technik und Quellen.md` und `Stand.md` im selben Ordner. Wenn sie vorhanden sind, zuerst `Start.md` und den obersten Abschnitt von `Codex-Übergabe.md` lesen.

## Zusammenarbeit mehrerer Agenten

- Jeder Agent arbeitet auf einem eigenen Branch, z. B. `codex/kamera-latein` oder `claude/statistik-filter`. Nie direkt zu zweit in denselben Dateien.
- Kleine, nachvollziehbare Commits, danach ein Pull Request auf `main`.
- Keine fertigen APKs committen. Releases kommen unter GitHub Releases.

## Fachliche Regeln

- Alle bisherigen Funktionen zusammen erhalten. Die Kameraerkennung ersetzt weder 3D-Raum noch Orakel, Statistik, Buchinhalte oder Bildkarten.
- Arabische Bücher, Autoren, Texterkennung und RTL sind gleichwertige Kernanforderungen.
- Nutzerdaten schützen: Bücher, Cover, unbekannte JSON-Felder, Status, Notizen und Bewertungen erhalten. Daten- und Coverkorrekturen nur nach bestätigter Auswahl. Die App beim Testen nie deinstallieren.
- Paketname und bestehende Signatur beibehalten.
- **Keine Geheimnisse ins Repository:** keine API-Schlüssel (Ollama, Google), Signierschlüssel, Kennwörter oder echte Nutzerbibliotheken. Schlüssel trägt die Nutzerin oder der Nutzer selbst in der App ein (`noBackupFilesDir`). Die Schlüsseldatei nie auslesen.
- Die KI „Allam“ ist nie Faktenquelle: ISBN, Cover und Jahr kommen nur aus Katalogen. Keine privaten Profildaten in KI-Prompts.
- Die KI-Lesehilfe nutzt nur vorhandene Beschreibungen und Zitate, schreibt keine Buchdaten um und erzeugt niemals Originalzitate. KI-Kernaussagen werden auch auf Bildkarten ausdrücklich als KI, sinngemäß und „kein Zitat“ gekennzeichnet.
- Ponytail: kleinste vollständige Lösung, gezielt testen, keine unnötigen Testschleifen. Ein früher bestandener Test ist kein Beleg für neuen Code.
- Neue Fremdbibliotheken oder Daten mit ihrer Lizenz in `THIRD_PARTY.md` eintragen. Nur Lizenzen verwenden, die eine nicht-kommerzielle Weitergabe erlauben.
