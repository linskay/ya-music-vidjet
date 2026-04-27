<script lang="ts">
  import { onMount } from 'svelte';

  type Mode = 'hud' | 'slim' | 'compact' | 'orb' | 'source';
  type PlayerState = {
    title: string;
    artist: string;
    cover: string;
    current: string | number;
    duration: string | number;
    progress: number;
    volume: number;
    playing: boolean;
    liked: boolean;
  };

  let mode: Mode = 'hud';
  let locked = false;
  let x = 120;
  let y = 40;
  let dragging = false;
  let offsetX = 0;
  let offsetY = 0;
  let volume = 65;
  let smoothProgress = 0;
  let syncError = false;

  let state: PlayerState = {
    title: 'Ожидание трека',
    artist: 'Yandex Music',
    cover: '',
    current: 0,
    duration: 0,
    progress: 0,
    volume: 0.65,
    playing: false,
    liked: false
  };

  const fallbackCover = 'https://images.unsplash.com/photo-1519608487953-e999c86e7455?auto=format&fit=crop&w=420&q=80';

  $: title = state.title || 'Ожидание трека';
  $: artist = state.artist || 'Yandex Music';
  $: cover = state.cover || fallbackCover;
  $: currentTime = formatTime(state.current);
  $: durationTime = formatTime(state.duration);
  $: progressWidth = `${Math.max(0, Math.min(100, smoothProgress * 100))}%`;
  $: playLabel = state.playing ? 'Ⅱ' : '▶';
  $: if (typeof state.volume === 'number') volume = Math.round(state.volume * 100);

  async function api(path: string) {
    try {
      await fetch(path);
      await fetchState();
    } catch (e) {
      console.error(e);
    }
  }

  async function fetchState() {
    try {
      const response = await fetch('/api/state', { cache: 'no-store' });
      const next = await response.json();
      state = { ...state, ...next };
      syncError = false;
    } catch (e) {
      syncError = true;
    }
  }

  function formatTime(value: string | number) {
    if (typeof value === 'string' && value.includes(':')) return value;
    const seconds = Math.max(0, Number(value) || 0);
    const min = Math.floor(seconds / 60);
    const sec = Math.floor(seconds % 60).toString().padStart(2, '0');
    return `${min}:${sec}`;
  }

  function setVolume(value: number) {
    volume = value;
    api(`/api/volume/${(volume / 100).toFixed(2)}`);
  }

  function down(event: MouseEvent) {
    if (locked) return;
    dragging = true;
    offsetX = event.clientX - x;
    offsetY = event.clientY - y;
  }

  function move(event: MouseEvent) {
    if (!dragging || locked) return;
    x = event.clientX - offsetX;
    y = event.clientY - offsetY;
  }

  function up() {
    if (!dragging) return;
    dragging = false;
    const margin = 18;
    const snap = 36;
    const w = mode === 'hud' ? 900 : mode === 'slim' ? 760 : 420;
    const h = mode === 'hud' ? 360 : 110;
    if (x < snap) x = margin;
    if (y < snap) y = margin;
    if (window.innerWidth - (x + w) < snap) x = window.innerWidth - w - margin;
    if (window.innerHeight - (y + h) < snap) y = window.innerHeight - h - margin;
  }

  onMount(() => {
    fetchState();
    const stateTimer = window.setInterval(fetchState, 700);
    const progressTimer = window.setInterval(() => {
      const target = Math.max(0, Math.min(1, Number(state.progress) || 0));
      smoothProgress = smoothProgress + (target - smoothProgress) * 0.22;
    }, 80);
    return () => {
      window.clearInterval(stateTimer);
      window.clearInterval(progressTimer);
    };
  });
</script>

<svelte:window on:mousemove={move} on:mouseup={up} />

