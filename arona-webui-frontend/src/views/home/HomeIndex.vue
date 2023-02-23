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
import { Application, Container, LoaderResource, Sprite, Texture } from "pixi.js";
import * as PIXI from "pixi.js";
import { AdvancedBloomFilter } from "@pixi/filter-advanced-bloom";
import { Spine } from "pixi-spine";
import gsap from "gsap";
import { SleepEmitterConfig, StandEmitterConfig } from "@/constant/emiterConfig";

(window as any).__PIXI_INSPECTOR_GLOBAL_HOOK__ && (window as any).__PIXI_INSPECTOR_GLOBAL_HOOK__.register({ PIXI });
const backgroundContainer = ref<HTMLElement>();
const backgroundWidth = 800;
const standardWidth = 800;
const standardRate = backgroundWidth / standardWidth;
const backgroundHeight = backgroundWidth * 0.7;
const backgroundPaddingLeft = 39;
const backgroundPaddingTop = 24;
const backgroundPaddingRight = 129;
const backgroundPaddingBottom = 98;
const app = new Application({ width: backgroundWidth, height: backgroundHeight });
app.stage.sortableChildren = true;
app.loader.add("space", "/spine/arona_workpage.skel");
app.loader.add("sleepMask", "/image/FX_TEX_Arona_C.png");
app.loader.add("arona", "/spine/arona_spr.skel");
app.loader.add("aronaMask", "/image/FX_TEX_Arona_Stand.png");
app.loader.load((_, resources) => {
  const spaceResource = Reflect.get(resources, "space");
  const space = loadSpace(spaceResource);
  const sleepMaskResource = Reflect.get(resources, "sleepMask");
  const sleepMask = loadSleepMask(sleepMaskResource);
  const aronaResource = Reflect.get(resources, "arona");
  const aronaContainer = new Container();
  aronaContainer.sortableChildren = true;
  const arona = loadArona(aronaResource, aronaContainer);
  app.stage.addChild(aronaContainer);
  const aronaMaskResource = Reflect.get(resources, "aronaMask");
  const aronaMask = loadAronaMask(aronaMaskResource, aronaContainer);

  const container = new Container();
  container.visible = false;
  container.filters = [new AdvancedBloomFilter({ bloomScale: 1.5, brightness: 1.5 })];
  container.position.set(standardRate * -15, standardRate * -30);
  container.scale.set(standardRate * 0.27);
  app.stage.addChild(container);
  const emitter = new Emitter(container, SleepEmitterConfig);
  emitter.autoUpdate = true;

  const container2 = new Container();
  container2.visible = false;
  container2.zIndex = 8;
  container2.filters = [new AdvancedBloomFilter({ bloomScale: 1.5, brightness: 1.5 })];
  container2.scale.set(standardRate * 0.8);
  aronaContainer.addChild(container2);
  const emitter2 = new Emitter(container2, StandEmitterConfig);
  emitter2.autoUpdate = true;

  setTimeout(() => {
    space.state.setAnimation(3, "Idle_02_Touch_A", false);
    space.state.setAnimation(4, "Idle_02_Touch_M", false);
    setTimeout(() => {
      sleepMask.visible = true;
      container.visible = true;
      let trigger = true;
      const tl = gsap.timeline();
      emitter.emit = true;
      tl.to(sleepMask, {
        alpha: 0.2,
        duration: 0.8,
        onUpdate: () => {
          if (sleepMask.alpha < 0.5 && trigger) {
            space.state.setAnimation(2, "Dummy", false);
            space.state.setAnimation(3, "Dummy", false);
            space.state.setAnimation(4, "Dummy", false);
            setTimeout(() => {
              space.state.clearTrack(3);
              space.state.clearTrack(4);
              space.state.clearTrack(2);
            }, 100);
            trigger = false;
          }
        },
      }).then(() => {
        sleepMask.visible = false;
        aronaMask.visible = true;
        arona.visible = true;
        emitter2.emit = true;
        container2.visible = true;
        const tl2 = gsap.timeline();
        tl2
          .to(aronaMask, {
            alpha: 0.2,
            duration: 1,
          })
          .then(() => {
            aronaMask.visible = false;
          });
      });
    }, 500);
  }, 2000);
});
function loadSleepMask(resource: LoaderResource) {
  const sprite = Sprite.from(resource.texture!);
  sprite.filters = [new AdvancedBloomFilter({ bloomScale: 1, brightness: 1 })];
  sprite.scale.set(standardRate * 1.3);
  sprite.position.set(standardRate * 87, standardRate * 115);
  sprite.zIndex = 10;
  app.stage.addChild(sprite);
  sprite.visible = false;
  return sprite;
}
function loadSpace(resource: LoaderResource) {
  const backgroundSpine = new Spine(resource.spineData!);
  const sourceWidth = backgroundSpine.width;
  const sourceHeight = backgroundSpine.height;
  const actualWidth = sourceWidth - backgroundPaddingLeft - backgroundPaddingRight;
  const actualHeight = sourceHeight - backgroundPaddingTop - backgroundPaddingBottom;
  const scale = backgroundWidth / actualWidth;
  backgroundSpine.pivot.set(-0.4777 * sourceWidth, -0.882 * sourceHeight);
  backgroundSpine.scale.set(scale);
  backgroundSpine.state.setAnimation(1, "Idle_background_00", true);
  backgroundSpine.state.setAnimation(2, "Idle_02", true);
  backgroundSpine.zIndex = 0;
  app.stage.addChild(backgroundSpine);
  return backgroundSpine;
}
function loadArona(resource: LoaderResource, container: Container) {
  const arona = new Spine(resource.spineData!);
  arona.pivot.set(-0.5 * arona.width, -0.63 * arona.height);
  arona.zIndex = 5;
  container.scale.set(standardRate * 0.35);
  container.position.set(standardRate * 50, standardRate * 120);
  arona.state.setAnimation(1, "Idle_01", true);
  container.addChild(arona);
  arona.visible = false;
  return arona;
}
function loadAronaMask(resource: LoaderResource, container: Container) {
  const sprite = Sprite.from(resource.texture!);
  sprite.filters = [new AdvancedBloomFilter({ bloomScale: 1, brightness: 1 })];
  sprite.scale.set(1.15);
  sprite.zIndex = 10;
  container.addChild(sprite);
  sprite.visible = false;
  return sprite;
}
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
