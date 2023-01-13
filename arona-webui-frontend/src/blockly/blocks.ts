import Blockly, { Block, FieldDropdown } from "blockly";
import BlocklyUtil, { doFetchGroupMember, friends, groups } from "@/blockly/BlocklyUtil";

export default function addBlocks() {
  Blockly.Blocks.senderBlock = {
    init(this: Block) {
      this.appendValueInput("IDValueInput")
        .setCheck("LogicType")
        .appendField("发送者为 ")
        .appendField(
          new FieldDropdown(() => {
            const root = this.getRootBlock();
            const parent = this.getParent();
            if (root != null) {
              switch (root?.getFieldValue("TriggerType")) {
                case "GroupMessageEvent":
                  if (BlocklyUtil.findContext(this, "groupIDBlock")!) {
                    return BlocklyUtil.groupMemberPool.get(parent?.getParent()?.getFieldValue("groupIDInput"));
                  }
                  break;
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
        if ((event.type === "change" || event.type === "move") && root.getField("TriggerType") != null) {
          switch (root?.getFieldValue("TriggerType")) {
            case "GroupMessageEvent":
              flag = false;
              if (res.getValue() === "") {
                flag = true;
                // eslint-disable-next-line no-restricted-syntax
                for (const item of friends) {
                  if (item[1] === res.getValue()) {
                    flag = !BlocklyUtil.findGroupMember(res.getValue());
                    break;
                  }
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
            BlocklyUtil.disableBlock(this, "选择的目标不属于当前事件范围内");
          } else {
            BlocklyUtil.enableBlock(this);
          }
        }
      });
      this.setOutput(true, "ExpressionType");
      this.setColour(230);
    },
  };
  Blockly.Blocks.groupIDBlock = {
    init(this: Block) {
      this.appendValueInput("groupIDValueInput")
        .setCheck("LogicType")
        .appendField("群为 ")
        .appendField(
          new FieldDropdown(() => {
            const root = this.getRootBlock();
            if (root?.getFieldValue("TriggerType")) {
              return groups;
            }
            return [["群号", ""]];
          }),
          "groupIDInput",
        );
      this.setOnChange((event) => {
        const root = this.getRootBlock();
        const res = this.getField("groupIDInput") as FieldDropdown;
        if ((event.type === "change" || event.type === "move") && root.getField("TriggerType") != null) {
          switch (root.getFieldValue("TriggerType")) {
            case "GroupMessageEvent":
              BlocklyUtil.enableBlock(this);
              if (res.getValue() !== "") {
                doFetchGroupMember(res.getValue());
              }
              break;
            default:
              BlocklyUtil.disableBlock(this, "该积木块只能在群消息事件中使用");
              break;
          }
          if (res.getValue() === "") {
            BlocklyUtil.disableBlock(this, "请选择目标群");
          }
        }
      });
      this.setOutput(true, "ExpressionType");
      this.setColour(230);
    },
  };
}
