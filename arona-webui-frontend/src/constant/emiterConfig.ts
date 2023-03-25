import { BehaviorEntry, EmitterConfigV3 } from "@pixi/particle-emitter/lib/EmitterConfig";
import { Texture } from "pixi.js";

const DEFAULT_EMITTER_CONFIG: EmitterConfigV3 = {
  lifetime: {
    min: 0.3,
    max: 0.5,
  },
  frequency: 0.01,
  pos: {
    x: 0,
    y: 0,
  },
  addAtBack: false,
  behaviors: [],
};

const DEFAULT_BEHAVIORS_ARONA_COLOR: BehaviorEntry[] = [
  {
    type: "colorStatic",
    config: {
      color: "#3fcbff",
    },
  },
];

const DEFAULT_BEHAVIORS_PLANA_COLOR: BehaviorEntry[] = [
  {
    type: "colorStatic",
    config: {
      color: "#ff8eff",
    },
  },
];

const DEFAULT_BEHAVIORS: BehaviorEntry[] = [
  {
    type: "alpha",
    config: {
      alpha: {
        list: [
          { value: 0.6, time: 0 },
          { value: 0.2, time: 0.1667 },
          { value: 0.6, time: 0.3333 },
          { value: 0.2, time: 0.6667 },
          { value: 0.6, time: 0.8333 },
          { value: 0.2, time: 1 },
        ],
      },
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
    type: "moveSpeedStatic",
    config: {
      min: 40,
      max: 40,
    },
  },
  {
    type: "textureSingle",
    config: {
      texture: Texture.from("/image/FX_TEX_Triangle_02_a.png"),
    },
  },
];

export const AronaSleepEmitterConfig: EmitterConfigV3 = {
  ...DEFAULT_EMITTER_CONFIG,
  particlesPerWave: 30,
  emitterLifetime: 0.5,
  maxParticles: 30,
  behaviors: [
    ...DEFAULT_BEHAVIORS,
    ...DEFAULT_BEHAVIORS_ARONA_COLOR,
    {
      type: "scale",
      config: {
        scale: {
          list: [
            {
              value: 0.5,
              time: 0,
            },
            {
              value: 0.3,
              time: 1,
            },
          ],
        },
      },
    },
    {
      type: "spawnShape",
      config: {
        type: "polygonalChain",
        data: [
          [
            { x: 466, y: 1274 },
            { x: 497, y: 994 },
          ],
          [
            { x: 743, y: 1016 },
            { x: 864, y: 1157 },
          ],
          [
            { x: 601, y: 1246 },
            { x: 845, y: 1246 },
          ],
          [
            { x: 406, y: 1524 },
            { x: 651, y: 1524 },
          ],
          [
            { x: 745, y: 1534 },
            { x: 789, y: 1773 },
          ],
        ],
      },
    },
  ],
};

export const AronaWatchEmitterConfig: EmitterConfigV3 = {
  ...DEFAULT_EMITTER_CONFIG,
  particlesPerWave: 30,
  emitterLifetime: 0.5,
  maxParticles: 30,
  behaviors: [
    ...DEFAULT_BEHAVIORS,
    ...DEFAULT_BEHAVIORS_ARONA_COLOR,
    {
      type: "scale",
      config: {
        scale: {
          list: [
            {
              value: 0.5,
              time: 0,
            },
            {
              value: 0.3,
              time: 1,
            },
          ],
        },
      },
    },
    {
      type: "spawnShape",
      config: {
        type: "polygonalChain",
        data: [
          [
            { x: 1067, y: 656 },
            { x: 1013, y: 962 },
          ],
          [
            { x: 1013, y: 962 },
            { x: 1017, y: 1218 },
          ],
          [
            { x: 1158, y: 656 },
            { x: 1105, y: 962 },
          ],
          [
            { x: 1077, y: 973 },
            { x: 1081, y: 1228 },
          ],
        ],
      },
    },
  ],
};

export const AronaSitEmitterConfig: EmitterConfigV3 = {
  ...DEFAULT_EMITTER_CONFIG,
  particlesPerWave: 50,
  emitterLifetime: 0.5,
  maxParticles: 50,
  behaviors: [
    ...DEFAULT_BEHAVIORS,
    ...DEFAULT_BEHAVIORS_ARONA_COLOR,
    {
      type: "scale",
      config: {
        scale: {
          list: [
            {
              value: 0.5,
              time: 0,
            },
            {
              value: 0.3,
              time: 1,
            },
          ],
        },
      },
    },
    {
      type: "spawnShape",
      config: {
        type: "polygonalChain",
        data: [
          [
            { x: 734, y: 593 },
            { x: 961, y: 598 },
          ],
          [
            { x: 701, y: 642 },
            { x: 512, y: 1277 },
          ],
          [
            { x: 900, y: 652 },
            { x: 825, y: 1179 },
          ],
          [
            { x: 617, y: 1154 },
            { x: 1077, y: 1278 },
          ],
          [
            { x: 547, y: 1289 },
            { x: 1007, y: 1366 },
          ],
          [
            { x: 944, y: 1355 },
            { x: 752, y: 1777 },
          ],
          [
            { x: 1037, y: 1335 },
            { x: 1234, y: 1756 },
          ],
          [
            { x: 769, y: 735 },
            { x: 835, y: 879 },
          ],
          [
            { x: 849, y: 743 },
            { x: 709, y: 861 },
          ],
          [
            { x: 675, y: 959 },
            { x: 793, y: 1123 },
          ],
          [
            { x: 809, y: 983 },
            { x: 647, y: 1081 },
          ],
        ],
      },
    },
  ],
};
export const PlanaSitEmitterConfig: EmitterConfigV3 = {
  ...DEFAULT_EMITTER_CONFIG,
  particlesPerWave: 30,
  emitterLifetime: 0.5,
  maxParticles: 30,
  behaviors: [
    ...DEFAULT_BEHAVIORS,
    ...DEFAULT_BEHAVIORS_PLANA_COLOR,
    {
      type: "scale",
      config: {
        scale: {
          list: [
            {
              value: 0.2,
              time: 0,
            },
            {
              value: 0.1,
              time: 1,
            },
          ],
        },
      },
    },
    {
      type: "spawnShape",
      config: {
        type: "polygonalChain",
        data: [
          [
            { x: 276.79998779296875, y: 176.1999969482422 },
            { x: 301.60003662109375, y: 183.40000915527344 },
          ],
          [
            { x: 304.79998779296875, y: 197.8000030517578 },
            { x: 274.4000244140625, y: 248.1999969482422 },
          ],
          [
            { x: 270.4000244140625, y: 190.59999084472656 },
            { x: 303.20001220703125, y: 241.00001525878906 },
          ],
          [
            { x: 308, y: 249.8000030517578 },
            { x: 277.60003662109375, y: 274.59999084472656 },
          ],
          [
            { x: 327.20001220703125, y: 294.59999084472656 },
            { x: 270.4000244140625, y: 324.1999969482422 },
          ],
          [
            { x: 269.60003662109375, y: 290.59999084472656 },
            { x: 329.60003662109375, y: 313.8000030517578 },
          ],
          [
            { x: 330.4000244140625, y: 300.1999969482422 },
            { x: 332.79998779296875, y: 364.1999969482422 },
          ],
          [
            { x: 315.20001220703125, y: 312.1999969482422 },
            { x: 357.60003662109375, y: 366.59999084472656 },
          ],
          [
            { x: 288, y: 328.1999969482422 },
            { x: 281.60003662109375, y: 353.00001525878906 },
          ],
        ],
      },
    },
  ],
};

export const PlanaCabinetEmitterConfig: EmitterConfigV3 = {
  ...DEFAULT_EMITTER_CONFIG,
  particlesPerWave: 30,
  emitterLifetime: 0.5,
  maxParticles: 30,
  behaviors: [
    ...DEFAULT_BEHAVIORS,
    ...DEFAULT_BEHAVIORS_PLANA_COLOR,
    {
      type: "scale",
      config: {
        scale: {
          list: [
            {
              value: 0.2,
              time: 0,
            },
            {
              value: 0.1,
              time: 1,
            },
          ],
        },
      },
    },
    {
      type: "spawnShape",
      config: {
        type: "polygonalChain",
        data: [
          [
            { x: 104, y: 127.40000915527344 },
            { x: 101.6000061035156, y: 147.39999389648438 },
          ],
          [
            { x: 86.39999389648438, y: 165.0000305175781 },
            { x: 97.6000061035156, y: 152.1999969482422 },
          ],
          [
            { x: 108, y: 144.1999969482422 },
            { x: 128, y: 159.39999389648438 },
          ],
          [
            { x: 128.80001831054688, y: 159.39999389648438 },
            { x: 106.39999389648438, y: 218.59999084472656 },
          ],
          [
            { x: 102.39999389648438, y: 163.39999389648438 },
            { x: 136, y: 212.1999969482422 },
          ],
          [
            { x: 140, y: 219.40000915527344 },
            { x: 102.39999389648438, y: 271.40000915527344 },
          ],
          [
            { x: 100.80001831054688, y: 214.59999084472656 },
            { x: 136, y: 265.8000030517578 },
          ],
          [
            { x: 96.80001831054688, y: 231.40000915527344 },
            { x: 84, y: 264.1999969482422 },
          ],
          [
            { x: 108, y: 271.40000915527344 },
            { x: 137.6000061035156, y: 298.59999084472656 },
          ],
          [
            { x: 135.20001220703125, y: 271.40000915527344 },
            { x: 115.20001220703125, y: 305.8000030517578 },
          ],
          [
            { x: 91.20001220703125, y: 317.00001525878906 },
            { x: 115.20001220703125, y: 309.8000030517578 },
          ],
        ],
      },
    },
  ],
};

export const PlanaUmbrellaEmitterConfig: EmitterConfigV3 = {
  ...DEFAULT_EMITTER_CONFIG,
  particlesPerWave: 30,
  emitterLifetime: 0.5,
  maxParticles: 30,
  behaviors: [
    ...DEFAULT_BEHAVIORS,
    ...DEFAULT_BEHAVIORS_PLANA_COLOR,
    {
      type: "scale",
      config: {
        scale: {
          list: [
            {
              value: 0.17,
              time: 0,
            },
            {
              value: 0.07,
              time: 1,
            },
          ],
        },
      },
    },
    {
      type: "spawnShape",
      config: {
        type: "polygonalChain",
        data: [
          [
            { x: 332.79998779296875, y: 138.6000061035156 },
            { x: 352, y: 147.39999389648438 },
          ],
          [
            { x: 353.60003662109375, y: 147.39999389648438 },
            { x: 333.60003662109375, y: 173.8000030517578 },
          ],
          [
            { x: 332.79998779296875, y: 146.6000061035156 },
            { x: 353.60003662109375, y: 177.8000030517578 },
          ],
          [
            { x: 353.60003662109375, y: 180.1999969482422 },
            { x: 336.79998779296875, y: 209.8000030517578 },
          ],
          [
            { x: 327.20001220703125, y: 193.8000030517578 },
            { x: 358.4000244140625, y: 222.59999084472656 },
          ],
          [
            { x: 358.4000244140625, y: 224.1999969482422 },
            { x: 336.79998779296875, y: 247.40000915527344 },
          ],
          [
            { x: 336, y: 228.1999969482422 },
            { x: 364.79998779296875, y: 248.1999969482422 },
          ],
          [
            { x: 298.4000244140625, y: 183.40000915527344 },
            { x: 334.4000244140625, y: 210.59999084472656 },
          ],
          [
            { x: 314.4000244140625, y: 177.0000305175781 },
            { x: 268, y: 204.1999969482422 },
          ],
          [
            { x: 262.4000244140625, y: 214.59999084472656 },
            { x: 340, y: 245.8000030517578 },
          ],
          [
            { x: 335.20001220703125, y: 217.00001525878906 },
            { x: 264, y: 249.8000030517578 },
          ],
        ],
      },
    },
  ],
};

export const PlanaPeekEmitterConfig: EmitterConfigV3 = {
  ...DEFAULT_EMITTER_CONFIG,
  particlesPerWave: 10,
  emitterLifetime: 0.5,
  maxParticles: 10,
  behaviors: [
    ...DEFAULT_BEHAVIORS,
    ...DEFAULT_BEHAVIORS_PLANA_COLOR,
    {
      type: "scale",
      config: {
        scale: {
          list: [
            {
              value: 0.1,
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
      type: "spawnShape",
      config: {
        type: "polygonalChain",
        data: [],
      },
    },
  ],
};

export const WatchSkyEmitterConfigA: EmitterConfigV3 = {
  ...DEFAULT_EMITTER_CONFIG,
  particlesPerWave: 10,
  emitterLifetime: 0.5,
  maxParticles: 10,
  behaviors: [
    ...DEFAULT_BEHAVIORS,
    ...DEFAULT_BEHAVIORS_PLANA_COLOR,
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
      type: "spawnShape",
      config: {
        type: "polygonalChain",
        data: [
          [
            { x: 378.4000244140625, y: 177.8000030517578 },
            { x: 361.60003662109375, y: 197.00001525878906 },
          ],
          [
            { x: 363.20001220703125, y: 177.0000305175781 },
            { x: 375.20001220703125, y: 193.8000030517578 },
          ],
          [
            { x: 377.60003662109375, y: 196.1999969482422 },
            { x: 380.79998779296875, y: 201.00001525878906 },
          ],
        ],
      },
    },
  ],
};
export const WatchSkyEmitterConfigB: EmitterConfigV3 = {
  ...DEFAULT_EMITTER_CONFIG,
  particlesPerWave: 10,
  emitterLifetime: 0.5,
  maxParticles: 10,
  behaviors: [
    ...DEFAULT_BEHAVIORS,
    ...DEFAULT_BEHAVIORS_ARONA_COLOR,
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
      type: "spawnShape",
      config: {
        type: "polygonalChain",
        data: [
          [
            { x: 401.60003662109375, y: 177.00003051757812 },
            { x: 421.60003662109375, y: 199.40000915527344 },
          ],
          [
            { x: 416, y: 182.59999084472656 },
            { x: 404, y: 209.8000030517578 },
          ],
          [
            { x: 400.79998779296875, y: 216.1999969482422 },
            { x: 413.60003662109375, y: 248.1999969482422 },
          ],
          [
            { x: 414.4000244140625, y: 222.59999084472656 },
            { x: 401.60003662109375, y: 235.40000915527344 },
          ],
        ],
      },
    },
  ],
};

export const AronaPeekEmitterConfig: EmitterConfigV3 = {
  ...DEFAULT_EMITTER_CONFIG,
  particlesPerWave: 10,
  emitterLifetime: 0.5,
  maxParticles: 10,
  behaviors: [
    ...DEFAULT_BEHAVIORS,
    ...DEFAULT_BEHAVIORS_ARONA_COLOR,
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
      type: "spawnShape",
      config: {
        type: "polygonalChain",
        data: [
          [
            { x: 400, y: 180.1999969482422 },
            { x: 405.60003662109375, y: 202.59999084472656 },
          ],
          [
            { x: 411.20001220703125, y: 186.59999084472656 },
            { x: 391.20001220703125, y: 194.59999084472656 },
          ],
          [
            { x: 387.20001220703125, y: 204.1999969482422 },
            { x: 394.4000244140625, y: 217.8000030517578 },
          ],
        ],
      },
    },
  ],
};

export const AronaEmitterConfig: EmitterConfigV3 = {
  ...DEFAULT_EMITTER_CONFIG,
  lifetime: {
    min: 0.25,
    max: 0.5,
  },
  particlesPerWave: 80,
  emitterLifetime: 0.5,
  maxParticles: 80,
  behaviors: [
    ...DEFAULT_BEHAVIORS,
    ...DEFAULT_BEHAVIORS_ARONA_COLOR,
    {
      type: "scale",
      config: {
        scale: {
          list: [
            {
              value: 0.3,
              time: 0,
            },
            {
              value: 0.15,
              time: 1,
            },
          ],
        },
      },
    },
    {
      type: "spawnShape",
      config: {
        type: "polygonalChain",
        data: [
          [
            { x: 189.60003662109375, y: 17.80000305175781 },
            { x: 185.60003662109375, y: 62.600006103515625 },
          ],
          [
            { x: 276.79998779296875, y: 68.19999694824219 },
            { x: 235.20001220703125, y: 77.80000305175781 },
          ],
          [
            { x: 147.20001220703125, y: 78.6000061035156 },
            { x: 240.79998779296875, y: 117.80000305175781 },
          ],
          [
            { x: 243.20001220703125, y: 127.40000915527344 },
            { x: 128.80001831054688, y: 230.59999084472656 },
          ],
          [
            { x: 131.20001220703125, y: 125.0000305175781 },
            { x: 215.20001220703125, y: 245.00001525878906 },
          ],
          [
            { x: 123.20001220703125, y: 246.59999084472656 },
            { x: 222.4000244140625, y: 290.59999084472656 },
          ],
          [
            { x: 228, y: 244.1999969482422 },
            { x: 119.20001220703125, y: 301.00001525878906 },
          ],
          [
            { x: 224.79998779296875, y: 303.40000915527344 },
            { x: 109.6000061035156, y: 369.80006408691406 },
          ],
          [
            { x: 127.20001220703125, y: 317.00001525878906 },
            { x: 240, y: 381.00001525878906 },
          ],
          [
            { x: 108, y: 390.59999084472656 },
            { x: 217.60003662109375, y: 448.99998474121094 },
          ],
          [
            { x: 247.20001220703125, y: 389.00001525878906 },
            { x: 135.20001220703125, y: 446.6000213623047 },
          ],
          [
            { x: 136, y: 448.1999969482422 },
            { x: 208.79998779296875, y: 497.80003356933594 },
          ],
          [
            { x: 219.20001220703125, y: 451.40000915527344 },
            { x: 141.6000061035156, y: 503.40000915527344 },
          ],
          [
            { x: 148, y: 531.4000091552734 },
            { x: 206.4000244140625, y: 552.9999847412109 },
          ],
          [
            { x: 209.60003662109375, y: 516.9999847412109 },
            { x: 143.20001220703125, y: 548.9999847412109 },
          ],
          [
            { x: 51.20001220703125, y: 375.40000915527344 },
            { x: 23.20001220703125, y: 392.1999969482422 },
          ],
          [
            { x: 280.79998779296875, y: 369.00001525878906 },
            { x: 327.20001220703125, y: 374.59999084472656 },
          ],
        ],
      },
    },
  ],
};

export const PlanaEmitterConfig: EmitterConfigV3 = {
  ...DEFAULT_EMITTER_CONFIG,
  lifetime: {
    min: 0.25,
    max: 0.5,
  },
  particlesPerWave: 80,
  emitterLifetime: 0.5,
  maxParticles: 80,
  behaviors: [
    ...DEFAULT_BEHAVIORS,
    ...DEFAULT_BEHAVIORS_PLANA_COLOR,
    {
      type: "scale",
      config: {
        scale: {
          list: [
            {
              value: 0.3,
              time: 0,
            },
            {
              value: 0.15,
              time: 1,
            },
          ],
        },
      },
    },
    {
      type: "spawnShape",
      config: {
        type: "polygonalChain",
        data: [
          [
            { x: 160.79998779296875, y: 43.400001525878906 },
            { x: 104.80001831054688, y: 145.0000305175781 },
          ],
          [
            { x: 188, y: 84.19999694824219 },
            { x: 251.20001220703125, y: 146.6000061035156 },
          ],
          [
            { x: 246.4000244140625, y: 107.40000915527344 },
            { x: 148.79998779296875, y: 193.00001525878906 },
          ],
          [
            { x: 156, y: 127.40000915527344 },
            { x: 247.20001220703125, y: 206.59999084472656 },
          ],
          [
            { x: 252, y: 233.8000030517578 },
            { x: 149.60003662109375, y: 317.8000030517578 },
          ],
          [
            { x: 136, y: 252.1999969482422 },
            { x: 261.60003662109375, y: 325.8000030517578 },
          ],
          [
            { x: 271.20001220703125, y: 315.40000915527344 },
            { x: 139.20001220703125, y: 411.40000915527344 },
          ],
          [
            { x: 141.60000610351562, y: 339.40000915527344 },
            { x: 267.20001220703125, y: 435.40000915527344 },
          ],
          [
            { x: 280.79998779296875, y: 428.1999969482422 },
            { x: 127.20001220703125, y: 514.6000213623047 },
          ],
          [
            { x: 140.80001831054688, y: 444.1999969482422 },
            { x: 264.79998779296875, y: 544.9999847412109 },
          ],
          [
            { x: 36.800018310546875, y: 425.80006408691406 },
            { x: 109.6000061035156, y: 433.00001525878906 },
          ],
          [
            { x: 375.20001220703125, y: 469.80003356933594 },
            { x: 286.4000244140625, y: 456.1999969482422 },
          ],
        ],
      },
    },
  ],
};
