import * as THREE from './three.module.min.js';

// An old Damascene book souk: one narrow aisle, worn plaster and ablaq stone, sagging walnut shelves,
// faded kilim, pierced brass lanterns. Everything is generated here; nothing is loaded from the network.
const renderer=new THREE.WebGLRenderer({antialias:true,powerPreference:'high-performance'});
renderer.setPixelRatio(Math.min(devicePixelRatio,1.65));renderer.setSize(innerWidth,innerHeight);
renderer.outputColorSpace=THREE.SRGBColorSpace;renderer.toneMapping=THREE.ACESFilmicToneMapping;renderer.toneMappingExposure=.95;
document.body.prepend(renderer.domElement);
const scene=new THREE.Scene();scene.background=new THREE.Color('#1d1611');scene.fog=new THREE.FogExp2('#241a13',.075);
// Portrait phones need a wider vertical angle, otherwise the side shelves stay out of view.
const fov=()=>innerWidth<innerHeight?84:64;
const camera=new THREE.PerspectiveCamera(fov(),innerWidth/innerHeight,.05,60);camera.rotation.order='YXZ';
const world=new THREE.Group();scene.add(world);const realGroup=new THREE.Group();scene.add(realGroup);
scene.add(new THREE.HemisphereLight(0xffdcae,0x1c140e,.8));
let seed=8372;function rnd(){seed=(1664525*seed+1013904223)>>>0;return seed/4294967296;}
const jitter=a=>(rnd()-.5)*2*a;
function texture(draw,w=256,h=256){const c=document.createElement('canvas');c.width=w;c.height=h;draw(c.getContext('2d'),w,h);const t=new THREE.CanvasTexture(c);t.colorSpace=THREE.SRGBColorSpace;t.anisotropy=4;return t;}
function stains(c,w,h,count,dark=.16){for(let i=0;i<count;i++){const x=rnd()*w,y=rnd()*h,r=4+rnd()*w*.12,g=c.createRadialGradient(x,y,0,x,y,r);g.addColorStop(0,'rgba(40,26,14,'+(rnd()*dark)+')');g.addColorStop(1,'rgba(40,26,14,0)');c.fillStyle=g;c.fillRect(x-r,y-r,r*2,r*2);}}
function cracks(c,w,h,count){c.strokeStyle='rgba(30,20,12,.35)';c.lineWidth=1;for(let i=0;i<count;i++){let x=rnd()*w,y=rnd()*h;c.beginPath();c.moveTo(x,y);for(let s=0;s<6;s++){x+=jitter(18);y+=4+rnd()*14;c.lineTo(x,y);}c.stroke();}}

// --- Materials -------------------------------------------------------------------------------------
const walnut=texture((c,w,h)=>{c.fillStyle='#3a2416';c.fillRect(0,0,w,h);for(let i=0;i<700;i++){const y=rnd()*h;c.strokeStyle='rgba('+(rnd()>.5?'150,100,58,':'10,6,3,')+(.03+rnd()*.13)+')';c.lineWidth=.5+rnd()*1.5;c.beginPath();c.moveTo(0,y);c.bezierCurveTo(w*.3,y+jitter(6),w*.7,y+jitter(6),w,y+jitter(3));c.stroke();}stains(c,w,h,20,.25);});
const plaster=texture((c,w,h)=>{c.fillStyle='#b9a483';c.fillRect(0,0,w,h);for(let i=0;i<9000;i++){const v=150+Math.floor(rnd()*60);c.fillStyle='rgba('+v+','+(v-14)+','+(v-36)+',.10)';c.fillRect(rnd()*w,rnd()*h,1+rnd()*2,1+rnd()*2);}stains(c,w,h,40,.35);cracks(c,w,h,7);
 // patches where the lime has fallen off and stone shows
 for(let i=0;i<5;i++){const x=rnd()*w,y=rnd()*h,s=10+rnd()*28;c.fillStyle='rgba(120,98,70,.55)';c.beginPath();c.ellipse(x,y,s,s*.6,rnd()*3,0,7);c.fill();}},512,512);
