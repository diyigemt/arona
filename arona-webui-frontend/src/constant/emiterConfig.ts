import { EmitterConfigV3 } from "@pixi/particle-emitter/lib/EmitterConfig";
import { Texture } from "pixi.js";

export const SleepEmitterConfig: EmitterConfigV3 = {
  lifetime: {
    min: 0.3,
    max: 0.5,
  },
  frequency: 0.01,
  spawnChance: 1,
  particlesPerWave: 30,
  emitterLifetime: 0.5,
  maxParticles: 30,
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
      type: "moveSpeedStatic",
      config: {
        min: 40,
        max: 40,
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
};

export const StandEmitterConfig: EmitterConfigV3 = {
  lifetime: {
    min: 0.2,
    max: 0.3,
  },
  frequency: 0.01,
  spawnChance: 1,
  particlesPerWave: 50,
  emitterLifetime: 0.3,
  maxParticles: 50,
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
              value: 0.9,
              time: 0,
            },
            {
              value: 0.7,
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
      type: "moveSpeedStatic",
      config: {
        min: 40,
        max: 40,
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
    {
      type: "textureSingle",
      config: {
        texture: Texture.from("/image/FX_TEX_Triangle_02_a.png"),
      },
    },
  ],
};
