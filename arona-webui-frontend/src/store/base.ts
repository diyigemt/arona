import { defineStore } from "pinia";
import { BaseStoreState } from "./type";
import { fetchBotContacts, fetchGroupMember } from "@/api/modules/contact";
import { Friend, Group, Member } from "@/types/contact";
import { ExtendGroup } from "@/interface/http";
import { HTTP_OK } from "@/constant/http";

const useBaseStore = defineStore({
  id: "common",
  state: (): BaseStoreState => ({
    contactList: [],
  }),
  getters: {
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
  },
  actions: {
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
     * @param group0 指定群号
     * @param bot 指定的机器人qq号
     */
    members(group0: number, bot?: number) {
      return new Promise<Member[]>((resolve, reject) => {
        const group = this.groups(bot);
        if (group.length === 0) {
          reject(new Error("获取群列表失败"));
          return;
        }
        const groupList = group.filter((group$) => group$.id === group0);
        if (groupList.length === 0) {
          reject(new Error("获取群失败"));
          return;
        }
        const { member } = groupList[0];
        // 尝试获取
        if (!member || member.length === 0) {
          fetchGroupMember(group0, bot).then((data) => {
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
     * @param group0 指定群号
     * @param bot 指定的机器人qq号
     */
    memberSync(group0: number, bot?: number): Member[] {
      const group = this.groups(bot);
      if (group.length === 0) {
        return [];
      }
      const groupList = group.filter((group$) => group$.id === group0);
      if (groupList.length === 0) {
        return [];
      }
      return groupList[0].member;
    },
  },
  persist: {
    key: "base",
    paths: [],
  },
});

export default useBaseStore;
