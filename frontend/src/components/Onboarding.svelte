<script lang="ts">
  import { createEventDispatcher } from 'svelte';
  import logo from '../assets/logo.svg';

  const dispatch = createEventDispatcher();
  let step = 1;

  function next() {
    if (step < 3) step++;
    else dispatch('complete');
  }

  function openAuth() {
    fetch('/api/login');
  }
</script>

<div class="onboarding-overlay">
  <section class="onboarding-card frame-glow">
    {#if step === 1}
      <div class="step">
        <img src={logo} alt="Logo" class="logo" />
        <h1>ДОБРО ПОЖАЛОВАТЬ</h1>
        <p>YA Music Widget — киберпанк-интерфейс для управления Яндекс Музыкой.</p>
        <button class="next-btn" on:click={next}>НАЧАТЬ НАСТРОЙКУ</button>
      </div>
    {:else if step === 2}
      <div class="step">
        <h2>АВТОРИЗАЦИЯ</h2>
        <p>Чтобы виджет мог управлять музыкой, необходимо войти в свой аккаунт Яндекс.</p>
        <p class="hint">Мы откроем окно браузера. Ваши данные сохраняются только локально.</p>
        <div class="actions">
          <button class="auth-btn" on:click={openAuth}>ВОЙТИ В ЯНДЕКС</button>
          <button class="skip-btn" on:click={next}>Я УЖЕ ВОШЕЛ</button>
        </div>
      </div>
    {:else}
      <div class="step">
        <h2>ВСЁ ГОТОВО</h2>
        <p>Виджет всегда доступен в системном трее.</p>
        <p>Вы можете менять режим (HUD/Slim/Orb) в настройках.</p>
        <button class="next-btn" on:click={next}>ПОЕХАЛИ</button>
      </div>
    {/if}
  </section>
</div>

<style>
  .onboarding-overlay { position: fixed; inset: 0; background: #02060a; display: grid; place-items: center; z-index: 2000; }
  .onboarding-card { width: 500px; padding: 40px; text-align: center; }
  .logo { width: 120px; margin-bottom: 24px; filter: drop-shadow(0 0 15px #35f3ff); }
  h1 { font-size: 24px; color: #35f3ff; letter-spacing: 4px; margin-bottom: 16px; }
  h2 { font-size: 20px; color: #ff5d68; letter-spacing: 3px; margin-bottom: 20px; }
  p { line-height: 1.6; color: #dff8ff; margin-bottom: 24px; opacity: .9; }
  .hint { font-size: 13px; color: #8fcbd2; }
  .next-btn { width: 100%; padding: 14px; font-weight: bold; border-color: #35f3ff; color: #35f3ff; font-size: 18px; }
  .actions { display: grid; gap: 12px; }
  .auth-btn { padding: 12px; background: rgba(53, 243, 255, .1); border-color: #35f3ff; color: #35f3ff; }
  .skip-btn { padding: 12px; border-color: rgba(223, 248, 255, .3); color: #dff8ff; }
</style>
