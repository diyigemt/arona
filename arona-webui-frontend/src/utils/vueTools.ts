import { App, createVNode, defineAsyncComponent, render } from "vue";
import { VNodeProps } from "@vue/runtime-core";

export function mountAsyncComponent(
  loader: Parameters<typeof defineAsyncComponent>[0],
  props?: (Record<string, unknown> & VNodeProps) | null,
): HTMLElement {
  const component = defineAsyncComponent(loader);
  const componentVNode = createVNode(component, props);
  componentVNode.appContext = _app._context;
  const container = document.createElement("div");
  render(componentVNode, container);
  return container;
}

let _app: App;
export function setApp(app: App) {
  _app = app;
}
export function useApp() {
  return _app;
}
