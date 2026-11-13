"""Gezielte ADB-Prüfung; niemals die Nutzer-App deinstallieren oder Testdaten importieren."""
import hashlib, io, json, os, pathlib, re, subprocess, sys, tarfile, xml.etree.ElementTree as ET

root=pathlib.Path(__file__).resolve().parent.parent
adb=pathlib.Path(r'C:\Users\49157\Documents\Mos Oase\Android\Werkzeuge\AndroidSDK\platform-tools\adb.exe')
serial=os.environ.get('WARAQ_ADB_SERIAL','R5CX157KZGK')
version=os.environ.get('WARAQ_CHECK_BUILD','3.3.1')
builds={
    '3.4.7':('20261106','c4ac5416b5fd4934de419cc5698553eda647e1865e07012de4fe2a5146d4ef8a'),
    '3.4.6':('20261105','107061f777426db2af475c3eadbbd70a522d0ed76f6d8baa932fad548453a749'),
    '3.4.5':('20261104','ce24890ded4a904609654a50df1b94895d294869265a8cbdb72649bc72ab1f4b'),
    '3.4.4':('20261104','f7aa82cdfe60c52741780fe625e879aa0922206fe489c3e9b7fe054366b9bd8b'),
    '3.4.3':('20261104','83677816aed08efec03545fc2de8a5f717035712a1b87b5dc7d68eb9c84fa50e'),
    '3.4.2':('20261104','466c66bc447dd752a32aecb18fdea6d0850b8186a3d9d05aad262586a797240e'),
    '3.4.1':('20261104','1a450ba994d49154287d1189667ddfbd24725ea008047a630c81efc50a2eaca9'),
    '3.4':('20261104','f9a6f3d1149b12102fd5bd80d3d259a21fcc5d7614959652a89bd3e7e940912b'),
    '3.3.5':('20261103','5268055437c3b5dc904dda3a3d93cb3601f4c38022517e9b62a80a26b0741d0e'),
    '3.3.4':('20261103','c686a016a2f93bb82332e1e600acb3561d2dbbd237cadf337dac4e754d6b82de'),
    '3.3.3':('20261031','8aab025c7953e2aee4416184040ba1deb02cf23b390c22b9062df7d8b834c46c'),
    '3.3.1':('20261030','f256a5414251bff9eabe410bf0c130910203eabcb429ed36d5cb464f4b61a33d'),
    '3.3.2':('20261031','214b6f6cf870c712959e5b07347404a2064ecc59bf8f558a3c14f228a9e05f39'),
}
date,expected=builds[version]
agent='Claude' if version in ('3.3.4','3.3.5','3.4','3.4.1','3.4.2','3.4.3','3.4.4','3.4.5') else 'Codex'
evidence=root/f'Pruefnachweise/{agent}-{version}-{date}'
backup=root/f'Sicherungen/Geraet-vor-{agent}-{version}-{date}'
evidence.mkdir(parents=True,exist_ok=True)

def run(*args,timeout=45):
    return subprocess.run([str(adb),'-s',serial,*args],capture_output=True,check=True,timeout=timeout).stdout

def package_hash():
    path=run('shell','pm','path','de.mo.bibliothek').decode().strip().removeprefix('package:')
    if not re.fullmatch(r'/data/app/[A-Za-z0-9._/=+~-]+/base\.apk',path):raise RuntimeError('Unerwarteter APK-Pfad')
    return run('shell','sha256sum',path).decode().split()[0].lower()

def cover_hashes(tar_bytes):
    with tarfile.open(fileobj=io.BytesIO(tar_bytes)) as archive:
        return {m.name:hashlib.sha256(archive.extractfile(m).read()).hexdigest() for m in archive if m.isfile() and re.fullmatch(r'files/covers/[a-f0-9-]{36}\.jpg',m.name)}

def dump():
    run('shell','uiautomator','dump','/sdcard/warraq-codex-ui.xml')
    xml=run('exec-out','cat','/sdcard/warraq-codex-ui.xml')
    return xml,ET.fromstring(xml)

