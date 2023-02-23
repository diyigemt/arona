<template>
  <el-config-provider :locale="locale">
    <router-view />
    <div ref="aronaContainer" class="arona-container" />
  </el-config-provider>
</template>

<script setup lang="ts">
import zhCn from "element-plus/lib/locale/lang/zh-cn";
import { AdvancedBloomFilter } from "@pixi/filter-advanced-bloom";
import { Sprite } from "pixi.js";
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
  // SpineManager.createSpine({
  //   el: aronaContainer.value!,
  //   name: "arona",
  //   url: "/spine/arona_spr.skel",
  //   height: 600,
  //   offsetX: 0,
  //   offsetY: 270,
  // }).then((arona) => {
  //   // arona.setAnimation(0, "Idle_01", true);
  //   const mask = Sprite.from("/image/FX_TEX_Arona_Stand.png");
  //   setTimeout(() => {
  //     console.log(arona.spine.height, mask.height);
  //     const scale = (arona.spine.height / 603.3371921592023) * 0.325;
  //     mask.scale.set(scale);
  //     mask.filters = [new AdvancedBloomFilter({ brightness: 1.5, quality: 7, pixelSize: 1 })];
  //     arona.app.stage.addChild(mask);
  //   }, 1000);
  // });
});
</script>

<style lang="scss" scoped>
.arona-container {
  position: absolute;
  top: 100px;
  left: 300px;
  height: 600px;
  width: 300px;
  background-color: black;
  display: none;
}
</style>
