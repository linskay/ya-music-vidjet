<script lang="ts">
  import { onMount } from 'svelte';
  import Settings, { type AppConfig } from './Settings.svelte';
  import { getReactiveLevel } from './lib/audioReactive';
  import logo from '../../docs/assets/logo.svg';

  type Mode = 'hud' | 'slim' | 'compact' | 'orb' | 'source';
  type PlayerState = { title: string; artist: string; cover: string; current: string | number; duration: string | number; progress: number; volume: number; playing: boolean; liked: boolean; };

  let mode: Mode = 'hud';
  let previousMode: Mode = 'hud';
  let showSettings = false;
  let locked = false;
  let booting = true;
  let x = 120;
  let y = 40;
  let dragging = false;
  let offsetX = 0;
  let offsetY = 0;
  let volume = 65;
  let smoothProgress = 0;
  let reactive = 0;
  let perfScale = 1;
  let syncError = false;
  let ws: WebSocket | undefined;
  let reconnectTimer: number | undefined;
  let lastCall = 0;
  let config: AppConfig | undefined;

  let state: PlayerState = { title: 'Ожидание трека', artist: 'Yandex Music', cover: '', current: 0, duration: 0, progress: 0, volume: 0.65, playing: false, liked: false };
  const fallbackCover = 'https://images.unsplash.com/photo-1519608487953-e999c86e7455?auto=format&fit=crop&w=420&q=80';

  $: title = state.title || 'Ожидание трека';
  $: artist = state.artist || 'Yandex Music';
  $: cover = state.cover || fallbackCover;
  $: currentTime = formatTime(state.current);
  $: durationTime = formatTime(state.duration);
  $: progressWidth = `${Math.max(0, Math.min(100, smoothProgress * 100))}%`;
  $: playLabel = state.playing ? 'Ⅱ' : '▶';
  $: if (typeof state.volume === 'number') volume = Math.round(state.volume * 100);

  async function loadConfig() { try { const res = await fetch('/api/config', { cache: 'no-store' }); config = await res.json(); applyConfig(config); } catch (e) { console.error(e); } }
  function applyConfig(next?: AppConfig) { if (!next) return; if (['hud', 'slim', 'orb'].includes(next.widget)) mode = next.widget as Mode; previousMode = mode; locked = !!next.locked; if (next.startSource === 'wave') safeApi('/api/wave'); if (next.startHidden || next.hideUi) mode = 'orb'; }
  function openSettings() { previousMode = mode === 'source' ? previousMode : mode; showSettings = true; }
  function closeSettings() { showSettings = false; if (mode === 'source') mode = previousMode; }
  function connectWS() { const protocol = location.protocol === 'https:' ? 'wss' : 'ws'; ws = new WebSocket(`${protocol}://${location.host}/ws/state`); ws.onmessage = (event) => { try { state = { ...state, ...JSON.parse(event.data) }; syncError = false; } catch (e) { console.error(e); } }; ws.onclose = () => { syncError = true; reconnectTimer = window.setTimeout(connectWS, 1500); }; ws.onerror = () => ws?.close(); }
  function safeApi(path: string) { const now = Date.now(); if (now - lastCall < 180) return; lastCall = now; fetch(path).catch(console.error); }
  function formatTime(value: string | number) { if (typeof value === 'string' && value.includes(':')) return value; const seconds = Math.max(0, Number(value) || 0); const min = Math.floor(seconds / 60); const sec = Math.floor(seconds % 60).toString().padStart(2, '0'); return `${min}:${sec}`; }
  function setVolume(value: number) { volume = value; safeApi(`/api/volume/${(volume / 100).toFixed(2)}`); }
  function down(event: MouseEvent) { if (locked) return; dragging = true; offsetX = event.clientX - x; offsetY = event.clientY - y; }
  function move(event: MouseEvent) { if (!dragging || locked) return; x = event.clientX - offsetX; y = event.clientY - offsetY; }
  function up() { if (!dragging) return; dragging = false; const margin = 18, snap = 36; const w = mode === 'hud' ? 900 : mode === 'slim' ? 760 : 420; const h = mode === 'hud' ? 360 : 110; if (x < snap) x = margin; if (y < snap) y = margin; if (window.innerWidth - (x + w) < snap) x = window.innerWidth - w - margin; if (window.innerHeight - (y + h) < snap) y = window.innerHeight - h - margin; }

  function reactiveMultiplier() {
    if (!config?.reactiveEffects) return 0;
    if (mode === 'orb' || config?.hideUi || config?.startHidden) return 0.18;
    if (config?.reactiveMode === 'low') return 0.45;
    if (config?.reactiveMode === 'aggressive') return 1.35;
    return 1;
  }

  onMount(() => {
    loadConfig();
    connectWS();
    window.setTimeout(() => booting = false, 1150);
    let frame = 0;
    let lastTick = 0;
    let lastFrame = performance.now();
    let slowFrames = 0;

    const loop = (ts: number) => {
      const frameDelta = ts - lastFrame;
      lastFrame = ts;
      if (frameDelta > 38) slowFrames++; else slowFrames = Math.max(0, slowFrames - 1);
      perfScale = slowFrames > 12 ? 0.55 : slowFrames > 6 ? 0.75 : 1;
      const tickMs = config?.reactiveMode === 'aggressive' ? 80 : config?.reactiveMode === 'low' ? 140 : 110;
      if (ts - lastTick >= tickMs) {
        lastTick = ts;
        const target = Math.max(0, Math.min(1, Number(state.progress) || 0));
        smoothProgress = smoothProgress + (target - smoothProgress) * 0.22;
        reactive = getReactiveLevel(smoothProgress, state.playing) * reactiveMultiplier() * perfScale;
        document.documentElement.style.setProperty('--react', Math.max(0.18, Math.min(1.4, reactive)).toString());
        document.documentElement.style.setProperty('--perf', perfScale.toString());
      }
      frame = requestAnimationFrame(loop);
    };

    frame = requestAnimationFrame(loop);
    return () => { cancelAnimationFrame(frame); if (reconnectTimer) window.clearTimeout(reconnectTimer); ws?.close(); };
  });
