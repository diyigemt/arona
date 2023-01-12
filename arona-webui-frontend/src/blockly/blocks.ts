import Blockly, { Block, FieldDropdown } from "blockly";
import { friends, groups } from "@/blockly/extensions";

export default function addBlocks() {
  Blockly.Blocks.senderBlock = {
    init(this: Block) {
      this.appendValueInput("IDValueInput")
        .setCheck("LogicType")
        .appendField("发送者为 ")
        .appendField(
          new FieldDropdown(() => {
            const root = this.getRootBlock();
            if (root != null) {
              switch (root?.getFieldValue("TriggerType")) {
                case "GroupMessageEvent":
                  return groups;
                case "FriendMessageEvent":
                  return friends;
                default:
                  break;
              }
            }
            return [["QQ号", ""]];
          }),
          "IDInput",
        );
      this.setOnChange((event) => {
        const root = this.getRootBlock();
        const res = this.getField("IDInput") as FieldDropdown;
        let flag = false;
        if (event.type === "change" && root != null) {
          switch (root?.getFieldValue("TriggerType")) {
            case "GroupMessageEvent":
              flag = false;
              if (res.getValue() === "") {
                flag = true;
                break;
              }
              // eslint-disable-next-line no-restricted-syntax
              for (const item of friends) {
                if (item[1] === res.getValue()) {
                  flag = true;
                  break;
                }
              }
              break;
            case "FriendMessageEvent":
              flag = false;
              if (res.getValue() === "") {
                flag = true;
                break;
              }
              // eslint-disable-next-line no-restricted-syntax
              for (const item of groups) {
                if (item[1] === res.getValue()) {
                  flag = true;
                  break;
                }
              }
              break;
            default:
              break;
          }
          if (flag) {
            this.setEnabled(false);
            this.setColour(0);
            this.setWarningText("目标不属于当前事件范围内");
          } else {
            this.setEnabled(true);
            this.setColour(230);
            this.setWarningText(null);
          }
        }
      });
      this.setOutput(true, "ExpressionType");
      this.setColour(230);
    },
  };
}
