import { API_BASE } from './api';

export const WS_BASE = API_BASE.replace(/^http/, 'ws');

export function createWebSocket(onMessage: (data: any) => void, onStatusChange: (connected: boolean) => void) {
    let ws: WebSocket | undefined;
    let reconnectTimer: number | undefined;

    function connect() {
        ws = new WebSocket(`${WS_BASE}/ws/state`);

        ws.onopen = () => {
            onStatusChange(true);
        };

        ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data);
                onMessage(data);
            } catch (e) {
                console.error('Failed to parse WS message', e);
            }
        };

        ws.onclose = () => {
            onStatusChange(false);
            reconnectTimer = window.setTimeout(connect, 1500);
        };

        ws.onerror = () => {
            ws?.close();
        };
    }

    connect();

    return {
        close: () => {
            if (reconnectTimer) window.clearTimeout(reconnectTimer);
            ws?.close();
        }
    };
}
