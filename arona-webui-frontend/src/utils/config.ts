import useBaseStore from "@/store/base";

const GlobalConfigProvider = {
  get<T>(key: string): T {
    const baseStore = useBaseStore();
    return baseStore.getConfig(key);
  },
  getGroup<T>(key: string, group?: number): T {
    const baseStore = useBaseStore();
    if (!group) {
      group = baseStore.activeGroupId;
    }
    return this.get(this.concatGroupKey(key, group));
  },
  concatGroupKey(key: string, group: number): string {
    return !group || group === 0 ? key : `${group}.${key}`;
  },
};
