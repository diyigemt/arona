import Blockly, { Block, FieldDropdown } from "blockly";
import { friends, groups } from "@/blockly/extensions";

export default function addBlocks() {
  Blockly.Blocks.senderBlock = {
    init(this: Block) {
      const input = this.appendValueInput("IDValueInput")
        .setCheck("LogicType")
        .appendField("发送者为 ")
        .appendField(
          new FieldDropdown(
            () => {
              const parentBlock = this.getParent();
              if (parentBlock != null) {
                switch (parentBlock?.getFieldValue("TriggerType")) {
                  case "GroupMessageEvent":
                    return groups;
                  case "FriendMessageEvent":
                    return friends;
                  default:
                    break;
                }
              }
              return [["QQ号", ""]];
            },
            // TODO: Under Construction
            // (newValue: string) => {
            //   // console.log(groups[0][0]);
            //   switch (this.getParent()?.getFieldValue("TriggerType")) {
            //     case "GroupMessageEvent":
            //       console.log(groups[0][0] === newValue);
            //       return newValue;
            //     case "FriendMessageEvent":
            //       console.log(friends[0][0]);
            //       return friends[0][0];
            //     default:
            //       break;
            //   }
            //   return newValue;
            // },
          ),
          "IDInput",
        );
      this.setOnChange((event) => {
        console.log(event.type);
      });
      this.setOutput(true, "ExpressionType");
      this.setColour(230);
    },
  };
}
