import { Config2Form, Config2Map } from "..";

// eslint-disable-next-line import/prefer-default-export
export const enum AvailableConfig {
  AronaConfig = "AronaConfig",
  AronaGachaConfig = "AronaGachaConfig",
  AronaReplyConfig = "AronaReplyConfig",
}

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

export interface AronaReplyConfigReplyRule {
  name: string;
  rule: AronaReplyConfigReplyMatchTree;
  messages: AronaReplyConfigReplyMessage[];
  messageType: AronaReplyConfigMessageType;
}

export enum AronaReplyConfigCondition {
  AND = "AND",
  OR = "OR",
  NOT = "NOT",
}

export enum AronaReplyConfigMatchType {
  SUFFIX = "SUFFIX",
  PREFIX = "PREFIX",
  CONTAINS = "CONTAINS",
  ACCURATE = "ACCURATE",
  SENDER = "SENDER",
  GROUP = "GROUP",
}

export enum AronaReplyConfigMessageType {
  MESSAGE = "MESSAGE",
  NUDGE = "NUDGE",
}

export interface AronaReplyConfigReplyMatchTree {
  left?: AronaReplyConfigReplyMatchTree;
  right?: AronaReplyConfigReplyMatchTree;
  condition?: AronaReplyConfigCondition;
  type?: AronaReplyConfigMatchType;
  value?: string;
  id: string;
  level: number;
  children?: AronaReplyConfigReplyMatchTree[];
  childrenCache?: AronaReplyConfigReplyMatchTree[];
  leaf: boolean;
}

export interface AronaReplyConfigReplyMessage {
  message: string;
  weight: number;
}

export interface AronaReplyConfig {
  rules: AronaReplyConfigReplyRule[];
}
