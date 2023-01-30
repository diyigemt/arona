import service from "@/api/http";
import { ReplyGroup, ReplyLabel } from "@/interface/modules/reply";

export function fetchReplyLabels() {
  return service.raw<ReplyLabel[]>({
    url: "/reply/label",
    method: "GET",
  });
}

export function fetchReplyGroups() {
  return service.raw<ReplyGroup[]>({
    url: "/reply/group",
    method: "GET",
  });
}
