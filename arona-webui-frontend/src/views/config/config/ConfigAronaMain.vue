<template>
  <el-row>
    <el-button @click="addBotEndpoint">添加bot节点</el-button>
    <el-button @click="addGroupEndpoint">添加群节点</el-button>
  </el-row>
  <el-row style="height: 70vh; margin-top: 16px">
    <div ref="containerEl" style="position: relative" class="jsp-container">
      <div ref="botGroupEl" style="position: relative"></div>
      <div ref="groupGroupEl" style="position: relative"></div>
    </div>
  </el-row>
</template>

<script setup lang="ts">
import * as jsPlumbBrowserUI from "@jsplumb/browser-ui";
import { BrowserJsPlumbInstance } from "@jsplumb/browser-ui";
import { EndpointOptions, UIGroup } from "@jsplumb/core";
import { BezierConnector } from "@jsplumb/connector-bezier";
import { mountAsyncComponent } from "@/utils/vueTools";
import mainEmitter from "@/views/config/config/event/main";
import useBaseStore from "@/store/base";
import { Friend } from "@/types/contact";
import MiraiApi from "@/api/modules/mirai";

const containerEl = ref<HTMLElement>();
const botGroupEl = ref<HTMLElement>();
const groupGroupEl = ref<HTMLElement>();
const groupList = useBaseStore().groups();
const botList = [] as Friend[];
let instance: BrowserJsPlumbInstance;
let botGroup: UIGroup<Element>;
let groupGroup: UIGroup<Element>;
// 保存drag实例和其对应的群号/机器人q号的关系
const uuidMap = new Map<string, number>();
function addBotEndpoint() {
  const uuid = generateUuid();
  const bot = mountAsyncComponent(() => import("./components/MainConfigDragItem.vue"), {
    uuid,
    type: true,
    bots: botList,
    groups: groupList,
  });
  bot.classList.add("absolute");
  botGroupEl.value!.appendChild(bot);
  const ep = instance.addEndpoint(bot, {}, botEndpointConfig);
  ep.data = {
    type: true,
    uuid,
  };
  instance.addToGroup(botGroup, bot);
}
function addGroupEndpoint() {
  const uuid = generateUuid();
  const group = mountAsyncComponent(() => import("./components/MainConfigDragItem.vue"), {
    uuid,
    type: false,
    bots: botList,
    groups: groupList,
  });
  group.classList.add("absolute");
  groupGroupEl.value!.appendChild(group);
  const ep = instance.addEndpoint(group, {}, groupEndpointConfig);
  ep.data = {
    type: false,
    uuid,
  };
  instance.addToGroup(groupGroup, group);
}
function updateContact(data: { uuid: string; value: number }) {
  uuidMap.set(data.uuid, data.value);
}
onMounted(() => {
  MiraiApi.fetchBotList().then(({ data }) => {
    botList.push(...data);
  });
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
  _instance.bind("connection", function (a, b) {
    console.log("connection");
    console.log(a, b);
    console.log(_instance.getConnections({ scope: ["green"] }));
  });
  _instance.bind("connection:detach", function (a, b) {
    console.log("drag");
    console.log(a, b);
    console.log(_instance.getConnections({ scope: ["green"] }));
  });
  _instance.getConnections();
  instance = _instance;
  mainEmitter.on("contact-update", updateContact);
});
onBeforeUnmount(() => {
  mainEmitter.off("contact-update", updateContact);
});
function generateUuid() {
  return crypto
    .getRandomValues(new Int8Array(10))
    .map((it) => Math.abs(it))
    .join("");
}
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
.jsp-container {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: row;
  gap: 16px;
  > div {
    flex: 1;
  }
}
</style>
