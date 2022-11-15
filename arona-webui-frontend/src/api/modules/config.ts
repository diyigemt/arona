import { forOwn } from "lodash";
import {
  AronaConfig,
  AronaConfigForm,
  AronaConfigMap,
  AronaGachaConfig,
  AronaGachaConfigForm,
  AronaGachaConfigMap,
  AvailableConfig,
  Config2Form,
} from "@/interface";
import service from "../http";

export function fetchAronaConfig<T>(config: AvailableConfig) {
  return service.raw<T>({
    url: `/config/${config}`,
    method: "GET",
  });
}

export function saveAronaConfig<T, D>(config: Config2Form<T>, configKey: AvailableConfig) {
  const data = {};
  forOwn(config, (value, key) => {
    Reflect.set(data, `${configKey}.${key}`, value);
  });
  return service.raw<D>({
    url: "/commit",
    method: "POST",
    data,
  });
}

export function fetchAronaMainConfig() {
  return fetchAronaConfig<AronaConfigMap>(AvailableConfig.AronaConfig);
}

export function fetchAronaGachaConfig() {
  return fetchAronaConfig<AronaGachaConfigMap>(AvailableConfig.AronaGachaConfig);
}

export function saveAronaMainConfig(config: AronaConfigForm) {
  return saveAronaConfig<AronaConfig, null>(config, AvailableConfig.AronaConfig);
}

export function saveAronaGachaConfig(config: AronaGachaConfigForm) {
  return saveAronaConfig<AronaGachaConfig, null>(config, AvailableConfig.AronaGachaConfig);
}
