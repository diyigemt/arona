<template>
  <div ref="backgroundContainer" class="background">
    <div
      ref="chatDialogOuter"
      class="chat-dialog-outer"
      :style="{
        top: `${chatStyle.position.y}px`,
        left: `${chatStyle.position.x}px`,
        transform: `translateY(${-middleOffset}px)`,
      }"
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
</template>

<script setup lang="ts">
import { Emitter } from "@pixi/particle-emitter";
import { Application, Container, DisplayObject, InteractionEvent, Loader, LoaderResource, Sprite } from "pixi.js";
import { AdvancedBloomFilter } from "@pixi/filter-advanced-bloom";
import { ColorOverlayFilter } from "@pixi/filter-color-overlay";
import { Spine } from "pixi-spine";
import { sound, filters } from "@pixi/sound";
import gsap from "gsap";
import { Dict } from "@pixi/utils";
import { Ref } from "vue";
import { AronaEmitterConfig, PlanaEmitterConfig } from "@/constant/emiterConfig";
import {
  EmitterConfig,
  PlanaPageAnimationConfig,
  Point,
  VoiceConfig,
  VoiceGroup,
  WaifuAppearConfig,
  WorkConfig,
} from "@/constant/spine";
import { deepCopy, pickRandomArrayItemAndPutBack, randomArrayItem, randomInt } from "@/utils";

let mainApp: Application;
const backgroundContainer = ref<HTMLElement>();
const chatDialogOuter = ref<HTMLElement>();
const chatDialog = ref<HTMLElement>();
let backgroundWidth = 800;
const superResolution = 2.5;
const superResolutionBack = 1 / superResolution;
backgroundWidth *= superResolution;
const standardWidth = 800;
let standardRate = backgroundWidth / standardWidth / superResolution;
const backgroundIdleTrack = 1;
const backgroundAnimationTrack = 2;
const AronaIdleTrack = 1;
const AronaFaceTrack = 2;
let backgroundHeight = backgroundWidth * 0.7;
let viewHeight = 0;
let middleOffset = 0;
const backgroundPaddingLeft = 39;
const backgroundPaddingTop = 24;
const backgroundPaddingRight = 129;
const backgroundPaddingBottom = 98;
const chatDialogArrowOffset = 40;
const randomTalkVoiceIntervalHandler: { timeout: number; stop?: () => void } = {
  timeout: 0,
  stop: undefined,
};
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
const VoiceResourceUrl = "https://yuuka.cdn.diyigemt.com/image/home_page/voice/";
const SpineResourceUrl = "https://yuuka.cdn.diyigemt.com/image/home_page/spine/";
const ImageResourceUrl = "https://yuuka.cdn.diyigemt.com/image/home_page/image/";
const PlanaHome = ["UIWorkPlanaSit",
  "UIWorkPlanaCabinet",
  "UIWorkPlanaUmbrella",
  "UIWorkCoexist_PlanaWatchSky",
  "UIWorkCoexist_PlanaSitPeek"];

