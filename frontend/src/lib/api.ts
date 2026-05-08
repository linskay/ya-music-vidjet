export const API_BASE = 'http://127.0.0.1:7070';

export async function safeApi(path: string) {
    try {
        const response = await fetch(`${API_BASE}${path}`);
        if (!response.ok) {
            console.error(`API error: ${response.status} ${response.statusText}`);
        }
        return await response.json();
    } catch (e) {
        console.error(`API request failed: ${path}`, e);
        throw e;
    }
}

export async function postApi(path: string, body?: any) {
    try {
        const response = await fetch(`${API_BASE}${path}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: body ? JSON.stringify(body) : undefined
        });
        if (!response.ok) {
            console.error(`API error: ${response.status} ${response.statusText}`);
        }
        return await response.json();
    } catch (e) {
        console.error(`API request failed: ${path}`, e);
        throw e;
    }
}