plaster.wrapS=plaster.wrapT=THREE.RepeatWrapping;plaster.repeat.set(3,1.5);
// Ablaq: alternating black basalt and pale limestone courses, typical of old Damascus.
const ablaq=texture((c,w,h)=>{let y=0,dark=false;while(y<h){const band=18+rnd()*14;c.fillStyle=dark?'#2d2a27':'#c2b08e';c.fillRect(0,y,w,band);for(let x=0;x<w;x+=40+rnd()*40){c.fillStyle='rgba(0,0,0,.22)';c.fillRect(x,y,2,band);}y+=band;c.fillStyle='rgba(60,45,30,.6)';c.fillRect(0,y,w,2);y+=2;dark=!dark;}stains(c,w,h,30,.3);cracks(c,w,h,4);},256,512);
ablaq.wrapS=ablaq.wrapT=THREE.RepeatWrapping;
const floorTex=texture((c,w,h)=>{c.fillStyle='#5b4c3b';c.fillRect(0,0,w,h);let y=0;while(y<h){const rowH=36+rnd()*40;let x=-rnd()*40;while(x<w){const tileW=40+rnd()*70,v=78+Math.floor(rnd()*40);c.fillStyle='rgb('+v+','+(v-10)+','+(v-24)+')';c.fillRect(x+2,y+2,tileW-4,rowH-4);if(rnd()<.25){c.fillStyle='rgba(30,22,14,.45)';c.beginPath();c.moveTo(x+rnd()*tileW,y+2);c.lineTo(x+tileW-2,y+rnd()*rowH);c.lineTo(x+tileW-2,y+2);c.fill();}x+=tileW;}y+=rowH;}stains(c,w,h,60,.4);for(let i=0;i<4000;i++){c.fillStyle='rgba(255,240,210,.04)';c.fillRect(rnd()*w,rnd()*h,1,1);}},512,512);
floorTex.wrapS=floorTex.wrapT=THREE.RepeatWrapping;floorTex.repeat.set(1.5,5);
// Faded kilim runner: madder, indigo and ochre lozenges, worn pale along the walking line, frayed ends.
const kilim=texture((c,w,h)=>{c.fillStyle='#6e2c22';c.fillRect(0,0,w,h);const cols=['#2b3450','#a9823f','#d8c8a1','#3d4a35'];
 c.fillStyle='#d8c8a1';c.fillRect(10,0,6,h);c.fillRect(w-16,0,6,h);
 for(let y=20,k=0;y<h-20;y+=46,k++){const col=cols[k%cols.length];c.fillStyle=col;c.beginPath();c.moveTo(w/2,y);c.lineTo(w/2+38,y+23);c.lineTo(w/2,y+46);c.lineTo(w/2-38,y+23);c.closePath();c.fill();c.fillStyle='#6e2c22';c.beginPath();c.moveTo(w/2,y+10);c.lineTo(w/2+20,y+23);c.lineTo(w/2,y+36);c.lineTo(w/2-20,y+23);c.closePath();c.fill();
  for(const side of [-1,1]){c.fillStyle=cols[(k+1)%cols.length];for(let s=0;s<3;s++)c.fillRect(w/2+side*(60+s*9)-3,y+10+s*9,6,6);}}
 const g=c.createLinearGradient(0,0,w,0);g.addColorStop(0,'rgba(0,0,0,0)');g.addColorStop(.5,'rgba(230,210,170,.22)');g.addColorStop(1,'rgba(0,0,0,0)');c.fillStyle=g;c.fillRect(0,0,w,h);
 stains(c,w,h,25,.3);c.strokeStyle='rgba(216,200,161,.8)';for(let x=4;x<w;x+=5){c.beginPath();c.moveTo(x,0);c.lineTo(x+jitter(2),8+rnd()*8);c.stroke();c.beginPath();c.moveTo(x,h);c.lineTo(x+jitter(2),h-8-rnd()*8);c.stroke();}},256,1024);
const spineTex=texture((c,w,h)=>{c.fillStyle='#ffffff';c.fillRect(0,0,w,h);c.fillStyle='rgba(255,230,160,.9)';for(const y of [14,22,h-30,h-22])c.fillRect(0,y,w,3);c.fillStyle='rgba(255,235,180,.55)';c.fillRect(w*.25,h*.3,w*.5,h*.22);for(let i=0;i<400;i++){c.fillStyle='rgba(0,0,0,'+rnd()*.12+')';c.fillRect(rnd()*w,rnd()*h,2,2);}},64,256);
const lanternTex=texture((c,w,h)=>{c.fillStyle='#120b05';c.fillRect(0,0,w,h);c.fillStyle='#ffcf7a';
 for(let y=18;y<h;y+=34)for(let x=(y/34%2)*16+8;x<w;x+=32){c.save();c.translate(x,y);for(let r=0;r<2;r++){c.rotate(Math.PI/4);c.fillRect(-5,-5,10,10);}c.restore();}},128,128);
