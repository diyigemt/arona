import { Block, CodeGenerator } from "blockly";
import { MutatorBlockSvg } from "@/blockly/mutators/AbstractMutator";

class AronaGenerator extends CodeGenerator {
  ORDER_ATOMIC = 0; // 0 "" ...

  ORDER_NONE = 99; // (...)

  // expressions用头插，actions用尾插
  expressions: unknown[] = [];

  actions: unknown[] = [];

  masterBlock = (block: MutatorBlockSvg) => {
    const result: { expressions: unknown[]; actions: unknown[] }[] = [];
    const dropdownTriggerType = block.getFieldValue("TriggerType");
    for (let item = 0; item <= block.itemCount; item++) {
      this.expressions = [];
      this.actions = [];
      let statementBlocks = block.getInputTargetBlock(`Actions${item}`);
      if (statementBlocks) {
        this.valueToCode(block, `Expressions${item}`, this.ORDER_NONE);
        do {
          this.blockToCode(statementBlocks, true);
          // eslint-disable-next-line no-cond-assign
        } while ((statementBlocks = statementBlocks.getNextBlock()));
        result.push({ expressions: this.expressions, actions: this.actions });
      }
    }
    return JSON.stringify({
      type: dropdownTriggerType,
      triggers: result,
    });
  };

  groupIDBlock = (block: Block) => {
    const textId = block.getFieldValue("groupIDInput");
    this.valueToCode(block, "groupIDValueInput", this.ORDER_ATOMIC);
    this.expressions.unshift({
      id: "ID",
      value: textId,
    });

    return ["", this.ORDER_NONE];
  };

  senderBlock = (block: Block) => {
    const textId = block.getFieldValue("IDInput");
    this.valueToCode(block, "IDValueInput", this.ORDER_ATOMIC);
    this.expressions.unshift({
      id: "SENDER",
      value: textId,
    });

    return ["", this.ORDER_NONE];
  };

  logicBlock = (block: Block) => {
    let dropdownLogic = block.getFieldValue("logicInput");
    this.valueToCode(block, "logicValueInput", this.ORDER_ATOMIC);
    if (dropdownLogic === "And" || dropdownLogic === "Or" || dropdownLogic === "Not") {
      dropdownLogic = dropdownLogic.toUpperCase();
    } else throw new Error(`Undefined logic type: ${dropdownLogic}`);
    this.expressions.unshift({
      id: dropdownLogic,
      value: "",
    });

    return ["", this.ORDER_NONE];
  };

  sendMsgBlock = (block: Block) => {
    const textMsg = block.getFieldValue("Msg");
    this.actions.push({
      id: "SEND_MSG",
      value: [textMsg],
    });

    return "";
  };
}

const aronaGenerator = new AronaGenerator("aronaGenerator");

export default aronaGenerator;
