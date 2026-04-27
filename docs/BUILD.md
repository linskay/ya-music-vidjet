# Build and packaging

## Local backend

```bash
mvn clean package
java -jar target/ya-music-vidjet-0.1.0-SNAPSHOT.jar
```

## Frontend

```bash
cd frontend
npm install
npm run dev
npm run build
```

## Tauri desktop build

Requirements:

- Java 21
- Maven
- Node.js / npm
- Rust via rustup
- Tauri CLI

Install Tauri CLI:

```bash
cargo install tauri-cli
```

Build frontend:

```bash
cd frontend
npm install
npm run build
cd ..
```

Build desktop app:

```bash
cargo tauri build
```

Expected output:

```text
src-tauri/target/release/bundle/
```

## Headless / tray mode plan

Goal:

```text
close window -> backend keeps running -> Playwright keeps running -> music keeps playing -> UI opens from tray
```

Already added:

- `TrayService` with `Open UI` and `Exit` menu items.
- `StartupService` for Windows Startup folder autostart.
- `AppConfig`, `ConfigService`, and `config.json`.
- Settings flags: `closeToTray`, `hideUi`, `startHidden`.

Remaining implementation:

- Hide Tauri window instead of terminating the app.
- Start Java backend as a child process from Tauri.
- Stop backend cleanly from tray Exit.
- Add final tray icon asset.
