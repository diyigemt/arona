import service from "@/api/http";
import { AvailableDB } from "@/interface/modules/db";
import { GachaPool } from "@/interface/modules/gacha";

const DBApi = {
  queryDefaultDB<T>(name: AvailableDB) {
    return service.raw<T>({
      url: `/db/default/${name}`,
    });
  },
  queryGachaPool() {
    return this.queryDefaultDB<GachaPool[]>(AvailableDB.GachaPools);
  },
};
