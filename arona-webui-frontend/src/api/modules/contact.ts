import service from "@/api/http";
import { BotContact } from "@/interface/http";

/**
 * 根据bot实例获取bot加入的群以及好友
 * @returns bot加入的群信息
 */
// eslint-disable-next-line import/prefer-default-export
export function fetchBotContacts() {
  return service.raw<BotContact>({
    url: "/contacts",
    method: "GET",
  });
}
