import { forOwn } from "lodash";
import { AronaConfigForm, AronaConfigMap } from "@/interface";
import service from "../http";

// eslint-disable-next-line import/prefer-default-export
export function fetchAronaMainConfig() {
  return service.raw<AronaConfigMap>({
    url: "/config/aronaConfig",
    method: "GET",
  });
}

export function saveAronaMainConfig(config: AronaConfigForm) {
  const MainConfigKey = "AronaConfig";
  const data = {};
  forOwn(config, (value, key) => {
    Reflect.set(data, `${MainConfigKey}.${key}`, value);
  });
  return service.raw<null>({
    url: "/test",
    method: "POST",
    data,
  });
}
