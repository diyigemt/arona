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
  updateReplyLabel(label: ReplyLabel) {
    return service.raw<void>({
      url: "/reply/label/update",
      data: {
        label,
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
};

export default ReplyApi;
