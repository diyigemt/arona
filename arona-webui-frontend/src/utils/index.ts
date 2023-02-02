import { forOwn } from "lodash";
import { Config2Form, Config2Map } from "@/interface";
import { Ref } from "vue";

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

export function fillForm<IForm extends object>(form: IForm, data: IForm, keys: (keyof IForm)[]) {
  keys.forEach((key) => {
    const value = Reflect.get(data, key);
    Reflect.set(form, key, value);
  });
}

export function dataFilterChain<T, Form extends object>(
  data: T[],
  filterForm: Form,
  filterFn: ((data: T[], value: any) => T[])[],
  keys: (keyof Form)[],
): T[] {
  if (keys.length !== filterFn.length) {
    return data;
  }
  filterFn.forEach((fn, index) => {
    const key = keys[index];
    const value = Reflect.get(filterForm, key);
    if (!value || (Array.isArray(value) && value.length === 0)) {
      return;
    }
    data = fn(data, value);
  });
  return data;
}
