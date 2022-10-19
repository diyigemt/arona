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
export type AronaConfigMap = Config2Map<AronaConfig>;
export type AronaConfigForm = Config2Form<AronaConfig>;
