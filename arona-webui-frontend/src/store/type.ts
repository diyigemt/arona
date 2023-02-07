import { ServerAronaConfig } from "@/interface";
import { BotContact } from "@/interface/modules/contact";
import { NetworkAdapterType } from "@/interface/http";

export interface BaseStoreState {
  config: ServerAronaConfig; // 配置文件大全
  activeGroupId: number; // 当前配置文件指向的group配置
  contactList: BotContact[]; // 所有arona能访问到的联系人信息
}

export interface SettingStoreState {
  adapter: NetworkAdapterType;
  theme: {
    themeType: string;
    themeColor: string | number;
  };
  api: {
    host?: string;
    port?: number;
  };
}
