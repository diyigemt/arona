import { forOwn } from "lodash";
import {AronaConfig, AronaConfigForm, AronaConfigMap, Config2Form} from "@/interface";
import service from "../http";

export function fetchAronaConfig<T>(config: string) {
  return service.raw<T>({
    url: `/config/${config}`,
    method: "GET",
  });
}

export function saveAronaConfig<T>(config: Config2Form<T>, configKey: string) {
  const data = {};
  forOwn(config, (value, key) => {
    Reflect.set(data, `${configKey}.${key}`, value);
  });
  return service.raw<null>({
    url: "/commit",
    method: "POST",
    data,
  });
}

export function fetchAronaMainConfig() {
  return fetchAronaConfig<AronaConfigMap>("aronaConfig");
}

export function saveAronaMainConfig(config: AronaConfigForm) {
  return saveAronaConfig<AronaConfig>(config, "AronaConfig");
}
