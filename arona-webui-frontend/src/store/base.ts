import { defineStore } from "pinia";
import { BaseStoreState } from "./type";

const useBaseStore = defineStore({
  id: "common",
  state: (): BaseStoreState => ({}),
  getters: {},
  actions: {},
});

export default useBaseStore;
