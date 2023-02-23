<template>
  <!--  <VideoBackground :src="src" poster="/image/arona_home_poster.jpg" style="height: 100vh">-->
  <div class="menu">
    <div class="filter">
      <el-space class="menu-content" wrap>
        <ShadowCard class="button-card kivotos" role="button" tabindex="0" @click="routerJump('/')">
          <div>基沃托斯</div>
        </ShadowCard>
        <ShadowCard class="button-card arona" role="button" tabindex="1" @click="routerJump('/config')">
          <div>设置</div>
        </ShadowCard>
        <ShadowCard class="button-card" style="opacity: 0; user-select: none; pointer-events: none">
          <div>管理</div>
        </ShadowCard>
        <ShadowCard class="button-card setting" role="button" tabindex="2" @click="routerJump('/admin')">
          <div>管理</div>
        </ShadowCard>
      </el-space>
    </div>
  </div>
  <div ref="backgroundContainer" class="emitter" />
  <!--  </VideoBackground>-->
</template>

<script setup lang="ts">
// @ts-ignore
import VideoBackground from "vue-responsive-video-background-player";
import { Emitter } from "@pixi/particle-emitter";
import { Application, Container, Texture } from "pixi.js";
import * as PIXI from "pixi.js";
import { AdvancedBloomFilter } from "@pixi/filter-advanced-bloom";

(window as any).__PIXI_INSPECTOR_GLOBAL_HOOK__ && (window as any).__PIXI_INSPECTOR_GLOBAL_HOOK__.register({ PIXI });
const backgroundContainer = ref<HTMLElement>();
const app = new Application({ width: 800, height: 600 });
const container = new Container();
container.filters = [new AdvancedBloomFilter({ bloomScale: 1.5, brightness: 1.5 })];
container.position.set(-100, -350);
container.scale.set(0.5);
app.stage.addChild(container);
const emitter = new Emitter(container, {
  lifetime: {
    min: 2,
    max: 2,
  },
  frequency: 0.01,
  spawnChance: 1,
  particlesPerWave: 1,
  emitterLifetime: -1,
  maxParticles: 100,
  pos: {
    x: 0,
    y: 0,
  },
  addAtBack: false,
  behaviors: [
    {
      type: "scale",
      config: {
        scale: {
          list: [
            {
              value: 0.15,
              time: 0,
            },
            {
              value: 0.05,
              time: 1,
            },
          ],
        },
      },
    },
    {
      type: "colorStatic",
      config: {
        color: "#3fcbff",
      },
    },
    {
      type: "alpha",
      config: {
        alpha: {
          list: [
            { value: 1, time: 0 },
            { value: 0.2, time: 0.25 },
            { value: 1, time: 0.5 },
            { value: 0.2, time: 0.75 },
            { value: 1, time: 1 },
          ],
        },
      },
    },
    {
      type: "moveSpeedStatic",
      config: {
        min: 30,
        max: 30,
      },
    },
    {
      type: "rotationStatic",
      config: {
        min: 0,
        max: 360,
      },
    },
    {
      type: "spawnShape",
      config: {
        type: "polygonalChain",
        data: [
          [
            {
              x: 466,
              y: 1274,
            },
            {
              x: 497,
              y: 994,
            },
          ],
          [
            {
              x: 743,
              y: 1016,
            },
            {
              x: 864,
              y: 1157,
            },
          ],
          [
            {
              x: 601,
              y: 1246,
            },
            {
              x: 845,
              y: 1246,
            },
          ],
          [
            {
              x: 406,
              y: 1524,
            },
            {
              x: 651,
              y: 1524,
            },
          ],
          [
            {
              x: 745,
              y: 1534,
            },
            {
              x: 789,
              y: 1773,
            },
          ],
        ],
      },
    },
    {
      type: "textureSingle",
      config: {
        texture: Texture.from("/image/FX_TEX_Triangle_02_a.png"),
      },
    },
  ],
});
emitter.autoUpdate = true;
emitter.emit = true;
const srcList = ["desk", "sleep", "look"].map((it) => `/video/arona_${it}.mp4`);
const srcIndex = Math.floor(Math.random() * srcList.length);
const src = srcList[srcIndex];
const router = useRouter();
function routerJump(path: string) {
  router.push(path);
}
onMounted(() => {
  backgroundContainer.value!.appendChild(app.view);
});
</script>

<style lang="scss" scoped>
* {
  font-family: "GameFont", serif;
  font-size: 1.2rem;
}
.emitter {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 800px;
  height: 600px;
}
$button-card-size: 200px;
.menu {
  position: absolute;
  top: 20%;
  right: 10%;
  transform: skewX(-10deg);
  .filter {
    position: relative;
    overflow: hidden;
    .menu-content {
      max-width: calc(2 * #{$button-card-size} + 2 * 8px);
      position: relative;
      z-index: 1;
    }
  }
}
.button-card {
  cursor: pointer;
  width: $button-card-size;
  height: $button-card-size;
  justify-content: center;
  border-radius: 6px;
  background: url("/image/poli-light.png") rgba(164, 216, 237, 0.3) no-repeat 0 40%;
}
.kivotos {
  background: linear-gradient(
      340deg,
      rgba(240, 240, 240, 0.1) 0%,
      rgba(240, 240, 240, 0.1) 30%,
      rgba(240, 240, 240, 1) 70%
    ),
    url("/image/poli-light.png"), url("/image/BG_View_Kivotos.jpg");
  background-repeat: no-repeat;
  background-position: 100% 100%, 20% 20%, 35% 75%;
  background-size: auto, auto, 300% 300%;
}
.setting {
  background: linear-gradient(340deg, rgba(240, 240, 240, 0.1) 0%, rgba(240, 240, 240, 0.1) 30%, rgb(240, 240, 240) 70%),
    url("/image/poli-light.png"), url("/image/BG_CS_PR_05.jpg");
  background-repeat: no-repeat;
  background-position: 100% 100%, 20% 20%, 50%;
  background-size: auto, auto, 200% 150%;
}
.arona {
  background: linear-gradient(340deg, rgba(240, 240, 240, 0.1) 0%, rgba(240, 240, 240, 0.1) 30%, rgb(240, 240, 240) 70%),
    url("/image/poli-light.png"), url("/image/BG_CS_PR_03.jpg");
  background-repeat: no-repeat;
  background-position: 100% 100%, 20% 20%, 50%;
  background-size: auto, auto, 200% 200%;
}
</style>