// Mashrabiya: eight-pointed stars (two overlaid squares) joined by diagonal laths.
const lattice=texture((c,w,h)=>{c.clearRect(0,0,w,h);c.strokeStyle='#3b2615';c.lineCap='round';const s=48;
 c.lineWidth=4;for(let k=-h;k<w+h;k+=s){c.beginPath();c.moveTo(k,0);c.lineTo(k+h,h);c.stroke();c.beginPath();c.moveTo(k,h);c.lineTo(k+h,0);c.stroke();}
 for(let y=0;y<=h;y+=s)for(let x=0;x<=w;x+=s){c.save();c.translate(x,y);c.fillStyle='#3b2615';for(let r=0;r<2;r++){c.rotate(Math.PI/4);c.fillRect(-12,-12,24,24);}c.clearRect(-5,-5,10,10);c.restore();}
 c.lineWidth=10;c.strokeRect(0,0,w,h);},384,576);

const woodMat=new THREE.MeshStandardMaterial({map:walnut,roughness:.85});
const plasterMat=new THREE.MeshStandardMaterial({map:plaster,roughness:1});
const ablaqMat=new THREE.MeshStandardMaterial({map:ablaq,roughness:.95});
const brass=new THREE.MeshStandardMaterial({color:0xa07a38,metalness:.85,roughness:.38});
const clay=new THREE.MeshStandardMaterial({color:0x8a4b2c,roughness:.95});
const boxGeo=new THREE.BoxGeometry(1,1,1);
function box(w,h,d,x,y,z,mat=woodMat,group=world){const m=new THREE.Mesh(boxGeo,mat);m.scale.set(w,h,d);m.position.set(x,y,z);group.add(m);return m;}

// --- Room shell: 4.5 m wide, one aisle, back alcove ------------------------------------------------
const floorMat=new THREE.MeshStandardMaterial({map:floorTex,roughness:.9});
box(4.5,.12,17.4,0,-.06,-1,floorMat);
for(const side of [-1,1])box(.2,4.2,17.4,side*2.3,2.1,-1,plasterMat);
const ceiling=new THREE.MeshStandardMaterial({color:0x2a1d14,roughness:1});box(4.6,.1,17.4,0,4.05,-1,ceiling);
for(let z=7.4;z>-9.6;z-=.9+rnd()*.5){const beam=box(4.6,.15+rnd()*.08,.13+rnd()*.06,0,3.92,z);beam.rotation.z=jitter(.015);beam.rotation.y=jitter(.02);}
// Back wall: ablaq lower courses, plaster above, a stone arch over a wooden mashrabiya screen.
box(4.6,2.5,.2,0,1.25,-9.7,ablaqMat);
box(4.6,1.7,.2,0,3.35,-9.7,plasterMat);
for(const side of [-1,1])box(.38,2.6,.3,side*.98,1.3,-9.5,ablaqMat);
const arch=new THREE.Mesh(new THREE.TorusGeometry(.98,.17,10,28,Math.PI),ablaqMat);arch.position.set(0,2.6,-9.5);world.add(arch);
const screen=new THREE.Mesh(new THREE.PlaneGeometry(1.6,2.55),new THREE.MeshStandardMaterial({map:lattice,transparent:true,alphaTest:.3,side:THREE.DoubleSide,roughness:.9}));screen.position.set(0,1.6,-9.48);world.add(screen);
const glow=new THREE.Mesh(new THREE.PlaneGeometry(1.6,2.6),new THREE.MeshBasicMaterial({color:0xffb860,fog:false}));glow.position.set(0,1.6,-9.58);world.add(glow);
const courtyard=new THREE.PointLight(0xffb05a,3,5,2);courtyard.position.set(0,1.8,-9.2);scene.add(courtyard);
// Shop board hung across the aisle on two chains: gold leaf on dark walnut, slightly crooked.
const sign=new THREE.Mesh(new THREE.PlaneGeometry(1.5,.38),new THREE.MeshStandardMaterial({color:0xffffff,roughness:.8,emissive:0xffffff,emissiveIntensity:.25}));sign.position.set(0,3.05,-6.9);sign.rotation.z=-.03;world.add(sign);
for(const x of [-.6,.6]){const chain=new THREE.Mesh(new THREE.CylinderGeometry(.006,.006,.8,4),brass);chain.position.set(x,3.6,-6.9);world.add(chain);}
document.fonts.load('80px Warraq').then(()=>{sign.material.map=sign.material.emissiveMap=texture((c,w,h)=>{c.drawImage(walnut.image,0,0,w,h);c.fillStyle='rgba(0,0,0,.35)';c.fillRect(0,0,w,h);c.strokeStyle='#8c6a30';c.lineWidth=6;c.strokeRect(8,8,w-16,h-16);c.fillStyle='#d6ab55';c.font='88px Warraq,serif';c.textAlign='center';c.textBaseline='middle';c.direction='rtl';c.fillText('الورّاق',w/2,h/2+6);stains(c,w,h,12,.4);},512,128);sign.material.needsUpdate=true;}).catch(()=>{});

