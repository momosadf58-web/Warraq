import json, pathlib, sys, urllib.request

root = pathlib.Path(__file__).parent
sys.path.insert(0, str(root / "tools" / "py"))
import duckdb

def get(url):
    with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": "Warraq/3.1 metadata build"}), timeout=25) as response:
        return json.load(response)

dataset = "alielfilali01/Hindawi-Books-dataset"
listing = get("https://huggingface.co/api/datasets/" + dataset + "/tree/main/data")
urls = ["https://huggingface.co/datasets/" + dataset + "/resolve/main/" + entry["path"] for entry in listing if entry.get("path", "").endswith(".parquet")]
print("Parquet-Metadatenquellen:", len(urls), flush=True)
db = duckdb.connect()
db.execute("SET extension_directory = ?", [str(root / "tools" / "duckdb-extensions")])
db.execute("INSTALL httpfs; LOAD httpfs")
db.execute("SET http_timeout=30; SET http_retries=1")
entries = {}
for url in urls:
    # Project only bibliographic fields; ChapterText is never requested or retained.
    rows = db.execute('SELECT DISTINCT BookName, AuthorName, AboutBook, BookLink FROM read_parquet(?)', [url]).fetchall()
    for title, author, about, link in rows:
        if title and about:
            entries[link] = {"title": title, "author": author or "", "description": about[:5000], "url": link}
    print("Beschreibungen:", len(entries), flush=True)
output = root / "project/app/src/main/assets/hindawi-blurbs.json"
output.write_text(json.dumps(list(entries.values()), ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
print("Fertig:", len(entries), output.stat().st_size, flush=True)
