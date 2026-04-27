#![cfg_attr(not(debug_assertions), windows_subsystem = "windows")]

use std::process::{Child, Command, Stdio};
use std::sync::Mutex;
use tauri::{Manager, State};

struct BackendProcess(Mutex<Option<Child>>);

fn spawn_backend(app: &tauri::AppHandle) -> Option<Child> {
    let path = app.path_resolver().resolve_resource("ya-music-vidjet-0.1.0-SNAPSHOT.jar")?;

    Command::new("java")
        .arg("-jar")
        .arg(path)
        .stdout(Stdio::null())
        .stderr(Stdio::null())
        .spawn()
        .ok()
}

#[tauri::command]
fn exit_app(app: tauri::AppHandle, state: State<BackendProcess>) {
    if let Some(mut child) = state.0.lock().unwrap().take() {
        let _ = child.kill();
    }
    std::process::exit(0);
}

fn main() {
    tauri::Builder::default()
        .manage(BackendProcess(Mutex::new(None)))
        .invoke_handler(tauri::generate_handler![exit_app])
        .setup(|app| {
            let window = app.get_window("main").unwrap();
            window.set_always_on_top(true).unwrap();

            let state: State<BackendProcess> = app.state();
            *state.0.lock().unwrap() = spawn_backend(&app.handle());

            let app_handle = app.handle().clone();
            tauri::tray::TrayIconBuilder::new()
                .tooltip("YA Music Widget")
                .on_tray_icon_event(move |_tray, _| {
                    if let Some(window) = app_handle.get_window("main") {
                        let _ = window.show();
                    }
                })
                .build(app)?;

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
