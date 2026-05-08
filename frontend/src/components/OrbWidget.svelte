<script lang="ts">
  import { playerStore } from '../stores/playerStore';

  export let onOpenSettings: () => void;
  export let onSwitchMode: (mode: 'hud') => void;

  $: state = $playerStore;
</script>

<button
  class="orb"
  class:paused={!state.playing}
  on:click|stopPropagation={() => onSwitchMode('hud')}
  on:contextmenu|preventDefault|stopPropagation={onOpenSettings}
  title={`${state.title || 'YA Music'} - ${state.artist || 'Yandex'}`}
>
  <div class="orb-content">
    <span></span>
    <div class="orb-ring"></div>
  </div>
  <div class="orb-tooltip">{state.title || 'YA Music'}</div>
</button>

<style>
  .orb { position: relative; width: 64px; height: 64px; border-radius: 0; border: none; background: rgba(4, 12, 18, .85); box-shadow: 0 0 calc(12px + 22px * var(--bass)) rgba(53, 243, 255, .35); clip-path: polygon(18% 0, 100% 0, 100% 82%, 82% 100%, 0 100%, 0 18%); animation: pulse 2.3s infinite; transform: scale(calc(1 + .035 * var(--bass))); display: grid; place-items: center; padding: 0; }
  .orb:hover { transform: scale(calc(1.1 + .035 * var(--bass))); box-shadow: 0 0 22px rgba(53, 243, 255, .55), inset 0 0 22px rgba(53, 243, 255, .18); }
  .orb:hover .orb-tooltip { opacity: 1; transform: translateY(-10px); }
  .orb.paused { animation-play-state: paused; filter: saturate(.4); }
  .orb-content { position: relative; width: 100%; height: 100%; display: grid; place-items: center; }
  .orb span { display: block; width: 22px; height: 22px; background: #35f3ff; clip-path: polygon(50% 0, 100% 50%, 50% 100%, 0 50%); box-shadow: 0 0 calc(16px + 12px * var(--bass)) #35f3ff; z-index: 2; }
  .orb-ring { position: absolute; inset: 10px; border: 1px solid rgba(53, 243, 255, .5); clip-path: polygon(18% 0, 100% 0, 100% 82%, 82% 100%, 0 100%, 0 18%); animation: rotate 4s linear infinite; }
  .orb-tooltip { position: absolute; bottom: 100%; left: 50%; transform: translateX(-50%) translateY(0); background: #02060a; border: 1px solid #35f3ff; color: #35f3ff; padding: 4px 10px; font-size: 10px; letter-spacing: 1px; white-space: nowrap; opacity: 0; pointer-events: none; transition: all .2s ease; z-index: 10; }

  @keyframes pulse { 0%, 100% { transform: scale(calc(1 + .015 * var(--bass))); } 50% { transform: scale(calc(1.04 + .045 * var(--bass))); } }
  @keyframes rotate { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
