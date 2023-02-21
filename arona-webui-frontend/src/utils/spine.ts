import { Application, Loader } from "pixi.js";
import { SpineParser, Spine } from "pixi-spine";
import * as PIXI from "pixi.js";
import { ITrackEntry } from "@pixi-spine/base";

(window as any).__PIXI_INSPECTOR_GLOBAL_HOOK__ && (window as any).__PIXI_INSPECTOR_GLOBAL_HOOK__.register({ PIXI });
Loader.registerPlugin(SpineParser);
const SpineManager: SpineManager = {
  get(name: string): SpineInstance {
    return this._map.get(name)!;
  },
  createSpine(config: SpineInstanceConfig): Promise<SpineInstance> {
    return new Promise<SpineInstance>((resolve) => {
      const app = new Application({ width: 100, height: 100, backgroundAlpha: 0 });
      const { name } = config;
      const offsetX = config.offsetX || 0;
      const offsetY = config.offsetY || 0;
      let ratio = 0;
      app.loader.add(name, config.url);
      app.loader.load((_, resources) => {
        const spineResource = Reflect.get(resources, "arona");
        if (!spineResource) {
          return;
        }
        const spine = new Spine(spineResource.spineData!);
        const sourceWidth = spine.width;
        const sourceHeight = spine.height;
        ratio = sourceWidth / sourceHeight;
        const scale = config.height ? config.height / spine.height : 1;
        spine.pivot.set(-0.5 * spine.width, -0.5 * spine.height);
        spine.scale.set(scale);
        spine.position.set(offsetX, offsetY);
        app.stage.addChild(spine);
        app.renderer.on("resize", (_weight: number, _height: number) => {
          const _scale = _height / (spine.height / spine.scale.y);
          spine.scale.set(_scale);
          const x = (spine.width / sourceWidth) * offsetX;
          const y = (spine.height / sourceHeight) * offsetY;
          spine.position.set(x, y);
        });
        const x = (spine.width / sourceWidth) * offsetX;
        const y = (spine.height / sourceHeight) * offsetY;
        spine.position.set(x, y);
        app.renderer.resize(spine.width, spine.height);
        config.el.appendChild(app.view);
        const res = {
          app,
          el: config.el,
          name: config.name,
          spine: app.stage.children[0] as Spine,
          width(): number {
            return this.app.stage.width;
          },
          height(): number {
            return this.app.stage.height;
          },
          scale(): number {
            return this.spine.scale.y;
          },
          resize(height: number) {
            this.app.renderer.resize(height * this._ratio, height);
          },
          setAnimation(trackIndex: number, animationName: string, loop: boolean): ITrackEntry {
            return this.spine.state.setAnimation(trackIndex, animationName, loop);
          },
          addAnimation(trackIndex: number, animationName: string, loop: boolean, delay: number): ITrackEntry {
            return this.spine.state.addAnimation(trackIndex, animationName, loop, delay);
          },
          _ratio: ratio,
        };
        this._map.set(name, res);
        resolve(res);
      });
    });
  },
  _map: new Map(),
};

interface SpineInstanceConfig {
  el: HTMLElement;
  name: string;
  url: string;
  height?: number;
  offsetX?: number;
  offsetY?: number;
  reactConfig?: SpineInstanceReactConfig[];
}

export interface SpineInstanceReactConfig {
  name: string;
  url: string;
  text: string;
  animation: string;
}

interface SpineInstance {
  app: Application;
  spine: Spine;
  el: HTMLElement;
  name: string;
  width(): number;
  height(): number;
  scale(): number;
  resize(height: number): void;
  setAnimation(trackIndex: number, animationName: string, loop: boolean): ITrackEntry;
  addAnimation(trackIndex: number, animationName: string, loop: boolean, delay: number): ITrackEntry;
  _ratio: number;
}

interface SpineManager {
  get(name: string): SpineInstance;
  createSpine(config: SpineInstanceConfig): Promise<SpineInstance>;
  _map: Map<string, SpineInstance>;
}

export default SpineManager;
