import service from "@/api/http";
import { BotContact } from "@/interface/http";
import { Friend, Member } from "@/types/contact";

/**
 * 根据bot实例获取bot加入的群以及好友
 * @returns bot加入的群信息
 */
export function fetchBotContacts() {
  return service.raw<BotContact>({
    url: "/contacts",
    method: "GET",
  });
}

/**
 * 获取一个群的所有联系人列表
 * @param group 群id
 * @param bot 通过指定的bot获取
 */
export function fetchGroupMember(group: number, bot?: number) {
  return service.raw<Member[]>({
    url: `/contacts/${group}`,
    method: "POST",
    data: {
      group,
      bot,
    },
  });
}
