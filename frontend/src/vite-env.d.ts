/// <reference types="vite/client" />
/// <reference types="svelte" />

declare module "*.svelte" {
    import { ComponentType, SvelteComponent } from "svelte";
    const component: ComponentType<SvelteComponent>;
    export default component;
}
