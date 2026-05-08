import { writable } from 'svelte/store';

export type AppStatus = {
  booting: boolean;
  syncError: boolean;
  authorized: boolean;
  coreStatus: string;
  audioDetected: boolean;
};

export const appStatusStore = writable<AppStatus>({
  booting: true,
  syncError: false,
  authorized: false,
  coreStatus: 'starting',
  audioDetected: false
});