function initBackground(el: HTMLElement) {
  const backgroundStyle = getComputedStyle(el);
  backgroundWidth = styleToPxNumber(backgroundStyle.width);
  backgroundWidth *= superResolution;
  viewHeight = styleToPxNumber(backgroundStyle.height);
  backgroundHeight = backgroundWidth * 0.7;
  standardRate = backgroundWidth / standardWidth;
  middleOffset = (backgroundHeight - viewHeight) / 2;
  const animationConfig = randomArrayItem(PlanaPageAnimationConfig);
  const appearArona = PlanaHome.indexOf(animationConfig.name) == -1;
  // const animationConfig = PlanaPageAnimationConfig[8];
  const interactionPoint = animationConfig.interaction;
  const disappearMaskPaths = animationConfig.masks.map((it) => it.path);
  const inVoiceList = animationConfig.voice.in;
  const talkVoiceList = animationConfig.voice.talk;
  const exitVoiceList = animationConfig.voice.exit;
  const aronaWorkVoiceList = WorkConfig.arona;
  const planaWorkVoiceList = WorkConfig.plana;
  let randomTalkVoiceList = [...deepCopy(talkVoiceList), ...deepCopy(inVoiceList)];
  const randomInVoice = randomArrayItem(inVoiceList);
  const randomExitVoice = randomArrayItem(exitVoiceList);
  randomInVoice.forEach((voice) => {
    sound.add(voice.voice, {
      url: `${ VoiceResourceUrl }${ voice.voice }.mp3`,
      preload: true,
    });
  });
  randomExitVoice.forEach((voice) => {
    sound.add(voice.voice, {
      url: `${ VoiceResourceUrl }${ voice.voice }.mp3`,
    });
  });
  sound.add("mute", {
    url: `${ VoiceResourceUrl }Mute.mp3`,
    preload: true,
  });
  sound.add("sensei-arona", {
    url: `${ VoiceResourceUrl }Arona_Default_TTS.mp3`,
  });
  sound.add("sensei-plana", {
    url: `${ VoiceResourceUrl }NP0035_Default_TTS.mp3`,
  });
  randomTalkVoiceList.flat().forEach((voice) => {
    sound.add(voice.voice, {
      url: `${ VoiceResourceUrl }${ voice.voice }.mp3`,
    });
  });
  aronaWorkVoiceList.forEach((voice) => {
    voice.voice.forEach((name) => {
      const fName = name.indexOf("CALLNAME") !== -1 ? "Arona_Default_TTS" : name;
      sound.add(fName, {
        url: `${ VoiceResourceUrl }${ fName }.mp3`,
      });
    });
  });
  planaWorkVoiceList.forEach((voice) => {
    voice.voice.forEach((name) => {
      const fName = name.indexOf("CALLNAME") !== -1 ? "NP0035_Default_TTS" : name;
      sound.add(fName, {
        url: `${ VoiceResourceUrl }${ fName }.mp3`,
      });
    });
  });
  const app = new Application({width: backgroundWidth, height: backgroundHeight});
  (window as any).__PIXI_APP__ = app;
  app.view.style.transform = `translateY(${ -middleOffset }px) scale(${ superResolutionBack })`;
  app.view.style.transformOrigin = "0";
  el.appendChild(app.view);
  app.stage.sortableChildren = true;
  // app.loader.add("space", `${SpineResourceUrl}${animationConfig.space}`);
  app.loader.add("space", `${ SpineResourceUrl }${ animationConfig.space }`);
  disappearMaskPaths.forEach((it, index) => {
    app.loader.add(`disappearMask-${ index }`, `${ ImageResourceUrl }${ it }`);
  });
  app.loader.add("arona", `${ SpineResourceUrl }arona_spr.skel`);
  app.loader.add("aronaMask", `${ ImageResourceUrl }FX_TEX_Arona_Stand.png`);
  app.loader.add("plana", `${ SpineResourceUrl }NP0035_spr.skel`);
  app.loader.add("planaMask", `${ ImageResourceUrl }FX_TEX_Plana_Stand.png`);
  app.stage.interactive = true;
  (window as any).__PIXI_APP = app;

  function checkHitBound(target: Point, bound: [Point, Point]) {
    return (
      target.x >= bound[0].x * standardRate &&
      target.x <= bound[1].x * standardRate &&
      target.y >= bound[0].y * standardRate &&
      target.y <= bound[1].y * standardRate
    );
  }

  function playListVoice(list: VoiceGroup, cb?: () => void, stereo = 0, index = 0) {
    let _pause = false;
    sound.play(list[index].voice, {
      complete() {
        if (index + 1 >= list.length) {
          if (cb) {
            cb();
          }
          return;
        }
        if (_pause) {
          return;
        }
        playListVoice(list, cb, stereo, index + 1);
      },
      filters: [new filters.StereoFilter(stereo)],
    });
    return () => {
      _pause = true;
    };
  }

  function stopListVoice(list: VoiceGroup) {
    list.forEach((voice) => sound.stop(voice.voice));
  }

  function playIdleVoice() {
    // clearChatDialog();
    const pickResult = pickRandomArrayItemAndPutBack(randomTalkVoiceList);
    randomTalkVoiceList = pickResult.arr;
    const nextVoice = pickResult.item;
    // updateChatDialog(nextVoice.textTW);
    randomTalkVoiceIntervalHandler.timeout = window.setTimeout(() => {
      randomTalkVoiceIntervalHandler.stop = playListVoice(nextVoice, playIdleVoice);
    }, 10000);
  }

  app.loader.load((_: Loader, resources: Dict<LoaderResource>) => {
    const backgroundResource = Reflect.get(resources, "space");
    const backgroundSpine = loadSpace(backgroundResource, app.stage);
    animationConfig.animation.idle.forEach((animation, index) => {
      backgroundSpine.state.setAnimation(backgroundAnimationTrack + index, animation, true);
    });
    sound.play("mute", {
      complete() {
        // updateChatStyleStatic(animationConfig.dialog.x, animationConfig.dialog.y);
        // updateChatDialog(randomInVoice.textTW, animationConfig.dialog.type, {
        //   x: animationConfig.dialog.x,
        //   y: animationConfig.dialog.y,
        // });
        app.stage.on("pointerdown", handleClick);
        randomTalkVoiceIntervalHandler.stop = playListVoice(randomInVoice, () => {
          playIdleVoice();
        });
      },
    });
    const disappearMasks = loadMask(
      disappearMaskPaths.map((_0, index) => Reflect.get(resources, `disappearMask-${ index }`)),
      app.stage,
      animationConfig.masks,
      animationConfig.masks.map((it) => it.path.indexOf("Arona") !== -1),
    );
    const resourceKey = appearArona ? "arona" : "plana";
    const positionKey = appearArona ? 0 : 1;
    const {waifu, container} = loadWaifuSpine(
      Reflect.get(resources, resourceKey),
      app.stage,
      Reflect.get(WaifuAppearConfig, resourceKey).position[positionKey]
    );
    const AppearEmitterConfig = appearArona ? AronaEmitterConfig : PlanaEmitterConfig;
    const AppearEmitter = loadEmitter(
      container,
      [{scale: 3, offset: {x: 0, y: 0}, config: AppearEmitterConfig}],
      false,
    )[0];
    const MaskKey = appearArona ? "aronaMask" : "planaMask";
    const Mask = loadMask(
      [Reflect.get(resources, MaskKey)],
      container,
      [Reflect.get(WaifuAppearConfig, resourceKey).mask],
      [true],
      false,
    )[0];

    const emitters = loadEmitter(app.stage, animationConfig.emitters);

    const WorkVoiceLock = ref(false);
    const workVoice = appearArona ? aronaWorkVoiceList : planaWorkVoiceList;
    function playWorkVoice(
      list: { AnimationName: string; textJP: string; voice: string[] }[],
      voiceLock: Ref<boolean>,
      spine: Spine,
      cb?: () => void,
      // eslint-disable-next-line @typescript-eslint/no-shadow
      stereo = 0,
    ) {
      if (voiceLock.value) {
        return;
      }
      const pickResult = randomArrayItem(list);
      voiceLock.value = true;
      new Promise<void>((resolve) => {
        const text = pickResult.textJP;
        playListVoice(
          pickResult.voice.map((it) => ({AnimationName: "", textJP: "", voice: it})),
          resolve,
          stereo,
        );
        // updateChatDialog(text);
        spine.state.setAnimation(AronaFaceTrack, pickResult.AnimationName, false);
      }).then(() => {
        spine.state.setAnimation(AronaFaceTrack, "00", false);
        // clearChatDialog();
        setTimeout(() => {
          spine.state.clearTrack(AronaFaceTrack);
        }, 500);
        if (cb) {
          cb();
        }
        setTimeout(() => {
          voiceLock.value = false;
        }, 2500);
      });
    }

    container.addListener("pointerdown", () => {
      playWorkVoice(workVoice, WorkVoiceLock, waifu, undefined, 1);
    });

    // let list: any[] = [];
    function handleClick(event: InteractionEvent) {
      const target = event.data.global;
      // list.push({ x: target.x, y: target.y });
      // if (list.length === 2) {
      //   console.log(JSON.stringify(list));
      //   list = [];
      // }
      if (!interactionPoint.map((it) => checkHitBound(target, it)).reduce((prv, cur) => prv || cur, false)) {
        return;
      }
      app.stage.off("pointerdown");
      // 停止播放进入音频
      stopListVoice(randomInVoice);
      // 停止播放当前音频
      clearInterval(randomTalkVoiceIntervalHandler.timeout);
      if (randomTalkVoiceIntervalHandler.stop) {
        randomTalkVoiceIntervalHandler.stop();
        stopListVoice(randomTalkVoiceList[randomTalkVoiceList.length - 1]);
      }
      playListVoice(randomExitVoice);
      // updateChatDialog(randomExitVoice.textTW);

      setTimeout(() => {
        showMasks(disappearMasks);
        startEmitters(emitters);
        // clearChatDialog();
        let trigger = true;
        const tl = gsap.timeline();
        disappearMasks.forEach((it, index) => {
          if (index === disappearMasks.length - 1) {
            tl.to(
              it,
              {
                alpha: 0.2,
                duration: 0.8,
                onUpdate: () => {
                  if (disappearMasks[0].alpha < 0.5 && trigger) {
                    clearBackgroundAnimation(
                      backgroundSpine,
                      animationConfig.animation.touch.length + animationConfig.animation.idle.length,
                    );
                    trigger = false;
                  }
                },
              },
              "<",
            );
          } else {
            tl.to(
              it,
              {
                alpha: 0.2,
                duration: 0.8,
              },
              "<",
            );
          }
        });
        tl.then(() => {
          hideMasks(disappearMasks);
          showMasks([Mask]);
          container.visible = true;
          waifu.visible = true;
          startEmitters([AppearEmitter]);
          // updateChatStyleStatic(HomePageDialogConfig.x, HomePageDialogConfig.y);
          // updateChatDialog("", "left", {
          //   x: HomePageDialogConfig.x,
          //   y: HomePageDialogConfig.y,
          // });
          // playWorkVoice(() => {
          //   aronaContainer.on("pointerdown", () => {
          //     playWorkVoice();
          //   });
          // });
          playWorkVoice(workVoice, WorkVoiceLock, waifu, undefined, 1);
          const tl2 = gsap.timeline();
          tl2
            .to(Mask, {
              alpha: 0,
              duration: 1,
            })
            .then(() => {
              Mask.visible = false;
              container.interactive = true;
            });
        });
      }, 600);
      const animationStartTrack = animationConfig.animation.idle.length;
      animationConfig.animation.touch.forEach((animation, index) => {
        backgroundSpine.state.setAnimation(backgroundAnimationTrack + animationStartTrack + index, animation, false);
      });
    }
  });
  return app;
}

