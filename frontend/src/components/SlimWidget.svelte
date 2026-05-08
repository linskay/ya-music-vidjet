<script lang="ts">
  import { playerStore } from '../stores/playerStore';
  import { safeApi } from '../lib/api';
  import { FALLBACK_COVER } from '../lib/constants';

  export let onOpenSettings: () => void;
  export let onSwitchMode: (mode: 'hud' | 'orb') => void;

  $: state = $playerStore;
  $: title = state.title || 'Ожидание трека';
  $: artist = state.artist || 'Yandex Music';
  $: cover = state.cover || FALLBACK_COVER;
  $: playLabel = state.playing ? 'Ⅱ' : '▶';
</script>

<section class="slim frame-glow" class:paused={!state.playing}>
  <img src={cover} alt="cover" />
  <div>
    <b>{title}</b>
    <span>{artist}</span>
  </div>
  <button class="hex" on:click|stopPropagation={() => safeApi('/api/prev')}>◀</button>
  <button class="hex primary" on:click|stopPropagation={() => safeApi('/api/play')}>{playLabel}</button>
  <button class="hex" on:click|stopPropagation={() => safeApi('/api/next')}>▶</button>
  <button class="hex like" class:active={state.liked} on:click|stopPropagation={() => safeApi('/api/like')}>♡</button>
  <button on:click|stopPropagation={onOpenSettings}>SET</button>
  <button on:click|stopPropagation={() => onSwitchMode('hud')}>HUD</button>
</section>

<style>
  .slim { width: 760px; height: 86px; padding: 12px 18px; display: flex; align-items: center; gap: 18px; animation: fadeScale .28s ease-out; }
  .slim img { width: 58px; height: 58px; object-fit: cover; border: 1px solid #35f3ff; box-shadow: 0 0 calc(8px + 8px * var(--bass)) rgba(53, 243, 255, .25); }
  .slim div { display: flex; flex-direction: column; min-width: 220px; max-width: 260px; }
  .slim b { font-size: 19px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .slim span { color: #ff5d68; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
  .slim .hex { min-width: 64px; height: 48px; font-size: 18px; }
  .slim.paused { filter: saturate(.78); }
</style>
