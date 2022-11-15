import { Config2Form, Config2Map } from "..";

// eslint-disable-next-line import/prefer-default-export
export const enum AvailableConfig {
  AronaConfig = "AronaConfig",
  AronaGachaConfig = "AronaGachaConfig",
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

interface AronaReplyConfigReplyRule {
  rule: AronaReplyConfigReplyMatchTree;
  messages: AronaReplyConfigReplyMessage[];
  messageType: AronaReplyConfigMessageType;
}

enum AronaReplyConfigCondition {
  AND = "AND",
  OR = "OR",
  NOT = "NOT",
}

enum AronaReplyConfigMatchType {
  SUFFIX = "SUFFIX",
  PREFIX = "PREFIX",
  CONTAINS = "CONTAINS",
  ACCURATE = "ACCURATE",
  TARGET = "TARGET",
}

enum AronaReplyConfigMessageType {
  MESSAGE = "MESSAGE",
  NUDGE = "NUDGE",
}

interface AronaReplyConfigReplyMatchTree {
  left: AronaReplyConfigReplyMatchTree;
  right: AronaReplyConfigReplyMatchTree;
  condition: AronaReplyConfigCondition;
  type: AronaReplyConfigMatchType;
  value: string;
}

interface AronaReplyConfigReplyMessage {
  message: string;
  weight: number;
}

export interface AronaReplyConfig {
  rules: AronaReplyConfigReplyRule[];
}
