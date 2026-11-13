"""Prüft nur lokale Sicherungsdateien, startet weder App noch Build."""
import hashlib
import json
from pathlib import Path

root = Path(__file__).resolve().parent.parent
manifest = json.loads((root / "Dateimanifest.json").read_text(encoding="utf-8"))
errors = []
for item in manifest["files"]:
    path = root / item["file"]
    if not path.is_file() or hashlib.sha256(path.read_bytes()).hexdigest() != item["sha256"]:
        errors.append(item["file"])
if errors:
    raise SystemExit("Abweichend/fehlend: " + ", ".join(errors))
print(f"{len(manifest['files'])} Dateien stimmen mit dem Sicherungsmanifest überein.")
