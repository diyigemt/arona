import { forOwn } from "lodash";
import { Config2Form, Config2Map } from "@/interface";

export function extraDomBounding(dom: HTMLElement) {
  const style = getComputedStyle(dom);
  return {
    width: Number(style.width.replace("px", "")),
    height: Number(style.width.replace("px", "")),
  };
}

export function fillConfigMap<T>(remote: Config2Map<T>, local: Config2Form<T>) {
  forOwn(remote, (value, key) => {
    Reflect.set(local, key, value.value);
  });
}

export function deepCopy<T>(data: T): T {
  return JSON.parse(JSON.stringify(data));
}