import Blockly, { FieldDropdown } from "blockly";
import { Abstract } from "blockly/core/events/events_abstract";
import BlocklyUtil from "@/blockly/BlocklyUtil";
import useBaseStore from "@/store/base";
import { blocks } from "@/blockly/index";
import DropDownView from "@/blockly/widgets/DropDownView";

export default function addBlocks() {
  Blockly.defineBlocksWithJsonArray(blocks);
  Blockly.Blocks.senderBlock = {
    init() {
      this.appendValueInput("IDValueInput")
        .setCheck("LogicType")
        .appendField("发送者为 ")
        .appendField(
          new DropDownView(
            () => {
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
                    return baseStore.friends().map((item) => [`${item.remark} (${item.id})`, item.id.toString()]);
                  default:
                    break;
                }
              }
              return [["QQ号", ""]];
            },
            undefined,
            undefined,
            true,
          ),
          "IDInput",
        );
      this.setOutput(true, "ExpressionType");
      this.setColour(230);
      this.setOnChange((event: Abstract) => {
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
    init() {
      this.appendValueInput("groupIDValueInput")
        .setCheck("LogicType")
        .appendField("群为 ")
        .appendField(
          new DropDownView(
            () => {
              const root = this.getRootBlock();
              const baseStore = useBaseStore();
              if (root?.getFieldValue("TriggerType")) {
                return baseStore.groups().map((group) => [`${group.name} (${group.id})`, group.id.toString()]);
              }
              return [["群号", ""]];
            },
            undefined,
            undefined,
          ),
          "groupIDInput",
        );
      this.setOnChange((event: Abstract) => {
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
  Blockly.Blocks.friend_search_block = {
    init() {
      this.appendValueInput("NAME")
        .setCheck(null)
        .appendField("单击以查找")
        .appendField(new Blockly.FieldLabelSerializable(""), "NAME");
      this.setOutput(true, null);
      this.setColour(230);
      this.setTooltip("");
      this.setHelpUrl("");
      // TODO 细化类型
      this.data = "friend";
    },
  };
}
