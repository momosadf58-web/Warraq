const fs=require('node:fs'),path=require('node:path');
(async()=>{
 const urls=[['metadata.tsv','https://open.doris.gu.snd.se/downloads/public/2024-145/1/documentation/metadata.tsv'],['corpus-README.md','https://open.doris.gu.snd.se/downloads/public/2024-145/1/documentation/README.md'],['hindawi-cover.svg','https://downloads.hindawi.org/covers/svg/270x360/74280904.svg?v=5']];
 for(const[name,url]of urls){try{const r=await fetch(url,{signal:AbortSignal.timeout(30000)});console.log(name,r.status);if(!r.ok)continue;const data=Buffer.from(await r.arrayBuffer());fs.writeFileSync(path.join(__dirname,'evidence',name),data);console.log(data.toString('utf8').slice(0,name.endsWith('.tsv')?1500:700));}catch(e){console.log(name,e.message);}}
})().catch(e=>{console.error(e);process.exitCode=1});
