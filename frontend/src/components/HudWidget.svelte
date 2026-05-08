<script lang="ts">
  import { playerStore } from '../stores/playerStore';
  import { configStore } from '../stores/configStore';
  import { appStatusStore } from '../stores/appStatusStore';
  import { safeApi } from '../lib/api';
  import { formatTime } from '../lib/format';
  import { FALLBACK_COVER } from '../lib/constants';

  export let onOpenSettings: () => void;
  export let onSwitchMode: (mode: 'slim' | 'orb' | 'source') => void;

  $: state = $playerStore;
  $: config = $configStore;
  $: status = $appStatusStore;

  $: title = state.title || 'Ожидание трека';
  $: artist = state.artist || 'Yandex Music';
  $: cover = state.cover || FALLBACK_COVER;
  $: currentTime = formatTime(state.current);
  $: durationTime = formatTime(state.duration);
  $: progressWidth = `${Math.max(0, Math.min(100, state.progress * 100))}%`;
  $: playLabel = state.playing ? 'Ⅱ' : '▶';
  $: volume = Math.round(state.volume * 100);

  function setVolume(val: number) {
    safeApi(`/api/volume/${(val / 100).toFixed(2)}`);
  }
</script>

<section class="hud frame-glow" class:paused={!state.playing}>
  <div class="beat-ring"></div>
  <div class="hud-noise"></div>
  <div class="hud-glitch"></div>
  <div class="tech-label label-a">PLAYER_CORE</div>
  <div class="tech-label label-b">NET_SYNC</div>
  <div class="corner corner-a"></div>
  <div class="corner corner-b"></div>

  <header class="hud-top">
    <button class="source" on:click|stopPropagation={() => onSwitchMode('source')}>
      <span class="wave"></span>МОЯ ВОЛНА⌄
    </button>
    <div class="status" class:error={status.syncError}>
      {status.syncError ? 'SYNC LOST' : state.playing ? 'AUDIO_ONLINE' : 'PAUSED'}
    </div>
    <div class="window-actions">
      <button on:click|stopPropagation={() => configStore.update(c => ({...c, locked: !c.locked}))}>
        {config.locked ? 'LOCK' : 'PIN'}
      </button>
      <button on:click|stopPropagation={onOpenSettings}>SET</button>
      <button on:click|stopPropagation={() => onSwitchMode('slim')}>MIN</button>
      <button on:click|stopPropagation={() => onSwitchMode('orb')}>ORB</button>
    </div>
  </header>

  <main class="hud-body">
    <div class="cover-wrap">
      <img src={cover} alt="cover" />
      <div class="cover-scan"></div>
      <div class="equalizer"><i></i><i></i><i></i><i></i><i></i></div>
    </div>
    <div class="info">
      <div class="micro">TRACK_DATA / ACTIVE</div>
      <h1>{title}</h1>
      <h2>{artist}</h2>
      <div class="progress-row">
        <span>{currentTime}</span>
        <div class="progress"><b style={`width:${progressWidth}`}></b></div>
        <span>{durationTime}</span>
      </div>
      <div class="controls">
        <button class="hex" on:click|stopPropagation={() => safeApi('/api/prev')}>◀◀</button>
        <button class="hex primary" on:click|stopPropagation={() => safeApi('/api/play')}>{playLabel}</button>
        <button class="hex" on:click|stopPropagation={() => safeApi('/api/next')}>▶▶</button>
        <button class="hex like" class:active={state.liked} on:click|stopPropagation={() => safeApi('/api/like')}>♡</button>
        <button class="hex" on:click|stopPropagation={() => safeApi('/api/dislike')}>▱</button>
      </div>
    </div>
  </main>

  <footer class="volume-row">
    <button class="speaker" on:click|stopPropagation={() => setVolume(0)}>◁×</button>
    <input type="range" min="0" max="100" value={volume} on:input={(e)=>setVolume(Number((e.target as HTMLInputElement).value))} />
    <strong>{volume}%</strong>
    <button class="speaker" on:click|stopPropagation={() => setVolume(100)}>◁))</button>
  </footer>
</section>