// --- Shelves: walnut cases on both sides, uneven boards, gaps, leaning and lying books ----------------
const spines=new THREE.InstancedMesh(boxGeo,new THREE.MeshStandardMaterial({map:spineTex,roughness:.9}),2600);world.add(spines);
let used=0;const dummy=new THREE.Object3D();
const leather=['#3c2a20','#5a2a22','#6b4a2e','#2e3a33','#4a3a2a','#72583a','#2f2b3a','#7a6a4c','#523026','#3a3026'];
function spine(x,y,z,depth,h,t,lean=0,lie=false){if(used>=2600)return;dummy.position.set(x,y+(lie?t/2:h/2),z);dummy.scale.set(depth,lie?t:h,lie?h:t);dummy.rotation.set(lean,0,0);dummy.updateMatrix();spines.setMatrixAt(used,dummy.matrix);
 const col=new THREE.Color(leather[Math.floor(rnd()*leather.length)]).multiplyScalar(.75+rnd()*.5);spines.setColorAt(used,col);used++;}
const BAYS=5;
for(const side of [-1,1])for(let bay=0;bay<BAYS;bay++){
 const z=5.1-bay*2.6,x=side*1.95,height=3.5+jitter(.12);
 box(.52,height,.07,x,height/2,z-1.22);box(.52,height,.07,x,height/2,z+1.22);box(.04,height,2.5,side*2.2,height/2,z);
 box(.56,.08,2.56,x,height+.04,z).rotation.x=jitter(.01);
 // a rolled manuscript or two on top
 if(rnd()<.6){const roll=new THREE.Mesh(new THREE.CylinderGeometry(.045,.045,.5,10),new THREE.MeshStandardMaterial({color:0xcdb88d,roughness:1}));roll.rotation.x=Math.PI/2;roll.rotation.z=jitter(.3);roll.position.set(x+jitter(.1),height+.12,z+jitter(.8));world.add(roll);}
 for(let level=0;level<5;level++){
  const y=.28+level*.72+jitter(.02),board=box(.52,.06,2.42,x,y,z);board.rotation.x=jitter(.008);
  let at=z-1.17;
  while(at<z+1.15){
   if(rnd()<.06){at+=.12+rnd()*.25;continue;}                        // gap
   if(rnd()<.07){const n=2+Math.floor(rnd()*5),d=.3+rnd()*.08;for(let k=0;k<n;k++)spine(x+jitter(.02),y+.03+k*.045,at+.17+jitter(.02),d,.32+rnd()*.06,.04,0,true);at+=.42;continue;} // lying stack
   const t=.03+rnd()*.07,h=.26+rnd()*.34,lean=rnd()<.12?jitter(.25):jitter(.03);
   spine(x+jitter(.03),y+.03,at+t/2,.24+rnd()*.1,h,t,lean);at+=t+.004;
  }
 }
}
spines.count=used;spines.instanceMatrix.needsUpdate=true;if(spines.instanceColor)spines.instanceColor.needsUpdate=true;

