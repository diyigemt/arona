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

export function deleteReplyLabel(id: number) {
  return service.raw<void>({
    url: "/reply/label/delete",
    data: {
      id,
    },
  });
}

export function updateReplyLabel(label: ReplyLabel) {
  return service.raw<void>({
    url: "/reply/label/update",
    data: {
      label,
    },
  });
}

export function createReplyLabel(label: ReplyLabel) {
  return service.raw<void>({
    url: "/reply/label/create",
    data: {
      label,
    },
  });
}
