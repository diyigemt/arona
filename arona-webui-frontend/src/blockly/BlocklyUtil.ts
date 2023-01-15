import Blockly, { Block, FieldImage } from "blockly";
import useBaseStore from "@/store/base";

export default class BlocklyUtil {
  static disableBlock(block: Block, warnMessage: string) {
    block.setEnabled(false);
    block.setColour(0);
    block.setWarningText(warnMessage);
  }

  static enableBlock(block: Block) {
    block.setEnabled(true);
    block.setColour(230);
    block.setWarningText(null);
  }

  static registerExtensions(name: string, extension: () => void) {
    if (Blockly.Extensions.isRegistered(name)) {
      Blockly.Extensions.unregister(name);
    }
    Blockly.Extensions.register(name, extension);
  }

  static registerMixin(
    name: string,
    mixin: unknown,
    helperFn?: () => unknown | undefined,
    blockList?: string[] | undefined,
  ) {
    if (Blockly.Extensions.isRegistered(name)) {
      Blockly.Extensions.unregister(name);
    }
    Blockly.Extensions.registerMutator(name, mixin, helperFn, blockList);
  }

  static createCustomField(src: string, fn?: (p1: FieldImage) => unknown) {
    return new FieldImage(src, 15, 15, undefined, fn);
  }

  static findContext(block: Block, type: string) {
    let parent = block.getParent();
    while (parent != null) {
      if (parent.type === type) {
        return parent;
      }
      parent = parent.getParent();
    }
    return null;
  }

  static findGroupMember(id: number) {
    const baseStore = useBaseStore();
    return baseStore.groups().some((group) => group.member.some((member) => Number(member.id) === Number(id)));
  }
}