// --- Floor life: kilim, piles, low table with tea, clay jars, cushions -----------------------------
const runner=box(1.25,.012,12.6,.05,.006,-.4,new THREE.MeshStandardMaterial({map:kilim,roughness:1}));runner.rotation.y=.012;
const small=box(1.5,.012,1,-.1,.008,-8.3,new THREE.MeshStandardMaterial({map:kilim,roughness:1}));small.rotation.y=Math.PI/2+.08;
function pile(x,z,n,y0=0){let y=y0;for(let k=0;k<n;k++){const w=.36+rnd()*.12,d=.26+rnd()*.08,t=.035+rnd()*.03,m=box(w,t,d,x+jitter(.03),y+t/2,z+jitter(.03),new THREE.MeshStandardMaterial({color:new THREE.Color(leather[k%leather.length]).multiplyScalar(.8+rnd()*.4),roughness:1}));m.rotation.y=jitter(.35);y+=t;}}
for(const [x,z,n]of [[-1.35,6.2,9],[1.4,3.9,5],[-1.38,1.25,12],[1.36,-1.4,7],[-1.36,-4.0,6],[1.38,-6.3,10]])pile(x,z,n);
box(1.15,.06,.68,0,.46,-8.4);for(const x of [-.5,.5])for(const z of [-8.68,-8.12])box(.06,.44,.06,x,.22,z);
const tray=new THREE.Mesh(new THREE.CylinderGeometry(.24,.24,.02,32),brass);tray.position.set(.3,.5,-8.38);world.add(tray);
const tea=new THREE.Mesh(new THREE.CylinderGeometry(.03,.025,.08,16),new THREE.MeshStandardMaterial({color:0x8a3a12,transparent:true,opacity:.75,roughness:.1}));tea.position.set(.3,.55,-8.35);world.add(tea);
const jarShape=[[0,0],[.12,.02],[.2,.15],[.22,.32],[.16,.48],[.09,.55],[.11,.6]].map(([r,y])=>new THREE.Vector2(r,y));
for(const [x,z,s]of [[-1.55,-6.78,1],[1.6,-6.85,.8],[1.45,6.6,1.15]]){const jar=new THREE.Mesh(new THREE.LatheGeometry(jarShape,18),clay);jar.scale.setScalar(s);jar.position.set(x,0,z);jar.rotation.z=jitter(.05);world.add(jar);}
for(const x of [-1.5,1.5]){const cushion=box(.75,.16,.55,x,.08,-9.15,new THREE.MeshStandardMaterial({map:kilim,roughness:1}));cushion.rotation.y=jitter(.1);}

// --- Pierced brass lanterns: the only real light ---------------------------------------------------
const lanterns=[];
const lanternGlass=new THREE.MeshStandardMaterial({map:lanternTex,emissive:0xffa64a,emissiveMap:lanternTex,emissiveIntensity:1.6,metalness:.6,roughness:.5});
for(const z of [3.4,.2,-3.0,-6.1]){const g=new THREE.Group();g.position.set(jitter(.1),3.05+jitter(.08),z);world.add(g);
 const chain=new THREE.Mesh(new THREE.CylinderGeometry(.006,.006,.9,4),brass);chain.position.y=.6;g.add(chain);
 g.add(new THREE.Mesh(new THREE.CylinderGeometry(.12,.15,.34,8,1,true),lanternGlass));
 const cap=new THREE.Mesh(new THREE.ConeGeometry(.15,.18,8),brass);cap.position.y=.26;g.add(cap);
 const foot=new THREE.Mesh(new THREE.ConeGeometry(.15,.1,8),brass);foot.rotation.x=Math.PI;foot.position.y=-.22;g.add(foot);
 const light=new THREE.PointLight(0xffa04a,7,6.5,1.8);light.position.y=-.05;g.add(light);lanterns.push({light,base:7,phase:rnd()*10});}

// --- Dust in the lamp light -----------------------------------------------------------------------
const dustCount=350,dustPos=new Float32Array(dustCount*3);for(let i=0;i<dustCount;i++){dustPos[i*3]=jitter(1.4);dustPos[i*3+1]=.3+rnd()*3.2;dustPos[i*3+2]=-9+rnd()*16;}
const dustGeo=new THREE.BufferGeometry();dustGeo.setAttribute('position',new THREE.BufferAttribute(dustPos,3));
const dust=new THREE.Points(dustGeo,new THREE.PointsMaterial({color:0xffd9a0,size:.012,transparent:true,opacity:.55,depthWrite:false,blending:THREE.AdditiveBlending}));scene.add(dust);

