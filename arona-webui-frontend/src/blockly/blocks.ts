import Blockly, { Block, BlockSvg, FieldDropdown, FieldTextInput } from "blockly";
import BlocklyUtil, { friends, groups } from "@/blockly/BlocklyUtil";
import { exchange } from "@/blockly/images";

export default function addBlocks() {
  Blockly.Blocks.senderBlock = {
    init(this: Block) {
      // @ts-ignore
      // @ts-ignore
      this.appendValueInput("IDValueInput")
        .setCheck("LogicType")
        .appendField(
          BlocklyUtil.createCustomField(exchange, (image) => {
            const block = image.getSourceBlock()! as BlockSvg;
            const dropDown = block.getField("IDInput") as FieldDropdown;
            const manualInput = block.getField("manualIDInput") as FieldTextInput;
            if (dropDown.isVisible()) {
              dropDown.setVisible(false);
              manualInput.setVisible(true);
              manualInput.setValue(dropDown.getValue());
            } else {
              dropDown.setVisible(true);
              manualInput.setVisible(false);
            }
            block.render(true);
          }),
        )
        .appendField("发送者为 ")
        .appendField(
          // @ts-ignore
          new FieldTextInput("123456", (value: string) => {
            const reg = /[A-z`~!@#$^\-&*()=|{}':;,\\[\].<>/?！￥…（）—【】；："。，、？\s]+/g;
            const qqReg = /[1-9][0-9]{4,10}/g;
            if (value.match(reg) != null || value.match(qqReg) == null || value === "") {
              return null;
            }
            return value.replace(reg, "");
          }),
          "manualIDInput",
        )
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
      (this.getField("manualIDInput")! as FieldTextInput).setVisible(false);
      this.setOutput(true, "ExpressionType");
      this.setColour(230);
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
