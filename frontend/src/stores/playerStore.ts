import { writable } from 'svelte/store';

export type PlayerState = {
  title: string;
  artist: string;
  cover: string;
  current: string | number;
  duration: string | number;
  progress: number;
  volume: number;
  playing: boolean;
  liked: boolean;
  disliked?: boolean;
  audioDetected?: boolean;
};

const initialState: PlayerState = {
  title: 'Ожидание трека',
  artist: 'Yandex Music',
  cover: '',
  current: 0,
  duration: 0,
  progress: 0,
  volume: 0.65,
  playing: false,
  liked: false
};

export const playerStore = writable<PlayerState>(initialState);