// --- Own books -------------------------------------------------------------------------------------
// ?z=…&yaw=… only for checking the room in a desktop browser; the app loads the page without parameters.
const debug=new URLSearchParams(location.search);
let yaw=0,pitch=0;function home(){camera.position.set(0,1.57,6.45);yaw=0;pitch=0;}home();
if(debug.has('z')){camera.position.z=+debug.get('z');yaw=+(debug.get('yaw')||0);}
let entries=[],targets=[],config=null,move={x:0,y:0},look=null,touchStart=null;
const ray=new THREE.Raycaster(),point=new THREE.Vector2(),clock=new THREE.Clock(),loader=new THREE.TextureLoader();
function labelTexture(book){return texture((c,w,h)=>{c.fillStyle='#4b3a2c';c.fillRect(0,0,w,h);c.strokeStyle='#c8a560';c.lineWidth=3;c.strokeRect(14,14,w-28,h-28);c.fillStyle='#efe1c4';c.font='25px Warraq,serif';c.textAlign='center';c.direction=/[؀-ۿ]/.test(book.title)?'rtl':'ltr';let line='',y=85;for(const word of book.title.split(' ')){if(c.measureText(line+' '+word).width>w-38){c.fillText(line,w/2,y);y+=38;line=word;}else line+=(line?' ':'')+word;}c.fillText(line,w/2,y);c.font='18px serif';c.fillText(book.author.slice(0,35),w/2,h-52);},256,384);}
// Every own book gets its own face-out slot: 2 sides x 5 bays x 5 levels x n per shelf.
// Mixed order, but at eye level in the bays visible from the entrance first, so a small collection is found right away.
let slots=[];
function shelfSlots(count){const per=Math.max(4,Math.ceil(count/50)),list=[];let s=4242;const r=()=>{s=(1664525*s+1013904223)>>>0;return s/4294967296;};
 for(const side of [1,-1])for(let bay=0;bay<BAYS;bay++)for(let level=0;level<5;level++)for(let i=0;i<per;i++)list.push({side,y:.66+level*.72,z:5.1-bay*2.6-1.05+(i+.5)*2.1/per,scale:Math.min(1,2.1/per/.48),order:[2,0,1,3,4][bay]*10+(level===0||level===4?7:Math.abs(level-2)*2)+r()*9});
 return list.sort((a,b)=>a.order-b.order);}
