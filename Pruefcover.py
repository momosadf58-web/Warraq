"""Publisher image recognition probe; binary transfer verified before opening Warraq."""
import hashlib
import subprocess
import urllib.request
import tempfile
from pathlib import Path

adb = r'C:\Users\49157\Documents\Mos Oase\Android\Werkzeuge\AndroidSDK\platform-tools\adb.exe'
base = [adb, '-s', 'R5CX157KZGK']
url = 'https://d11v1jq8owgw3u.cloudfront.net/wp-content/uploads/2020/11/09024827/alrewaq-7_optimized.jpg'
with urllib.request.urlopen(url, timeout=30) as response:
    data = response.read()
assert len(data) > 1000 and data.startswith(b'\xff\xd8'), 'No valid JPEG received'
path = 'cache/rea-publisher-cover.jpg'
with tempfile.TemporaryDirectory(prefix='waraq-cover-') as temp:
    local = Path(temp) / 'cover.jpg'
    local.write_bytes(data)
    subprocess.run(base + ['push', str(local), '/data/local/tmp/warraq-probe.jpg'], check=True)
    subprocess.run(base + ['shell', 'run-as', 'de.mo.bibliothek', 'cp', '/data/local/tmp/warraq-probe.jpg', path], check=True)
subprocess.run(base + ['shell', 'rm', '/data/local/tmp/warraq-probe.jpg'], check=True)
received = subprocess.check_output(base + ['exec-out', 'run-as', 'de.mo.bibliothek', 'cat', path])
assert received == data, 'Image transfer changed bytes'
print('Verified JPEG:', len(data), hashlib.sha256(data).hexdigest())
subprocess.run(base + ['shell', 'am', 'start', '-n', 'de.mo.bibliothek/.MainActivity', '-a', 'android.intent.action.SEND', '-t', 'image/jpeg', '--eu', 'android.intent.extra.STREAM', 'file:///data/user/0/de.mo.bibliothek/' + path], check=True)