const AronaColor = [63 / 255, 203 / 255, 255 / 255];
const PlanaColor = [252 / 255, 172 / 255, 252 / 255];

function loadMask(
  resources: LoaderResource[],
  app: Container,
  config: { scale: number; offset: { x: number; y: number } }[],
  arona: boolean[],
  loadOffset = true,
) {
  const baseScaleRate = loadOffset ? standardRate : 1;
  return resources.map((resource, index) => {
    const sprite = Sprite.from(resource.texture!);
    sprite.alpha = 0.8;
    sprite.filters = [
      new AdvancedBloomFilter({bloomScale: 1.5, brightness: 1.5}),
      new ColorOverlayFilter(arona[index] ? AronaColor : PlanaColor, 1),
    ];
    sprite.scale.set(baseScaleRate * config[index].scale);
    sprite.position.set(baseScaleRate * config[index].offset.x, baseScaleRate * config[index].offset.y);
    sprite.zIndex = 10;
    app.addChild(sprite);
    sprite.visible = false;
    return sprite;
  });
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

function loadWaifuSpine(resource: LoaderResource, app: Container, init: Point) {
  const container = new Container();
  container.sortableChildren = true;
  container.visible = false;
  const waifu = new Spine(resource.spineData!);
  waifu.pivot.set(-0.5 * waifu.width, -0.63 * waifu.height);
  waifu.zIndex = 5;
  container.scale.set(standardRate * 0.35);
  container.position.set(standardRate * init.x, standardRate * init.y);
  waifu.state.setAnimation(AronaIdleTrack, "Idle_01", true);
  container.addChild(waifu);
  app.addChild(container);
  return {
    waifu,
    container,
  };
}

function loadEmitter(app: Container, configs: EmitterConfig[], loadOffset = true) {
  const baseScaleRate = loadOffset ? standardRate : 1;
  return configs.map((config) => {
    const sourceParticleContainer = new Container();
    sourceParticleContainer.visible = false;
    sourceParticleContainer.filters = [new AdvancedBloomFilter({bloomScale: 3, brightness: 1.5})];
    sourceParticleContainer.position.set(baseScaleRate * config.offset.x, baseScaleRate * config.offset.y);
    sourceParticleContainer.scale.set(baseScaleRate * config.scale);
    sourceParticleContainer.zIndex = 10;
    app.addChild(sourceParticleContainer);
    const emitter = new Emitter(sourceParticleContainer, config.config);
    emitter.autoUpdate = true;
    emitter.emit = false;
    return {
      emitter,
      sourceParticleContainer,
    };
  });
}

function showMasks(masks: (Sprite | Container)[]) {
  // eslint-disable-next-line no-return-assign
  masks.forEach((it) => (it.visible = true));
}

function hideMasks(masks: (Sprite | Container)[]) {
  // eslint-disable-next-line no-return-assign
  masks.forEach((it) => (it.visible = false));
}

function startEmitters(items: { emitter: Emitter; sourceParticleContainer: Container<DisplayObject> }[]) {
  items.forEach((it) => {
    it.sourceParticleContainer.visible = true;
    it.emitter.emit = true;
  });
}

function clearBackgroundAnimation(background: Spine, length: number) {
  let len = Number(length - 1);
  while (len >= 0) {
    background.state.setAnimation(backgroundAnimationTrack + length, "Dummy", false);
    len--;
  }
  setTimeout(() => {
    len = Number(length - 1);
    while (len >= 0) {
      background.state.clearTrack(backgroundAnimationTrack + length);
      len--;
    }
  }, 100);
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
    const afterHeight = styleToPxNumber(afterStyle.height);
    if (position) {
      chatStyle.position.y = position.y * standardRate - (standardRate - 1) * afterHeight;
      chatStyle.position.x = position.x * standardRate + (standardRate - 1) * (chatDialogArrowOffset + afterHeight);
    }
    const style = getComputedStyle(chatDialogOuter.value!);
    const chatDialogHeight = styleToPxNumber(style.height);
    chatDialogHalfHeight.value = chatDialogHeight / 2;
    const width = Number(style.width.replace("px", ""));
    if (chatStyle.type === "right") {
      const l = width - chatDialogArrowOffset - afterHeight;
      chatStyle.position.x = chatStyleStatic.x + (standardRate - 1) * l + (249.6 - width) * standardRate;
    }
    // 检查y是不是超过屏幕底部了
    if (chatStyle.position.y + chatDialogHeight > viewHeight + middleOffset) {
      chatStyle.position.y = viewHeight + middleOffset - chatDialogHeight - 20;
    }
  });
}

function updateChatStyleStatic(x: number, y: number) {
  chatStyleStatic.x = x * standardRate;
  chatStyleStatic.y = y * standardRate;
}

function styleToPxNumber(str: string) {
  return Number(str.replace("px", ""));
}

const router = useRouter();

function routerJump(path: string) {
  router.push(path);
}

onMounted(() => {
  mainApp = initBackground(backgroundContainer.value!);
});
onUnmounted(() => {
  if (mainApp) {
    try {
      mainApp.destroy(true);
    } catch (e: unknown) {
      console.log(e);
    }
  }
  clearTimeout(randomTalkVoiceIntervalHandler.timeout);
  if (randomTalkVoiceIntervalHandler.stop) {
    randomTalkVoiceIntervalHandler.stop();
  }
});
</script>

<style lang="scss" scoped>
div {
  font-family: "Game Font", sans-serif;
  font-size: 1.2rem;
}

.background {
  position: relative;
  overflow: hidden;
  user-select: none;
  width: 100%;
  height: 100%;

  .chat-dialog-outer {
    position: absolute;
    opacity: 0;
    z-index: 99;
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
