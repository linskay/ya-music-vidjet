import { writable } from 'svelte/store';

export type AppConfig = {
  version?: number;
  autostart: boolean;
  widget: string;
  startSource: string;
  alwaysOnTop: boolean;
  desktopPin: boolean;
  floating: boolean;
  locked: boolean;
  closeToTray: boolean;
  startHidden: boolean;
  reactiveEffects: boolean;
  reactiveMode: string;
  experimentalFftAnalyzer: boolean;
  fftMode: string;
  disableFftInBackground: boolean;
  theme: string;
  reducedMotion: boolean;
  hideUi?: boolean;
  lastPosition?: { x: number; y: number };
};

export const defaultConfig: AppConfig = {
  autostart: false,
  widget: 'hud',
  startSource: 'wave',
  alwaysOnTop: true,
  desktopPin: false,
  floating: true,
  locked: false,
  closeToTray: true,
  startHidden: false,
  reactiveEffects: true,
  reactiveMode: 'normal',
  experimentalFftAnalyzer: false,
  fftMode: 'low',
  disableFftInBackground: true,
  theme: 'neon-cyber',
  reducedMotion: false,
  lastPosition: { x: 120, y: 40 }
};

export const configStore = writable<AppConfig>(defaultConfig);
