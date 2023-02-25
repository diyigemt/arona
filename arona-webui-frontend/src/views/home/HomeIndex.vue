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
import { Application, Container, InteractionEvent, Loader, LoaderResource, Sprite } from "pixi.js";
import * as PIXI from "pixi.js";
import { AdvancedBloomFilter } from "@pixi/filter-advanced-bloom";
import { Spine } from "pixi-spine";
import { Sound, sound } from "@pixi/sound";
import gsap from "gsap";
import { Dict } from "@pixi/utils";
import { StandEmitterConfig } from "@/constant/emiterConfig";
import { HomePageAnimationConfigs, VoiceConfig } from "@/constant/spine";
import { pickRandomArrayItemAndPutBack, randomArrayItem, randomInt } from "@/utils";

(window as any).__PIXI_INSPECTOR_GLOBAL_HOOK__ && (window as any).__PIXI_INSPECTOR_GLOBAL_HOOK__.register({ PIXI });
const backgroundContainer = ref<HTMLElement>();
const backgroundWidth = 800;
const standardWidth = 800;
const standardRate = backgroundWidth / standardWidth;
const backgroundIdleTrack = 1;
const backgroundAnimationTrack = 2;
const backgroundActionTrack1 = 3;
const backgroundActionTrack2 = 4;
const AronaIdleTrack = 1;
const AronaFaceTrack = 2;
const backgroundHeight = backgroundWidth * 0.7;
const backgroundPaddingLeft = 39;
const backgroundPaddingTop = 24;
const backgroundPaddingRight = 129;
const backgroundPaddingBottom = 98;

function initBackground(el: HTMLElement) {
  const animationConfig = randomArrayItem(HomePageAnimationConfigs);
  const interactionPoint = animationConfig.interaction;
  const disappearMaskPath = animationConfig.mask.path;
  const inVoiceList = animationConfig.voice.in;
  const talkVoiceList = animationConfig.voice.talk;
  const exitVoiceList = animationConfig.voice.exit;
  let workVoiceList = VoiceConfig;
  let randomTalkVoiceList = talkVoiceList.map((voice) => voice.voice);
  let randomTalkVoiceIntervalHandler: number;
  const randomInVoice = randomArrayItem(inVoiceList);
  const randomExitVoice = randomArrayItem(exitVoiceList);
  sound.add("inVoice", {
    url: `https://yuuka.diyigemt.com/image/full-extra/output/media/Audio/VOC_JP/JP_Arona/${randomInVoice.voice}.mp3`,
    preload: true,
  });
  sound.add("exitVoice", {
    url: `https://yuuka.diyigemt.com/image/full-extra/output/media/Audio/VOC_JP/JP_Arona/${randomExitVoice.voice}.mp3`,
    preload: true,
  });
  talkVoiceList.forEach((voice) => {
    sound.add(voice.voice, {
      url: `https://yuuka.diyigemt.com/image/full-extra/output/media/Audio/VOC_JP/JP_Arona/${voice.voice}.mp3`,
    });
  });
  workVoiceList.forEach((voice) => {
    sound.add(voice.voice, {
      url: `https://yuuka.diyigemt.com/image/full-extra/output/media/Audio/VOC_JP/JP_Arona/${voice.voice}.mp3`,
    });
  });
  const app = new Application({ width: backgroundWidth, height: backgroundHeight });
  el.appendChild(app.view);
  app.stage.sortableChildren = true;
  app.loader.add("space", "/spine/arona_workpage.skel");
  app.loader.add("disappearMask", disappearMaskPath);
  app.loader.add("arona", "/spine/arona_spr.skel");
  app.loader.add("aronaMask", "/image/FX_TEX_Arona_Stand.png");
  app.stage.interactive = true;
  function playIdleVoice() {
    randomTalkVoiceIntervalHandler = window.setTimeout(() => {
      const pickResult = pickRandomArrayItemAndPutBack(randomTalkVoiceList);
      randomTalkVoiceList = pickResult.arr;
      const nextVoiceName = pickResult.item;
      sound.play(nextVoiceName, {
        complete: playIdleVoice,
      });
    }, 5000);
  }
  app.loader.load((_: Loader, resources: Dict<LoaderResource>) => {
    const backgroundResource = Reflect.get(resources, "space");
    const backgroundSpine = loadSpace(backgroundResource, app.stage);
    backgroundSpine.state.setAnimation(backgroundAnimationTrack, animationConfig.animation.background, true);
    sound.play("inVoice", {
      complete: playIdleVoice,
    });

    const disappearMaskResource = Reflect.get(resources, "disappearMask");
    const disappearMask = loadSleepMask(
      disappearMaskResource,
      app.stage,
      animationConfig.mask.scale,
      animationConfig.mask.offset,
    );

    const aronaResource = Reflect.get(resources, "arona");
    const aronaContainer = new Container();
    aronaContainer.sortableChildren = true;
    aronaContainer.visible = false;
    aronaContainer.interactive = true;
    app.stage.addChild(aronaContainer);

    const arona = loadArona(aronaResource, aronaContainer);
    let workVoiceLock = false;
    function playWorkVoice(cb?: () => void) {
      if (workVoiceLock) {
        return;
      }
      const pickResult = pickRandomArrayItemAndPutBack(workVoiceList);
      workVoiceList = pickResult.arr;
      const nextVoice = pickResult.item;
      workVoiceLock = true;
      sound.play(nextVoice.voice, {
        complete() {
          arona.state.setAnimation(AronaFaceTrack, "00", false);
          setTimeout(() => {
            arona.state.clearTrack(AronaFaceTrack);
          }, 100);
          if (cb) {
            cb();
          }
          setTimeout(() => {
            workVoiceLock = false;
          }, 2500);
        },
      });
      arona.state.setAnimation(AronaFaceTrack, nextVoice.animationName, false);
    }

    const aronaMaskResource = Reflect.get(resources, "aronaMask");
    const aronaMask = loadAronaMask(aronaMaskResource, aronaContainer);

    const sourceParticleContainer = new Container();
    sourceParticleContainer.visible = false;
    sourceParticleContainer.filters = [new AdvancedBloomFilter({ bloomScale: 1.5, brightness: 1.5 })];
    sourceParticleContainer.position.set(
      standardRate * animationConfig.emitter.offset.x,
      standardRate * animationConfig.emitter.offset.y,
    );
    sourceParticleContainer.scale.set(standardRate * animationConfig.emitter.scale);
    app.stage.addChild(sourceParticleContainer);
    const emitter = new Emitter(sourceParticleContainer, animationConfig.emitter.config);
    emitter.autoUpdate = true;

    const destParticleContainer = new Container();
    destParticleContainer.visible = false;
    destParticleContainer.zIndex = 8;
    destParticleContainer.filters = [new AdvancedBloomFilter({ bloomScale: 1.5, brightness: 1.5 })];
    destParticleContainer.position.set(-140, -80);
    aronaContainer.addChild(destParticleContainer);
    const emitter2 = new Emitter(destParticleContainer, StandEmitterConfig);
    emitter2.autoUpdate = true;
    app.stage.on("click", (event: InteractionEvent) => {
      const target = event.data.global;
      const action =
        target.x >= interactionPoint.pa.x * standardRate &&
        target.x <= interactionPoint.pb.x * standardRate &&
        target.y >= interactionPoint.pa.y * standardRate &&
        target.y <= interactionPoint.pb.y * standardRate;
      if (!action) {
        return;
      }
      app.stage.off("click");
      // 停止播放进入音频
      sound.pause("inVoice");
      // 停止播放当前音频
      if (randomTalkVoiceIntervalHandler) {
        clearInterval(randomTalkVoiceIntervalHandler);
        sound.pause(randomTalkVoiceList[randomTalkVoiceList.length - 1]);
      }
      sound.play("exitVoice");

      setTimeout(() => {
        disappearMask.visible = true;
        sourceParticleContainer.visible = true;
        let trigger = true;
        const tl = gsap.timeline();
        emitter.emit = true;
        tl.to(disappearMask, {
          alpha: 0.2,
          duration: 0.8,
          onUpdate: () => {
            if (disappearMask.alpha < 0.5 && trigger) {
              backgroundSpine.state.setAnimation(backgroundAnimationTrack, "Dummy", false);
              backgroundSpine.state.setAnimation(backgroundActionTrack1, "Dummy", false);
              backgroundSpine.state.setAnimation(backgroundActionTrack2, "Dummy", false);
              setTimeout(() => {
                backgroundSpine.state.clearTrack(backgroundAnimationTrack);
                backgroundSpine.state.clearTrack(backgroundActionTrack1);
                backgroundSpine.state.clearTrack(backgroundActionTrack2);
              }, 100);
              trigger = false;
            }
          },
        }).then(() => {
          disappearMask.visible = false;
          aronaContainer.visible = true;
          aronaMask.visible = true;
          arona.visible = true;
          emitter2.emit = true;
          destParticleContainer.visible = true;
          playWorkVoice(() => {
            aronaContainer.on("click", () => {
              playWorkVoice();
            });
          });
          const tl2 = gsap.timeline();
          tl2
            .to(aronaMask, {
              alpha: 0,
              duration: 1,
            })
            .then(() => {
              aronaMask.visible = false;
            });
        });
      }, 600);
      backgroundSpine.state.setAnimation(backgroundActionTrack1, animationConfig.animation.arona[0], false);
      backgroundSpine.state.setAnimation(backgroundActionTrack2, animationConfig.animation.arona[1], false);
    });
  });
}

