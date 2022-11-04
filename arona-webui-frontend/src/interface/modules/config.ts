import { Config2Form, Config2Map } from "..";

export interface AronaConfig {
  qq: number;
  groups: number[];
  managerGroup: number[];
  permissionDeniedMessage: string;
  sendOnlineMessage: boolean;
  onlineMessage: string;
  sendOfflineMessage: boolean;
  offlineMessage: string;
  updateCheckTime: number;
  endWithSensei: string;
  sendStatus: false;
  uuid: string;
  remoteCheckInterval: number;
}

export interface AronaGachaConfig {
  star1Rate: number;
  star2Rate: number;
  star3Rate: number;
  star2PickupRate: number;
  star3PickupRate: number;
  activePool: number;
  revokeTime: number;
  limit: number;
  day: number;
}

export type AronaConfigMap = Config2Map<AronaConfig>;
export type AronaConfigForm = Config2Form<AronaConfig>;
export type AronaGachaConfigMap = Config2Map<AronaGachaConfig>;
export type AronaGachaConfigForm = Config2Form<AronaGachaConfig>;