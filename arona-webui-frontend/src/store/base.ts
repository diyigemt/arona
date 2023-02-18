import { defineStore } from "pinia";
import { BaseStoreState } from "./type";
import ContactApi from "@/api/modules/contact";
import { Friend, Member } from "@/types/contact";
import { HTTP_OK } from "@/constant/http";
import { UserData } from "@/api/modules/blockly";
import { ExtendGroup } from "@/interface/modules/contact";
import ConfigApi from "@/api/modules/config";

const useBaseStore = defineStore({
  id: "common",
  state: (): BaseStoreState => ({
    config: {},
    activeGroupId: 0,
    contactList: [],
  }),
  getters: {
    /**
     * 拿到当前配置的群的信息
     *
     * 注意, 如果当前没有选择任何群, 返回的信息为{id: 0, name: "全部"}
     */
    activeGroup(ctx: BaseStoreState): () => ExtendGroup {
      return () => {
        return this.findExtendGroupById(ctx.activeGroupId);
      };
    },
    /**
     * 拿到所有群列表或者拿到对应bot的群列表
     *
     * Params: bot? - 指定的机器人qq号
     */
    groups(ctx: BaseStoreState): (bot?: number) => ExtendGroup[] {
      return (bot?: number) => {
        const list = bot ? ctx.contactList.filter((contact) => contact.bot === bot) : ctx.contactList;
        return list.map((contact) => contact.groups).flat(1);
      };
    },
    /**
     * 拿到所有好友列表或者拿到对应bot的好友列表
     *
     * Params: bot? - 指定的机器人qq号
     */
    friends(ctx: BaseStoreState): (bot?: number) => Friend[] {
      return (bot?: number) => {
        const list = bot ? ctx.contactList.filter((contact) => contact.bot === bot) : ctx.contactList;
        return list.map((contact) => contact.friends).flat(1);
      };
    },
    /**
     * 根据群id获取群的详细信息
     *
     * 注意, 如果群不存在, 返回的信息为{id: 0, name: "全部"}
     */
    findExtendGroupById(): (id: number) => ExtendGroup {
      return (id: number) => {
        const ac = this.groups().filter((group) => group.id === id);
        if (ac.length === 0) {
          return DEFAULT_GROUP;
        }
        return ac[0];
      };
    },
    /**
     * 根据key拿到配置信息
     *
     * Params: key - 指定key
     */
    getConfig(ctx: BaseStoreState): <T>(key: string) => T {
      return <T>(key: string): T => {
        const data = Reflect.get(ctx.config, key);
        // 假设所有配置都是json对象
        if (typeof data === "string") {
          try {
            const aCast = JSON.parse(data);
            this.setConfig(key, aCast);
            return aCast;
          } catch (e: unknown) {
            return data as T;
          }
        }
        return data as T;
      };
    },
    setConfig(ctx: BaseStoreState): (key: string, value: unknown) => void {
      return (key: string, value: unknown) => {
        Reflect.set(ctx.config, key, value);
      };
    },
  },
  actions: {
    setActiveGroupId(group?: number) {
      this.activeGroupId = group || 0;
    },
    /**
     * 拿到给定群的成员列表
     *
     * 如果同时指定了bot, 则只在该bot的群列表中获取, 否则查找所有bot
     * @param group 指定群号
     * @param bot 指定的机器人qq号
     */
    members(group: number, bot?: number) {
      return new Promise<Member[]>((resolve, reject) => {
        const group0 = this.groups(bot);
        if (group0.length === 0) {
          reject(new Error("获取群列表失败"));
          return;
        }
        const groupList = group0.filter((group$) => group$.id === group);
        if (groupList.length === 0) {
          reject(new Error("获取群失败"));
          return;
        }
        const { member } = groupList[0];
        // 尝试获取
        if (!member || member.length === 0) {
          ContactApi.fetchGroupMember(group, bot).then((data) => {
            if (data.code === HTTP_OK) {
              // 缓存一下
              groupList[0].member = data.data;
              resolve(data.data);
            } else {
              reject(new Error("获取群员列表失败"));
            }
          });
          return;
        }
        resolve(member);
      });
    },
    /**
     * [member]的同步方法
     * @param group 指定群号
     * @param bot 指定的机器人qq号
     */
    memberSync(group: number, bot?: number): Member[] {
      const group0 = this.groups(bot);
      if (group0.length === 0) {
        return [];
      }
      const groupList = group0.filter((group$) => group$.id === group);
      if (groupList.length === 0) {
        return [];
      }
      return groupList[0].member;
    },
    /**
     * 通过反序列化加载数据
     * @param json JSON字符串
     * @param callBack 当数据准备就绪时的回调，可能会失败几次
     */
    // TODO 群员不存在时加载失败
    loadDataFromSave(json: string, callBack: () => void) {
      const userData = JSON.parse(json) as UserData;
      const execute = () => {
        console.log("call");
        return userData.groups.map((item) => {
          return this.members(item);
        });
      };
      Promise.all(execute())
        .then(callBack)
        .catch(() => {
          setTimeout(this.loadDataFromSave, 70, json, callBack);
        });
    },
    /**
     * 拿到所有管理的bot的联系人列表
     */
    fetchBotContact() {
      return ContactApi.fetchBotContacts().then((data) => {
        this.contactList = data.data;
        return data;
      });
    },
    /**
     * 拿到所有配置文件信息
     */
    fetchConfig() {
      return ConfigApi.fetchConfig().then((data) => {
        this.config = data.data;
        return data;
      });
    },
  },
  persist: {
    key: "base",
    paths: [],
  },
});

const DEFAULT_GROUP: ExtendGroup = {
  id: 0,
  name: "全部",
  permission: "ADMINISTRATOR",
  member: [],
};

export default useBaseStore;
