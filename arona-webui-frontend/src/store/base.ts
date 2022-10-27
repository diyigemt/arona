import { defineStore, StoreActions, StoreGetters, StoreState } from "pinia";
import { BotContact } from "@/interface/http";
import { fetchBotContacts } from "@/api/modules/contact";
import { currentAPI, updateAPIService } from "@/api/http";

interface BaseStoreState extends BotContact {
  host?: string;
  port?: number;
}

const useBaseStore = defineStore({
  id: "common",
  state: (): BaseStoreState => ({
    host: undefined,
    port: undefined,
    groups: [],
    friends: [],
  }),
  getters: {
    baseURL(state: BaseStoreState) {
      if (state.host && state.port) {
        currentAPI(state.host, state.port);
      }
      return undefined;
    },
    botGroups(state: BaseStoreState) {
      return state.groups;
    },
    botFriends(state: BaseStoreState) {
      return state.friends;
    },
  },
  actions: {
    async syncContacts() {
      fetchBotContacts().then((res) => {
        this.groups = res.data.groups;
        this.friends = res.data.friends;
      });
    },
    saveAPISetting(host: string, port: number) {
      this.host = host;
      this.port = port;
      updateAPIService(this.baseURL!);
    },
  },
  persist: {
    afterRestore: (ctx) => {
      updateAPIService(ctx.store.baseURL);
    },
  },
});

export default useBaseStore;
