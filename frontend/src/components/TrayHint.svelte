<script lang="ts">
  import { onMount } from 'svelte';
  import { createEventDispatcher } from 'svelte';
  const dispatch = createEventDispatcher();

  onMount(() => {
    const timer = setTimeout(() => dispatch('hide'), 5000);
    return () => clearTimeout(timer);
  });
</script>

<div class="tray-hint">
  <p>Приложение работает в фоне. Управление доступно через иконку в трее.</p>
  <button on:click={() => dispatch('hide')}>ПОНЯТНО</button>
</div>

<style>
  .tray-hint { position: fixed; bottom: 20px; right: 20px; width: 240px; background: rgba(4, 12, 18, 0.95); border: 1px solid #ff5d68; padding: 16px; animation: slideIn 0.4s ease-out; z-index: 500; }
  p { font-size: 13px; margin: 0 0 12px; line-height: 1.4; color: #dff8ff; }
  button { width: 100%; padding: 6px; font-size: 11px; border-color: #ff5d68; color: #ff5d68; }
  @keyframes slideIn { from { transform: translateX(100%); opacity: 0; } to { transform: translateX(0); opacity: 1; } }
</style>
