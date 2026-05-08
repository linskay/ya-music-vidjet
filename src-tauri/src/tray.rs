use tauri::{
    menu::{Menu, MenuItem},
    tray::{TrayIconBuilder, TrayIcon},
    Manager, Runtime,
};
use crate::window;

pub fn setup_tray<R: Runtime>(app: &tauri::AppHandle<R>) -> tauri::Result<TrayIcon<R>> {
    let play = MenuItem::with_id(app, "play", "Play / Pause", true, None::<&str>)?;
    let next = MenuItem::with_id(app, "next", "Next", true, None::<&str>)?;
    let prev = MenuItem::with_id(app, "prev", "Prev", true, None::<&str>)?;
    let settings = MenuItem::with_id(app, "settings", "Settings", true, None::<&str>)?;
    let restart_core = MenuItem::with_id(app, "restart_core", "Restart Core", true, None::<&str>)?;
    let quit = MenuItem::with_id(app, "quit", "Exit", true, None::<&str>)?;

    let menu = Menu::with_items(app, &[
        &play, &next, &prev,
        &MenuItem::separator(app)?,
        &settings, &restart_core,
        &MenuItem::separator(app)?,
        &quit
    ])?;

    let tray = TrayIconBuilder::with_id("main-tray")
        .icon(app.default_window_icon().unwrap().clone())
        .menu(&menu)
        .tooltip("YA Music Widget")
        .on_menu_event(move |app, event| {
            match event.id.as_ref() {
                "play" => call_api("/api/player/play-pause"),
                "next" => call_api("/api/player/next"),
                "prev" => call_api("/api/player/previous"),
                "settings" => {
                    // Можно реализовать команду для открытия настроек во фронтенде
                },
                "restart_core" => {
                   // Логика перезапуска
                },
                "quit" => app.exit(0),
                _ => {}
            }
        })
        .on_tray_icon_event(|tray, event| {
            if let tauri::tray::TrayIconEvent::Click { .. } = event {
                let app = tray.app_handle();
                window::show_window(app);
            }
        })
        .build(app)?;

    Ok(tray)
}

fn call_api(path: &str) {
    let url = format!("http://127.0.0.1:7070{}", path);
    std::thread::spawn(move || {
        let _ = reqwest::blocking::Client::new().post(url).send();
    });
}
