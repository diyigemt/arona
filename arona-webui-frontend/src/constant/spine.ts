import { SitEmitterConfig, SleepEmitterConfig, WatchEmitterConfig } from "@/constant/emiterConfig";

export const HomePageDialogConfig = {
  x: 100,
  y: 450,
};
export const PlanaPageAnimationConfig = [
  {
    animation: {
      idle: ["Idle_01"],
      touch: [],
    },
  },
];
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
      config: SleepEmitterConfig,
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
      config: WatchEmitterConfig,
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
      config: SitEmitterConfig,
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
