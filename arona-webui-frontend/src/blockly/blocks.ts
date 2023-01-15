import Blockly, { Block, BlockSvg, FieldDropdown, FieldTextInput } from "blockly";
import BlocklyUtil from "@/blockly/BlocklyUtil";
import { exchange } from "@/blockly/images";
import useBaseStore from "@/store/base";
import { blocks } from "@/blockly/index";

export default function addBlocks() {
  Blockly.defineBlocksWithJsonArray(blocks);
  Blockly.Blocks.senderBlock = {
    init(this: Block) {
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
            const baseStore = useBaseStore();
            const groupBlock = BlocklyUtil.findContext(this, "groupIDBlock");
            if (root != null) {
              switch (root?.getFieldValue("TriggerType")) {
                case "GroupMessageEvent":
                  if (groupBlock) {
                    return baseStore
                      .memberSync(Number(groupBlock.getFieldValue("groupIDInput")) || 0)
                      .map((item) => [`${item.memberName} (${item.id})`, item.id.toString()]);
                  }
                  break;
                case "FriendMessageEvent":
                  return baseStore.friends();
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
          const baseStore = useBaseStore();
          switch (root?.getFieldValue("TriggerType")) {
            case "GroupMessageEvent":
              flag = false;
              if (res.getValue() === "") {
                flag = true;
                // eslint-disable-next-line no-restricted-syntax
                for (const item of baseStore.friends()) {
                  if (item.id === res.getValue()) {
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
              for (const item of baseStore.groups()) {
                if (item.id === res.getValue()) {
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
            const baseStore = useBaseStore();
            if (root?.getFieldValue("TriggerType")) {
              return baseStore.groups().map((group) => [`${group.name} (${group.id})`, group.id.toString()]);
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
          const group = res.getValue() as string;
          if (group === "") {
            BlocklyUtil.disableBlock(this, "请选择目标群");
          } else {
            // 提前预载群成员列表
            const { members } = useBaseStore();
            members(Number(group)).then();
          }
        }
      });
      this.setOutput(true, "ExpressionType");
      this.setColour(230);
    },
  };
}