<style>
  .hud { width: 900px; min-height: 350px; padding: 28px 34px; animation: float 5s ease-in-out infinite, fadeScale .42s ease-out; }
  .beat-ring { position: absolute; inset: 18px; pointer-events: none; border: 1px solid rgba(255, 93, 104, calc(.06 + .24 * var(--bass))); clip-path: polygon(0 0, 16% 0, 18% 10%, 82% 10%, 84% 0, 100% 0, 100% 100%, 80% 100%, 78% 90%, 22% 90%, 20% 100%, 0 100%); transform: scale(calc(1 + .014 * var(--bass))); opacity: calc(.18 + .42 * var(--bass)); animation: beatSlice .42s ease-out infinite; }
  .hud-glitch { position: absolute; inset: 0; pointer-events: none; background: repeating-linear-gradient(90deg, transparent, transparent 3px, rgba(255, 93, 104, calc(.012 + .035 * var(--react))) 4px); mix-blend-mode: screen; animation: glitch calc(2.1s - .55s * var(--react)) infinite; }
  .hud.paused { filter: saturate(.78); }
  .hud:before { content: "ACTIVE"; position: absolute; top: 0; left: 270px; width: 210px; height: 22px; background: rgba(53, 243, 255, .22); border: 1px solid rgba(53, 243, 255, .72); color: #9ff9ff; text-align: center; font-size: 12px; letter-spacing: 2px; }
  .hud:after { content: ""; position: absolute; inset: 12px; border-top: 1px solid rgba(53, 243, 255, .45); border-bottom: 1px solid rgba(255, 93, 104, .28); pointer-events: none; clip-path: polygon(0 0, 18% 0, 20% 12%, 78% 12%, 80% 0, 100% 0, 100% 100%, 74% 100%, 72% 88%, 28% 88%, 26% 100%, 0 100%); }
  .hud-noise { position: absolute; inset: 0; pointer-events: none; opacity: calc(.035 + .045 * var(--react)); background: repeating-linear-gradient(0deg, rgba(255, 255, 255, .11) 0 1px, transparent 1px 5px); mix-blend-mode: screen; }
  .tech-label { position: absolute; font-size: 10px; letter-spacing: 3px; color: #ff5d68; text-shadow: 0 0 8px rgba(255, 93, 104, .55); z-index: 3; }
  .label-a { left: 38px; top: 18px; }
  .label-b { right: 210px; top: 18px; }
  .micro { font-size: 11px; letter-spacing: 3px; color: #ff5d68; margin-top: 10px; margin-bottom: -8px; }
  .corner { position: absolute; width: 120px; height: 70px; border-color: #35f3ff; filter: drop-shadow(0 0 4px #35f3ff); opacity: .75; }
  .corner-a { left: 18px; top: 18px; border-left: 1px solid; border-top: 1px solid; }
  .corner-b { right: 18px; bottom: 18px; border-right: 1px solid; border-bottom: 1px solid; }
  .hud-top { display: flex; align-items: center; justify-content: space-between; position: relative; z-index: 2; }
  .source { font-size: 18px; letter-spacing: 2px; padding: 8px 18px; border-color: rgba(53, 243, 255, .35); background: rgba(3, 14, 20, .25); }
  .wave { display: inline-block; width: 32px; height: 22px; margin-right: 14px; background: repeating-linear-gradient(90deg, #35f3ff 0 2px, transparent 2px 7px); filter: drop-shadow(0 0 4px #35f3ff); vertical-align: middle; animation: wavePulse 1.2s infinite; }
  .status { font-size: 11px; letter-spacing: 3px; color: #35f3ff; text-shadow: 0 0 8px rgba(53, 243, 255, .7); }
  .status.error { color: #ff5d68; text-shadow: 0 0 8px rgba(255, 93, 104, .8); }
  .window-actions { display: flex; gap: 10px; }
  .window-actions button { padding: 8px 12px; font-size: 12px; }
  .hud-body { display: grid; grid-template-columns: 245px 1fr; gap: 38px; margin-top: 26px; position: relative; z-index: 2; }
  .cover-wrap { position: relative; width: 220px; height: 220px; padding: 8px; border: 1px solid rgba(53, 243, 255, .72); box-shadow: 0 0 calc(8px + 14px * var(--bass)) rgba(53, 243, 255, .22); clip-path: polygon(7% 0, 100% 0, 100% 86%, 86% 100%, 0 100%, 0 12%); transform: translateY(calc(-2px * var(--bass))) scale(calc(1 + .012 * var(--bass))); }
  .cover-wrap:before { content: "COVER_FEED"; position: absolute; left: 10px; top: -1px; background: rgba(53, 243, 255, .22); color: #9ff9ff; font-size: 10px; letter-spacing: 2px; padding: 2px 10px; z-index: 2; }
  .cover-wrap img { width: 100%; height: 100%; object-fit: cover; filter: saturate(calc(1.18 + .25 * var(--bass))) contrast(1.08); }
  .cover-scan { position: absolute; inset: 8px; background: linear-gradient(180deg, transparent, rgba(53, 243, 255, calc(.12 + .18 * var(--bass))), transparent); animation: scan calc(3.8s - 1.1s * var(--bass)) linear infinite; mix-blend-mode: screen; }
  .equalizer { position: absolute; bottom: 18px; left: 28px; display: flex; gap: 4px; align-items: flex-end; }
  .equalizer i { width: 2px; background: #35f3ff; box-shadow: 0 0 calc(4px + 8px * var(--bass)) #35f3ff; animation: eq 900ms infinite alternate; transform: scaleY(calc(1 + .65 * var(--bass))); transform-origin: bottom; }
  .paused .equalizer i { animation-play-state: paused; opacity: .45; }
  .equalizer i:nth-child(2) { height: 22px; animation-delay: .15s; }
  .equalizer i:nth-child(3) { height: 12px; animation-delay: .3s; }
  .equalizer i:nth-child(4) { height: 26px; animation-delay: .45s; }
  .equalizer i:nth-child(5) { height: 16px; animation-delay: .6s; }
  h1 { font-size: 34px; margin: 18px 0 6px; font-weight: 500; text-shadow: 0 0 calc(6px + 8px * var(--bass)) rgba(126, 239, 255, .25); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 570px; }
  h2 { font-size: 22px; margin: 0 0 24px; color: #ff5d68; text-shadow: 0 0 calc(6px + 8px * var(--bass)) rgba(255, 93, 104, .45); white-space: nowrap; overflow: hidden; text-overflow: ellipsis; max-width: 570px; }
  .progress-row { display: grid; grid-template-columns: 62px 1fr 62px; align-items: center; gap: 18px; color: #35f3ff; font-size: 18px; }
  .progress { height: 4px; background: rgba(53, 243, 255, .13); box-shadow: none; border: 1px solid rgba(53, 243, 255, .22); }
  .progress b { display: block; height: 100%; background: linear-gradient(90deg, #35f3ff, #ff5d68); box-shadow: 0 0 calc(5px + 10px * var(--bass)) rgba(53, 243, 255, .7); position: relative; transition: width .18s linear; }
  .progress b:after { content: ""; position: absolute; right: -5px; top: -5px; width: 12px; height: 12px; background: #35f3ff; clip-path: polygon(50% 0, 100% 50%, 50% 100%, 0 50%); box-shadow: 0 0 calc(8px + 8px * var(--bass)) #35f3ff; }
  .controls { display: flex; gap: 22px; margin-top: 34px; align-items: center; }
  .hex { min-width: 86px; height: 58px; font-size: 22px; clip-path: polygon(9% 0, 100% 0, 100% 70%, 84% 100%, 0 100%, 0 18%); border-color: rgba(53, 243, 255, .72); background: rgba(2, 12, 18, .42); }
  .hex.primary { min-width: 98px; height: 72px; font-size: 40px; color: #ff6b75; border-color: #ff5d68; box-shadow: inset 0 0 calc(10px + 12px * var(--bass)) rgba(255, 93, 104, .16); animation: primaryPulse 2.2s ease-in-out infinite; transform: scale(calc(1 + .018 * var(--bass))); }
  .hex.like { color: #ff5d68; border-color: rgba(255, 93, 104, .72); font-size: 32px; }
  .hex.like.active { background: rgba(255, 93, 104, .12); box-shadow: inset 0 0 14px rgba(255, 93, 104, .18); }
  .volume-row { display: grid; grid-template-columns: 70px 1fr 70px 70px; gap: 22px; align-items: center; margin-top: 30px; position: relative; z-index: 2; }
  .volume-row input { accent-color: #35f3ff; width: 100%; filter: drop-shadow(0 0 calc(4px + 5px * var(--bass)) #35f3ff); }
  .speaker { border: none; background: transparent; font-size: 24px; }
  strong { color: #35f3ff; font-size: 18px; }

  @keyframes float { 0%, 100% { transform: translateY(0); } 50% { transform: translateY(calc(-3px - 2px * var(--bass))); } }
  @keyframes beatSlice { 0% { opacity: calc(.14 + .24 * var(--bass)); transform: scale(1); } 100% { opacity: 0; transform: scale(calc(1.012 + .028 * var(--bass))); } }
  @keyframes glitch { 0%, 100% { transform: translateX(0); } 20% { transform: translateX(calc(1px * var(--react))); } 40% { transform: translateX(calc(-1px * var(--react))); } 60% { transform: translateX(calc(.7px * var(--react))); } 80% { transform: translateX(calc(-.7px * var(--react))); } }
  @keyframes primaryPulse { 0%, 100% { filter: drop-shadow(0 0 calc(3px + 5px * var(--bass)) #ff5d68); } 50% { filter: drop-shadow(0 0 calc(9px + 10px * var(--bass)) #ff5d68); } }
  @keyframes wavePulse { 0%, 100% { opacity: .72; transform: scaleY(calc(.8 + .35 * var(--bass))); } 50% { opacity: 1; transform: scaleY(calc(1.12 + .52 * var(--bass))); } }
</style>
