import Blockly, { Block, FieldDropdown } from "blockly";
import BlocklyUtil from "@/blockly/BlocklyUtil";

export default async function injectExtensions() {
  Blockly.Extensions.register("logic_block_extension", logicBlockExtension);
}

function logicBlockExtension(this: Block) {
  this.setOnChange((event) => {
    const root = this.getRootBlock();
    if ((event.type === "change" || event.type === "move") && root.getField("TriggerType") != null) {
      const parent = this.getParent();
      const dropDown = this.getField("logicInput") as FieldDropdown;
      switch (dropDown.getValue()) {
        case "Not":
          if (parent?.type === root.type || parent?.type === this.type) {
            BlocklyUtil.enableBlock(this);
          } else {
            BlocklyUtil.disableBlock(this, "逻辑非只能出现在起始位置以及或和且后面");
          }
          break;
        default:
          if (parent?.type !== this.type && dropDown.getValue() === "Not") {
            BlocklyUtil.disableBlock(this, "逻辑非只能出现在起始位置以及或和且后面");
          }
          if (parent?.type === "logic_block" && parent?.getFieldValue("logicInput") !== "Not") {
            BlocklyUtil.disableBlock(this, "且和或不许叠加");
          } else {
            BlocklyUtil.enableBlock(this);
          }
          break;
      }
      if (parent?.type === root.type) {
        dropDown.setValue("Not");
      }
      if (parent?.getParent()?.type === "logic_block" && parent?.type === "logic_block") {
        BlocklyUtil.disableBlock(this, "相邻的逻辑积木块总数不能超过2个");
      }
      if (parent?.getFieldValue("logicInput") === "Not" && dropDown.getValue() === "Not") {
        BlocklyUtil.disableBlock(this, "无效的逻辑非");
      }
    }
  });
}
