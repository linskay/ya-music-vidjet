<script>
  let mode = 'compact';

  const api = (path) => fetch(path);
</script>

<div class="root" class:compact={mode==='compact'} class:hud={mode==='hud'}>
  <div class="panel">
    <div class="top">
      <button on:click={()=>mode='compact'}>C</button>
      <button on:click={()=>mode='hud'}>H</button>
    </div>

    <div class="controls">
      <button on:click={()=>api('/api/play')}>▶</button>
      <button on:click={()=>api('/api/next')}>⏭</button>
      <button on:click={()=>api('/api/like')}>❤</button>
    </div>
  </div>
</div>

<style>
.root {
  position: fixed;
  top: 20px;
  right: 20px;
}
.panel {
  padding: 12px;
  border-radius: 12px;
  background: rgba(10,10,20,0.7);
  backdrop-filter: blur(12px);
  box-shadow: 0 0 20px #00ffff55;
}
button {
  margin: 4px;
  border: 1px solid #00ffff;
  background: transparent;
  color: #00ffff;
}
button:hover {
  box-shadow: 0 0 10px #00ffff;
}
.hud .panel {
  box-shadow: 0 0 40px #ff00ff;
}
</style>
