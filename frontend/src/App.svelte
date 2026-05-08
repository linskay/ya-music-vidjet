<script lang="ts">
  import { onMount } from 'svelte';
  import './app.css';
  import { playerStore } from './stores/playerStore';
  import { configStore, type AppConfig } from './stores/configStore';
  import { appStatusStore } from './stores/appStatusStore';
  import { createWebSocket } from './lib/ws';
  import { safeApi } from './lib/api';
  import { getReactiveLevel } from './lib/audioReactive';

  import HudWidget from './components/HudWidget.svelte';
  import SlimWidget from './components/SlimWidget.svelte';
  import OrbWidget from './components/OrbWidget.svelte';
  import SettingsPanel from './components/SettingsPanel.svelte';
  import Onboarding from './components/Onboarding.svelte';
  import StatusBanner from './components/StatusBanner.svelte';
  import logo from './assets/logo.svg';

  let mode: 'hud' | 'slim' | 'orb' | 'source' = 'hud';
  let previousMode: 'hud' | 'slim' | 'orb' | 'source' = 'hud';
  let showSettings = false;
  let showOnboarding = false;

  let x = 120;
  let y = 40;
  let dragging = false;
  let offsetX = 0;
  let offsetY = 0;

  let smoothProgress = 0;
  let perfScale = 1;

  async function loadConfig() {
    try {
      const res = await fetch('/api/config');
      const cfg: AppConfig = await res.json();
      configStore.set(cfg);
      applyConfig(cfg);
    } catch (e) {
      console.error('Failed to load config', e);
    }
  }

  function applyConfig(cfg: AppConfig) {
    if (['hud', 'slim', 'orb'].includes(cfg.widget)) {
        mode = cfg.widget as any;
    }
    if (cfg.lastPosition) {
        x = cfg.lastPosition.x;
        y = cfg.lastPosition.y;
    }
  }

  function onSwitchMode(newMode: 'hud' | 'slim' | 'orb' | 'source') {
    previousMode = mode;
    mode = newMode;
  }

  function down(event: MouseEvent) {
    if ($configStore.locked) return;
    dragging = true;
    offsetX = event.clientX - x;
    offsetY = event.clientY - y;
  }

  function move(event: MouseEvent) {
    if (!dragging || $configStore.locked) return;
    x = event.clientX - offsetX;
    y = event.clientY - offsetY;
  }

  function up() {
    if (!dragging) return;
    dragging = false;
    configStore.update(c => ({...c, lastPosition: {x, y}}));
    saveConfig();
  }

  async function saveConfig() {
    try {
        await fetch('/api/config', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify($configStore)
        });
    } catch (e) {
        console.error('Failed to save config', e);
    }
  }

  onMount(() => {
    loadConfig();

    const ws = createWebSocket(
        (data) => playerStore.set(data),
        (connected) => appStatusStore.update(s => ({...s, syncError: !connected}))
    );

    setTimeout(() => appStatusStore.update(s => ({...s, booting: false})), 1200);

    let lastFrame = performance.now();
    let slowFrames = 0;
    let frameId: number;

    const loop = (ts: number) => {
      const delta = ts - lastFrame;
      lastFrame = ts;

      if (delta > 38) slowFrames++;
      else slowFrames = Math.max(0, slowFrames - 1);

      perfScale = slowFrames > 12 ? 0.55 : slowFrames > 6 ? 0.75 : 1;

      const target = $playerStore.progress || 0;
      smoothProgress = smoothProgress + (target - smoothProgress) * 0.2;

      const reactive = getReactiveLevel(smoothProgress, $playerStore.playing) * ($configStore.reactiveEffects ? 1 : 0) * perfScale;
      const bass = $playerStore.playing ? Math.max(0.05, reactive * 1.2 + Math.abs(Math.sin(ts / 170)) * 0.1) : 0;

      document.documentElement.style.setProperty('--react', reactive.toString());
      document.documentElement.style.setProperty('--bass', bass.toString());
      document.documentElement.style.setProperty('--perf', perfScale.toString());

      frameId = requestAnimationFrame(loop);
    };

    frameId = requestAnimationFrame(loop);

    return () => {
      cancelAnimationFrame(frameId);
      ws.close();
    };
  });
</script>

<svelte:window on:mousemove={move} on:mouseup={up} />

