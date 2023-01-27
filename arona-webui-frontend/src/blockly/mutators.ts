import MasterBlockMutator from "@/blockly/mutators/MasterBlockMutator";
import BlocklyUtil from "@/blockly/BlocklyUtil";

export default function addMutators() {
  BlocklyUtil.registerMixin("masterBlockMutator", new MasterBlockMutator());
}
