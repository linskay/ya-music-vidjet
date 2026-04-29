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
    experimentalFftAnalyzer?: boolean;
    fftMode?: string;
    disableFftInBackground?: boolean;
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
    reactiveMode: 'normal',
    experimentalFftAnalyzer: false,
    fftMode: 'low',
    disableFftInBackground: true
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
  <header>
    <h1>НАСТРОЙКИ</h1>
    <button class="close" on:click={() => dispatch('close')}>×</button>
  </header>

  <nav class="steps">
    <button class:active={step === 1} on:click={() => step = 1}>01 БАЗА</button>
    <button class:active={step === 2} on:click={() => step = 2}>02 ОКНО</button>
    <button class:active={step === 3} on:click={() => step = 3}>03 ФОН</button>
    <button class:active={step === 4} on:click={() => step = 4}>04 ЭФФЕКТЫ</button>
  </nav>

  {#if step === 4}
    <div class="grid two">
      <label class="toggle">
        <input type="checkbox" bind:checked={config.reactiveEffects}/>
        <b>Реактивные эффекты</b>
        <span class="tip">? Простые визуальные эффекты под музыку. Почти не нагружают ПК.</span>
      </label>

      <label class="select">
        Режим
        <select bind:value={config.reactiveMode}>
          <option value="low">Low</option>
          <option value="normal">Normal</option>
          <option value="aggressive">Aggressive</option>
        </select>
      </label>
    </div>

    <div class="fft-block">
      <h3>FFT анализ (эксперимент)</h3>
      <span class="tip">? Реальный анализ звука. Красиво, но может нагружать процессор.</span>

      <label class="toggle">
        <input type="checkbox" bind:checked={config.experimentalFftAnalyzer}/>
        <b>Включить FFT</b>
      </label>

      <label class="select">
        Нагрузка
        <select bind:value={config.fftMode}>
          <option value="low">Low (лёгкий)</option>
          <option value="normal">Normal</option>
          <option value="high">High (нагрузка)</option>
        </select>
      </label>

      <label class="toggle">
        <input type="checkbox" bind:checked={config.disableFftInBackground}/>
        <b>Отключать в фоне</b>
        <span class="tip">? Экономит ресурсы, когда виджет скрыт.</span>
      </label>
    </div>
  {/if}

  <footer>
    <button disabled={step === 1} on:click={() => step--}>← Назад</button>
    {#if step < 4}
      <button class="primary" on:click={() => step++}>Далее →</button>
    {:else}
      <button class="primary" on:click={save}>{saving ? 'СОХРАНЕНИЕ...' : saved ? 'СОХРАНЕНО' : 'СОХРАНИТЬ'}</button>
    {/if}
  </footer>
</section>

<style>
.tip { margin-left: 6px; font-size: 11px; color: #7eefff; cursor: help; }
.fft-block { margin-top: 20px; padding: 10px; border: 1px solid #444; }
</style>
