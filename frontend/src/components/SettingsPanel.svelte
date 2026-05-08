<script lang="ts">
  import { createEventDispatcher } from 'svelte';
  import { configStore } from '../stores/configStore';

  const dispatch = createEventDispatcher();

  $: config = $configStore;

  let saving = false;

  async function save() {
    saving = true;
    try {
      const res = await fetch('/api/config', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(config)
      });
      const saved = await res.json();
      configStore.set(saved);
      dispatch('saved', saved);
    } catch (e) {
      console.error(e);
    } finally {
      setTimeout(() => saving = false, 600);
    }
  }

  function resetSession() {
    if (confirm('Сбросить сессию Яндекс Музыки?')) {
        fetch('/api/session/reset').catch(console.error);
    }
  }
</script>

<div class="settings-overlay" on:mousedown|self={() => dispatch('close')}>
  <section class="settings-panel frame-glow">
    <header>
      <h2>НАСТРОЙКИ СИСТЕМЫ</h2>
      <button class="close-btn" on:click={() => dispatch('close')}>×</button>
    </header>

    <div class="scroll-area">
      <div class="group">
        <h3>ОСНОВНОЕ</h3>
        <label>
          <span>Автозапуск Windows</span>
          <input type="checkbox" bind:checked={config.autostart} on:change={save} />
        </label>
        <label>
          <span>Режим виджета</span>
          <select bind:value={config.widget} on:change={save}>
            <option value="hud">HUD (Полный)</option>
            <option value="slim">Slim (Узкий)</option>
            <option value="orb">Orb (Мини)</option>
          </select>
        </label>
      </div>

      <div class="group">
        <h3>ОКНО</h3>
        <label>
          <span>Поверх всех окон</span>
          <input type="checkbox" bind:checked={config.alwaysOnTop} on:change={save} />
        </label>
        <label>
          <span>Закрепить позицию</span>
          <input type="checkbox" bind:checked={config.locked} on:change={save} />
        </label>
        <label>
          <span>Закрывать в трей</span>
          <input type="checkbox" bind:checked={config.closeToTray} on:change={save} />
        </label>
      </div>

      <div class="group">
        <h3>ЭФФЕКТЫ</h3>
        <label>
          <span>Реактивные эффекты</span>
          <input type="checkbox" bind:checked={config.reactiveEffects} on:change={save} />
        </label>
        <label>
          <span>Интенсивность</span>
          <select bind:value={config.reactiveMode} on:change={save}>
            <option value="low">Спокойный</option>
            <option value="normal">Нормальный</option>
            <option value="aggressive">Агрессивный</option>
          </select>
        </label>
      </div>

      <div class="group">
        <h3>АККАУНТ</h3>
        <button class="action-btn" on:click={resetSession}>СБРОСИТЬ СЕССИЮ</button>
      </div>
    </div>

    <footer>
      <div class="save-status">{saving ? 'СОХРАНЕНИЕ...' : 'ИЗМЕНЕНИЯ ПРИМЕНЕНЫ'}</div>
      <button class="primary-btn" on:click={() => dispatch('close')}>ГОТОВО</button>
    </footer>
  </section>
</div>

<style>
  .settings-overlay { position: fixed; inset: 0; background: rgba(1, 4, 8, .72); display: grid; place-items: center; z-index: 1000; backdrop-filter: blur(4px); }
  .settings-panel { width: 480px; max-height: 85vh; padding: 32px; display: flex; flex-direction: column; animation: fadeScale .2s ease-out; }
  header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 24px; border-bottom: 1px solid rgba(53, 243, 255, .3); padding-bottom: 12px; }
  h2 { font-size: 20px; color: #35f3ff; margin: 0; letter-spacing: 4px; }
  .close-btn { background: transparent; border: none; font-size: 32px; color: #ff5d68; cursor: pointer; }
  .scroll-area { flex: 1; overflow-y: auto; padding-right: 12px; }
  .group { margin-bottom: 28px; }
  h3 { font-size: 13px; color: #ff5d68; letter-spacing: 3px; margin-bottom: 16px; border-left: 2px solid; padding-left: 10px; }
  label { display: flex; justify-content: space-between; align-items: center; margin-bottom: 14px; font-size: 16px; }
  select { background: #040c12; color: #35f3ff; border: 1px solid #35f3ff; padding: 4px 8px; font: inherit; }
  input[type="checkbox"] { width: 18px; height: 18px; accent-color: #35f3ff; }
  .action-btn { width: 100%; padding: 10px; border-color: #ff5d68; color: #ff5d68; background: rgba(255, 93, 104, .05); }
  footer { margin-top: 24px; display: flex; justify-content: space-between; align-items: center; border-top: 1px solid rgba(53, 243, 255, .2); padding-top: 18px; }
  .save-status { font-size: 11px; letter-spacing: 2px; color: #7eefff; opacity: .8; }
  .primary-btn { padding: 10px 32px; background: rgba(53, 243, 255, .15); border-color: #35f3ff; color: #35f3ff; font-weight: bold; }
</style>
