import { defineStore } from "pinia";
import { updateAPIService } from "@/api/http";
import { SettingStoreState } from "./type";

const useSettingStore = defineStore({
  id: "setting",
  state: (): SettingStoreState => {
    return {
      theme: {
        themeType: "亮蓝色",
        themeColor: "#2080F0FF",
      },
      api: {
        host: undefined,
        port: undefined,
      },
    };
  },
  getters: {
    getThemeType: (state: SettingStoreState) => state.theme.themeType,
    getThemeColor: (state: SettingStoreState) => state.theme.themeColor,
    isRestoreBackend: (state: SettingStoreState) => state.api.host && state.api.port,
  },
  actions: {
    setThemeType(type: string) {
      this.theme.themeType = type;
    },
    saveAPISetting(host: string, port: number) {
      this.api.host = host;
      this.api.port = port;
    },
  },
  persist: {
    afterRestore: (ctx) => {
      if (ctx.store.isRestoreBackend) {
        updateAPIService(ctx.store.api.host, ctx.store.api.port);
      }
    },
  },
});

export default useSettingStore;
