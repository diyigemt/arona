import mitt from "mitt";
import useBaseStore from "@/store/base";

type Events = {
  "api-update": undefined;
};

const emitter = mitt<Events>();

export function initEventBus() {
  const baseStore = useBaseStore();
  emitter.on("api-update", baseStore.fetchBotContact);
  emitter.on("api-update", baseStore.fetchConfig);
}

export default emitter;
