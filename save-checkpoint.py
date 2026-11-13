import hashlib, json, pathlib, re, shutil, tarfile, zipfile

stage = pathlib.Path(__file__).parent
vault = pathlib.Path(r"C:\Users\49157\OneDrive\Desktop\KI MO\AI Brain")
target = vault / "Projekte" / "Waraq"
previous = pathlib.Path(r"C:\Users\49157\Documents\Codex\2026-10-29\new-chat")
target.mkdir(parents=True, exist_ok=True)
manifest = []
excluded = []
ignored_dirs = {"build", ".gradle", ".kotlin", ".git", "node_modules", "__pycache__", ".obsidian", ".aws", ".codex"}
secret_names = re.compile(r"(?:\.keystore|\.jks|\.p12|\.pfx|\.pem|\.key)$|^\.env(?:\.|$)|credentials|secrets", re.I)
secret_text = re.compile(r"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----|\bsk-[A-Za-z0-9_-]{25,}|\bhf_[A-Za-z0-9]{25,}|\bghp_[A-Za-z0-9]{25,}")

def safe_text(path):
    if path.suffix.lower() not in {".md", ".kt", ".kts", ".java", ".xml", ".json", ".ps1", ".cjs", ".js", ".py", ".txt", ".properties", ".log", ".html"}:
        return
    text = path.read_text(encoding="utf-8-sig", errors="replace")
    if secret_text.search(text) or re.search(r"--(?:ks|key)-pass\s+pass:", text):
        raise RuntimeError("Möglicher Schlüssel/Zugangsdaten im Inhalt; nicht kopiert: " + str(path))

def copy(source, relative):
    if secret_names.search(source.name):
        excluded.append(str(source)); return
    safe_text(source)
    destination = target / relative
    destination.parent.mkdir(parents=True, exist_ok=True)
    source_hash = hashlib.sha256(source.read_bytes()).hexdigest()
    if destination.exists() and hashlib.sha256(destination.read_bytes()).hexdigest() != source_hash:
        raise RuntimeError("Abweichende Zieldatei nicht überschrieben: " + str(destination))
    shutil.copy2(source, destination)
    if hashlib.sha256(destination.read_bytes()).hexdigest() != source_hash:
        raise RuntimeError("Hashprüfung fehlgeschlagen: " + str(destination))
    manifest.append({"file": destination.relative_to(target).as_posix(), "bytes": destination.stat().st_size, "sha256": source_hash})

def tree(source, relative):
    for path in sorted(source.rglob("*")):
        if path.is_file() and not any(part in ignored_dirs for part in path.relative_to(source).parts):
            copy(path, pathlib.Path(relative) / path.relative_to(source))

tree(stage / "project", "Code/Android")
tree(previous / "outputs/bookie3", "Historie/Compose-3.0-vor-dieser-Arbeit")
tree(previous / "outputs/bookie", "Historie/Bookie-2.1")
for source in stage.iterdir():
    if source.is_file() and source.suffix.lower() in {".cjs", ".py", ".ps1"}:
        copy(source, pathlib.Path("Werkzeuge/Entwicklungsverlauf") / source.name)
    if source.is_file() and source.suffix.lower() == ".log":
        copy(source, pathlib.Path("Pruefnachweise/Build-Protokolle") / source.name)
copy(stage / "baseline.json", pathlib.Path("Historie/baseline-vor-Arbeit.json"))
for source in (stage / "evidence").iterdir():
    if source.is_file() and source.name != "before-files.tar":
        copy(source, pathlib.Path("Pruefnachweise") / source.name)
for source in stage.glob("*.apk*"):
    copy(source, pathlib.Path("Ausgaben/Signierte-Zwischenstaende") / source.name)
apk_root = stage / "project/app/build/outputs/apk"
for source in apk_root.rglob("*"):
    if source.is_file():
        copy(source, pathlib.Path("Ausgaben/Gradle-Builds-vor-letzten-Codeaenderungen") / source.relative_to(apk_root))
for source in (previous / "outputs").glob("*.apk*"):
    copy(source, pathlib.Path("Historie/Fruehere-APKs") / source.name)
attachment = pathlib.Path(r"C:\Users\49157\.codex\attachments\e017f72c-9837-4266-bce1-353b2a50a87e\Eingefügter Text.txt")
copy(attachment, pathlib.Path("Quellen/Anti-Gravity-Verlauf.txt"))

# Retain the complete library/cover backup, but exclude platform and MLKit identifiers/configuration.
allowed = re.compile(r"files/library\.json|files/(?:covers/[a-f0-9-]{36}|remote-covers/[a-f0-9]{64})\.jpg|shared_prefs/bookie\.xml")
backup_path = target / "Sicherungen/Android-Dateien-bereinigt.zip"
backup_path.parent.mkdir(parents=True, exist_ok=True)
with tarfile.open(stage / "evidence/before-files.tar") as tar, zipfile.ZipFile(backup_path, "w", zipfile.ZIP_DEFLATED) as archive:
    for member in tar.getmembers():
        if member.isfile() and allowed.fullmatch(member.name):
            data = tar.extractfile(member).read()
            if member.name.endswith("bookie.xml"):
                import xml.etree.ElementTree as ET
                prefs = ET.fromstring(data)
                if any(child.attrib.get("name") not in {"language", "yearGoal", "viewMode", "sort"} for child in prefs):
                    raise RuntimeError("Unerwartete Einstellung im Backup")
            archive.writestr(member.name, data)
with tarfile.open(stage / "evidence/before-files.tar") as tar, zipfile.ZipFile(target / "Sicherungen/Warraq-Bibliothek-vor-Arbeit.zip", "w", zipfile.ZIP_DEFLATED) as archive:
    books = json.loads((stage / "evidence/before-library.json").read_text(encoding="utf-8"))
    archive.writestr("manifest.json", json.dumps({"format": "mos-buecher", "version": 2, "books": books}, ensure_ascii=False))
    for name in sorted({book["cover"] for book in books if book.get("cover")}):
        archive.writestr("covers/" + name, tar.extractfile("files/covers/" + name).read())

old = vault / "Projekte/Bookie.md"
history = target / "Projektverlauf.md"
if old.exists() and not history.exists():
    copy(old, pathlib.Path("Projektverlauf.md"))
    old.write_text("# Warraq / Waraq\n\nDer vollständige, pausierte Projektstand liegt unter [[Projekte/Waraq/Start]].\n\nHistorischer Verlauf: [[Projekte/Waraq/Projektverlauf]].\n", encoding="utf-8")
(target / "Dateimanifest.json").write_text(json.dumps({"date": "2026-10-30", "files": manifest, "excluded_credentials": excluded, "note": "Build-/Download-Caches und private Schlüssel bleiben außerhalb des Vaults. Generierte Übergabedokumente werden separat ergänzt."}, ensure_ascii=False, indent=2), encoding="utf-8")
print(json.dumps({"copied_and_verified": len(manifest), "bytes": sum(entry["bytes"] for entry in manifest), "directory": str(target)}, ensure_ascii=False))