{#if $appStatusStore.booting}
  <div class="boot-screen">
    <div class="boot-reactor"></div>
    <img class="boot-logo-img" src={logo} alt="YA Music Widget" />
    <div class="boot-text">YA MUSIC WIDGET</div>
    <div class="boot-status">CONNECTING AUDIO CORE</div>
    <div class="boot-line"><span></span></div>
  </div>
{/if}

{#if showOnboarding}
  <Onboarding on:complete={() => showOnboarding = false} />
{/if}

{#if showSettings}
  <SettingsPanel on:close={() => showSettings = false} />
{/if}

<div
  class="stage"
  style={`left:${x}px; top:${y}px`}
  class:locked={$configStore.locked}
  class:desktop-pin={$configStore.desktopPin}
  class:topmost={$configStore.alwaysOnTop}
  on:mousedown={down}
>
  {#if mode === 'hud'}
    <HudWidget onOpenSettings={() => showSettings = true} onSwitchMode={onSwitchMode} />
  {:else if mode === 'slim'}
    <SlimWidget onOpenSettings={() => showSettings = true} onSwitchMode={onSwitchMode} />
  {:else if mode === 'orb'}
    <OrbWidget onOpenSettings={() => showSettings = true} onSwitchMode={onSwitchMode} />
  {:else if mode === 'source'}
    <section class="source-menu frame-glow">
        <header>ИСТОЧНИК <button on:click|stopPropagation={() => mode = previousMode}>×</button></header>
        <button on:click|stopPropagation={() => { safeApi('/api/wave'); mode = previousMode; }}>▥ МОЯ ВОЛНА</button>
        <button>☷ ПЛЕЙЛИСТЫ</button>
        <button>☆ НОВИНКИ</button>
        <button>HQ КАЧЕСТВО <span>HQ</span></button>
    </section>
  {/if}

  {#if $appStatusStore.syncError}
    <StatusBanner message="СИНХРОНИЗАЦИЯ ПОТЕРЯНА" type="error" />
  {/if}
</div>

<style>
  .boot-screen { position: fixed; inset: 0; display: grid; place-items: center; background: radial-gradient(circle, #101a22, #02040c 68%); z-index: 9999; animation: bootFade 1.15s ease forwards; overflow: hidden; }
  .boot-reactor { position: absolute; width: 430px; height: 430px; border-radius: 50%; border: 1px solid rgba(53, 243, 255, .25); box-shadow: 0 0 calc(18px + 22px * var(--react)) rgba(53, 243, 255, .18), inset 0 0 34px rgba(53, 243, 255, .08); animation: reactorPulse .72s ease-in-out infinite alternate; }
  .boot-logo-img { width: 360px; max-width: 72vw; filter: drop-shadow(0 0 calc(10px + 8px * var(--react)) #35f3ff) drop-shadow(0 0 calc(12px + 8px * var(--react)) #ff4df2); animation: bootPop .7s ease, logoBeat .7s ease-in-out infinite alternate; z-index: 2; }
  .boot-text { margin-top: 178px; position: absolute; color: #ff5d68; letter-spacing: 8px; text-shadow: 0 0 10px rgba(255, 93, 104, .65); z-index: 2; }
  .boot-status { margin-top: 214px; position: absolute; color: #7eefff; font-size: 11px; letter-spacing: 4px; text-shadow: 0 0 10px #35f3ff; z-index: 2; }
  .boot-line { position: absolute; margin-top: 252px; width: 300px; height: 3px; background: rgba(126, 239, 255, .12); overflow: hidden; z-index: 2; }
  .boot-line span { display: block; height: 100%; width: 70%; background: linear-gradient(90deg, #35f3ff, #ff5d68); animation: bootLoad 1s ease; }

  .stage { position: fixed; z-index: 10; user-select: none; filter: drop-shadow(0 0 calc(8px + 8px * var(--react)) rgba(53, 243, 255, .22)); animation: stageIn .58s cubic-bezier(.16, 1, .3, 1); }
  .stage.topmost { z-index: 2147483000; }
  .stage.desktop-pin { filter: drop-shadow(0 0 8px rgba(53, 243, 255, .14)); opacity: .96; }
  .stage:not(.locked) { cursor: move; }

  .source-menu { width: 360px; padding: 18px; animation: fadeScale .22s ease-out; }
  .source-menu header { display: flex; justify-content: space-between; color: #35f3ff; margin-bottom: 14px; }
  .source-menu button { display: flex; justify-content: space-between; width: 100%; padding: 14px 18px; margin: 8px 0; border-color: rgba(53, 243, 255, .55); color: #dff8ff; text-align: left; }
  .source-menu button:first-of-type { color: #ff5d68; box-shadow: inset 0 0 12px rgba(255, 93, 104, .10); }

  @keyframes bootFade { 0%, 78% { opacity: 1 } 100% { opacity: 0; visibility: hidden } }
  @keyframes reactorPulse { from { transform: scale(.94); opacity: .62 } to { transform: scale(1.05); opacity: .95 } }
  @keyframes logoBeat { from { transform: scale(.985) } to { transform: scale(calc(1.01 + .035 * var(--react))) } }
  @keyframes bootPop { from { opacity: 0; transform: scale(.75) rotate(-8deg) } to { opacity: 1; transform: scale(1) rotate(0) } }
  @keyframes bootLoad { from { transform: translateX(-100%) } to { transform: translateX(58%) } }
</style>
