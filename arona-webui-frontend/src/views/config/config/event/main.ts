import mitt from "mitt";
import useBaseStore from "@/store/base";
import {Friend} from "@/types/contact";

type Events = {
  "contact-update": {
    uuid: string;
    value: number;
  };
  "bot-list-update": Friend[];
};

const MainConfigEmitter = mitt<Events>();
// 保存drag实例和其对应的群号/机器人q号的关系
const uuidMap = ref(new Map<string, number>());
const botList = ref<Friend[]>([]);
function updateContact(data: { uuid: string; value: number }) {
  if (data.value) {
    uuidMap.value.set(data.uuid, data.value);
  } else {
    uuidMap.value.delete(data.uuid);
  }
}
function updateBotList(data: Friend[]) {
  botList.value = data;
}
MainConfigEmitter.on("contact-update", updateContact);
MainConfigEmitter.on("bot-list-update", updateBotList);

export const freeGroup = computed(() => {
  const baseStore = useBaseStore();
  return baseStore.groups().filter((group) => ![...uuidMap.value.values()].some((it) => it === group.id));
});
export const freeBot = computed(() => {
  return botList.value.filter((bot) => ![...uuidMap.value.values()].some((it) => it === bot.id));
});

export default MainConfigEmitter;
