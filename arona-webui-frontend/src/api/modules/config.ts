import service from "@/api/http";
import { Group } from "@/types/contact";

/**
 * 根据bot实例获取bot加入的群
 * @returns bot加入的群信息
 */
// eslint-disable-next-line import/prefer-default-export
export function fetchBotGroupList() {
  return service.raw<Group[]>({
    url: "/group",
    method: "GET",
  });
}
