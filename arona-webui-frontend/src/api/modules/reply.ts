import service from "@/api/http";
import { ReplyGroup, ReplyLabel } from "@/interface/modules/reply";

const ReplyApi = {
  fetchReplyLabels() {
    return service.raw<ReplyLabel[]>({
      url: "/reply/label",
      method: "GET",
    });
  },
  fetchReplyGroups() {
    return service.raw<ReplyGroup[]>({
      url: "/reply/group",
      method: "GET",
    });
  },
  deleteReplyLabel(id: number) {
    return service.raw<void>({
      url: "/reply/label/delete",
      method: "POST",
      data: {
        id,
      },
    });
  },
  deleteReplyGroup(id: number) {
    return service.raw<void>({
      url: "/reply/group/delete",
      method: "POST",
      data: {
        id,
      },
    });
  },
  updateReplyLabel(label: ReplyLabel) {
    return service.raw<void>({
      url: "/reply/label/update",
      method: "POST",
      data: {
        label,
      },
    });
  },
  updateReplyGroup(group: ReplyGroup) {
    return service.raw<void>({
      url: "/reply/group/update",
      method: "POST",
      data: {
        group,
      },
    });
  },
  createReplyLabel(label: ReplyLabel) {
    return service.raw<void>({
      url: "/reply/label/create",
      method: "POST",
      data: {
        label,
      },
    });
  },
  createReplyGroup(group: ReplyGroup) {
    return service.raw<void>({
      url: "/reply/group/create",
      method: "POST",
      data: {
        group,
      },
    });
  },
};

export default ReplyApi;
