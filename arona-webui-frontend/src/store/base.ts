import { defineStore } from "pinia";
import { BaseStoreState } from "./type";
import { fetchBotContacts } from "@/api/modules/contact";

const useBaseStore = defineStore({
  id: "common",
  state: (): BaseStoreState => ({
    contactList: [],
  }),
  getters: {
    groups(ctx: BaseStoreState) {
      return ctx.contactList.map((contact) => contact.groups).flat(1);
    },
  },
  actions: {
    fetchBotContact() {
      return fetchBotContacts().then((contact) => {
        this.contactList = [contact.data];
      });
    },
  },
  persist: {
    key: "base",
  },
});

export default useBaseStore;
