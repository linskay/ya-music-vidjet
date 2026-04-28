# Task: real audio-reactive UI

Goal: replace the current fake reactive glow with a real audio-driven signal.

## Target behavior

- Web Audio API reads actual `<audio>` output from the hidden Yandex Music page when technically available.
- Backend exposes normalized audio energy level through `/api/state` or a separate `/ws/visualizer` channel.
- HUD uses the level for:
  - frame glow strength;
  - equalizer height;
  - glitch intensity;
  - orb pulse;
  - progress bar flare.

## Implementation options

### Option A: frontend-side analyzer

Works only if the audio element is available in the visible frontend context.

```ts
const ctx = new AudioContext();
const analyser = ctx.createAnalyser();
const source = ctx.createMediaElementSource(audio);
source.connect(analyser);
analyser.connect(ctx.destination);
```

### Option B: backend-side approximation

Use Playwright to evaluate audio state and generate a stable visual energy value from current time, playing state, and track progress. This is less accurate but safe and cross-context.

### Option C: native/system audio capture

Future advanced mode. Requires native code and Windows-specific audio session capture.

## Acceptance criteria

- UI reacts differently during pause and playback.
- Equalizer becomes driven by energy value, not only CSS animation.
- User can disable audio-reactive effects from settings.
