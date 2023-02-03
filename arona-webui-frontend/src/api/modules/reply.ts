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
      data: {
        id,
      },
    });
  },
  deleteReplyGroup(id: number) {
    return service.raw<void>({
      url: "/reply/group/delete",
      data: {
        id,
      },
    });
  },
  updateReplyLabel(label: ReplyLabel) {
    return service.raw<void>({
      url: "/reply/label/update",
      data: {
        label,
      },
    });
  },
  updateReplyGroup(group: ReplyGroup) {
    return service.raw<void>({
      url: "/reply/group/update",
      data: {
        group,
      },
    });
  },
  createReplyLabel(label: ReplyLabel) {
    return service.raw<void>({
      url: "/reply/label/create",
      data: {
        label,
      },
    });
  },
  createReplyGroup(group: ReplyGroup) {
    return service.raw<void>({
      url: "/reply/group/create",
      data: {
        group,
      },
    });
  },
};

export default ReplyApi;
