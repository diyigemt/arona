import { defineStore } from "pinia";
import { SettingStoreState } from "./type";
import { NetworkAdapterType } from "@/interface/http";
import { updateAPIService } from "@/api/adapter/localhost";

const useSettingStore = defineStore({
  id: "setting",
  state: (): SettingStoreState => ({
    adapter: "Localhost",
    theme: {
      themeType: "亮蓝色",
      themeColor: "#2080F0FF",
    },
    api: {
      host: undefined,
      port: undefined,
    },
  }),
  getters: {
    getThemeType: (state: SettingStoreState) => state.theme.themeType,
    getThemeColor: (state: SettingStoreState) => state.theme.themeColor,
    isRestoreBackend: (state: SettingStoreState) => state.api.host && state.api.port,
  },
  actions: {
    setAdapter(adapter: NetworkAdapterType) {
      this.adapter = adapter;
    },
    setThemeType(type: string) {
      this.theme.themeType = type;
    },
    saveAPISetting(host: string, port: number) {
      this.api.host = host;
      this.api.port = port;
    },
  },
  persist: {
    key: "setting",
    afterRestore: (ctx) => {
      if (ctx.store.isRestoreBackend) {
        updateAPIService(ctx.store.api.host, ctx.store.api.port);
      }
    },
  },
});

export default useSettingStore;
