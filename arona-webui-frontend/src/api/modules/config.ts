import service from "@/api/http";
import { BotGroupConfig } from "@/interface";

const ConfigApi = {
  fetchConfig() {
    return service.raw<BotGroupConfig[]>({
      url: "/config",
      method: "GET",
    });
  },
};

export default ConfigApi;
