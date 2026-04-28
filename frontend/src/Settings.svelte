<script lang="ts">
  import { onMount, createEventDispatcher } from 'svelte';

  export type AppConfig = {
    autostart: boolean;
    widget: string;
    startSource: string;
    windowMode: string;
    alwaysOnTop: boolean;
    desktopPin: boolean;
    floating: boolean;
    locked: boolean;
    closeToTray: boolean;
    hideUi: boolean;
    startHidden: boolean;
    reactiveEffects: boolean;
    reactiveMode: string;
  };

  const dispatch = createEventDispatcher<{ saved: AppConfig; close: void }>();
  let step = 1;
  let saving = false;
  let saved = false;

  let config: AppConfig = {
    autostart: true,
    widget: 'hud',
    startSource: 'wave',
    windowMode: 'floating',
    alwaysOnTop: true,
    desktopPin: false,
    floating: true,
    locked: false,
    closeToTray: true,
    hideUi: false,
    startHidden: false,
    reactiveEffects: true,
    reactiveMode: 'normal'
  };

  async function load() {
    try {
      const res = await fetch('/api/config', { cache: 'no-store' });
      config = { ...config, ...(await res.json()) };
    } catch (e) {
      console.error(e);
    }
  }

  async function save() {
    saving = true;
    saved = false;
    const res = await fetch('/api/config', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(config)
    });
    config = await res.json();
    saving = false;
    saved = true;
    dispatch('saved', config);
  }

  function chooseWidget(widget: string) {
    config.widget = widget;
  }

  onMount(load);
</script>

<section class="settings-shell">
  <div class="scanline"></div>
  <header>
    <div>
      <span class="eyebrow">YA MUSIC VIDJET</span>
      <h1>SETTINGS WIZARD</h1>
    </div>
    <button class="close" on:click={() => dispatch('close')}>×</button>
  </header>

  <nav class="steps">
    <button class:active={step === 1} on:click={() => step = 1}>01 BASE</button>
    <button class:active={step === 2} on:click={() => step = 2}>02 WINDOW</button>
    <button class:active={step === 3} on:click={() => step = 3}>03 BACKGROUND</button>
    <button class:active={step === 4} on:click={() => step = 4}>04 EFFECTS</button>
  </nav>

  {#if step === 1}
    <div class="grid two">
      <label class="toggle"><input type="checkbox" bind:checked={config.autostart}/><span></span><b>Автозапуск Windows</b></label>
      <label class="select">Стартовый источник<select bind:value={config.startSource}><option value="wave">Моя волна</option><option value="last">Последний источник</option></select></label>
    </div>

    <div class="cards">
      <button class:active={config.widget === 'hud'} on:click={() => chooseWidget('hud')}><b>FULL HUD</b><span>Большой киберпанк плеер</span></button>
      <button class:active={config.widget === 'slim'} on:click={() => chooseWidget('slim')}><b>SLIM BAR</b><span>Тонкая панель</span></button>
      <button class:active={config.widget === 'orb'} on:click={() => chooseWidget('orb')}><b>FLOATING ORB</b><span>Шар с раскрытием</span></button>
    </div>
  {:else if step === 2}
    <div class="grid two">
      <label class="toggle"><input type="checkbox" bind:checked={config.alwaysOnTop}/><span></span><b>Always on top</b></label>
      <label class="toggle"><input type="checkbox" bind:checked={config.desktopPin}/><span></span><b>Закрепить на рабочем столе</b></label>
      <label class="toggle"><input type="checkbox" bind:checked={config.floating}/><span></span><b>Floating режим</b></label>
      <label class="toggle"><input type="checkbox" bind:checked={config.locked}/><span></span><b>Lock позицию</b></label>
    </div>
    <label class="select wide">Режим окна<select bind:value={config.windowMode}><option value="floating">Floating</option><option value="desktop">Desktop pin</option><option value="topmost">Always on top</option></select></label>
  {:else if step === 3}
    <div class="grid two">
      <label class="toggle"><input type="checkbox" bind:checked={config.closeToTray}/><span></span><b>Закрытие в трей</b></label>
      <label class="toggle"><input type="checkbox" bind:checked={config.hideUi}/><span></span><b>Скрывать UI</b></label>
      <label class="toggle"><input type="checkbox" bind:checked={config.startHidden}/><span></span><b>Запуск без окна</b></label>
    </div>
  {:else}
    <div class="grid two">
      <label class="toggle"><input type="checkbox" bind:checked={config.reactiveEffects}/><span></span><b>Audio reactive effects</b></label>
      <label class="select">Mode<select bind:value={config.reactiveMode}><option value="low">Low</option><option value="normal">Normal</option><option value="aggressive">Aggressive</option></select></label>
    </div>
  {/if}

  <footer>
    <button disabled={step === 1} on:click={() => step--}>← BACK</button>
    {#if step < 4}<button class="primary" on:click={() => step++}>NEXT →</button>{:else}<button class="primary" on:click={save}>{saving ? 'SAVING...' : saved ? 'SAVED' : 'SAVE CONFIG'}</button>{/if}
  </footer>
</section>

<style>
/* styles unchanged */
</style>
