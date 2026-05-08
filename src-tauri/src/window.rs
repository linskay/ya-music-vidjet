use tauri::{Manager, Runtime, Window};

pub fn setup_window<R: Runtime>(window: &Window<R>) {
    let _ = window.set_always_on_top(true);
    // Дополнительная настройка окна может быть здесь
}

pub fn show_window<R: Runtime>(app: &tauri::AppHandle<R>) {
    if let Some(window) = app.get_window("main") {
        let _ = window.show();
        let _ = window.set_focus();
    }
}
