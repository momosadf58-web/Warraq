# Warraq

> **Geschichten, die bleiben.**

Warraq ist eine ruhige, moderne Buchplattform für Menschen, die neue Bücher
entdecken, Empfehlungen speichern und bewusst lesen möchten. Die Oberfläche
verbindet eine redaktionelle Startseite mit einer einfachen Buchsuche und
einer persönlichen Merkliste.

## Was enthalten ist

- **Entdecken:** kuratierte Bücher mit Kategorie, Autor, Jahr und Bewertung
- **Suche:** filtert nach Buchtitel, Kategorie und Autor
- **Buchdetails:** öffnet eine ausführliche Detailansicht direkt auf der Seite
- **Merkliste:** speichert Lieblingsbücher dauerhaft im Browser
- **Kategorien:** schneller Einstieg über Romane, Biografien, Geschichte und Philosophie
- **Magazin:** redaktioneller Einstieg für Lesethemen und Essays
- **Newsletter:** Formular mit direkter Rückmeldung
- **Responsive Design:** optimiert für Desktop, Tablet und Smartphone

## Lokal starten

Warraq ist aktuell ein statischer Prototyp ohne Build-Schritt und ohne externe
Abhängigkeiten. Direkt öffnen:

```bash
open index.html
```

Oder mit einem lokalen Webserver:

```bash
python3 -m http.server 8000
```

Danach ist die Seite unter <http://localhost:8000> erreichbar.

## Projektstruktur

```text
.
├── index.html   # Seitenstruktur und Inhalte
├── styles.css   # Layout, Farben, Typografie und responsive Regeln
├── script.js    # Suche, Merkliste, Dialoge und Formulare
└── README.md    # Projektdokumentation
```

## Technologie

- HTML5
- CSS3
- Vanilla JavaScript
- `localStorage` für die lokale Merkliste
- Google Fonts: DM Sans und Playfair Display

## Nächste Ausbaustufen

1. Buchdaten aus einer API oder Datenbank laden
2. Nutzerkonten und synchronisierte persönliche Leselisten
3. Echte Magazinartikel und redaktionelle Sammlungen
4. GitHub Pages oder ein eigener Hosting-Deploy