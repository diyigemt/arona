import service from "@/api/http";
import { AvailableDB } from "@/interface/modules/db";
import { GachaPool } from "@/interface/modules/gacha";

export function queryDefaultDB<T>(name: AvailableDB) {
  return service.raw<T>({
    url: `/db/default/${name}`,
  });
}

// eslint-disable-next-line import/prefer-default-export
export function queryGachaPool() {
  return queryDefaultDB<GachaPool[]>(AvailableDB.GachaPools);
}
