import { defineStore } from "pinia";
import { BaseStoreState } from "./type";
import { fetchBotContacts, fetchGroupMember } from "@/api/modules/contact";
import { Friend, Member } from "@/types/contact";
import { ExtendGroup } from "@/interface/http";
import { HTTP_OK } from "@/constant/http";
import { UserData } from "@/api/modules/blockly";

const useBaseStore = defineStore({
  id: "common",
  state: (): BaseStoreState => ({
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
  },
  actions: {
    setActiveGroupId(group?: number) {
      this.activeGroupId = group || 0;
    },
    fetchBotContact() {
      return fetchBotContacts().then((contact) => {
        this.contactList = [contact.data];
        return contact;
      });
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
          fetchGroupMember(group, bot).then((data) => {
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
     */
    // TODO 群员不存在时加载失败
    loadDataFromSave(json: string) {
      const userData = JSON.parse(json) as UserData;
      return Promise.all(
        userData.members.map((item) => {
          return this.members(item.groupId);
        }),
      );
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
