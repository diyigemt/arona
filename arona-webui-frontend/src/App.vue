<template>
  <el-config-provider :locale="locale">
    <router-view />
    <div ref="aronaContainer" class="arona-container" />
  </el-config-provider>
</template>

<script setup lang="ts">
import zhCn from "element-plus/lib/locale/lang/zh-cn";
import SpineManager, { SpineInstanceReactConfig } from "@/utils/spine";

const locale = zhCn;
const aronaContainer = ref<HTMLElement>();
const aronaConfig: SpineInstanceReactConfig[] = [
  {
    name: "",
    url: "",
    text: "",
    animation: "",
  },
];
onMounted(() => {
  SpineManager.createSpine({
    el: aronaContainer.value!,
    name: "arona",
    url: "/spine/arona_spr.skel",
    height: 200,
    offsetX: 0,
    offsetY: 270,
  }).then((arona) => {
    arona.setAnimation(0, "Idle_01", true);
  });
});
</script>

<style lang="scss" scoped>
.arona-container {
  position: absolute;
  top: 100px;
  height: 200px;
  width: 100px;
  background-color: black;
}
</style>
