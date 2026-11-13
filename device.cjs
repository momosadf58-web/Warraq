const fs = require('node:fs');
const path = require('node:path');
const {execFileSync} = require('node:child_process');
const adb = 'C:\\Users\\49157\\Documents\\Mos Oase\\Android\\Werkzeuge\\AndroidSDK\\platform-tools\\adb.exe';
const out = path.join(__dirname, 'evidence');
fs.mkdirSync(out, {recursive:true});
function run(...args) { return execFileSync(adb, ['-s','R5CX157KZGK',...args], {maxBuffer:64*1024*1024, timeout:45000}); }
const action = process.argv[2];
if (action === 'baseline') {
  run('shell','am','force-stop','de.mo.bibliothek');
  fs.writeFileSync(path.join(out,'before-files.tar'),run('exec-out','run-as','de.mo.bibliothek','tar','-cf','-','files','shared_prefs'));
  const json=run('exec-out','run-as','de.mo.bibliothek','cat','files/library.json');
  fs.writeFileSync(path.join(out,'before-library.json'),json);
  const books=JSON.parse(json.toString());
  console.log(JSON.stringify({books:books.length,covers:books.filter(b=>b.cover).length,dated:books.filter(b=>b.readAt>0).length}));
  run('shell','am','start','-n','de.mo.bibliothek/.MainActivity');
} else if (action === 'asset') {
  fs.writeFileSync(path.join(out,'live-quote-card.png'),run('exec-out','run-as','de.mo.bibliothek','cat','cache/live-quote-card.png'));
  fs.writeFileSync(path.join(out,'live-result.txt'),run('exec-out','run-as','de.mo.bibliothek','cat','cache/live-result.txt'));
  console.log('Online-Bildkarte und Ergebnis übernommen.');
} else if (action === 'verify') {
  const before=fs.readFileSync(path.join(out,'before-library.json'));
  const after=run('exec-out','run-as','de.mo.bibliothek','cat','files/library.json');
  fs.writeFileSync(path.join(out,'after-library.json'),after);
  if(!before.equals(after))throw Error('Bibliotheksdatei verändert: prüfen, nicht automatisch überschreiben.');
  console.log('library.json bytegleich: '+JSON.parse(after).length+' Bücher erhalten.');
  console.log(run('exec-out','run-as','de.mo.bibliothek','sh','-c','find files/covers -type f | wc -l').toString().trim()+' lokale Coverdateien.');
} else if (action === 'shot') {
  const name=(process.argv[3]||'screen').replace(/[^a-z0-9_-]/gi,'');
  run('shell','uiautomator','dump','/sdcard/warraq-test-ui.xml');
  fs.writeFileSync(path.join(out,name+'.xml'),run('exec-out','cat','/sdcard/warraq-test-ui.xml'));
  fs.writeFileSync(path.join(out,name+'.png'),run('exec-out','screencap','-p'));
  console.log(path.join(out,name+'.png'));
} else if (action === 'tap') {
  run('shell','uiautomator','dump','/sdcard/warraq-test-ui.xml');
  const xml=run('exec-out','cat','/sdcard/warraq-test-ui.xml').toString();
  const label=process.argv[3];
  const nodes=[...xml.matchAll(/<node\b[^>]+>/g)].map(m=>Object.fromEntries([...m[0].matchAll(/([\w-]+)="([^"]*)"/g)].map(a=>[a[1],a[2]])));
  const hit=nodes.find(n=>n.package==='de.mo.bibliothek' && (n.text===label||n['content-desc']===label));
  if(!hit)throw Error('Nicht sichtbar: '+label+'; Texte: '+nodes.filter(n=>n.package==='de.mo.bibliothek').map(n=>n.text||n['content-desc']).filter(Boolean).join(' | '));
  const b=hit.bounds.match(/\d+/g).map(Number);
  run('shell','input','tap',String(Math.round((b[0]+b[2])/2)),String(Math.round((b[1]+b[3])/2)));
  console.log('Getippt: '+label);
} else throw new Error('Unbekannte Aktion');
