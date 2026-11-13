const fs=require('node:fs'),path=require('node:path'),crypto=require('node:crypto');
const root=path.join(__dirname,'project');
async function get(url){const r=await fetch(url,{signal:AbortSignal.timeout(45000),headers:{'User-Agent':'Warraq/3.1 (private library)'}});if(!r.ok)throw Error(url+': '+r.status);return r;}
(async()=>{
 const manifest=[];
 for(const [language,repo] of [['ara','tessdata_best'],['deu','tessdata_fast'],['eng','tessdata_fast']]){
  const url=`https://raw.githubusercontent.com/tesseract-ocr/${repo}/4.1.0/${language}.traineddata`;
  const data=Buffer.from(await(await get(url)).arrayBuffer());
  const dir=path.join(root,'app/src/main/assets/tessdata');fs.mkdirSync(dir,{recursive:true});
  fs.writeFileSync(path.join(dir,language+'.traineddata'),data);
  manifest.push({language,url,bytes:data.length,sha256:crypto.createHash('sha256').update(data).digest('hex')});
 }
 fs.writeFileSync(path.join(root,'app/src/main/assets/tessdata/manifest.json'),JSON.stringify(manifest,null,2));
 const license=await(await get('https://raw.githubusercontent.com/tesseract-ocr/tessdata_fast/4.1.0/LICENSE')).text();
 fs.writeFileSync(path.join(root,'LIZENZEN','Tesseract-models-Apache-2.0.txt'),license);
 console.log(JSON.stringify(manifest.map(x=>({language:x.language,bytes:x.bytes}))));
})().catch(e=>{console.error(e.message);process.exitCode=1;});


