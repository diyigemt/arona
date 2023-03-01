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

const DEFAULT_BEHAVIORS: BehaviorEntry[] = [
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

export const SleepEmitterConfig: EmitterConfigV3 = {
  ...DEFAULT_EMITTER_CONFIG,
  particlesPerWave: 30,
  emitterLifetime: 0.5,
  maxParticles: 30,
  behaviors: [
    ...DEFAULT_BEHAVIORS,
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

export const WatchEmitterConfig: EmitterConfigV3 = {
  ...DEFAULT_EMITTER_CONFIG,
  particlesPerWave: 30,
  emitterLifetime: 0.5,
  maxParticles: 30,
  behaviors: [
    ...DEFAULT_BEHAVIORS,
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

export const SitEmitterConfig: EmitterConfigV3 = {
  ...DEFAULT_EMITTER_CONFIG,
  particlesPerWave: 50,
  emitterLifetime: 0.5,
  maxParticles: 50,
  behaviors: [
    ...DEFAULT_BEHAVIORS,
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

export const StandEmitterConfig: EmitterConfigV3 = {
  ...DEFAULT_EMITTER_CONFIG,
  lifetime: {
    min: 0.2,
    max: 0.35,
  },
  particlesPerWave: 30,
  emitterLifetime: 0.35,
  maxParticles: 30,
  behaviors: [
    ...DEFAULT_BEHAVIORS,
    {
      type: "scale",
      config: {
        scale: {
          list: [
            {
              value: 0.8,
              time: 0,
            },
            {
              value: 0.6,
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
            { x: 580, y: 284 },
            { x: 432, y: 410 },
          ],
          [
            { x: 648, y: 203 },
            { x: 822, y: 296 },
          ],
          [
            { x: 846, y: 354 },
            { x: 889, y: 546 },
          ],
          [
            { x: 432, y: 690 },
            { x: 421, y: 883 },
          ],
          [
            { x: 818, y: 691 },
            { x: 861, y: 877 },
          ],
          [
            { x: 436, y: 1072 },
            { x: 390, y: 1260 },
          ],
          [
            { x: 817, y: 1088 },
            { x: 923, y: 1250 },
          ],
          [
            { x: 374, y: 1392 },
            { x: 434, y: 1579 },
          ],
          [
            { x: 896, y: 1392 },
            { x: 848, y: 1582 },
          ],
        ],
      },
    },
  ],
};
