import { BlocklyProject } from "@/interface/modules/blockly";
import service from "@/api/http";

const BlocklyApi = {
  fetchBlocklyProjectList() {
    return service.raw<BlocklyProject[]>({
      url: "/blockly/commit",
      method: "GET",
    });
  },
  saveBlocklyProject(data: BlocklyProjectSaveData) {
    return service.raw<null>({
      url: "/blockly/commit",
      method: "POST",
      data: {
        mode: "CREATE",
        ...data,
      },
    });
  },

  updateBlocklyProject(data: BlocklyProjectSaveData) {
    return service.raw<null>({
      url: "/blockly/commit",
      method: "POST",
      data: {
        mode: "UPDATE",
        ...data,
      },
    });
  },

  deleteBlocklyProject(data: BlocklyProjectSaveData) {
    return service.raw<null>({
      url: "/blockly/commit",
      method: "POST",
      data: {
        mode: "DELETE",
        ...data,
      },
    });
  },
};

export default BlocklyApi;

interface BlocklyProjectSaveData {
  trigger: string;
  uuid: string | null;
  projectName: string;
  blocklyProject: string;
  userData: UserData | string;
}

export interface UserData {
  groups: number[];
}
