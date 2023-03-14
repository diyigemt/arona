import { EmitterConfigV3 } from "@pixi/particle-emitter/index";
import {
  AronaPeekEmitterConfig,
  AronaSitEmitterConfig,
  AronaSleepEmitterConfig,
  AronaWatchEmitterConfig,
  PlanaCabinetEmitterConfig,
  PlanaPeekEmitterConfig,
  PlanaSitEmitterConfig,
  PlanaUmbrellaEmitterConfig,
  WatchSkyEmitterConfigA,
  WatchSkyEmitterConfigB,
} from "@/constant/emiterConfig";

export const HomePageDialogConfig = {
  x: 100,
  y: 450,
};
export interface Point {
  x: number;
  y: number;
}
export type DialogArrow = "left" | "right";
export type VoiceConfig = {
  AnimationName: string;
  textJP: string;
  textCN?: string;
  voice: string;
};
export type VoiceGroup = VoiceConfig[];
export type EmitterConfig = {
  scale: number;
  offset: Point;
  config: EmitterConfigV3;
};
export type PlanaPageAnimationConfig = {
  animation: {
    idle: string[];
    touch: string[];
  };
  masks: { path: string; scale: number; offset: Point }[];
  emitters: EmitterConfig[];
  dialogs: (Point & { type: DialogArrow })[];
  interaction: [Point, Point][];
  name: string;
  space: string;
  voice: {
    in: VoiceGroup[];
    talk: VoiceGroup[];
    exit: VoiceGroup[];
  };
};
// const b = Object.keys(a).filter(key => key.indexOf("Talk") === -1).map(key => Reflect.get(a, key))
// const c = Object.keys(a).filter(key => key.indexOf("Talk") !== -1).map(key => Reflect.get(a, key))
// const d = [...new Set(b.flat().map(it => it.DialogCategory))].map(key => b.flat().filter(it => it.DialogCategory === key))
// const f = {}
// d.forEach(it => Reflect.set(f,it[0].DialogCategory,{in: it.filter(arr => arr.DialogCondition === "Enter").map(arr => ({AnimationName: arr.AnimationName, textJP:arr.LocalizeJP,voice:arr.VoiceClipsJp.reverse()[0], group: arr.GroupId})), talk: it.filter(arr => arr.DialogCondition === "Idle").map(arr => ({AnimationName: arr.AnimationName, textJP:arr.LocalizeJP,voice:arr.VoiceClipsJp.reverse()[0], group: arr.GroupId})), exit: it.filter(arr => arr.DialogCondition === "Exit").map(arr => ({AnimationName: arr.AnimationName, textJP:arr.LocalizeJP,voice:arr.VoiceClipsJp.reverse()[0], group: arr.GroupId}))}))
// Object.keys(f).forEach(key => {const source = Reflect.get(f, key); Object.keys(source).forEach(k => { Reflect.set(source, k, [...new Set(Reflect.get(source, k).map(it => it.group))].map(group => Reflect.get(source, k).filter(it => it.group === group).map(it => { delete it.group; return it }))) })})
// const g = Object.keys(f).map(key =>
// ({animation:{idle:[],touch:[]},masks:[{path:"",scale:1,offset:{x:0,y:0}}],emitter:[{scale:1,offset:{x:0,y:0},config:{}}],dialogs:[{x:0,y:0,type:"left"}],interaction:[[{x:0,y:0},{x:0,y:0}]],name: key, voice: Reflect.get(f, key)}))
// const h = {}
// Reflect.set(h, "arona", c[0].map(it => ({DialogCategory: it.DialogCategory,DialogCondition: it.DialogCondition,AnimationName:it.AnimationName,LocalizeJP:it.LocalizeJP,VoiceClipsJp:it.VoiceClipsJp})))
// Reflect.set(h, "plana", c[1].map(it => ({DialogCategory: it.DialogCategory,DialogCondition: it.DialogCondition,AnimationName:it.AnimationName,LocalizeJP:it.LocalizeJP,VoiceClipsJp:it.VoiceClipsJp})))
// h.arona = h.arona.map(arr => ({AnimationName: arr.AnimationName, textJP:arr.LocalizeJP,voice:arr.VoiceClipsJp}))
// h.plana = h.plana.map(arr => ({AnimationName: arr.AnimationName, textJP:arr.LocalizeJP,voice:arr.VoiceClipsJp}))
export const PlanaPageAnimationConfig: PlanaPageAnimationConfig[] = [
  {
    animation: { idle: ["Idle_02"], touch: ["Idle_02_Touch_A", "Idle_02_Touch_M"] },
    masks: [{ path: "FX_TEX_Arona_C.png", scale: 1.37, offset: { x: 87, y: 95 } }],
    emitters: [{ scale: 0.26, offset: { x: -20, y: -20 }, config: AronaSitEmitterConfig }],
    dialogs: [{ x: 88, y: 365, type: "left" }],
    interaction: [
      [
        { x: 181.6000061035156, y: 100.19999694824219 },
        { x: 211.1999969482422, y: 147.40000915527344 },
      ],
      [
        { x: 207.1999969482422, y: 113.00001525878906 },
        { x: 242.40000915527344, y: 153.00001525878906 },
      ],
      [
        { x: 126.4000015258789, y: 144.1999969482422 },
        { x: 216, y: 206.59999084472656 },
      ],
      [
        { x: 143.1999969482422, y: 117.80000305175781 },
        { x: 223.1999969482422, y: 162.59999084472656 },
      ],
      [
        { x: 134.40000915527344, y: 200.1999969482422 },
        { x: 201.6000061035156, y: 324.1999969482422 },
      ],
      [
        { x: 155.1999969482422, y: 324.99998474121094 },
        { x: 89.5999984741211, y: 312.1999969482422 },
      ],
      [
        { x: 105.5999984741211, y: 250.59999084472656 },
        { x: 154.40000915527344, y: 293.80003356933594 },
      ],
      [
        { x: 198.40000915527344, y: 320.99998474121094 },
        { x: 299.20001220703125, y: 447.40000915527344 },
      ],
      [
        { x: 168, y: 288.1999969482422 },
        { x: 266.3999938964844, y: 323.40000915527344 },
      ],
      [
        { x: 180.8000030517578, y: 270.6000213623047 },
        { x: 220, y: 293.80003356933594 },
      ],
    ],
    name: "UIWorkAronaSit",
    space: "arona_workpage_daytime.skel",
    voice: {
      in: [[{ AnimationName: "", textJP: "ルンルン～♬", voice: "Arona_Work_Sit_In_1" }]],
      talk: [
        [{ AnimationName: "", textJP: "ランララン、ララ～ン♪", voice: "Arona_Work_Sit_Talk_1" }],
        [{ AnimationName: "", textJP: "ふんふんふん～♩", voice: "Arona_Work_Sit_Talk_2" }],
        [
          {
            AnimationName: "",
            textJP: "今日はまたっ、どんなことがっ♬\\n待ってるのかな～♪",
            voice: "Arona_Work_Sit_Talk_3",
          },
        ],
      ],
      exit: [
        [{ AnimationName: "", textJP: "あっ？", voice: "Arona_Work_Sit_Exit_1" }],
        [{ AnimationName: "", textJP: "あれ？", voice: "Arona_Work_Sit_Exit_2" }],
        [{ AnimationName: "", textJP: "あっ！", voice: "Arona_Work_Sit_Exit_3" }],
      ],
    },
  },
  {
    animation: { idle: ["Idle_00"], touch: ["Idle_00_Touch_A", "Idle_00_Touch_M"] },
    masks: [{ path: "FX_TEX_Arona_A.png", scale: 2.2, offset: { x: 58, y: 209 } }],
    emitters: [{ scale: 0.27, offset: { x: -15, y: -30 }, config: AronaSleepEmitterConfig }],
    dialogs: [{ x: 50, y: 344, type: "left" }],
    interaction: [
      [
        { x: 118.4000015258789, y: 225.80006408691406 },
        { x: 212.8000030517578, y: 314.6000213623047 },
      ],
      [
        { x: 66.4000015258789, y: 260.1999969482422 },
        { x: 120.80000305175781, y: 382.6000213623047 },
      ],
      [
        { x: 124.80000305175781, y: 343.40000915527344 },
        { x: 192.8000030517578, y: 383.40000915527344 },
      ],
      [
        { x: 152, y: 387.40000915527344 },
        { x: 229.6000061035156, y: 483.40000915527344 },
      ],
    ],
    name: "UIWorkAronaSleep",
    space: "arona_workpage_daytime.skel",
    voice: {
      in: [
        [{ AnimationName: "", textJP: "むにゃ、\\nイチゴミルクは…うひひ。", voice: "Arona_Work_Sleep_In_1" }],
        [{ AnimationName: "", textJP: "そんなに\\n食べられません…。", voice: "Arona_Work_Sleep_In_2" }],
      ],
      talk: [
        [{ AnimationName: "", textJP: "先生ったら…。", voice: "Arona_Work_Sleep_Talk_1" }],
        [{ AnimationName: "", textJP: "…うひひひひ。", voice: "Arona_Work_Sleep_Talk_2" }],
        [{ AnimationName: "", textJP: "むにゃ。", voice: "Arona_Work_Sleep_Talk_3" }],
        [
          {
            AnimationName: "",
            textJP: "居眠りしているわけでは\\nないですよぉ…むにゃ。",
            voice: "Arona_Work_Sleep_Talk_4",
          },
        ],
        [{ AnimationName: "", textJP: "うひっ、にゃあ…。", voice: "Arona_Work_Sleep_Talk_5" }],
        [{ AnimationName: "", textJP: "先生、\\nちゃんとしてください…。", voice: "Arona_Work_Sleep_Talk_6" }],
      ],
      exit: [
        [{ AnimationName: "", textJP: "んっ、うひゃっ？", voice: "Arona_Work_Sleep_Exit_1" }],
        [{ AnimationName: "", textJP: "あひゃっ！？", voice: "Arona_Work_Sleep_Exit_2" }],
        [{ AnimationName: "", textJP: "…んぃ？", voice: "Arona_Work_Sleep_Exit_3" }],
      ],
    },
  },
  {
    animation: { idle: ["Idle_01"], touch: ["Idle_01_Touch_A", "Idle_01_Touch_M"] },
    masks: [{ path: "FX_TEX_Arona_B.png", scale: 0.7, offset: { x: 210, y: 120 } }],
    emitters: [{ scale: 0.25, offset: { x: -12, y: -10 }, config: AronaWatchEmitterConfig }],
    dialogs: [{ x: 100, y: 262, type: "right" }],
    interaction: [
      [
        { x: 240, y: 127.40000915527344 },
        { x: 280.8000183105469, y: 168.1999969482422 },
      ],
      [
        { x: 241.6000061035156, y: 169.80006408691406 },
        { x: 269.6000061035156, y: 209.80006408691406 },
      ],
      [
        { x: 232.8000030517578, y: 206.59999084472656 },
        { x: 268.8000183105469, y: 294.6000213623047 },
      ],
      [
        { x: 206.40000915527344, y: 202.59999084472656 },
        { x: 237.6000061035156, y: 221.00001525878906 },
      ],
    ],
    name: "UIWorkAronaWatch",
    space: "arona_workpage_daytime.skel",
    voice: {
      in: [
        [{ AnimationName: "", textJP: "うん、よし！\\n今日も良い天気ですね。", voice: "Arona_Work_Watch_In_1" }],
        [{ AnimationName: "", textJP: "うーん…雨、降るのかな。", voice: "Arona_Work_Watch_In_2" }],
      ],
      talk: [
        [{ AnimationName: "", textJP: "遥か空の向こうには、\\n何があるんだろう…。", voice: "Arona_Work_Watch_Talk_1" }],
        [{ AnimationName: "", textJP: "……。", voice: "Arona_Work_Watch_Talk_2" }],
        [{ AnimationName: "", textJP: "…うーん。", voice: "Arona_Work_Watch_Talk_3" }],
      ],
      exit: [
        [{ AnimationName: "", textJP: "あっ！", voice: "Arona_Work_Watch_Exit_1" }],
        [{ AnimationName: "", textJP: "あれ。", voice: "Arona_Work_Watch_Exit_2" }],
      ],
    },
  },
  {
    animation: { idle: ["Idle_01"], touch: ["Idle_01_Touch_A", "Idle_01_Touch_M"] },
    masks: [{ path: "FX_TEX_Plana_B.png", scale: 0.667, offset: { x: 243, y: 146 } }],
    emitters: [{ scale: 1, offset: { x: 0, y: 0 }, config: PlanaSitEmitterConfig }],
    dialogs: [{ x: 0, y: 0, type: "left" }],
    interaction: [
      [
        { x: 249.6000061035156, y: 157.8000030517578 },
        { x: 310.3999938964844, y: 369.80003356933594 },
      ],
      [
        { x: 315.20001220703125, y: 288.99998474121094 },
        { x: 364.8000183105469, y: 371.40000915527344 },
      ],
    ],
    name: "UIWorkPlanaSit",
    space: "arona_workpage_nighttime.skel",
    voice: {
      in: [
        [{ AnimationName: "", textJP: "うーん……。", voice: "NP0035_Work_Sit_In_1" }],
        [{ AnimationName: "", textJP: "うーーん……。", voice: "NP0035_Work_Sit_In_2" }],
      ],
      talk: [[{ AnimationName: "", textJP: "……。", voice: "NP0035_Work_Sit_Talk_1" }]],
      exit: [[{ AnimationName: "", textJP: "あ……！", voice: "NP0035_Work_Sit_Exit_1" }]],
    },
  },
  {
    animation: { idle: ["Idle_00"], touch: ["Idle_00_Touch_A", "Idle_00_Touch_M"] },
    masks: [{ path: "FX_TEX_Plana_A.png", scale: 0.74, offset: { x: 79, y: 120 } }],
    emitters: [{ scale: 1, offset: { x: 0, y: 0 }, config: PlanaCabinetEmitterConfig }],
    dialogs: [{ x: 0, y: 0, type: "left" }],
    interaction: [
      [
        { x: 82.4000015258789, y: 126.59999084472656 },
        { x: 145.6000061035156, y: 310.6000213623047 },
      ],
    ],
    name: "UIWorkPlanaCabinet",
    space: "arona_workpage_nighttime.skel",
    voice: {
      in: [
        [{ AnimationName: "", textJP: "ここは……\\nそういうことなのですね。", voice: "NP0035_Work_Cabinet_In_1" }],
        [{ AnimationName: "", textJP: "……そういうことですね。", voice: "NP0035_Work_Cabinet_In_2" }],
      ],
      talk: [
        [{ AnimationName: "", textJP: "ふむ……なるほど。", voice: "NP0035_Work_Cabinet_Talk_1" }],
        [{ AnimationName: "", textJP: "ふむ……\\nそういう構造なんですね。", voice: "NP0035_Work_Cabinet_Talk_2" }],
      ],
      exit: [[{ AnimationName: "", textJP: "……あ。", voice: "NP0035_Work_Cabinet_Exit_1" }]],
    },
  },
  {
    animation: { idle: ["Idle_02"], touch: ["Idle_02_Touch_A", "Idle_02_Touch_M"] },
    masks: [{ path: "FX_TEX_Plana_C.png", scale: 0.575, offset: { x: 256, y: 126 } }],
    emitters: [{ scale: 1, offset: { x: 0, y: 0 }, config: PlanaUmbrellaEmitterConfig }],
    dialogs: [{ x: 0, y: 0, type: "left" }],
    interaction: [
      [
        { x: 316.8000183105469, y: 131.40000915527344 },
        { x: 368, y: 265.80006408691406 },
      ],
      [
        { x: 261.6000061035156, y: 179.40000915527344 },
        { x: 335.20001220703125, y: 267.40000915527344 },
      ],
    ],
    name: "UIWorkPlanaUmbrella",
    space: "arona_workpage_nighttime.skel",
    voice: {
      in: [[{ AnimationName: "", textJP: "雨が降ったらこれで……", voice: "NP0035_Work_Umbrella_In_1" }]],
      talk: [
        [{ AnimationName: "", textJP: "役に立つのでしょうか……？", voice: "NP0035_Work_Umbrella_Talk_1" }],
        [{ AnimationName: "", textJP: "私も……一緒に……", voice: "NP0035_Work_Umbrella_Talk_2" }],
      ],
      exit: [[{ AnimationName: "", textJP: "あ。", voice: "NP0035_Work_Umbrella_Exit_1" }]],
    },
  },
  {
    animation: {
      idle: ["Idle_00", "Idle_12"],
      touch: ["Idle_00_Touch_A", "Idle_00_Touch_M", "Idle_12_Touch_A", "Idle_12_Touch_M"],
    },
    masks: [
      { path: "FX_TEX_Arona_A.png", scale: 2.2, offset: { x: 58, y: 209 } },
      { path: "FX_TEX_Plana_D.png", scale: 0.45, offset: { x: 253, y: 165 } },
    ],
    emitters: [
      { scale: 1, offset: { x: 270, y: 180 }, config: PlanaPeekEmitterConfig },
      { scale: 0.27, offset: { x: -15, y: -30 }, config: AronaSleepEmitterConfig },
    ],
    dialogs: [{ x: 0, y: 0, type: "left" }],
    interaction: [
      [
        { x: 255.1999969482422, y: 168.1999969482422 },
        { x: 280, y: 192.1999969482422 },
      ],
      [
        { x: 118.4000015258789, y: 225.80006408691406 },
        { x: 212.8000030517578, y: 314.6000213623047 },
      ],
      [
        { x: 66.4000015258789, y: 260.1999969482422 },
        { x: 120.80000305175781, y: 382.6000213623047 },
      ],
      [
        { x: 124.80000305175781, y: 343.40000915527344 },
        { x: 192.8000030517578, y: 383.40000915527344 },
      ],
      [
        { x: 152, y: 387.40000915527344 },
        { x: 229.6000061035156, y: 483.40000915527344 },
      ],
    ],
    name: "UIWorkCoexist_AronaSleepPeek",
    space: "arona_workpage_daytime.skel",
    voice: {
      in: [
        [
          { AnimationName: "", textJP: "むにゃ、\\nイチゴミルクは…うひひ。", voice: "Arona_Work_Sleep_In_1" },
          { AnimationName: "", textJP: "寝ているの\\nでしょうか……。", voice: "NP0035_Work_AronaSleepPeek_In_1_2" },
        ],
        [
          { AnimationName: "", textJP: "そんなに\\n食べられません……。", voice: "Arona_Work_Sleep_In_2" },
          { AnimationName: "", textJP: "やはり、\\n寝てますね……", voice: "NP0035_Work_AronaSleepPeek_In_2_2" },
        ],
      ],
      talk: [
        [{ AnimationName: "", textJP: "先生ったら…。", voice: "Arona_Work_Sleep_Talk_1" }],
        [{ AnimationName: "", textJP: "…うひひひひ。", voice: "Arona_Work_Sleep_Talk_2" }],
        [{ AnimationName: "", textJP: "むにゃ。", voice: "Arona_Work_Sleep_Talk_3" }],
        [
          {
            AnimationName: "",
            textJP: "居眠りしているわけでは\\nないですよぉ…むにゃ。",
            voice: "Arona_Work_Sleep_Talk_4",
          },
        ],
        [{ AnimationName: "", textJP: "うひっ、にゃあ…。", voice: "Arona_Work_Sleep_Talk_5" }],
        [{ AnimationName: "", textJP: "先生、\\nちゃんとしてください…。", voice: "Arona_Work_Sleep_Talk_6" }],
      ],
      exit: [
        [{ AnimationName: "", textJP: "んっ、うひゃっ？", voice: "Arona_Work_Sleep_Exit_1" }],
        [{ AnimationName: "", textJP: "あひゃっ！？", voice: "Arona_Work_Sleep_Exit_2" }],
        [{ AnimationName: "", textJP: "…んぃ？", voice: "Arona_Work_Sleep_Exit_3" }],
      ],
    },
  },
  {
    animation: {
      idle: ["Idle_00", "Idle_11"],
      touch: ["Idle_00_Touch_A", "Idle_00_Touch_M", "Idle_11_Touch_A", "Idle_11_Touch_M"],
    },
    masks: [
      { path: "FX_TEX_Arona_A.png", scale: 2.2, offset: { x: 58, y: 209 } },
      { path: "FX_TEX_Plana_B.png", scale: 0.658, offset: { x: 245, y: 144 } },
    ],
    emitters: [
      { scale: 0.27, offset: { x: -15, y: -30 }, config: AronaSleepEmitterConfig },
      { scale: 1, offset: { x: 0, y: 0 }, config: PlanaSitEmitterConfig },
    ],
    dialogs: [{ x: 0, y: 0, type: "left" }],
    interaction: [
      [
        { x: 258.3999938964844, y: 162.59999084472656 },
        { x: 305.6000061035156, y: 352.99998474121094 },
      ],
      [
        { x: 302.3999938964844, y: 283.40000915527344 },
        { x: 340, y: 320.99998474121094 },
      ],
      [
        { x: 316, y: 321.80003356933594 },
        { x: 364.8000183105469, y: 370.6000213623047 },
      ],
      [
        { x: 118.4000015258789, y: 225.80006408691406 },
        { x: 212.8000030517578, y: 314.6000213623047 },
      ],
      [
        { x: 66.4000015258789, y: 260.1999969482422 },
        { x: 120.80000305175781, y: 382.6000213623047 },
      ],
      [
        { x: 124.80000305175781, y: 343.40000915527344 },
        { x: 192.8000030517578, y: 383.40000915527344 },
      ],
      [
        { x: 152, y: 387.40000915527344 },
        { x: 229.6000061035156, y: 483.40000915527344 },
      ],
    ],
    name: "UIWorkCoexist_AronaSleepSit",
    space: "arona_workpage_daytime.skel",
    voice: {
      in: [
        [
          { AnimationName: "", textJP: "むにゃ、\\nイチゴミルクは…うひひ。", voice: "Arona_Work_Sleep_In_1" },
          { AnimationName: "", textJP: "イチゴミルク……？", voice: "NP0035_Work_AronaSleepSit_In_1_2" },
        ],
        [
          { AnimationName: "", textJP: "そんなに\\n食べられません……。", voice: "Arona_Work_Sleep_In_2" },
          { AnimationName: "", textJP: "先輩、そろそろ\\n起きないと……。", voice: "NP0035_Work_AronaSleepSit_In_2_2" },
        ],
      ],
      talk: [
        [{ AnimationName: "", textJP: "…うひひひひ。", voice: "Arona_Work_Sleep_Talk_2" }],
        [{ AnimationName: "", textJP: "むにゃ。", voice: "Arona_Work_Sleep_Talk_3" }],
        [
          {
            AnimationName: "",
            textJP: "居眠りしているわけでは\\nないですよぉ…むにゃ。",
            voice: "Arona_Work_Sleep_Talk_4",
          },
          { AnimationName: "", textJP: "本当ですか……？", voice: "NP0035_Work_AronaSleepSit_Talk_4_2" },
        ],
      ],
      exit: [
        [{ AnimationName: "", textJP: "んっ、うひゃっ？", voice: "Arona_Work_Sleep_Exit_1" }],
        [{ AnimationName: "", textJP: "あひゃっ！？", voice: "Arona_Work_Sleep_Exit_2" }],
        [{ AnimationName: "", textJP: "…んぃ？", voice: "Arona_Work_Sleep_Exit_3" }],
      ],
    },
  },
  {
    animation: { idle: ["Idle_03"], touch: ["Idle_03_Touch_A", "Idle_03_Touch_M"] },
    masks: [
      { path: "FX_TEX_Arona_E.png", scale: 0.53, offset: { x: 390, y: 167 } },
      { path: "FX_TEX_Plana_F.png", scale: 0.53, offset: { x: 359, y: 162 } },
    ],
    emitters: [
      { scale: 1, offset: { x: 0, y: 0 }, config: WatchSkyEmitterConfigA },
      { scale: 1, offset: { x: 0, y: 0 }, config: WatchSkyEmitterConfigB },
    ],
    dialogs: [{ x: 0, y: 0, type: "left" }],
    interaction: [
      [
        { x: 363.20001220703125, y: 168.1999969482422 },
        { x: 381.60003662109375, y: 186.59999084472656 },
      ],
      [
        { x: 361.60003662109375, y: 185.00001525878906 },
        { x: 380, y: 198.59999084472656 },
      ],
      [
        { x: 398.4000244140625, y: 172.1999969482422 },
        { x: 420, y: 198.59999084472656 },
      ],
      [
        { x: 400.79998779296875, y: 200.1999969482422 },
        { x: 418.4000244140625, y: 231.40000915527344 },
      ],
      [
        { x: 392, y: 209.8000030517578 },
        { x: 412, y: 233.8000030517578 },
      ],
      [
        { x: 404.79998779296875, y: 229.8000030517578 },
        { x: 419.20001220703125, y: 253.8000030517578 },
      ],
      [
        { x: 418.4000244140625, y: 178.6000061035156 },
        { x: 436, y: 199.40000915527344 },
      ],
    ],
    name: "UIWorkCoexist_PlanaWatchSky",
    space: "arona_workpage_nighttime.skel",
    voice: {
      in: [
        [
          { AnimationName: "", textJP: "あれ、見えますか？プラナちゃん？", voice: "Arona_Work_PlanaWatchSky_In_1_1" },
          { AnimationName: "", textJP: "あれのことですか、先輩？", voice: "NP0035_Work_PlanaWatchSky_In_1_2" },
          { AnimationName: "", textJP: "はい！あれです！", voice: "Arona_Work_PlanaWatchSky_In_1_3" },
        ],
      ],
      talk: [
        [
          { AnimationName: "", textJP: "あっちはどうですか？", voice: "Arona_Work_PlanaWatchSky_Talk_1_1" },
          { AnimationName: "", textJP: "うーん……。", voice: "NP0035_Work_PlanaWatchSky_Talk_1_2" },
        ],
      ],
      exit: [[{ AnimationName: "", textJP: "……あっ。", voice: "NP0035_Work_PlanaWatchSky_Exit_1" }]],
    },
  },
  {
    animation: {
      idle: ["Idle_01", "Idle_11"],
      touch: ["Idle_01_Touch_A", "Idle_01_Touch_M", "Idle_11_Touch_A", "Idle_11_Touch_M"],
    },
    masks: [
      { path: "FX_TEX_Plana_B.png", scale: 0.667, offset: { x: 243, y: 146 } },
      { path: "FX_TEX_Arona_D.png", scale: 0.5, offset: { x: 379, y: 173 } },
    ],
    emitters: [
      { scale: 1, offset: { x: 0, y: 0 }, config: AronaPeekEmitterConfig },
      { scale: 1, offset: { x: 0, y: 0 }, config: PlanaSitEmitterConfig },
    ],
    dialogs: [{ x: 0, y: 0, type: "left" }],
    interaction: [
      [
        { x: 396, y: 174.59999084472656 },
        { x: 416.8000183105469, y: 203.40000915527344 },
      ],
      [
        { x: 380, y: 189.80006408691406 },
        { x: 405.6000061035156, y: 225.00001525878906 },
      ],
      [
        { x: 258.3999938964844, y: 162.59999084472656 },
        { x: 305.6000061035156, y: 352.99998474121094 },
      ],
      [
        { x: 302.3999938964844, y: 283.40000915527344 },
        { x: 340, y: 320.99998474121094 },
      ],
      [
        { x: 316, y: 321.80003356933594 },
        { x: 364.8000183105469, y: 370.6000213623047 },
      ],
    ],
    name: "UIWorkCoexist_PlanaSitPeek",
    space: "arona_workpage_nighttime.skel",
    voice: {
      in: [
        [
          { AnimationName: "", textJP: "じー", voice: "Arona_Work_PlanaSitPeek_In_1_1" },
          { AnimationName: "", textJP: "うー－ん……", voice: "NP0035_Work_PlanaSitPeek_In_1_2" },
        ],
      ],
      talk: [
        [{ AnimationName: "", textJP: "……視線を感じます。", voice: "NP0035_Work_PlanaSitPeek_Talk_1" }],
        [{ AnimationName: "", textJP: "……困ります。", voice: "NP0035_Work_PlanaSitPeek_Talk_2" }],
      ],
      exit: [[{ AnimationName: "", textJP: "あ。", voice: "NP0035_Work_PlanaSitPeek_Exit_1" }]],
    },
  },
];
export type IWaifuAppearConfig = {
  mask: {
    scale: number;
    offset: Point;
  };
  position: [Point, Point];
};
export const WaifuAppearConfig: { arona: IWaifuAppearConfig; plana: IWaifuAppearConfig } = {
  arona: {
    mask: {
      scale: 1.16,
      offset: {
        x: -30,
        y: 0,
      },
    },
    position: [
      {
        x: 50,
        y: 120,
      },
      {
        x: 420,
        y: 120,
      },
    ],
  },
  plana: {
    mask: {
      scale: 4.77,
      offset: {
        x: -60,
        y: 40,
      },
    },
    position: [
      {
        x: 400,
        y: 110,
      },
      {
        x: 20,
        y: 110,
      },
    ],
  },
};
export const WorkConfig = {
  arona: [
    {
      AnimationName: "12",
      textJP: "[USERNAME]先生！\\nお待ちしておりました！",
      voice: ["Arona_Default_TTS", "Arona_Work_In_1"],
    },
    { AnimationName: "25", textJP: "さあ、\\nお仕事を始める時間です！", voice: ["Arona_Work_In_2"] },
    { AnimationName: "31", textJP: "どのお仕事から始めますか、\\n先生？", voice: ["Arona_Work_In_3"] },
    { AnimationName: "32", textJP: "私が先生のお仕事を\\n手伝います！", voice: ["Arona_Work_In_4"] },
    {
      AnimationName: "01",
      textJP: "ここで先生の、様々なお仕事を\\n始めることができます！",
      voice: ["Arona_Work_Talk_1"],
    },
    {
      AnimationName: "25",
      textJP: "[USERNAME] 先生！\\nご希望のお仕事を選んでください。\\n私が隣で手伝います！",
      voice: ["Arona_Default_TTS", "Arona_Work_Talk_2"],
    },
    {
      AnimationName: "13",
      textJP: "解決していただかねば\\nならないお仕事がたくさんです。\\n大人って大変ですね。",
      voice: ["Arona_Work_Talk_3"],
    },
    {
      AnimationName: "12",
      textJP: "解決するべきことがいっぱいですね！\\n頑張りましょう！",
      voice: ["Arona_Work_Talk_4"],
    },
    {
      AnimationName: "18",
      textJP: "たまには身体のことも考えないと、\\n先生の健康が心配です！",
      voice: ["Arona_Work_Talk_5"],
    },
    { AnimationName: "29", textJP: "うわぁ…。\\nものすごい量の仕事ですね。", voice: ["Arona_Work_Talk_6"] },
    {
      AnimationName: "22",
      textJP: "さあ、頑張りましょう！先生！",
      voice: ["Arona_Work_Talk_7_1", "Arona_Work_Talk_7_2"],
    },
  ],
  plana: [
    { AnimationName: "02", textJP: "接続確認。", voice: ["NP0035_Work_In_1_1"] },
    {
      AnimationName: "03",
      textJP: "[USERNAME]先生。\\nお待ちしておりました。",
      voice: ["NP0035_Default_TTS", "NP0035_Work_In_1_2"],
    },
    {
      AnimationName: "15",
      textJP: "お仕事を始める時間です。\\n[USERNAME]先生。",
      voice: ["NP0035_Work_In_2", "NP0035_Default_TTS"],
    },
    { AnimationName: "02", textJP: "どのお仕事から始めますか。\\n先生？", voice: ["NP0035_Work_In_3"] },
    {
      AnimationName: "02",
      textJP: "待機中。\\n解決しなければならない作業が\\n多数存在しています。",
      voice: ["NP0035_Work_In_4"],
    },
    { AnimationName: "03", textJP: "ここで先生の様々なお仕事を\\n進行できます。", voice: ["NP0035_Work_Talk_1"] },
    {
      AnimationName: "15",
      textJP: "[USERNAME]先生。\\nご希望のお仕事を選んでください。",
      voice: ["NP0035_Default_TTS", "NP0035_Work_Talk_2"],
    },
    {
      AnimationName: "03",
      textJP: "解決していただかねばならない\\nお仕事がたくさんあります。\\nでは、お願いします。",
      voice: ["NP0035_Work_Talk_3"],
    },
    {
      AnimationName: "13",
      textJP: "混乱。理解できない行動です。\\nつつかないでください。故障します。 ",
      voice: ["NP0035_Work_Talk_4"],
    },
    {
      AnimationName: "12",
      textJP: "理解しました。先生は今、\\n特にやるべきことがないのですね。\\n暇なのですね。",
      voice: ["NP0035_Work_Talk_5"],
    },
  ],
};
export const HomePageAnimationConfigs = [
  {
    animation: {
      background: "Idle_00",
      arona: ["Idle_00_Touch_A", "Idle_00_Touch_M"],
    },
    mask: {
      path: "/image/FX_TEX_Arona_A.png",
      scale: 2,
      offset: {
        x: 70,
        y: 230,
      },
    },
    emitter: {
      scale: 0.27,
      offset: {
        x: -15,
        y: -30,
      },
      config: AronaSleepEmitterConfig,
    },
    dialog: {
      x: 50,
      y: 344,
      type: "left",
    },
    interaction: {
      pa: {
        x: 89.16415,
        y: 291.94717,
      },
      pb: {
        x: 282.16415,
        y: 605.94717,
      },
    },
    voice: {
      in: [
        {
          animationName: "",
          textJP: "むにゃ、イチゴミルクは…\nうひひ。",
          textTW: "姆喵，草莓牛奶……\n嘻嘻。",
          voice: "Arona_Work_Sleep_In_1",
        },
        {
          animationName: "",
          textJP: "そんなに食べられません…。",
          textTW: "我吃不了這麼多……",
          voice: "Arona_Work_Sleep_In_2",
        },
      ],
      talk: [
        { animationName: "", textJP: "先生ったら…。", textTW: "老師真是的……", voice: "Arona_Work_Sleep_Talk_1" },
        { animationName: "", textJP: "…うひひひひ。", textTW: "……咿嘻嘻嘻嘻。", voice: "Arona_Work_Sleep_Talk_2" },
        { animationName: "", textJP: "むにゃ。", textTW: "姆喵。", voice: "Arona_Work_Sleep_Talk_3" },
        {
          animationName: "",
          textJP: "居眠りしているわけでは\nないですよぉ…むにゃ。",
          textTW: "我沒有在\n打瞌睡哦……姆喵。",
          voice: "Arona_Work_Sleep_Talk_4",
        },
        { animationName: "", textJP: "うひっ、にゃあ…。", textTW: "唔嘻，喵……", voice: "Arona_Work_Sleep_Talk_5" },
        {
          animationName: "",
          textJP: "先生、ちゃんとしてください…。",
          textTW: "老師，請認真一點……",
          voice: "Arona_Work_Sleep_Talk_6",
        },
      ],
      exit: [
        { animationName: "", textJP: "んっ、うひゃっ？", textTW: "嗯，嗚哈！", voice: "Arona_Work_Sleep_Exit_1" },
        { animationName: "", textJP: "あひゃっ！？", textTW: "啊！？", voice: "Arona_Work_Sleep_Exit_2" },
        { animationName: "", textJP: "…んぃ？", textTW: "……咦？", voice: "Arona_Work_Sleep_Exit_3" },
      ],
    },
  },
  {
    animation: {
      background: "Idle_01",
      arona: ["Idle_01_Touch_A", "Idle_01_Touch_M"],
    },
    mask: {
      path: "/image/FX_TEX_Arona_B.png",
      scale: 0.65,
      offset: {
        x: 220,
        y: 140,
      },
    },
    emitter: {
      scale: 0.25,
      offset: {
        x: -5,
        y: -10,
      },
      config: AronaWatchEmitterConfig,
    },
    dialog: {
      x: 100,
      y: 262,
      type: "right",
    },
    interaction: {
      pa: {
        x: 246.16415,
        y: 175.94717,
      },
      pb: {
        x: 287.16415,
        y: 380.94717,
      },
    },
    voice: {
      in: [
        {
          animationName: "",
          textJP: "うん、よし！\n今日も良い天気ですね。",
          textTW: "嗯，好！\n今天天氣也很好呢。",
          voice: "Arona_Work_Watch_In_1",
        },
        {
          animationName: "",
          textJP: "うーん…雨、降るのかな。",
          textTW: "唔嗯……會下雨嗎？",
          voice: "Arona_Work_Watch_In_2",
        },
      ],
      talk: [
        {
          animationName: "",
          textJP: "遥か空の向こうには、\n何があるんだろう…。",
          textTW: "遙遠天空的那端會有什麼呢……",
          voice: "Arona_Work_Watch_Talk_1",
        },
        // { animationName: "", textJP: "……。", textTW: "……", voice: "Arona_Work_Watch_Talk_2" },
        { animationName: "", textJP: "…うーん。", textTW: "……唔嗯。", voice: "Arona_Work_Watch_Talk_3" },
      ],
      exit: [
        { animationName: "", textJP: "あっ！", textTW: "啊！", voice: "Arona_Work_Watch_Exit_1" },
        { animationName: "", textJP: "あれ。", textTW: "咦？", voice: "Arona_Work_Watch_Exit_2" },
      ],
    },
  },
  {
    animation: {
      background: "Idle_02",
      arona: ["Idle_02_Touch_A", "Idle_02_Touch_M"],
    },
    mask: {
      path: "/image/FX_TEX_Arona_C.png",
      scale: 1.3,
      offset: {
        x: 87,
        y: 115,
      },
    },
    emitter: {
      scale: 0.26,
      offset: {
        x: -15,
        y: -10,
      },
      config: AronaSitEmitterConfig,
    },
    dialog: {
      x: 88,
      y: 365,
      type: "left",
    },
    interaction: {
      pa: {
        x: 137.16415,
        y: 163.94717,
      },
      pb: {
        x: 377.16415,
        y: 554.94717,
      },
    },
    voice: {
      in: [{ animationName: "", textJP: "ルンルン～♬", textTW: "哼哼～♬", voice: "Arona_Work_Sit_In_1" }],
      talk: [
        {
          animationName: "",
          textJP: "ランララン、ララ～ン♪",
          textTW: "啦～啦啦，啦啦～♪",
          voice: "Arona_Work_Sit_Talk_1",
        },
        { animationName: "", textJP: "ふんふんふん～♩", textTW: "哼哼哼～♩", voice: "Arona_Work_Sit_Talk_2" },
        {
          animationName: "",
          textJP: "今日はまたっ、どんなことがっ♬\n待ってるのかな～♪",
          textTW: "今天又有什麼事♬\n在等著我呢～♪",
          voice: "Arona_Work_Sit_Talk_3",
        },
      ],
      exit: [
        { animationName: "", textJP: "あっ？", textTW: "啊？", voice: "Arona_Work_Sit_Exit_1" },
        { animationName: "", textJP: "あれ？", textTW: "咦？", voice: "Arona_Work_Sit_Exit_2" },
        { animationName: "", textJP: "あっ！", textTW: "啊！", voice: "Arona_Work_Sit_Exit_3" },
      ],
    },
  },
];
// https://yuuka.diyigemt.com/image/full-extra/output/media/Audio/VOC_JP/JP_Arona/
export const VoiceConfig = [
  {
    animationName: "12",
    textJP: "[USERNAME]先生！\nお待ちしておりました！",
    textTW: "[USERNAME]老師！\n我等你好久了！",
    voice: "Arona_Work_In_1",
  },
  {
    animationName: "25",
    textJP: "さあ、\nお仕事を始める時間です！",
    textTW: "來，工作時間到了！",
    voice: "Arona_Work_In_2",
  },
  {
    animationName: "31",
    textJP: "どのお仕事から始めますか、\n先生？",
    textTW: "要從哪件工作開始呢？\n老師？",
    voice: "Arona_Work_In_3",
  },
  {
    animationName: "32",
    textJP: "私が先生のお仕事を\n手伝います！",
    textTW: "我來協助老師的工作！",
    voice: "Arona_Work_In_4",
  },
  {
    animationName: "01",
    textJP: "ここで先生の、様々なお仕事を\n始めることができます！",
    textTW: "可以在這裡開始\n老師的各種工作！",
    voice: "Arona_Work_Talk_1",
  },
  {
    animationName: "25",
    textJP: "[USERNAME] 先生！\nご希望のお仕事を選んでください。\n私が隣で手伝います！",
    textTW: "[USERNAME]老師！\n請選擇想要進行的工作。\n我會在身邊協助你的！",
    voice: "Arona_Work_Talk_2",
  },
  {
    animationName: "13",
    textJP: "解決していただかねば\nならないお仕事がたくさんです。\n大人って大変ですね。",
    textTW: "需要解決的工作好多啊。\n大人真辛苦呢。",
    voice: "Arona_Work_Talk_3",
  },
  {
    animationName: "12",
    textJP: "解決するべきことがいっぱいですね！\n頑張りましょう！",
    textTW: "要解決的事真多呢！\n一起加油吧！",
    voice: "Arona_Work_Talk_4",
  },
  {
    animationName: "18",
    textJP: "たまには身体のことも考えないと、\n先生の健康が心配です！",
    textTW: "偶爾也要注意一下身體才行，\n我很擔心老師的健康哦！",
    voice: "Arona_Work_Talk_5",
  },
  {
    animationName: "29",
    textJP: "うわぁ…。\nものすごい量の仕事ですね。",
    textTW: "哇啊……\n真是不得了的工作量呢。",
    voice: "Arona_Work_Talk_6",
  },
  {
    animationName: "22",
    textJP: "さあ、頑張りましょう！先生！",
    textTW: "來，一起加油吧！\n老師！",
    voice: "Arona_Work_Talk_7_1",
  },
  {
    animationName: "12",
    textJP: "[USERNAME]先生、おかえりなさい。\nお待ちしておりました！",
    textTW: "[USERNAME]老師，歡迎回來。\n我一直在等你！",
    voice: "Arona_Attendance_Enter_1",
  },
  {
    animationName: "03",
    textJP: " [USERNAME]先生、\n今日もよろしくお願いしますね！",
    textTW: "[USERNAME]老師，\n今天也請多多指教哦！",
    voice: "Arona_Attendance_Enter_2",
  },
  {
    animationName: "32",
    textJP: "[USERNAME]先生！\nおかえりなさい！",
    textTW: "[USERNAME]老師！\n歡迎回來！",
    voice: "Arona_Attendance_Enter_3",
  },
  {
    animationName: "31",
    textJP: "[USERNAME]先生、\n今日もお疲れさまです♪",
    textTW: "[USERNAME]老師，\n今天也辛苦你了♪",
    voice: "Arona_Attendance_Enter_4",
  },
  {
    animationName: "03",
    textJP: "[USERNAME]先生！\n頑張ってください！",
    textTW: "[USERNAME]老師！\n請再加把勁！",
    voice: "Arona_Academy_Talk_4",
  },
  {
    animationName: "13",
    textJP: "先生のお仕事はこんなにハードな\nものなんですね…。",
    textTW: "老師的工作這麼艱難啊……",
    voice: "Arona_Academy_Talk_5",
  },
  {
    animationName: "03",
    textJP: "[USERNAME]先生！\n頑張ってください！",
    textTW: "[USERNAME]老師！\n請再加把勁！",
    voice: "Arona_Academy_Talk_4",
  },
  {
    animationName: "13",
    textJP: "先生のお仕事はこんなにハードな\nものなんですね…。",
    textTW: "老師的工作這麼艱難啊……",
    voice: "Arona_Academy_Talk_5",
  },
  {
    animationName: "32",
    textJP: "私が先生のお仕事を\n手伝います！",
    textTW: "我來協助老師的工作！",
    voice: "Arona_Work_In_4",
  },
];
