export type FftMode = 'low' | 'normal' | 'high';
export type FftBands = { bass: number; mid: number; treble: number; energy: number };

const ZERO: FftBands = { bass: 0, mid: 0, treble: 0, energy: 0 };

export class FftAnalyzer {
  private ctx?: AudioContext;
  private analyser?: AnalyserNode;
  private data?: Uint8Array;
  private source?: MediaElementAudioSourceNode;
  private active = false;

  async start(mode: FftMode = 'low'): Promise<boolean> {
    const audio = document.querySelector('audio') as HTMLAudioElement | null;
    if (!audio) return false;

    this.ctx ??= new AudioContext();
    await this.ctx.resume();

    if (!this.analyser) {
      this.analyser = this.ctx.createAnalyser();
      this.source = this.ctx.createMediaElementSource(audio);
      this.source.connect(this.analyser);
      this.analyser.connect(this.ctx.destination);
    }

    this.analyser.fftSize = mode === 'high' ? 1024 : mode === 'normal' ? 512 : 256;
    this.analyser.smoothingTimeConstant = mode === 'high' ? 0.72 : 0.84;
    this.data = new Uint8Array(this.analyser.frequencyBinCount);
    this.active = true;
    return true;
  }

  stop() {
    this.active = false;
  }

  sample(): FftBands {
    if (!this.active || !this.analyser || !this.data) return ZERO;
    this.analyser.getByteFrequencyData(this.data);
    const avg = (from: number, to: number) => {
      let sum = 0;
      const end = Math.min(this.data!.length, to);
      for (let i = from; i < end; i++) sum += this.data![i];
      return end > from ? sum / (end - from) / 255 : 0;
    };
    const bass = avg(1, 8);
    const mid = avg(8, 34);
    const treble = avg(34, 96);
    return { bass, mid, treble, energy: Math.min(1, bass * 0.5 + mid * 0.35 + treble * 0.15) };
  }
}
