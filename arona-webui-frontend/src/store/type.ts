import { BotContact } from "@/interface/http";

export interface BaseStoreState {
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
