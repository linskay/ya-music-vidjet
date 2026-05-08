<script lang="ts">
  export let message: string;
  export let type: 'error' | 'info' | 'warn' = 'info';
</script>

<div class="status-banner" class:error={type === 'error'} class:warn={type === 'warn'}>
  <div class="status-content">
    <span class="indicator"></span>
    <span class="text">{message}</span>
  </div>
</div>

<style>
  .status-banner { position: absolute; top: -40px; left: 50%; transform: translateX(-50%); background: rgba(4, 12, 18, 0.9); border: 1px solid #35f3ff; padding: 4px 16px; border-radius: 2px; white-space: nowrap; z-index: 100; animation: slideDown 0.3s ease-out forwards; }
  .status-content { display: flex; align-items: center; gap: 8px; }
  .indicator { width: 8px; height: 8px; border-radius: 50%; background: #35f3ff; box-shadow: 0 0 8px #35f3ff; }
  .text { font-size: 11px; letter-spacing: 2px; color: #35f3ff; font-weight: bold; }
  .error { border-color: #ff5d68; }
  .error .indicator { background: #ff5d68; box-shadow: 0 0 8px #ff5d68; }
  .error .text { color: #ff5d68; }
  @keyframes slideDown { from { top: -40px; opacity: 0; } to { top: 10px; opacity: 1; } }
</style>