action=sys.argv[1]
if action=='update':
    if backup.exists():raise RuntimeError('Sicherung existiert bereits, nicht überschrieben')
    apk=root/f'Ausgaben/Signierte-Zwischenstaende/Warraq-{version}.apk'
    if hashlib.sha256(apk.read_bytes()).hexdigest()!=expected:raise RuntimeError('APK-Hash stimmt nicht')
    policy=run('shell','dumpsys','window','policy').decode()
    if re.search(r'^\s+showing=true\s*$',policy,re.M):raise RuntimeError('Handy ist gesperrt')
    backup.mkdir(parents=True)
    before_apk=package_hash()
    run('shell','am','force-stop','de.mo.bibliothek')
    before=run('exec-out','run-as','de.mo.bibliothek','cat','files/library.json')
    tar=run('exec-out','run-as','de.mo.bibliothek','tar','-cf','-','files/library.json','files/covers')
    (backup/'library.json').write_bytes(before);(backup/'library-and-covers.tar').write_bytes(tar)
    covers=cover_hashes(tar);(backup/'covers-sha256.json').write_text(json.dumps(covers,indent=2),encoding='utf-8')
    result=run('install','--no-incremental','-r',str(apk),timeout=120).decode()
    if 'Success' not in result:raise RuntimeError(result)
    after=run('exec-out','run-as','de.mo.bibliothek','cat','files/library.json')
    after_tar=run('exec-out','run-as','de.mo.bibliothek','tar','-cf','-','files/covers')
    if before!=after or covers!=cover_hashes(after_tar):raise RuntimeError('Datenvergleich fehlgeschlagen; nichts überschreiben')
    after_apk=package_hash()
    if after_apk!=expected:raise RuntimeError('Installierter APK-Hash stimmt nicht')
    report={'installed_apk_sha256':after_apk,'previous_apk_sha256':before_apk,'books':len(json.loads(before)),'cover_files':len(covers),'library_sha256':hashlib.sha256(after).hexdigest(),'library_byte_equal':True,'covers_byte_equal':True}
    (evidence/'update-result.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
    print(json.dumps(report))
    print(run('shell','am','start','-n','de.mo.bibliothek/.MainActivity').decode())
elif action=='final':
    original=(backup/'library.json').read_bytes()
    covers=json.loads((backup/'covers-sha256.json').read_text(encoding='utf-8'))
    current=run('exec-out','run-as','de.mo.bibliothek','cat','files/library.json')
    archive=run('exec-out','run-as','de.mo.bibliothek','tar','-cf','-','files/library.json','files/covers')
    report={'books':len(json.loads(current)), 'library_byte_equal':original==current,
            'covers_byte_equal':covers==cover_hashes(archive), 'cover_files':len(cover_hashes(archive)),
            'installed_apk_sha256':package_hash()}
    (evidence/'final-data-check.json').write_text(json.dumps(report,indent=2),encoding='utf-8')
    print(json.dumps(report))
    assert report['library_byte_equal'] and report['covers_byte_equal'] and report['installed_apk_sha256']==expected
elif action=='shot':
    name=sys.argv[2]
    if not re.fullmatch(r'[a-zA-Z0-9_-]+',name):raise ValueError('Ungültiger Name')
    xml,_=dump();(evidence/(name+'.xml')).write_bytes(xml)
    path=evidence/(name+'.png');path.write_bytes(run('exec-out','screencap','-p'));print(path)
elif action=='texts':
    _,tree=dump()
    for n in tree.iter('node'):
        if n.get('package') in {'de.mo.bibliothek','android','com.google.android.permissioncontroller','com.android.permissioncontroller'}:
            value=n.get('text') or n.get('content-desc')
            if value:print(value)
elif action=='scroll':
    _,tree=dump()
    hits=[n for n in tree.iter('node') if n.get('package')=='de.mo.bibliothek' and n.get('scrollable')=='true']
    if not hits:raise RuntimeError('Kein sichtbarer Scrollbereich in Warraq')
    boxes=[list(map(int,re.findall(r'\d+',n.get('bounds')))) for n in hits]
    b=max(boxes,key=lambda b:(b[2]-b[0])*(b[3]-b[1]))
    x=(b[0]+b[2])//2; height=b[3]-b[1]
    run('shell','input','swipe',str(x),str(b[1]+height*3//4),str(x),str(b[1]+height//4),'350')
    print('Sichtbaren App-Bereich gescrollt')
elif action=='type':
    value=sys.argv[2]
    if not re.fullmatch(r'[A-Za-z0-9 ]{1,120}',value):raise ValueError('Nur feste lateinische Testeingaben')
    _,tree=dump()
    hits=[n for n in tree.iter('node') if n.get('package')=='de.mo.bibliothek' and n.get('class')=='android.widget.EditText']
    if len(hits)!=1:raise RuntimeError('Kein eindeutiges Eingabefeld')
    b=list(map(int,re.findall(r'\d+',hits[0].get('bounds'))))
    run('shell','input','tap',str((b[0]+b[2])//2),str((b[1]+b[3])//2))
    run('shell','input','text',value.replace(' ','%s'))
    print('Testtext eingegeben')
elif action=='tap':
    label=sys.argv[2];_,tree=dump()
    hits=[n for n in tree.iter('node') if n.get('text')==label or n.get('content-desc')==label]
    if not hits:raise RuntimeError('Nicht sichtbar: '+label)
    n=hits[int(sys.argv[3]) if len(sys.argv)>3 else 0]
    if n.get('package') not in {'de.mo.bibliothek','android','com.google.android.permissioncontroller','com.android.permissioncontroller'}:raise RuntimeError('Nicht in Warraq/Systemdialog')
    b=list(map(int,re.findall(r'\d+',n.get('bounds'))))
    run('shell','input','tap',str((b[0]+b[2])//2),str((b[1]+b[3])//2));print('Getippt: '+label)
else:raise ValueError('Unbekannte Aktion')
