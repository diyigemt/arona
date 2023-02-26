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
  <div ref="backgroundContainer" class="background">
    <div
      ref="chatDialogOuter"
      class="chat-dialog-outer"
      :style="{ top: `${chatStyle.position.y}px`, left: `${chatStyle.position.x}px` }"
    >
      <div
        ref="chatDialog"
        class="chat-dialog"
        :class="{ 'chat-dialog-left': chatStyle.type === 'left', 'chat-dialog-right': chatStyle.type === 'right' }"
        :style="{ '--half-height': `${chatDialogHalfHeight}px`, '--arrow-offset': `${chatDialogArrowOffset}px` }"
      >
        {{ chatStyle.chat }}
      </div>
    </div>
  </div>
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
import { sound } from "@pixi/sound";
import gsap from "gsap";
import { Dict } from "@pixi/utils";
import { StandEmitterConfig } from "@/constant/emiterConfig";
import { HomePageAnimationConfigs, HomePageDialogConfig, VoiceConfig } from "@/constant/spine";
import { deepCopy, pickRandomArrayItemAndPutBack, randomArrayItem } from "@/utils";

(window as any).__PIXI_INSPECTOR_GLOBAL_HOOK__ && (window as any).__PIXI_INSPECTOR_GLOBAL_HOOK__.register({ PIXI });
const backgroundContainer = ref<HTMLElement>();
const chatDialogOuter = ref<HTMLElement>();
const chatDialog = ref<HTMLElement>();
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
const chatDialogArrowOffset = 40;
const chatStyle = reactive({
  chat: "",
  type: "right",
  position: {
    x: 0,
    y: 0,
  },
});
const chatDialogHalfHeight = ref<number>(0);
const chatStyleStatic = {
  x: 0,
  y: 0,
};

function initBackground(el: HTMLElement) {
  // const animationConfig = randomArrayItem(HomePageAnimationConfigs);
  const animationConfig = HomePageAnimationConfigs[1];
  const interactionPoint = animationConfig.interaction;
  const disappearMaskPath = animationConfig.mask.path;
  const inVoiceList = animationConfig.voice.in;
  const talkVoiceList = animationConfig.voice.talk;
  const exitVoiceList = animationConfig.voice.exit;
  let workVoiceList = VoiceConfig;
  let randomTalkVoiceList = [...deepCopy(talkVoiceList), ...deepCopy(inVoiceList)];
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
  randomTalkVoiceList.forEach((voice) => {
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
    clearChatDialog();
    randomTalkVoiceIntervalHandler = window.setTimeout(() => {
      const pickResult = pickRandomArrayItemAndPutBack(randomTalkVoiceList);
      randomTalkVoiceList = pickResult.arr;
      const nextVoice = pickResult.item;
      updateChatDialog(nextVoice.textTW);
      sound.play(nextVoice.voice, {
        // complete: playIdleVoice,
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
    updateChatStyleStatic(animationConfig.dialog.x, animationConfig.dialog.y);
    updateChatDialog(randomInVoice.textTW, animationConfig.dialog.type, {
      x: animationConfig.dialog.x,
      y: animationConfig.dialog.y,
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
      updateChatDialog(nextVoice.textTW);
      sound.play(nextVoice.voice, {
        complete() {
          arona.state.setAnimation(AronaFaceTrack, "00", false);
          clearChatDialog();
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
        sound.pause(randomTalkVoiceList[randomTalkVoiceList.length - 1].voice);
      }
      sound.play("exitVoice");
      updateChatDialog(randomExitVoice.textTW);

      setTimeout(() => {
        disappearMask.visible = true;
        sourceParticleContainer.visible = true;
        clearChatDialog();
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
          updateChatStyleStatic(HomePageDialogConfig.x, HomePageDialogConfig.y);
          updateChatDialog("", "left", {
            x: HomePageDialogConfig.x,
            y: HomePageDialogConfig.y,
          });
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
function clearChatDialog() {
  const timeline = gsap.timeline();
  timeline.to(chatDialogOuter.value!, {
    opacity: 0,
    duration: 0.8,
    onComplete() {
      updateChatDialog("");
    },
  });
}
function updateChatDialog(text: string, type?: string, position?: { x: number; y: number }) {
  chatStyle.chat = text;
  if (text) {
    const timeline = gsap.timeline();
    timeline.to(chatDialogOuter.value!, {
      opacity: 1,
      duration: 0.8,
    });
  }
  if (type) {
    chatStyle.type = type;
  }
  nextTick(() => {
    const afterStyle = getComputedStyle(chatDialog.value!, "after");
    const afterHeight = Number(afterStyle.height.replace("px", ""));
    if (position) {
      chatStyle.position.y = position.y * standardRate - (standardRate - 1) * afterHeight;
      chatStyle.position.x = position.x * standardRate + (standardRate - 1) * (chatDialogArrowOffset + afterHeight);
    }
    const style = getComputedStyle(chatDialogOuter.value!);
    chatDialogHalfHeight.value = Number(style.height.replace("px", "")) / 2;
    const width = Number(style.width.replace("px", ""));
    if (chatStyle.type === "right") {
      chatStyle.position.x = chatStyleStatic.x + (standardRate - 1) * (width - chatDialogArrowOffset - afterHeight);
    }
  });
}
function updateChatStyleStatic(x: number, y: number) {
  chatStyleStatic.x = x * standardRate;
  chatStyleStatic.y = y * standardRate;
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
.background {
  position: relative;
  user-select: none;
  width: 800px;
  height: 600px;
  transform: translate(200px, 200px);
  .chat-dialog-outer {
    position: absolute;
    opacity: 0;
  }
  $chat-dialog-color: rgba(255, 255, 255, 0.8);
  .chat-dialog {
    --half-height: 6px;
    --arrow-offset: 40px;
    background: $chat-dialog-color;
    white-space: pre-line;
    text-align: center;
    color: black;
    padding: 0.5em 2em;
    position: relative;
    border-radius: var(--half-height);
    &:after {
      content: "";
      position: absolute;
      border: 0.5em solid transparent;
    }
  }
  $chat-triangle-pos: 40px;
  .chat-dialog-right:after {
    border-bottom-color: $chat-dialog-color;
    border-left-color: $chat-dialog-color;
    right: var(--arrow-offset);
    top: -1em;
  }
  .chat-dialog-left:after {
    border-bottom-color: $chat-dialog-color;
    border-right-color: $chat-dialog-color;
    left: var(--arrow-offset);
    top: -1em;
  }
  .chat-fade-in {
    animation: chat-fade-in 1s;
  }
  .chat-fade-out {
    animation: chat-fade-out 1s;
  }
  @keyframes chat-fade-in {
    0% {
      opacity: 0;
    }
    100% {
      opacity: 1;
    }
  }
  @keyframes chat-fade-out {
    0% {
      opacity: 1;
    }
    100% {
      opacity: 0;
    }
  }
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
