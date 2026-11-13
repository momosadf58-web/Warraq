const fs=require('node:fs');const path=require('node:path');
async function json(url){const r=await fetch(url,{headers:{'User-Agent':'Warraq/3.1 (private personal library; Android)'},signal:AbortSignal.timeout(12000)});if(!r.ok)throw Error('HTTP '+r.status);return r.json();}
(async()=>{
 const search=await json('https://en.wikipedia.org/w/api.php?action=query&list=search&srlimit=3&format=json&srsearch=Fahrenheit%20451%20Ray%20Bradbury');
 const id=search.query.search[0].pageid;
 const p=Object.values((await json(`https://en.wikipedia.org/w/api.php?action=query&prop=extracts%7Cinfo%7Cpageprops&exintro=1&explaintext=1&inprop=url&format=json&pageids=${id}`)).query.pages)[0];
 const item=p.pageprops.wikibase_item;
 const sites=(await json(`https://www.wikidata.org/w/api.php?action=wbgetentities&ids=${item}&props=sitelinks&format=json`)).entities[item].sitelinks;
 const title=sites.enwikiquote?.title;
 if(!title)throw Error('Kein zugeordneter Wikiquote-Eintrag');
 const html=(await json('https://en.wikiquote.org/w/api.php?action=parse&prop=text&format=json&page='+encodeURIComponent(title))).parse.text['*'];
 fs.writeFileSync(path.join(__dirname,'evidence','wikiquote-live.html'),html);
 console.log(JSON.stringify({article:p.title,summaryLength:p.extract.length,quotePage:title,htmlLength:html.length,item}));
})().catch(e=>{console.error(e.message);process.exitCode=1;});
