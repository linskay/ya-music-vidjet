#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

mod tray;
mod window;

use std::process::{Child, Command, Stdio};
use std::sync::Mutex;
use tauri::{Manager, State};

struct BackendProcess(Mutex<Option<Child>>);

fn spawn_backend(app: &tauri::AppHandle) -> Option<Child> {
    let path = app.path_resolver().resolve_resource("ya-music-vidjet.jar")?;

    // В будущем здесь можно добавить поиск bundled JRE
    Command::new("java")
        .arg("-jar")
        .arg(path)
        .stdout(Stdio::null())
        .stderr(Stdio::null())
        .spawn()
        .ok()
}

#[tauri::command]
fn exit_app(state: State<BackendProcess>) {
    stop_backend(&state);
    std::process::exit(0);
}

fn stop_backend(state: &BackendProcess) {
    // Graceful shutdown attempt
    let _ = reqwest::blocking::Client::new()
        .post("http://127.0.0.1:7070/api/system/shutdown")
        .timeout(std::time::Duration::from_secs(1))
        .send();

    std::thread::sleep(std::time::Duration::from_millis(500));

    if let Some(mut child) = state.0.lock().unwrap().take() {
        let _ = child.kill();
    }
}

fn main() {
    tauri::Builder::default()
        .plugin(tauri_plugin_updater::Builder::new().build())
        .manage(BackendProcess(Mutex::new(None)))
        .invoke_handler(tauri::generate_handler![exit_app])
        .setup(|app| {
            let main_window = app.get_window("main").unwrap();
            window::setup_window(&main_window);

            let state: State<BackendProcess> = app.state();
            *state.0.lock().unwrap() = spawn_backend(&app.handle());

            tray::setup_tray(app.handle())?;

            Ok(())
        })
        .on_window_event(|window, event| {
            if let tauri::WindowEvent::CloseRequested { api, .. } = event {
                api.prevent_close();
                let _ = window.hide();
            }
        })
        .run(tauri::generate_context!())
        .expect("error while running tauri application");
}