<div class="stage" style={`left:${x}px; top:${y}px`} class:locked on:mousedown={down}>
  {#if mode === 'hud'}
    <section class="hud frame-glow" class:paused={!state.playing}>
      <header class="hud-top">
        <button class="source" on:click|stopPropagation={() => mode='source'}><span class="wave"></span>МОЯ ВОЛНА⌄</button>
        <div class="status" class:error={syncError}>{syncError ? 'SYNC LOST' : state.playing ? 'PLAYING' : 'PAUSED'}</div>
        <div class="window-actions">
          <button on:click|stopPropagation={() => locked = !locked}>{locked ? 'LOCK' : 'PIN'}</button>
          <button on:click|stopPropagation={() => mode='slim'}>MIN</button>
          <button on:click|stopPropagation={() => mode='orb'}>ORB</button>
        </div>
      </header>

      <main class="hud-body">
        <div class="cover-wrap"><img src={cover} alt="cover" /><div class="equalizer"><i></i><i></i><i></i><i></i><i></i></div></div>
        <div class="info">
          <h1>{title}</h1>
          <h2>{artist}</h2>
          <div class="progress-row"><span>{currentTime}</span><div class="progress"><b style={`width:${progressWidth}`}></b></div><span>{durationTime}</span></div>
          <div class="controls">
            <button class="hex" on:click|stopPropagation={() => api('/api/prev')}>◀◀</button>
            <button class="hex primary" on:click|stopPropagation={() => api('/api/play')}>{playLabel}</button>
            <button class="hex" on:click|stopPropagation={() => api('/api/next')}>▶▶</button>
            <button class="hex like" class:active={state.liked} on:click|stopPropagation={() => api('/api/like')}>♡</button>
            <button class="hex" on:click|stopPropagation={() => api('/api/dislike')}>▱</button>
          </div>
        </div>
      </main>

      <footer class="volume-row">
        <button class="speaker" on:click|stopPropagation={() => setVolume(0)}>◁×</button>
        <input type="range" min="0" max="100" bind:value={volume} on:input={(e)=>setVolume(Number((e.target as HTMLInputElement).value))} />
        <strong>{volume}%</strong>
        <button class="speaker" on:click|stopPropagation={() => setVolume(100)}>◁))</button>
      </footer>
    </section>
  {:else if mode === 'slim'}
    <section class="slim frame-glow" class:paused={!state.playing}>
      <img src={cover} alt="cover" /><div><b>{title}</b><span>{artist}</span></div>
      <button class="hex" on:click|stopPropagation={() => api('/api/prev')}>◀</button><button class="hex primary" on:click|stopPropagation={() => api('/api/play')}>{playLabel}</button><button class="hex" on:click|stopPropagation={() => api('/api/next')}>▶</button><button class="hex like" class:active={state.liked} on:click|stopPropagation={() => api('/api/like')}>♡</button><button on:click|stopPropagation={() => mode='hud'}>HUD</button>
    </section>
  {:else if mode === 'source'}
    <section class="source-menu frame-glow">
      <header>ИСТОЧНИК <button on:click|stopPropagation={() => mode='hud'}>×</button></header>
      <button on:click|stopPropagation={() => { api('/api/wave'); mode='hud'; }}>▥ МОЯ ВОЛНА</button>
      <button>☷ ПЛЕЙЛИСТЫ</button>
      <button>☆ НОВИНКИ</button>
      <button>HQ КАЧЕСТВО <span>HQ</span></button>
    </section>
  {:else}
    <button class="orb" class:paused={!state.playing} on:click|stopPropagation={() => mode='hud'}><span></span></button>
  {/if}
</div>

