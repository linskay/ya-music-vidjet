export function getReactiveLevel(progress:number, playing:boolean){
  if(!playing) return 0.2;
  const base = Math.sin(Date.now()/220) * 0.5 + 0.5;
  return Math.min(1, base * (0.6 + progress));
}
