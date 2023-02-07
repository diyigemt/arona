import { Request, Response } from "express";
import { packageResponse } from "../utils";
import { ServerAronaConfig, BotGroupConfig } from "../../src/interface";
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
const AronaMainConfig: ServerAronaConfig = {
  bots: BotConfig,
  managerGroup: [1355247243],
  everyDayHour: 8,
  onlineMessage: "",
  offlineMessage: "",
  permissionDeniedMessage: "",
};
const AronaNotifyConfig: ServerAronaConfig = {
  enableJP: "",
  enableEN: "",
  notifyStringJP: "",
  notifyStringEN: "",
  defaultActivityCommandServer: "",
};
const AronaTarotConfig: ServerAronaConfig = {
  dayOne: true,
  image: true,
};
const AronaRepeatConfig: ServerAronaConfig = {
  repeat: 3,
};
const AronaTrainerConfig: ServerAronaConfig = {
  tipWhenNull: true,
};
const AronaNGAConfig: ServerAronaConfig = {
  uid: "",
  cid: "",
  checkInterval: "",
  source: "",
  cache: "",
};
const AronaRemoteConfig: ServerAronaConfig = {
  checkInterval: 3,
};
const AronaWebuiConfig: ServerAronaConfig = {
  port: 3,
};

function buildConfig(configs: ServerAronaConfig[]): ServerAronaConfig {
  const builder = {};
  configs.forEach((config) => {
    Object.keys(config).forEach((key) => {
      Reflect.set(builder, key, Reflect.get(config, key));
    });
  });
  return builder;
}