</script>

<svelte:window on:mousemove={move} on:mouseup={up} />
{#if booting}<div class="boot-screen"><div class="boot-reactor"></div><img class="boot-logo-img" src={logo} alt="YA Music Widget" /><div class="boot-text">YA MUSIC WIDGET</div><div class="boot-status">CONNECTING AUDIO CORE</div><div class="boot-line"><span></span></div></div>{/if}
{#if showSettings}<Settings on:saved={(e) => { config = e.detail; applyConfig(config); }} on:close={closeSettings} />{/if}

<div class="stage" style={`left:${x}px; top:${y}px`} class:locked class:desktop-pin={config?.desktopPin} class:topmost={config?.alwaysOnTop} class:light-effects={mode === 'orb' || config?.hideUi || config?.startHidden || !config?.reactiveEffects} on:mousedown={down}>
  {#if mode === 'hud'}
    <section class="hud frame-glow" class:paused={!state.playing}>
      <div class="hud-noise"></div><div class="hud-glitch"></div><div class="tech-label label-a">PLAYER_CORE</div><div class="tech-label label-b">NET_SYNC</div><div class="corner corner-a"></div><div class="corner corner-b"></div>
      <header class="hud-top"><button class="source" on:click|stopPropagation={() => mode='source'}><span class="wave"></span>МОЯ ВОЛНА⌄</button><div class="status" class:error={syncError}>{syncError ? 'SYNC LOST' : state.playing ? 'AUDIO_ONLINE' : 'PAUSED'}</div><div class="window-actions"><button on:click|stopPropagation={() => locked = !locked}>{locked ? 'LOCK' : 'PIN'}</button><button on:click|stopPropagation={openSettings}>SET</button><button on:click|stopPropagation={() => mode='slim'}>MIN</button><button on:click|stopPropagation={() => mode='orb'}>ORB</button></div></header>
      <main class="hud-body"><div class="cover-wrap"><img src={cover} alt="cover" /><div class="cover-scan"></div><div class="equalizer"><i></i><i></i><i></i><i></i><i></i></div></div><div class="info"><div class="micro">TRACK_DATA / ACTIVE</div><h1>{title}</h1><h2>{artist}</h2><div class="progress-row"><span>{currentTime}</span><div class="progress"><b style={`width:${progressWidth}`}></b></div><span>{durationTime}</span></div><div class="controls"><button class="hex" on:click|stopPropagation={() => safeApi('/api/prev')}>◀◀</button><button class="hex primary" on:click|stopPropagation={() => safeApi('/api/play')}>{playLabel}</button><button class="hex" on:click|stopPropagation={() => safeApi('/api/next')}>▶▶</button><button class="hex like" class:active={state.liked} on:click|stopPropagation={() => safeApi('/api/like')}>♡</button><button class="hex" on:click|stopPropagation={() => safeApi('/api/dislike')}>▱</button></div></div></main>
      <footer class="volume-row"><button class="speaker" on:click|stopPropagation={() => setVolume(0)}>◁×</button><input type="range" min="0" max="100" bind:value={volume} on:input={(e)=>setVolume(Number((e.target as HTMLInputElement).value))} /><strong>{volume}%</strong><button class="speaker" on:click|stopPropagation={() => setVolume(100)}>◁))</button></footer>
    </section>
  {:else if mode === 'slim'}
    <section class="slim frame-glow" class:paused={!state.playing}><img src={cover} alt="cover" /><div><b>{title}</b><span>{artist}</span></div><button class="hex" on:click|stopPropagation={() => safeApi('/api/prev')}>◀</button><button class="hex primary" on:click|stopPropagation={() => safeApi('/api/play')}>{playLabel}</button><button class="hex" on:click|stopPropagation={() => safeApi('/api/next')}>▶</button><button class="hex like" class:active={state.liked} on:click|stopPropagation={() => safeApi('/api/like')}>♡</button><button on:click|stopPropagation={openSettings}>SET</button><button on:click|stopPropagation={() => mode='hud'}>HUD</button></section>
  {:else if mode === 'source'}
    <section class="source-menu frame-glow"><header>ИСТОЧНИК <button on:click|stopPropagation={() => mode=previousMode}>×</button></header><button on:click|stopPropagation={() => { safeApi('/api/wave'); mode=previousMode; }}>▥ МОЯ ВОЛНА</button><button>☷ ПЛЕЙЛИСТЫ</button><button>☆ НОВИНКИ</button><button>HQ КАЧЕСТВО <span>HQ</span></button></section>
  {:else}
    <button class="orb" class:paused={!state.playing} on:click|stopPropagation={() => mode='hud'} on:contextmenu|preventDefault|stopPropagation={openSettings}><span></span></button>
  {/if}
</div>

<style>
:global(body){margin:0;background:radial-gradient(circle at 20% 10%,#0a1117,#03060a 62%,#010204);color:#dff8ff;font-family:Inter,Segoe UI,Arial,sans-serif;overflow:hidden}:global(:root){--react:.2;--perf:1;--cyan:#35f3ff;--red:#ff5d68;--pink:#ff4df2}.boot-screen{position:fixed;inset:0;display:grid;place-items:center;background:radial-gradient(circle,#101a22,#02040c 68%);z-index:9999;animation:bootFade 1.15s ease forwards;overflow:hidden}.boot-screen:before{content:"";position:absolute;inset:-20%;background:repeating-linear-gradient(0deg,rgba(255,255,255,.035) 0 1px,transparent 1px 6px);opacity:calc(.10 + .08 * var(--react));animation:bootScan 1.1s linear infinite}.boot-reactor{position:absolute;width:430px;height:430px;border-radius:50%;border:1px solid rgba(53,243,255,.25);box-shadow:0 0 calc(18px + 22px * var(--react)) rgba(53,243,255,.18),inset 0 0 34px rgba(53,243,255,.08);animation:reactorPulse .72s ease-in-out infinite alternate}.boot-reactor:before,.boot-reactor:after{content:"";position:absolute;inset:34px;border-radius:50%;border:1px dashed rgba(255,93,104,.35);animation:reactorSpin 4s linear infinite}.boot-reactor:after{inset:74px;border-color:rgba(53,243,255,.38);animation-duration:2.8s;animation-direction:reverse}.boot-logo-img{width:360px;max-width:72vw;filter:drop-shadow(0 0 calc(10px + 8px * var(--react)) #35f3ff) drop-shadow(0 0 calc(12px + 8px * var(--react)) #ff4df2);animation:bootPop .7s ease,logoBeat .7s ease-in-out infinite alternate;z-index:2}.boot-text{margin-top:178px;position:absolute;color:#ff5d68;letter-spacing:8px;text-shadow:0 0 10px rgba(255,93,104,.65);z-index:2}.boot-status{margin-top:214px;position:absolute;color:#7eefff;font-size:11px;letter-spacing:4px;text-shadow:0 0 10px #35f3ff;z-index:2}.boot-line{position:absolute;margin-top:252px;width:300px;height:3px;background:rgba(126,239,255,.12);overflow:hidden;z-index:2}.boot-line span{display:block;height:100%;width:70%;background:linear-gradient(90deg,#35f3ff,#ff5d68);animation:bootLoad 1s ease}.stage{position:fixed;z-index:10;user-select:none;filter:drop-shadow(0 0 calc(8px + 8px * var(--react)) rgba(53,243,255,.22));animation:stageIn .58s cubic-bezier(.16,1,.3,1)}.stage.light-effects .hud-glitch{display:none}.stage.light-effects .hud-noise{opacity:.018}.stage.topmost{z-index:2147483000}.stage.desktop-pin{filter:drop-shadow(0 0 8px rgba(53,243,255,.14));opacity:.96}.locked{cursor:default}.stage:not(.locked){cursor:move}button{font:inherit;color:#bff9ff;background:rgba(2,10,16,.52);border:1px solid rgba(53,243,255,.75);cursor:pointer;text-shadow:0 0 6px rgba(53,243,255,.7);transition:transform .14s ease,box-shadow .14s ease,border-color .14s ease,background .14s ease;position:relative;overflow:hidden;clip-path:polygon(10% 0,100% 0,100% 72%,86% 100%,0 100%,0 20%)}button:before{content:"";position:absolute;inset:0;transform:translateX(-120%);background:linear-gradient(90deg,transparent,rgba(53,243,255,.20),transparent);transition:transform .3s ease}button:hover:before{transform:translateX(120%)}button:hover{background:rgba(6,22,29,.68);box-shadow:inset 0 0 12px rgba(53,243,255,.16);transform:translateY(-1px)}button:active{transform:translateX(1px) scale(.97);filter:saturate(1.25)}.frame-glow{position:relative;background:linear-gradient(135deg,rgba(4,12,18,.84),rgba(3,8,13,.68));border:1px solid rgba(53,243,255,.78);box-shadow:0 0 0 1px rgba(53,243,255,.18) inset,0 0 calc((10px + 8px * var(--react)) * var(--perf)) rgba(53,243,255,.20);clip-path:polygon(3% 0,22% 0,24% 3%,45% 3%,46% 0,97% 0,100% 8%,100% 88%,96% 100%,70% 100%,68% 97%,28% 97%,26% 100%,3% 100%,0 92%,0 8%);animation:borderPulse 3.6s ease-in-out infinite}.frame-glow:after{content:"";position:absolute;left:32px;right:32px;top:72px;height:1px;background:linear-gradient(90deg,transparent,rgba(53,243,255,.55),transparent);opacity:.65}.hud{width:900px;min-height:350px;padding:28px 34px;animation:float 5s ease-in-out infinite,fadeScale .42s ease-out}.hud-glitch{position:absolute;inset:0;pointer-events:none;background:repeating-linear-gradient(90deg,transparent,transparent 3px,rgba(255,93,104,calc(.012 + .035 * var(--react))) 4px);mix-blend-mode:screen;animation:glitch calc(2.1s - .55s * var(--react)) infinite}.hud.paused,.slim.paused{filter:saturate(.78)}.hud:before{content:"ACTIVE";position:absolute;top:0;left:270px;width:210px;height:22px;background:rgba(53,243,255,.22);border:1px solid rgba(53,243,255,.72);color:#9ff9ff;text-align:center;font-size:12px;letter-spacing:2px}.hud:after{content:"";position:absolute;inset:12px;border-top:1px solid rgba(53,243,255,.45);border-bottom:1px solid rgba(255,93,104,.28);pointer-events:none;clip-path:polygon(0 0,18% 0,20% 12%,78% 12%,80% 0,100% 0,100% 100%,74% 100%,72% 88%,28% 88%,26% 100%,0 100%)}.hud-noise{position:absolute;inset:0;pointer-events:none;opacity:calc(.035 + .045 * var(--react));background:repeating-linear-gradient(0deg,rgba(255,255,255,.11) 0 1px,transparent 1px 5px);mix-blend-mode:screen}.tech-label{position:absolute;font-size:10px;letter-spacing:3px;color:#ff5d68;text-shadow:0 0 8px rgba(255,93,104,.55);z-index:3}.label-a{left:38px;top:18px}.label-b{right:210px;top:18px}.micro{font-size:11px;letter-spacing:3px;color:#ff5d68;margin-top:10px;margin-bottom:-8px}.corner{position:absolute;width:120px;height:70px;border-color:#35f3ff;filter:drop-shadow(0 0 4px #35f3ff);opacity:.75}.corner-a{left:18px;top:18px;border-left:1px solid;border-top:1px solid}.corner-b{right:18px;bottom:18px;border-right:1px solid;border-bottom:1px solid}.hud-top{display:flex;align-items:center;justify-content:space-between;position:relative;z-index:2}.source{font-size:18px;letter-spacing:2px;padding:8px 18px;border-color:rgba(53,243,255,.35);background:rgba(3,14,20,.25)}.wave{display:inline-block;width:32px;height:22px;margin-right:14px;background:repeating-linear-gradient(90deg,#35f3ff 0 2px,transparent 2px 7px);filter:drop-shadow(0 0 4px #35f3ff);vertical-align:middle;animation:wavePulse 1.2s infinite}.status{font-size:11px;letter-spacing:3px;color:#35f3ff;text-shadow:0 0 8px rgba(53,243,255,.7)}.status.error{color:#ff5d68;text-shadow:0 0 8px rgba(255,93,104,.8)}.window-actions{display:flex;gap:10px}.window-actions button{padding:8px 12px;font-size:12px}.hud-body{display:grid;grid-template-columns:245px 1fr;gap:38px;margin-top:26px;position:relative;z-index:2}.cover-wrap{position:relative;width:220px;height:220px;padding:8px;border:1px solid rgba(53,243,255,.72);box-shadow:0 0 12px rgba(53,243,255,.2);clip-path:polygon(7% 0,100% 0,100% 86%,86% 100%,0 100%,0 12%)}.cover-wrap:before{content:"COVER_FEED";position:absolute;left:10px;top:-1px;background:rgba(53,243,255,.22);color:#9ff9ff;font-size:10px;letter-spacing:2px;padding:2px 10px;z-index:2}.cover-wrap img{width:100%;height:100%;object-fit:cover;filter:saturate(1.22) contrast(1.08)}.cover-scan{position:absolute;inset:8px;background:linear-gradient(180deg,transparent,rgba(53,243,255,.16),transparent);animation:scan 3.8s linear infinite;mix-blend-mode:screen}.equalizer{position:absolute;bottom:18px;left:28px;display:flex;gap:4px;align-items:flex-end}.equalizer i{width:2px;background:#35f3ff;box-shadow:0 0 5px #35f3ff;animation:eq 900ms infinite alternate}.paused .equalizer i{animation-play-state:paused;opacity:.45}.equalizer i:nth-child(2){height:22px;animation-delay:.15s}.equalizer i:nth-child(3){height:12px;animation-delay:.3s}.equalizer i:nth-child(4){height:26px;animation-delay:.45s}.equalizer i:nth-child(5){height:16px;animation-delay:.6s}h1{font-size:34px;margin:18px 0 6px;font-weight:500;text-shadow:0 0 8px rgba(126,239,255,.25);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:570px}h2{font-size:22px;margin:0 0 24px;color:#ff5d68;text-shadow:0 0 8px rgba(255,93,104,.45);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:570px}.progress-row{display:grid;grid-template-columns:62px 1fr 62px;align-items:center;gap:18px;color:#35f3ff;font-size:18px}.progress{height:4px;background:rgba(53,243,255,.13);box-shadow:none;border:1px solid rgba(53,243,255,.22)}.progress b{display:block;height:100%;background:linear-gradient(90deg,#35f3ff,#ff5d68);box-shadow:0 0 calc(5px + 6px * var(--react)) rgba(53,243,255,.7);position:relative;transition:width .18s linear}.progress b:after{content:"";position:absolute;right:-5px;top:-5px;width:12px;height:12px;background:#35f3ff;clip-path:polygon(50% 0,100% 50%,50% 100%,0 50%);box-shadow:0 0 8px #35f3ff}.controls{display:flex;gap:22px;margin-top:34px;align-items:center}.hex{min-width:86px;height:58px;font-size:22px;clip-path:polygon(9% 0,100% 0,100% 70%,84% 100%,0 100%,0 18%);border-color:rgba(53,243,255,.72);background:rgba(2,12,18,.42)}.hex.primary{min-width:98px;height:72px;font-size:40px;color:#ff6b75;border-color:#ff5d68;box-shadow:inset 0 0 14px rgba(255,93,104,.14);animation:primaryPulse 2.2s ease-in-out infinite}.hex.like{color:#ff5d68;border-color:rgba(255,93,104,.72);font-size:32px}.hex.like.active{background:rgba(255,93,104,.12);box-shadow:inset 0 0 14px rgba(255,93,104,.18)}.volume-row{display:grid;grid-template-columns:70px 1fr 70px 70px;gap:22px;align-items:center;margin-top:30px;position:relative;z-index:2}.volume-row input{accent-color:#35f3ff;width:100%;filter:drop-shadow(0 0 4px #35f3ff)}.speaker{border:none;background:transparent;font-size:24px}strong{color:#35f3ff;font-size:18px}.slim{width:760px;height:86px;padding:12px 18px;display:flex;align-items:center;gap:18px;animation:fadeScale .28s ease-out}.slim img{width:58px;height:58px;object-fit:cover;border:1px solid #35f3ff;box-shadow:0 0 8px rgba(53,243,255,.25)}.slim div{display:flex;flex-direction:column;min-width:220px;max-width:260px}.slim b{font-size:19px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.slim span{color:#ff5d68;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.slim .hex{min-width:64px;height:48px;font-size:18px}.source-menu{width:360px;padding:18px;animation:fadeScale .22s ease-out}.source-menu header{display:flex;justify-content:space-between;color:#35f3ff;margin-bottom:14px}.source-menu button{display:flex;justify-content:space-between;width:100%;padding:14px 18px;margin:8px 0;border-color:rgba(53,243,255,.55);color:#dff8ff;text-align:left}.source-menu button:first-of-type{color:#ff5d68;box-shadow:inset 0 0 12px rgba(255,93,104,.10)}.orb{width:80px;height:80px;border-radius:0;border-color:#35f3ff;background:rgba(4,12,18,.74);box-shadow:0 0 calc(12px + 14px * var(--react)) rgba(53,243,255,.35),inset 0 0 16px rgba(53,243,255,.12);clip-path:polygon(18% 0,100% 0,100% 82%,82% 100%,0 100%,0 18%);animation:pulse 2.3s infinite}.orb:hover{transform:scale(1.08);box-shadow:0 0 22px rgba(53,243,255,.55),inset 0 0 22px rgba(53,243,255,.18)}.orb.paused{animation-play-state:paused;filter:saturate(.6)}.orb span{display:block;width:30px;height:30px;margin:auto;background:#35f3ff;clip-path:polygon(50% 0,100% 50%,50% 100%,0 50%);box-shadow:0 0 16px #35f3ff}@keyframes bootFade{0%,78%{opacity:1}100%{opacity:0;visibility:hidden}}@keyframes bootPop{from{opacity:0;transform:scale(.75) rotate(-8deg)}to{opacity:1;transform:scale(1) rotate(0)}}@keyframes bootLoad{from{transform:translateX(-100%)}to{transform:translateX(58%)}}@keyframes bootScan{from{transform:translateY(-40px)}to{transform:translateY(40px)}}@keyframes reactorPulse{from{transform:scale(.94);opacity:.62}to{transform:scale(1.05);opacity:.95}}@keyframes reactorSpin{to{transform:rotate(360deg)}}@keyframes logoBeat{from{transform:scale(.985)}to{transform:scale(calc(1.01 + .035 * var(--react)))}}@keyframes stageIn{from{opacity:0;transform:translateY(16px) scale(.97);filter:blur(6px)}to{opacity:1;transform:translateY(0) scale(1);filter:blur(0)}}@keyframes fadeScale{from{opacity:0;transform:scale(.96)}to{opacity:1;transform:scale(1)}}@keyframes borderPulse{0%,100%{border-color:rgba(53,243,255,.62)}50%{border-color:rgba(53,243,255,.95)}}@keyframes float{0%,100%{transform:translateY(0)}50%{transform:translateY(-3px)}}@keyframes pulse{0%,100%{transform:scale(1)}50%{transform:scale(1.04)}}@keyframes primaryPulse{0%,100%{filter:drop-shadow(0 0 3px #ff5d68)}50%{filter:drop-shadow(0 0 9px #ff5d68)}}@keyframes wavePulse{0%,100%{opacity:.72;transform:scaleY(.8)}50%{opacity:1;transform:scaleY(1.12)}}@keyframes scan{0%{transform:translateY(-120%)}100%{transform:translateY(120%)}}@keyframes glitch{0%,100%{transform:translateX(0)}20%{transform:translateX(calc(1px * var(--react)))}40%{transform:translateX(calc(-1px * var(--react)))}60%{transform:translateX(calc(.7px * var(--react)))}80%{transform:translateX(calc(-.7px * var(--react)))}}@keyframes eq{from{height:6px}to{height:30px}}
</style>
