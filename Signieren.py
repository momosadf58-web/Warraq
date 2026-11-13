"""Sign the current release with the existing external identity, without storing credentials."""
import hashlib
import json
import os
from pathlib import Path
import re
import subprocess
import sys

root = Path(__file__).resolve().parent.parent
release = Path(sys.argv[1]).resolve()
metadata = json.loads((release / 'output-metadata.json').read_text(encoding='utf-8'))
version = metadata['elements'][0]['versionName']
assert version == '3.4.7' and metadata['elements'][0]['versionCode'] == 20
unsigned = release / metadata['elements'][0]['outputFile']
external = Path(r'C:\Users\49157\Documents\Codex\2026-10-29\new-chat\work')
config = (external / 'build-bookie3.ps1').read_text(encoding='utf-8-sig')
passwords = re.findall(r'pass:([^\s"\x27]+)', config)
alias = re.search(r'--ks-key-alias\s+["\x27]?([\w-]+)', config)
assert len(passwords) == 2 and alias, 'Existing signing configuration not recognized'
environment = os.environ.copy()
environment['WARAQ_SIGN_STORE'] = passwords[0]
environment['WARAQ_SIGN_KEY'] = passwords[1]
environment['JAVA_HOME'] = r'C:\Users\49157\Documents\Mos Oase\Android\Werkzeuge\Java\jdk-17.0.20.1+1'
signer = r'C:\Users\49157\Documents\Mos Oase\Android\Werkzeuge\AndroidSDK\build-tools\35.0.1\apksigner.bat'
output = root / 'Ausgaben/Signierte-Zwischenstaende' / f'Warraq-{version}.apk'
assert not output.exists(), 'Do not overwrite a signed release'
subprocess.run([signer, 'sign', '--ks', str(external / 'bookie-debug.keystore'), '--ks-pass', 'env:WARAQ_SIGN_STORE', '--key-pass', 'env:WARAQ_SIGN_KEY', '--ks-key-alias', alias[1], '--out', str(output), str(unsigned)], env=environment, check=True, capture_output=True)
result = subprocess.check_output([signer, 'verify', '--verbose', '--print-certs', str(output)], env=environment).decode('utf-8', errors='replace')
assert '440335587995d04852b97be898d2462389e5fdc5f5eed89583c93f74f8296d42' in result.lower(), 'Unexpected signing identity'
evidence = root / 'Pruefnachweise/Codex-3.4.7-20261106'
(evidence / 'signature.txt').write_text(result, encoding='utf-8')
print('Verified release', version, hashlib.sha256(output.read_bytes()).hexdigest())
