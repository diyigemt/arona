import Blockly, { Block, FieldDropdown } from "blockly";
import { friends, groups } from "@/blockly/extensions";

export default function addBlocks() {
  Blockly.Blocks.senderBlock = {
    // eslint-disable-next-line no-unused-vars
    init(this: Block) {
      const input = this.appendValueInput("IDValueInput")
        .setCheck("LogicType")
        .appendField("发送者为 ")
        .appendField(
          new FieldDropdown(() => {
            return [["QQ号", ""]];
          }),
          "IDInput",
        );
      this.setOnChange(() => {
        const parentBlock = this.getParent();
        switch (parentBlock?.getFieldValue("TriggerType")) {
          case "GroupMessageEvent":
            input.removeField("IDInput");
            input.appendField(
              new FieldDropdown(() => {
                return groups;
              }),
              "IDInput",
            );
            break;
          case "FriendMessageEvent":
            input.removeField("IDInput");
            input.appendField(
              new FieldDropdown(() => {
                return friends;
              }),
              "IDInput",
            );
          // eslint-disable-next-line no-fallthrough
          default:
            break;
        }
      });
      this.setOutput(true, "ExpressionType");
      this.setColour(230);
    },
  };
}
