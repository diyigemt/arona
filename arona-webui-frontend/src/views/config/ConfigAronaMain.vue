<template>
  <div>
    <el-button @click="addBotEndpoint">添加bot节点</el-button>
    <el-button @click="addGroupEndpoint">添加群节点</el-button>
  </div>
  <div style="width: 500px; height: 500px">
    <div ref="container" style="position: relative"></div>
  </div>
</template>

<script setup lang="ts">
import * as jsPlumbBrowserUI from "@jsplumb/browser-ui";
import { BrowserJsPlumbInstance } from "@jsplumb/browser-ui";
import { EndpointOptions } from "@jsplumb/core";
import { BezierConnector } from "@jsplumb/connector-bezier";

const container = ref<HTMLElement>();
const instance = ref<BrowserJsPlumbInstance>();
const form = reactive({});
function addBotEndpoint() {
  const bot = document.createElement("div");
  bot.classList.add("config-drag-item");
  bot.innerText = "bot123";
  container.value!.appendChild(bot);
  instance.value!.addEndpoint(bot, {}, botEndpointConfig);
}
function addGroupEndpoint() {
  const group = document.createElement("div");
  group.classList.add("config-drag-item");
  group.innerText = "group123";
  container.value!.appendChild(group);
  instance.value!.addEndpoint(group, {}, groupEndpointConfig);
}
onMounted(() => {
  instance.value = jsPlumbBrowserUI.newInstance({
    container: container.value,
  });
});
const botEndpointConfig: EndpointOptions = {
  endpoint: { type: "Dot", options: { radius: 11 } },
  paintStyle: { fill: "#316b31" },
  source: true,
  scope: "green",
  anchor: "AutoDefault",
  connectorStyle: { stroke: "#316b31", strokeWidth: 6 },
  connector: { type: "Bezier", options: { curviness: 63 } },
  connectorOverlays: [{ type: "Arrow", options: { location: 1 } }],
  maxConnections: 3,
  target: false,
};
const groupEndpointConfig: EndpointOptions = {
  endpoint: { type: "Dot", options: { radius: 11 } },
  paintStyle: { fill: "#316b31" },
  source: false,
  scope: "green",
  anchor: "AutoDefault",
  connectorStyle: { stroke: "#316b31", strokeWidth: 6 },
  connector: { type: BezierConnector.type, options: { curviness: 63 } },
  connectorOverlays: [{ type: "Arrow", options: { location: 1 } }],
  maxConnections: 3,
  target: true,
};
</script>

<style scoped lang="scss">
.drag-item {
}
</style>
