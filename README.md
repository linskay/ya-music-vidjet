# YA Music Widget

Cyberpunk desktop overlay for Yandex Music on Windows.

Это не просто кнопки управления. Это нативный desktop-виджет с живым HUD, floating orb, настройками, tray/background режимом, WebSocket-синхронизацией и Tauri-сборкой под `.exe` / installer.

## Что уже есть

- Java 21 + Maven backend.
- Javalin HTTP API.
- Playwright управление Yandex Music в фоне.
- Svelte + Vite frontend.
- Tauri desktop shell.
- WebSocket live state sync.
- Full HUD, Slim Bar, Floating Orb, Source Menu.
- Cyberpunk UI: neon glow, scanline, glitch, animated equalizer, HUD corners, reactive glow.
- Drag & Snap.
- Lock position.
- Settings wizard.
- `config.json`.
- Tray/background режим.
- Tauri packaging skeleton.
- MSI / NSIS installer config.
- Updater endpoint config.
- Adaptive performance: low / normal / aggressive reactive effects.

## Концепт

```text
закрыл окно → приложение не умерло
виджет/музыка продолжают работать
tray открывает UI обратно
Yandex Music живёт в скрытом Playwright-браузере
```

Пользователь видит только лёгкий неоновый overlay, а не тяжёлое официальное приложение.

## Архитектура

```text
Tauri native shell
  ↓
Svelte/Vite UI
  ↓ HTTP + WebSocket
Java 21 + Maven + Javalin backend
  ↓ Playwright
Yandex Music web player
```

## Режимы UI

### Full HUD

Большой киберпанк-плеер:

- обложка;
- трек / артист;
- progress bar;
- play / prev / next;
- like / dislike;
- volume;
- source selector;
- animated glow / scanline / glitch.

### Slim Bar

Компактная длинная панель для постоянного отображения.

### Floating Orb

Минимальный режим: круглая неоновая кнопка, которую можно раскрыть в HUD. В этом режиме тяжёлые эффекты отключаются.

### Source Menu

Панель источников: `Моя волна`, плейлисты, новинки, качество.

## Settings

Wizard настроек содержит:

- автозапуск Windows;
- выбор виджета: HUD / Slim / Orb;
- стартовый источник: `Моя волна`;
- always-on-top;
- desktop-pin;
- floating mode;
- lock position;
- close to tray;
- hide UI;
- start hidden;
- audio reactive effects;
- reactive mode: low / normal / aggressive.

Конфиг сохраняется локально:

```json
{
  "autostart": true,
  "widget": "hud",
  "startSource": "wave",
  "windowMode": "floating",
  "alwaysOnTop": true,
  "desktopPin": false,
  "floating": true,
  "locked": false,
  "closeToTray": true,
  "hideUi": false,
  "startHidden": false,
  "reactiveEffects": true,
  "reactiveMode": "normal",
  "experimentalFftAnalyzer": false
}
```

## Performance

UI не делает тяжёлый системный audio capture по умолчанию.

Сейчас используется лёгкая reactive-модель:

```text
Yandex Music state → progress / playing / volume
  ↓
lightweight energy model
  ↓
CSS variables: --react / --perf
  ↓
glow / glitch / orb / progress / noise
```

Оптимизация:

- `requestAnimationFrame` вместо `setInterval`;
- tick rate зависит от режима;
- adaptive performance снижает эффекты при просадке FPS;
- orb / hidden mode отключают glitch;
- FFT/audio capture оставлен как experimental future task.

## Backend API

```text
GET  /api/config
POST /api/config
GET  /api/state
WS   /ws/state
GET  /api/play
GET  /api/prev
GET  /api/next
GET  /api/like
GET  /api/dislike
GET  /api/wave
GET  /api/volume/{value}
```

## Локальный запуск

Backend:

```bash
mvn clean package
java -jar target/ya-music-vidjet-0.1.0-SNAPSHOT.jar
```

Frontend dev:

```bash
cd frontend
npm install
npm run dev
```

Frontend build:

```bash
cd frontend
npm run build
```

## Desktop build / installer

Требования:

- Java 21;
- Maven;
- Node.js / npm;
- Rust через rustup;
- Tauri CLI.

Tauri CLI:

```bash
cargo install tauri-cli
```

Полная сборка:

```bash
mvn clean package
cd frontend
npm install
npm run build
cd ..
cargo tauri build
```

Ожидаемый результат:

```text
src-tauri/target/release/bundle/
```

Настроены:

- Tauri resource bundle для backend jar;
- MSI target;
- NSIS target;
- updater endpoint skeleton.

## Auto-update

В `src-tauri/tauri.conf.json` заложен updater endpoint:

```text
https://linskay.github.io/ya-music-vidjet/latest.json
```

Для полноценного updater нужно добавить:

- production `latest.json`;
- подпись релиза через Tauri signer;
- публичный ключ updater вместо placeholder;
- загрузку `.msi` / `.exe` из GitHub Releases или Pages.

## Tray / background mode

Поведение:

```text
close window → hide window
backend продолжает работать
Playwright продолжает работать
tray click → открыть UI обратно
Exit → остановить backend и выйти
```

## Project status

### Done

- [x] Java backend.
- [x] Playwright control.
- [x] WebSocket state sync.
- [x] Svelte HUD.
- [x] Cyberpunk UI pack.
- [x] Settings wizard.
- [x] Local config persistence.
- [x] Tray skeleton.
- [x] Tauri shell.
- [x] Backend bundled as Tauri resource.
- [x] Adaptive reactive effects.
- [x] Installer config.
- [x] Updater config skeleton.

### Next

- [ ] Реальные release assets для updater.
- [ ] Настоящий `.ico` / icon pack в `src-tauri/icons`.
- [ ] Полный test build на Windows.
- [ ] Проверка логина Yandex Music при первом запуске.
- [ ] Более точные селекторы Yandex Music при необходимости.
- [ ] Experimental real audio FFT analyzer, если понадобится.

## Important limitation

Yandex Music DOM может меняться. Управление вынесено в `PlayerController` с fallback-селекторами, чтобы чинить изменения быстро и локально.
