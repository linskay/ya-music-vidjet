<script lang="ts">
  import { onMount, createEventDispatcher } from 'svelte';
  import logo from '../../docs/assets/logo.svg';

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

<section class="launcher">
  <div class="grid-bg"></div>
  <aside class="side">
    <img src={logo} alt="YA Pulse" />
    <div class="side-title">YA PULSE</div>
    <div class="side-sub">SYSTEM MENU</div>
    <nav>
      <button class:active={step === 1} on:click={() => step = 1}>01 / БАЗА</button>
      <button class:active={step === 2} on:click={() => step = 2}>02 / ОКНО</button>
      <button class:active={step === 3} on:click={() => step = 3}>03 / ФОН</button>
      <button class:active={step === 4} on:click={() => step = 4}>04 / ЭФФЕКТЫ</button>
    </nav>
  </aside>

  <main class="panel">
    <header>
      <div>
        <span>CONFIGURATION</span>
        <h1>{step === 1 ? 'Базовые настройки' : step === 2 ? 'Поведение окна' : step === 3 ? 'Фоновый режим' : 'Эффекты и звук'}</h1>
      </div>
      <button class="close" on:click={() => dispatch('close')}>×</button>
    </header>

    {#if step === 1}
      <div class="cards">
        <button class:active={config.widget === 'hud'} on:click={() => chooseWidget('hud')}><b>HUD</b><small>Большой интерфейс</small></button>
        <button class:active={config.widget === 'slim'} on:click={() => chooseWidget('slim')}><b>SLIM</b><small>Узкая панель</small></button>
        <button class:active={config.widget === 'orb'} on:click={() => chooseWidget('orb')}><b>ORB</b><small>Мини-режим</small></button>
      </div>
      <label class="row"><input type="checkbox" bind:checked={config.autostart}/><b>Автозапуск Windows</b><i>? Запускать виджет вместе с системой.</i></label>
      <label class="row select"><b>Стартовый источник</b><select bind:value={config.startSource}><option value="wave">Моя волна</option><option value="last">Последний источник</option></select><i>? Что включать при старте приложения.</i></label>
    {:else if step === 2}
      <label class="row"><input type="checkbox" bind:checked={config.alwaysOnTop}/><b>Поверх всех окон</b><i>? Виджет всегда виден поверх других программ.</i></label>
      <label class="row"><input type="checkbox" bind:checked={config.desktopPin}/><b>Закрепить на рабочем столе</b><i>? Меньше отвлекает и ведёт себя как элемент рабочего стола.</i></label>
      <label class="row"><input type="checkbox" bind:checked={config.floating}/><b>Floating режим</b><i>? Можно перетаскивать виджет по экрану.</i></label>
      <label class="row"><input type="checkbox" bind:checked={config.locked}/><b>Заблокировать позицию</b><i>? Случайно не сдвинешь виджет мышкой.</i></label>
    {:else if step === 3}
      <label class="row"><input type="checkbox" bind:checked={config.closeToTray}/><b>Закрытие в трей</b><i>? При закрытии окно скрывается, музыка продолжает играть.</i></label>
      <label class="row"><input type="checkbox" bind:checked={config.hideUi}/><b>Скрывать UI</b><i>? Оставляет приложение работать в фоне.</i></label>
      <label class="row"><input type="checkbox" bind:checked={config.startHidden}/><b>Запуск без окна</b><i>? При старте появится только трей.</i></label>
    {:else}
      <label class="row"><input type="checkbox" bind:checked={config.reactiveEffects}/><b>Реактивные эффекты</b><i>? Лёгкие эффекты под состояние музыки. Почти не нагружают ПК.</i></label>
      <label class="row select"><b>Интенсивность</b><select bind:value={config.reactiveMode}><option value="low">Low</option><option value="normal">Normal</option><option value="aggressive">Aggressive</option></select><i>? Чем выше режим, тем ярче анимации.</i></label>
      <div class="module">
        <strong>FFT AUDIO ANALYZER</strong>
        <p>Экспериментальный реальный анализ звука. Красиво, но может добавить нагрузку.</p>
        <label class="row"><input type="checkbox" bind:checked={config.experimentalFftAnalyzer}/><b>Включить FFT</b><i>? Анализирует аудио и даёт живую реакцию интерфейса.</i></label>
        <label class="row select"><b>Нагрузка</b><select bind:value={config.fftMode}><option value="low">Low</option><option value="normal">Normal</option><option value="high">High</option></select><i>? Low — безопасный режим для слабых ПК.</i></label>
        <label class="row"><input type="checkbox" bind:checked={config.disableFftInBackground}/><b>Отключать в фоне</b><i>? Экономит ресурсы, когда виджет скрыт.</i></label>
      </div>
    {/if}

    <footer>
      <button disabled={step === 1} on:click={() => step--}>← НАЗАД</button>
      {#if step < 4}
        <button class="primary" on:click={() => step++}>ДАЛЕЕ →</button>
      {:else}
        <button class="primary" on:click={save}>{saving ? 'СОХРАНЕНИЕ...' : saved ? 'СОХРАНЕНО' : 'СОХРАНИТЬ'}</button>
      {/if}
    </footer>
  </main>
</section>

<style>
.launcher{position:fixed;inset:34px;display:grid;grid-template-columns:260px 1fr;z-index:9000;color:#cfffff;background:rgba(2,8,12,.86);border:1px solid rgba(53,243,255,.75);clip-path:polygon(0 0,96% 0,100% 7%,100% 100%,4% 100%,0 93%);box-shadow:0 0 24px rgba(53,243,255,.18);overflow:hidden}.grid-bg{position:absolute;inset:0;background:linear-gradient(90deg,rgba(53,243,255,.05) 1px,transparent 1px),linear-gradient(0deg,rgba(53,243,255,.04) 1px,transparent 1px);background-size:42px 42px;opacity:.35;pointer-events:none}.side,.panel{position:relative;z-index:1}.side{padding:28px 22px;border-right:1px solid rgba(53,243,255,.45);background:rgba(4,16,22,.62)}.side img{width:180px;display:block;margin:0 auto 10px;filter:drop-shadow(0 0 10px #35f3ff)}.side-title{color:#ff5d68;font-size:24px;letter-spacing:5px}.side-sub{font-size:11px;letter-spacing:4px;color:#7eefff;margin-bottom:28px}.side nav{display:grid;gap:10px}.side button,.panel button{height:44px;background:rgba(3,12,18,.72);border:1px solid rgba(53,243,255,.55);color:#bff9ff;clip-path:polygon(8% 0,100% 0,100% 70%,86% 100%,0 100%,0 22%);text-align:left;padding:0 16px;letter-spacing:2px;cursor:pointer}.side button.active,.cards button.active,.primary{border-color:#ff5d68!important;color:#ff8b91!important;background:rgba(255,93,104,.10)!important}.panel{padding:28px 34px}.panel header{display:flex;justify-content:space-between;align-items:flex-start;border-bottom:1px solid rgba(53,243,255,.35);padding-bottom:18px;margin-bottom:22px}.panel header span{font-size:11px;letter-spacing:4px;color:#ff5d68}.panel h1{margin:4px 0 0;font-weight:500;letter-spacing:2px}.close{width:48px;text-align:center!important;font-size:26px}.cards{display:grid;grid-template-columns:repeat(3,1fr);gap:14px;margin-bottom:18px}.cards button{height:94px}.cards b{display:block;font-size:24px}.cards small{color:#7eefff}.row{position:relative;display:grid;grid-template-columns:28px 230px 1fr;gap:12px;align-items:center;margin:12px 0;padding:14px 16px;background:rgba(5,18,24,.58);border:1px solid rgba(53,243,255,.32);clip-path:polygon(2% 0,100% 0,100% 78%,96% 100%,0 100%,0 18%)}.row input{accent-color:#ff5d68}.row b{color:#e8ffff}.row i{font-style:normal;color:#7eefff;font-size:12px;opacity:.85}.select{grid-template-columns:230px 180px 1fr}.select select{background:#061018;color:#bff9ff;border:1px solid rgba(53,243,255,.55);padding:8px;clip-path:polygon(6% 0,100% 0,100% 74%,90% 100%,0 100%,0 24%)}.module{margin-top:20px;padding:16px;border:1px solid rgba(255,93,104,.45);background:rgba(255,93,104,.045);clip-path:polygon(3% 0,100% 0,100% 88%,96% 100%,0 100%,0 12%)}.module strong{color:#ff5d68;letter-spacing:3px}.module p{color:#9eeff7;margin:8px 0 12px}footer{display:flex;justify-content:space-between;margin-top:24px}.panel button:hover,.side button:hover{box-shadow:inset 0 0 14px rgba(53,243,255,.14);transform:translateY(-1px)}button:disabled{opacity:.35;cursor:not-allowed}
</style>