function loadSleepMask(resource: LoaderResource, app: Container, scale: number, offset: { x: number; y: number }) {
  const sprite = Sprite.from(resource.texture!);
  sprite.filters = [new AdvancedBloomFilter({ bloomScale: 1, brightness: 1 })];
  sprite.scale.set(standardRate * scale);
  sprite.position.set(standardRate * offset.x, standardRate * offset.y);
  sprite.zIndex = 10;
  app.addChild(sprite);
  sprite.visible = false;
  return sprite;
}
function loadSpace(resource: LoaderResource, app: Container) {
  const backgroundSpine = new Spine(resource.spineData!);
  const sourceWidth = backgroundSpine.width;
  const sourceHeight = backgroundSpine.height;
  const actualWidth = sourceWidth - backgroundPaddingLeft - backgroundPaddingRight;
  const scale = backgroundWidth / actualWidth;
  backgroundSpine.pivot.set(-0.4777 * sourceWidth, -0.882 * sourceHeight);
  backgroundSpine.scale.set(scale);
  backgroundSpine.state.setAnimation(backgroundIdleTrack, "Idle_background_00", true);
  backgroundSpine.zIndex = 0;
  app.addChild(backgroundSpine);
  return backgroundSpine;
}
function loadArona(resource: LoaderResource, container: Container) {
  const arona = new Spine(resource.spineData!);
  arona.pivot.set(-0.5 * arona.width, -0.63 * arona.height);
  arona.zIndex = 5;
  container.scale.set(standardRate * 0.35);
  container.position.set(standardRate * 50, standardRate * 120);
  arona.state.setAnimation(AronaIdleTrack, "Idle_01", true);
  container.addChild(arona);
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
  initBackground(backgroundContainer.value!);
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