// Year stack: one pile on the low table beside the tea tray, then four floor piles around it; beyond that they grow in turn.
const piles=[[-.27,-8.42,.49,10],[-1.05,-8.3,0,14],[1.05,-8.35,0,14],[-1.3,-7.65,0,14],[1.3,-7.7,0,14]];
function pileSpot(index){let rest=index;for(const [x,z,y,cap]of piles){if(rest<cap)return [x,z,y,rest];rest-=cap;}const p=piles[rest%piles.length];return [p[0],p[1],p[2],p[3]+Math.floor(rest/piles.length)];}
const plate=new THREE.MeshStandardMaterial({color:0xc29a4a,metalness:.9,roughness:.3,emissive:0x3a2608});
function addBook(book,index,stack=false){
 const material=new THREE.MeshStandardMaterial({color:0x6a4630,roughness:.8});const coverMat=new THREE.MeshStandardMaterial({map:labelTexture(book),roughness:.8,emissive:0x2a1c0a,emissiveIntensity:.18});
 const group=new THREE.Group();realGroup.add(group);const volume=box(.43,.62,.07,0,0,0,material,group);const front=new THREE.Mesh(new THREE.PlaneGeometry(.415,.60),coverMat);front.position.z=.037;group.add(front);
 if(stack){const [x,z,y,level]=pileSpot(index);group.rotation.x=-Math.PI/2;group.rotation.z=(rnd()-.5)*.35;group.position.set(x,y+.04+level*.075,z);}
 else {const slot=slots[index];group.scale.setScalar(slot.scale);group.position.set(slot.side*1.66,slot.y-(1-slot.scale)*.31,slot.z);group.rotation.y=slot.side>0?-Math.PI/2:Math.PI/2;group.rotation.z=jitter(.03);
  // small brass plate below: marks an own book against the stock around it
  box(.16,.035,.012,0,-.335,.04,plate,group);}
 group.userData.book=book;front.userData.book=book;volume.userData.book=book;targets.push(front,volume);entries.push({group,front,coverMat,book,loaded:false,stack});
}
function options(id,values,all){const select=document.getElementById(id),old=select.value;select.replaceChildren();for(const [value,title]of [['',all],...values]){const o=document.createElement('option');o.value=value;o.textContent=title;select.append(o);}if([...select.options].some(o=>o.value===old))select.value=old;select.onchange=filter;}
function filter(){const genre=document.getElementById('genre').value,year=document.getElementById('year').value,kind=document.getElementById('kind').value;for(const e of entries){const match=(!genre||e.book.genre===genre)&&(!year||String(e.book.year)===year)&&(!kind||e.book.kind===kind);e.coverMat.color.set(match?0xffffff:0x37312a);e.coverMat.emissive.set(match&&(genre||year||kind)?0x80672c:0x2a1c0a);e.coverMat.emissiveIntensity=match?.4:0;}}
let generation=0;
window.updateRoom=async function(data){const current=++generation;await document.fonts.ready;if(current!==generation)return;config=data;document.documentElement.dir=data.arabic?'rtl':'ltr';document.getElementById('hint').textContent=data.arabic?'يساراً للمشي · اسحب يميناً للنظر · المس كتابك لفتحه':'Links gehen · Rechts umsehen · Dein Buch antippen';document.getElementById('progress').textContent=(data.arabic?'هدف السنة ':'Jahresziel ')+data.year+': '+data.yearBooks.length+' / '+data.goal;
 document.getElementById('back').onclick=()=>window.Warraq.closeRoom();document.getElementById('home').onclick=home;document.getElementById('list').onclick=()=>{const box=document.getElementById('books');box.hidden=!box.hidden;};
 for(const entry of entries){entry.coverMat.map?.dispose();entry.coverMat.dispose();}realGroup.clear();entries=[];targets=[];
 slots=shelfSlots(data.books.length);data.books.forEach((b,i)=>addBook(b,i));data.yearBooks.forEach((b,i)=>addBook(b,i,true));
 const unique=key=>[...new Set(data.books.map(b=>String(b[key])))].sort().map(v=>[v,v||(data.arabic?'غير محدد':'Ohne Angabe')]);
 options('genre',unique('genre'),data.arabic?'كل الأنواع':'Alle Genres');options('year',unique('year').map(([v])=>[v,v==='0'?(data.arabic?'بلا تاريخ':'Ohne Datum'):v]),data.arabic?'كل السنوات':'Alle Lesejahre');
 options('kind',[['NOVEL',data.arabic?'رواية':'Roman'],['NONFICTION',data.arabic?'غير روائي':'Sachbuch'],['OTHER',data.arabic?'أنواع أخرى':'Weitere Genres'],['UNKNOWN',data.arabic?'بلا تصنيف':'Ohne Zuordnung']],data.arabic?'كل الكتب':'Alle Bucharten');
 const list=document.getElementById('books');list.replaceChildren();data.books.forEach(b=>{const button=document.createElement('button');button.textContent=b.title;button.onclick=()=>window.Warraq.openBook(b.id);list.append(button);});
 if(!data.books.length){const text=document.createElement('p');text.textContent=data.arabic?'ستظهر كتبك هنا بعد إكمال قراءتها.':'Deine abgeschlossenen Bücher erscheinen hier.';list.append(text);list.hidden=false;}
 document.getElementById('loading').hidden=true;filter();window.Warraq.sceneReady();
};

