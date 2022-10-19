import { Group } from "@/interface";
import service from "@/api/http";

// eslint-disable-next-line import/prefer-default-export
export function fetchBotGroupList() {
  return service.raw<Group[]>({
    url: "/group",
    method: "GET",
  });
}
