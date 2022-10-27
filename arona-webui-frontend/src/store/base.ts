import { defineStore } from "pinia";
import { Friend } from "@/types/contact";
import { BotContact } from "@/interface/http";
import { fetchBotContacts } from "@/api/modules/contact";

type BaseStoreState = BotContact;

const useBaseStore = defineStore({
  id: "common",
  state: (): BaseStoreState => ({
    groups: [],
    friends: [],
  }),
  getters: {
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
  },
  persist: true,
});

export default useBaseStore;
