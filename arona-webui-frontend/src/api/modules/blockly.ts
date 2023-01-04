import { BlocklyBlock } from "@/interface/modules/blockly";
import service from "@/api/http";

// eslint-disable-next-line import/prefer-default-export
export function fetchBlocklyBlockList() {
  return service.raw<BlocklyBlock[]>({
    url: "/blockly/commit",
    method: "GET",
  });
}
