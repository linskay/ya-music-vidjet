export function formatTime(value: string | number) {
    if (typeof value === 'string' && value.includes(':')) return value;
    const seconds = Math.max(0, Number(value) || 0);
    const min = Math.floor(seconds / 60);
    const sec = Math.floor(seconds % 60).toString().padStart(2, '0');
    return `${min}:${sec}`;
}
