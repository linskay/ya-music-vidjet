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
    startHidden: false
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
  {:else}
    <div class="grid two">
      <label class="toggle"><input type="checkbox" bind:checked={config.closeToTray}/><span></span><b>Закрытие в трей</b></label>
      <label class="toggle"><input type="checkbox" bind:checked={config.hideUi}/><span></span><b>Скрывать UI</b></label>
      <label class="toggle"><input type="checkbox" bind:checked={config.startHidden}/><span></span><b>Запуск без окна</b></label>
    </div>
    <p class="hint">После tray-этапа: закрыл окно настроек → backend, Playwright и виджет продолжают работать.</p>
  {/if}

  <footer>
    <button disabled={step === 1} on:click={() => step--}>← BACK</button>
    {#if step < 3}<button class="primary" on:click={() => step++}>NEXT →</button>{:else}<button class="primary" on:click={save}>{saving ? 'SAVING...' : saved ? 'SAVED' : 'SAVE CONFIG'}</button>{/if}
  </footer>
</section>

<style>
  .settings-shell{position:fixed;left:50%;top:50%;transform:translate(-50%,-50%);width:820px;min-height:520px;padding:28px;color:#dff8ff;background:linear-gradient(135deg,rgba(5,8,22,.96),rgba(13,5,24,.92));border:1px solid #e34dff;box-shadow:0 0 44px rgba(227,77,255,.35),inset 0 0 28px rgba(0,238,255,.08);clip-path:polygon(3% 0,23% 0,25% 3%,76% 3%,78% 0,97% 0,100% 8%,100% 92%,97% 100%,72% 100%,70% 97%,30% 97%,28% 100%,3% 100%,0 92%,0 8%);z-index:50}.scanline{position:absolute;inset:0;pointer-events:none;background:repeating-linear-gradient(180deg,rgba(255,255,255,.035) 0 1px,transparent 1px 5px);mix-blend-mode:screen}header{display:flex;justify-content:space-between;align-items:flex-start}h1{margin:4px 0 0;font-size:36px;letter-spacing:4px;text-shadow:0 0 18px #25eaff}.eyebrow{color:#ff58f2;letter-spacing:3px}.close{width:44px;height:44px;font-size:28px}.steps{display:grid;grid-template-columns:repeat(3,1fr);gap:14px;margin:28px 0}.steps button,.cards button,footer button,.close{color:#7eefff;background:rgba(0,20,40,.4);border:1px solid #15d8ff;text-shadow:0 0 10px #35e8ff;cursor:pointer}.steps button{padding:12px}.steps button.active,.cards button.active,.primary{border-color:#ff58f2!important;color:#ff79f6!important;box-shadow:0 0 22px rgba(255,88,242,.35),inset 0 0 16px rgba(255,88,242,.14)}.grid.two{display:grid;grid-template-columns:1fr 1fr;gap:18px}.toggle,.select{display:flex;align-items:center;justify-content:space-between;gap:16px;padding:18px;border:1px solid rgba(21,216,255,.5);background:rgba(3,18,34,.45)}.toggle input{display:none}.toggle span{width:54px;height:28px;border:1px solid #15d8ff;border-radius:999px;position:relative;box-shadow:0 0 12px rgba(21,216,255,.25)}.toggle span:after{content:"";position:absolute;top:4px;left:4px;width:18px;height:18px;border-radius:50%;background:#15d8ff;box-shadow:0 0 12px #15d8ff;transition:.18s}.toggle input:checked+span{border-color:#ff58f2}.toggle input:checked+span:after{left:30px;background:#ff58f2;box-shadow:0 0 14px #ff58f2}.select select{min-width:190px;background:#050712;color:#7eefff;border:1px solid #15d8ff;padding:10px}.wide{margin-top:18px}.cards{display:grid;grid-template-columns:repeat(3,1fr);gap:16px;margin-top:22px}.cards button{height:120px;text-align:left;padding:18px;clip-path:polygon(8% 0,92% 0,100% 18%,100% 82%,92% 100%,8% 100%,0 82%,0 18%)}.cards b{display:block;font-size:20px;margin-bottom:12px}.cards span{color:#c9f7ff}.hint{padding:18px;border-left:2px solid #ff58f2;background:rgba(255,88,242,.08);color:#c9f7ff}footer{display:flex;justify-content:flex-end;gap:14px;margin-top:32px}footer button{padding:12px 22px}button:disabled{opacity:.35;cursor:not-allowed}
</style>
