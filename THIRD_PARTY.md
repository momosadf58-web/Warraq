# Drittanbieter-Bestandteile / Third-party components

Die [PolyForm-Noncommercial-Lizenz](LICENSE) dieses Repositorys gilt nur für den eigenen Code von Warraq. Alles hier Aufgeführte behält **seine eigene Lizenz**, und deren Bedingungen gehen vor.

*The repository's PolyForm Noncommercial license covers only Warraq's own code. Everything listed here keeps its own license.*

## Mitgelieferte Dateien (im Repository und in der APK)

| Bestandteil | Wo | Lizenz |
|---|---|---|
| [Three.js](https://github.com/mrdoob/three.js) r170 | `assets/room/three.module.min.js` | MIT (siehe `assets/room/THREE-LICENSE.txt`) |
| [Tesseract tessdata_best](https://github.com/tesseract-ocr/tessdata_best) 4.1.0, Arabisch | `assets/tessdata/` | Apache 2.0 (siehe `Code/Android/LIZENZEN/Tesseract-models-Apache-2.0.txt`) |
| [PaddleOCR](https://github.com/PaddlePaddle/PaddleOCR) PP-OCRv3, ONNX-Fassung von [monkt/paddleocr-onnx](https://huggingface.co/monkt/paddleocr-onnx) | `assets/paddle/` | Apache 2.0 |
| Schrift [Aref Ruqaa](https://github.com/alif-type/aref-ruqaa) | `res/font/` | SIL Open Font License 1.1 (siehe `Code/Android/LIZENZEN/ArefRuqaa-OFL.txt`) |
| Hindawi-Metadaten aus [The Arabic E-Book Corpus](https://researchdata.se/en/catalogue/dataset/2024-145/1) (Andreas Hallberg, Universität Göteborg) | `assets/hindawi-catalog.json` | CC BY 4.0 |
| Buchbeschreibungen aus dem [Hindawi-Books-dataset](https://huggingface.co/datasets/alielfilali01/Hindawi-Books-dataset) (alielfilali01) | `assets/hindawi-blurbs.json` | CC BY-NC 4.0 (nur nicht-kommerziell) |

## Bibliotheken (werden beim Bauen über Gradle geladen)

| Bibliothek | Lizenz |
|---|---|
| AndroidX, Jetpack Compose, CameraX, WebKit, Palette | Apache 2.0 |
| Kotlin | Apache 2.0 |
| [Tesseract4Android](https://github.com/adaptech-cz/Tesseract4Android) 4.9.0 | Apache 2.0 |
| [ONNX Runtime](https://github.com/microsoft/onnxruntime) 1.23.0 | MIT |
| [Coil](https://github.com/coil-kt/coil) 2.7.0 | Apache 2.0 |
| [AndroidSVG](https://github.com/BigBadaboom/androidsvg) 1.4 | Apache 2.0 |
| Google ML Kit Text Recognition, Google Code Scanner (Play Services) | [Google APIs Terms of Service](https://developers.google.com/terms) und [ML Kit Terms](https://developers.google.com/ml-kit/terms) |
| JUnit 4 (nur Tests) | EPL 1.0 |
| org.json 20240303 (nur Tests) | Public Domain |

## Online-Dienste (werden zur Laufzeit abgefragt, nichts davon ist mitgeliefert)

| Dienst | Hinweis |
|---|---|
| [Open Library](https://openlibrary.org/developers/api) | Daten CC0, Cover laut [Open-Library-Richtlinien](https://openlibrary.org/dev/docs/api/covers) |
| [Deutsche Nationalbibliothek](https://www.dnb.de/DE/Professionell/Metadatendienste/Datenbezug/SRU/sru_node.html) SRU | Metadaten CC0 |
| [Google Books API](https://developers.google.com/books) | Google APIs Terms of Service |
| Alrewaq (arabischer Verlag), Hindawi-Coverbilder | Bilder und Texte gehören den jeweiligen Verlagen. Sie werden nur zur Anzeige im eigenen Regal abgerufen. |
| [Wikipedia](https://www.wikipedia.org/), [Wikidata](https://www.wikidata.org/), [Wikiquote](https://www.wikiquote.org/) | Texte CC BY-SA 4.0, Wikidata CC0. Die App zeigt den Quellenlink an. |
| [Google Gemini API](https://aistudio.google.com/api-reference/generate-content) (optional, eigener Schlüssel) | [Nutzungsbedingungen der Gemini API](https://ai.google.dev/gemini-api/terms) |

Buchcover, die die App aus diesen Diensten lädt, sind urheberrechtlich geschützt und gehören den Verlagen. Sie werden nicht mit dem Code verbreitet.
