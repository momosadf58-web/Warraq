const fs=require('node:fs'),path=require('node:path'),crypto=require('node:crypto'),{execFileSync}=require('node:child_process');
const root=path.join(__dirname,'project/app/src/main/assets');
(async()=>{
 const source=fs.readFileSync(path.join(__dirname,'evidence/metadata.tsv'),'utf8').trim().split(/\r?\n/),keys=source.shift().split('\t');
 const rows=source.map(line=>Object.fromEntries(line.split('\t').map((v,i)=>[keys[i],v]))).map(r=>({id:r.booknr,title:r.title,author:r.author,year:r.origpubdate||r.pubdate.slice(0,4),genre:r.category.main||r.category,originalTitle:r.origtitle,originalAuthor:r.origauthor}));
 fs.mkdirSync(root,{recursive:true});fs.writeFileSync(path.join(root,'hindawi-catalog.json'),JSON.stringify(rows));
 console.log('Arabische Katalogeinträge: '+rows.length+'; ماجدولين: '+JSON.stringify(rows.find(r=>r.id==='74280904')));
 const dataset='alielfilali01/Hindawi-Books-dataset';
 const api='https://datasets-server.huggingface.co/filter?dataset='+encodeURIComponent(dataset)+'&config=default&split=train&length=1&where='+encodeURIComponent('"BookName"=\'ماجدولين\'');
 try{const res=await fetch(api,{signal:AbortSignal.timeout(20000)});const json=await res.json();const row=json.rows?.[0]?.row;console.log('HF filter',res.status,row?JSON.stringify({title:row.BookName,author:row.AuthorName,url:row.BookLink,descriptionLength:row.AboutBook?.length}):JSON.stringify(json).slice(0,200));}catch(e){console.log('HF:',e.message);}
 const meta=await(await fetch('https://registry.npmjs.org/three/0.170.0')).json();
 const buffer=Buffer.from(await(await fetch(meta.dist.tarball)).arrayBuffer());
 const digest='sha512-'+crypto.createHash('sha512').update(buffer).digest('base64');if(digest!==meta.dist.integrity)throw Error('npm-Integrität stimmt nicht');
 const file=path.join(__dirname,'three.tgz');fs.writeFileSync(file,buffer);const dir=path.join(root,'room');fs.mkdirSync(dir,{recursive:true});
 for(const [member,name]of [['package/build/three.module.min.js','three.module.min.js'],['package/LICENSE','THREE-LICENSE.txt']])fs.writeFileSync(path.join(dir,name),execFileSync('tar.exe',['-xOf',file,member],{maxBuffer:8*1024*1024}));
 console.log('Three.js r170 lokal mit geprüfter npm-Signatur/Integrität bereitgestellt.');
})().catch(e=>{console.error(e);process.exitCode=1;});
