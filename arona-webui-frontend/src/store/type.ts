import { BotContact } from "@/interface/http";

export interface BaseStoreState {
  activeGroupId: number; // 当前配置文件指向的group配置
  contactList: BotContact[]; // 所有arona能访问到的联系人信息
}

export interface SettingStoreState {
  theme: {
    themeType: string;
    themeColor: string | number;
  };
  api: {
    host?: string;
    port?: number;
  };
}
