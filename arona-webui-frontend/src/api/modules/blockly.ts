import { BlocklyProject } from "@/interface/modules/blockly";
import service from "@/api/http";

export function fetchBlocklyProjectList() {
  return service.raw<BlocklyProject[]>({
    url: "/blockly/commit",
    method: "GET",
  });
}

interface BlocklyProjectSaveData {
  trigger: string;
  projectName: string;
  blocklyProject: string;
}

export function saveBlocklyProject(data: BlocklyProjectSaveData) {
  return service.raw<null>({
    url: "/blockly/commit",
    method: "POST",
    data: {
      mod: "CREATE",
      ...data,
    },
  });
}
