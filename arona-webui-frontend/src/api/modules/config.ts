import { forOwn } from "lodash";
import {
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
    url: "/config/commit",
    method: "POST",
    data,
  });
}