// --- Touch: left pad walks, dragging elsewhere looks, a short tap opens an own book ------------------
const pad=document.getElementById('pad'),stick=document.getElementById('stick');let padId=null;
function padMove(e){const r=pad.getBoundingClientRect(),x=(e.clientX-r.left-r.width/2)/(r.width/2),y=(e.clientY-r.top-r.height/2)/(r.height/2),n=Math.max(1,Math.hypot(x,y));move={x:x/n,y:y/n};stick.style.transform='translate('+(move.x*30)+'px,'+(move.y*30)+'px)';}
pad.addEventListener('pointerdown',e=>{e.stopPropagation();padId=e.pointerId;pad.setPointerCapture(e.pointerId);padMove(e);});pad.addEventListener('pointermove',e=>{if(e.pointerId===padId)padMove(e)});
function stopPad(){padId=null;move={x:0,y:0};stick.style.transform='';}pad.addEventListener('pointerup',stopPad);pad.addEventListener('pointercancel',stopPad);
renderer.domElement.addEventListener('pointerdown',e=>{look={id:e.pointerId,x:e.clientX,y:e.clientY};touchStart={x:e.clientX,y:e.clientY};renderer.domElement.setPointerCapture(e.pointerId);});
renderer.domElement.addEventListener('pointermove',e=>{if(!look||look.id!==e.pointerId)return;yaw-=(e.clientX-look.x)*.004;pitch=THREE.MathUtils.clamp(pitch-(e.clientY-look.y)*.003,-.8,.65);look.x=e.clientX;look.y=e.clientY;});
renderer.domElement.addEventListener('pointerup',e=>{if(touchStart&&Math.hypot(e.clientX-touchStart.x,e.clientY-touchStart.y)<12){point.set(e.clientX/innerWidth*2-1,-e.clientY/innerHeight*2+1);ray.setFromCamera(point,camera);const hit=ray.intersectObjects(targets,false)[0];if(hit&&hit.distance<9.5)window.Warraq.openBook(hit.object.userData.book.id);}look=null;touchStart=null;});
document.addEventListener('visibilitychange',()=>{if(document.hidden)stopPad()});
// Arrow keys / WASD for emulators and a hardware keyboard; Q/E turn.
const keys=new Set();const keyMove=()=>{move={x:(keys.has('d')||keys.has('arrowright')?1:0)-(keys.has('a')||keys.has('arrowleft')?1:0),y:(keys.has('s')||keys.has('arrowdown')?1:0)-(keys.has('w')||keys.has('arrowup')?1:0)};};
addEventListener('keydown',e=>{const k=e.key.toLowerCase();if(k==='q')yaw+=.08;else if(k==='e')yaw-=.08;else{keys.add(k);keyMove();}});addEventListener('keyup',e=>{keys.delete(e.key.toLowerCase());keyMove();});
window.addEventListener('resize',()=>{camera.aspect=innerWidth/innerHeight;camera.fov=fov();camera.updateProjectionMatrix();renderer.setSize(innerWidth,innerHeight);});
function frame(){requestAnimationFrame(frame);if(document.hidden)return;const dt=Math.min(clock.getDelta(),.04),speed=dt*1.4,time=clock.elapsedTime;
 let nx=camera.position.x+(move.x*Math.cos(yaw)+move.y*Math.sin(yaw))*speed,nz=camera.position.z+(-move.x*Math.sin(yaw)+move.y*Math.cos(yaw))*speed;
 camera.position.x=THREE.MathUtils.clamp(nx,-.95,.95);camera.position.z=THREE.MathUtils.clamp(nz,-7.4,6.75);
 // a slight step bob while walking
 camera.position.y=1.57+(move.x||move.y?Math.sin(time*7)*.012:0);camera.rotation.set(pitch,yaw,0,'YXZ');
 for(const l of lanterns)l.light.intensity=l.base*(.9+.06*Math.sin(time*7.3+l.phase)+.04*Math.sin(time*13.1+l.phase*2));
 const p=dustGeo.attributes.position.array;for(let i=0;i<dustCount;i++){p[i*3+1]+=Math.sin(time*.3+i)*.0006-.0002;if(p[i*3+1]<.2)p[i*3+1]=3.4;}dustGeo.attributes.position.needsUpdate=true;
 for(const e of entries){if(!e.loaded&&e.book.cover&&e.group.position.distanceTo(camera.position)<11){e.loaded=true;loader.load(e.book.cover,t=>{t.colorSpace=THREE.SRGBColorSpace;e.coverMat.map=t;e.coverMat.needsUpdate=true;},undefined,()=>{});}}
 renderer.render(scene,camera);
}frame();
window.Warraq.ready();
