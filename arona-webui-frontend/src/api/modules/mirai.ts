import service from "@/api/http";
import { Friend } from "@/types/contact";

const MiraiApi = {
  fetchBotList() {
    return service.raw<Friend[]>({
      url: "/mirai/bot",
      method: "GET",
    });
  },
};

export default MiraiApi;