<style>
  :global(body){margin:0;background:#050712;color:#dff8ff;font-family:Inter,Segoe UI,Arial,sans-serif;overflow:hidden;}
  .stage{position:fixed;z-index:10;user-select:none;filter:drop-shadow(0 0 24px rgba(193,57,255,.35));}
  .locked{cursor:default}.stage:not(.locked){cursor:move}
  button{font:inherit;color:#7eefff;background:rgba(0,20,40,.38);border:1px solid #15d8ff;cursor:pointer;text-shadow:0 0 10px #35e8ff;transition:transform .16s ease, box-shadow .16s ease, border-color .16s ease;}
  button:hover{box-shadow:0 0 18px #20e8ff, inset 0 0 18px rgba(32,232,255,.16);transform:translateY(-1px)}
  .frame-glow{position:relative;background:linear-gradient(135deg,rgba(5,8,22,.88),rgba(4,8,18,.74));border:1px solid rgba(224,64,255,.88);box-shadow:0 0 0 1px rgba(0,229,255,.25) inset,0 0 42px rgba(204,45,255,.35),0 0 24px rgba(0,229,255,.18);clip-path:polygon(3% 0,22% 0,24% 3%,45% 3%,46% 0,97% 0,100% 8%,100% 88%,96% 100%,70% 100%,68% 97%,28% 97%,26% 100%,3% 100%,0 92%,0 8%);}
  .hud{width:900px;min-height:350px;padding:28px 34px;animation:float 5s ease-in-out infinite;}
  .hud.paused,.slim.paused{filter:saturate(.78)}
  .hud:before,.hud:after{content:"";position:absolute;inset:12px;border-top:1px solid rgba(0,238,255,.5);border-bottom:1px solid rgba(205,47,255,.45);pointer-events:none;clip-path:polygon(0 0,18% 0,20% 12%,78% 12%,80% 0,100% 0,100% 100%,74% 100%,72% 88%,28% 88%,26% 100%,0 100%)}
  .hud-top{display:flex;align-items:center;justify-content:space-between;position:relative;z-index:2}.source{font-size:20px;letter-spacing:2px;padding:8px 18px;border-color:transparent;background:transparent}.wave{display:inline-block;width:32px;height:22px;margin-right:14px;background:repeating-linear-gradient(90deg,#17eaff 0 3px,transparent 3px 7px);filter:drop-shadow(0 0 8px #17eaff);vertical-align:middle}.status{font-size:12px;letter-spacing:3px;color:#24f2ff;text-shadow:0 0 12px #24f2ff}.status.error{color:#ff4ee8;text-shadow:0 0 12px #ff4ee8}.window-actions{display:flex;gap:14px}.window-actions button{padding:8px 12px;border-radius:6px}
  .hud-body{display:grid;grid-template-columns:245px 1fr;gap:38px;margin-top:26px;position:relative;z-index:2}.cover-wrap{position:relative;width:220px;height:220px;padding:10px;border:1px solid #df45ff;box-shadow:0 0 22px rgba(223,69,255,.55);clip-path:polygon(8% 0,92% 0,100% 8%,100% 92%,92% 100%,8% 100%,0 92%,0 8%)}.cover-wrap img{width:100%;height:100%;object-fit:cover;filter:saturate(1.35) contrast(1.1)}.equalizer{position:absolute;bottom:18px;left:28px;display:flex;gap:4px;align-items:flex-end}.equalizer i{width:3px;background:#26f6ff;box-shadow:0 0 8px #26f6ff;animation:eq 900ms infinite alternate}.paused .equalizer i{animation-play-state:paused;opacity:.45}.equalizer i:nth-child(2){height:22px;animation-delay:.15s}.equalizer i:nth-child(3){height:12px;animation-delay:.3s}.equalizer i:nth-child(4){height:26px;animation-delay:.45s}.equalizer i:nth-child(5){height:16px;animation-delay:.6s}
  h1{font-size:34px;margin:20px 0 6px;font-weight:500;text-shadow:0 0 12px rgba(126,239,255,.35);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:570px}h2{font-size:24px;margin:0 0 24px;color:#f14cff;text-shadow:0 0 16px rgba(241,76,255,.6);white-space:nowrap;overflow:hidden;text-overflow:ellipsis;max-width:570px}.progress-row{display:grid;grid-template-columns:62px 1fr 62px;align-items:center;gap:18px;color:#33cfff;font-size:18px}.progress{height:5px;background:rgba(89,39,146,.55);box-shadow:0 0 12px rgba(207,54,255,.22)}.progress b{display:block;height:100%;background:#d34cff;box-shadow:0 0 14px #d34cff;position:relative;transition:width .18s linear}.progress b:after{content:"";position:absolute;right:-7px;top:-5px;width:15px;height:15px;border-radius:50%;background:#df62ff;box-shadow:0 0 14px #df62ff}
  .controls{display:flex;gap:26px;margin-top:34px;align-items:center}.hex{min-width:88px;height:62px;font-size:24px;clip-path:polygon(18% 0,82% 0,100% 28%,100% 72%,82% 100%,18% 100%,0 72%,0 28%);border-color:#158bdf}.hex.primary{min-width:102px;height:78px;font-size:42px;color:#ff65f5;border-color:#f04dff;box-shadow:0 0 24px rgba(240,77,255,.35),inset 0 0 18px rgba(240,77,255,.16)}.hex.like{color:#ff4ee8;border-color:#a22bda;font-size:34px}.hex.like.active{background:rgba(255,78,232,.18);box-shadow:0 0 26px #ff4ee8,inset 0 0 20px rgba(255,78,232,.24)}.volume-row{display:grid;grid-template-columns:70px 1fr 70px 70px;gap:22px;align-items:center;margin-top:30px;position:relative;z-index:2}.volume-row input{accent-color:#25eaff;width:100%;filter:drop-shadow(0 0 8px #25eaff)}.speaker{border:none;background:transparent;font-size:24px}strong{color:#33cfff;font-size:18px}
  .slim{width:760px;height:86px;padding:12px 18px;display:flex;align-items:center;gap:18px}.slim img{width:58px;height:58px;object-fit:cover;border:1px solid #df45ff;box-shadow:0 0 12px #df45ff}.slim div{display:flex;flex-direction:column;min-width:220px;max-width:260px}.slim b{font-size:19px;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.slim span{color:#ef4cff;white-space:nowrap;overflow:hidden;text-overflow:ellipsis}.slim .hex{min-width:64px;height:48px;font-size:18px}
  .source-menu{width:360px;padding:18px}.source-menu header{display:flex;justify-content:space-between;color:#48cfff;margin-bottom:14px}.source-menu button{display:flex;justify-content:space-between;width:100%;padding:14px 18px;margin:8px 0;border-color:#9134dc;color:#dff8ff;text-align:left}.source-menu button:first-of-type{color:#ff5df1;box-shadow:inset 0 0 18px rgba(240,77,255,.12)}
  .orb{width:86px;height:86px;border-radius:50%;border-color:#e04cff;background:radial-gradient(circle,#331044,#050712 62%);box-shadow:0 0 30px #e04cff, inset 0 0 22px rgba(0,238,255,.2);animation:pulse 2.3s infinite}.orb.paused{animation-play-state:paused;filter:saturate(.6)}.orb span{display:block;width:34px;height:34px;margin:auto;border-radius:50%;background:#25eaff;box-shadow:0 0 24px #25eaff}
  @keyframes float{0%,100%{transform:translateY(0)}50%{transform:translateY(-5px)}}@keyframes pulse{0%,100%{transform:scale(1)}50%{transform:scale(1.06)}}@keyframes eq{from{height:6px}to{height:30px}}
</style>
