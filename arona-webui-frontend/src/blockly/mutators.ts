import Blockly, { Block, Workspace } from "blockly";
import BlocklyUtil from "@/blockly/BlocklyUtil";

export default function addMutators() {
  BlocklyUtil.registerMixin("senderBlockMixin", senderBlockMixin);
}

const senderBlockMixin = {
  saveExtraState: () => {
    // 空着,但是不要删
  },
  loadExtraState: () => {
    // TODO: 反序列化解析
  },
  decompose(this: Block, workspace: Workspace) {},
  compose(this: Block, workspace: Workspace) {},
};
