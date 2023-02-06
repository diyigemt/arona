<template>
  <div>
    <el-button @click="addBotEndpoint">添加bot节点</el-button>
    <el-button @click="addGroupEndpoint">添加群节点</el-button>
  </div>
  <div style="width: 500px; height: 500px">
    <div ref="containerEl" style="position: relative" class="container">
      <div ref="botGroupEl" style="width: 50%; height: 100%; position: relative">测试1</div>
      <div ref="groupGroupEl" style="width: 100%; height: 100%; position: relative">测试2</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import * as jsPlumbBrowserUI from "@jsplumb/browser-ui";
import { BrowserJsPlumbInstance } from "@jsplumb/browser-ui";
import { EndpointOptions, UIGroup } from "@jsplumb/core";
import { BezierConnector } from "@jsplumb/connector-bezier";

const containerEl = ref<HTMLElement>();
const botGroupEl = ref<HTMLElement>();
const groupGroupEl = ref<HTMLElement>();
let instance: BrowserJsPlumbInstance;
let botGroup: UIGroup<Element>;
let groupGroup: UIGroup<Element>;
function addBotEndpoint() {
  const bot = document.createElement("div");
  bot.classList.add("config-drag-item");
  bot.innerText = "bot123";
  botGroupEl.value!.appendChild(bot);
  instance.addEndpoint(bot, {}, botEndpointConfig);
  instance.addToGroup(botGroup, bot);
}
function addGroupEndpoint() {
  const group = document.createElement("div");
  group.classList.add("config-drag-item");
  group.innerText = "group123";
  groupGroupEl.value!.appendChild(group);
  instance.addEndpoint(group, {}, groupEndpointConfig);
  instance.addToGroup(groupGroup, group);
}
onMounted(() => {
  const _instance = jsPlumbBrowserUI.newInstance({
    container: containerEl.value,
    dragOptions: {
      // @ts-ignore
      containment: "notNegative",
    },
  });
  botGroup = _instance.addGroup({
    el: botGroupEl.value!,
    id: "botGroup",
    constrain: true,
    droppable: false,
  });
  groupGroup = _instance.addGroup({
    el: groupGroupEl.value!,
    id: "groupGroup",
    constrain: true,
    droppable: false,
  });
  _instance.setDraggable(botGroupEl.value!, false);
  _instance.setDraggable(groupGroupEl.value!, false);
  instance = _instance;
});
const botEndpointConfig: EndpointOptions = {
  endpoint: { type: "Dot", options: { radius: 11 } },
  paintStyle: { fill: "#316b31" },
  source: true,
  scope: "green",
  anchor: ["Right", "AutoDefault"],
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
  anchor: ["Left", "AutoDefault"],
  connectorStyle: { stroke: "#316b31", strokeWidth: 6 },
  connector: { type: BezierConnector.type, options: { curviness: 63 } },
  connectorOverlays: [{ type: "Arrow", options: { location: 1 } }],
  maxConnections: 3,
  target: true,
};
</script>

<style scoped lang="scss">
.container {
  height: 100%;
}
</style>
