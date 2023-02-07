import service from "@/api/http";
import { ServerAronaConfig } from "@/interface";

const ConfigApi = {
  fetchConfig() {
    return service.raw<ServerAronaConfig>({
      url: "/config",
      method: "GET",
    });
  },
};

export default ConfigApi;
