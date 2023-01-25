import { Request, Response } from "express";
import { packageResponse } from "../utils";
import { AronaConfig, BotGroupConfig } from "../../src/interface";
// eslint-disable-next-line import/prefer-default-export
export function getConfig(_: Request, response: Response): Promise<Response> {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve(
        packageResponse(
          response,
          buildConfig([
            AronaMainConfig,
            AronaNotifyConfig,
            AronaTarotConfig,
            AronaRepeatConfig,
            AronaTrainerConfig,
            AronaNGAConfig,
            AronaRemoteConfig,
            AronaWebuiConfig,
          ]),
        ),
      );
    }, 50);
  });
}

const BotConfig: BotGroupConfig[] = [
  {
    bot: 135524723,
    groups: [1355247242],
  },
];
const AronaMainConfig: AronaConfig = {
  bots: BotConfig,
  managerGroup: [1355247243],
  everyDayHour: 8,
  onlineMessage: "",
  offlineMessage: "",
  permissionDeniedMessage: "",
};
const AronaNotifyConfig: AronaConfig = {
  enableJP: "",
  enableEN: "",
  notifyStringJP: "",
  notifyStringEN: "",
  defaultActivityCommandServer: "",
};
const AronaTarotConfig: AronaConfig = {
  dayOne: true,
  image: true,
};
const AronaRepeatConfig: AronaConfig = {
  repeat: 3,
};
const AronaTrainerConfig: AronaConfig = {
  tipWhenNull: true,
};
const AronaNGAConfig: AronaConfig = {
  uid: "",
  cid: "",
  checkInterval: "",
  source: "",
  cache: "",
};
const AronaRemoteConfig: AronaConfig = {
  checkInterval: 3,
};
const AronaWebuiConfig: AronaConfig = {
  port: 3,
};

function buildConfig(configs: AronaConfig[]): AronaConfig {
  const builder = {};
  configs.forEach((config) => {
    Object.keys(config).forEach((key) => {
      Reflect.set(builder, key, Reflect.get(config, key));
    });
  });
  return builder;
}
