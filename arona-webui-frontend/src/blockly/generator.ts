import { Block, CodeGenerator } from "blockly";

class AronaGenerator extends CodeGenerator {
  ORDER_ATOMIC = 0; // 0 "" ...

  ORDER_NONE = 99; // (...)

  // expressions用头插，actions用尾插
  expressions: unknown[] = [];

  actions: unknown[] = [];

  masterBlock = (block: Block) => {
    this.expressions = [];
    this.actions = [];
    const dropdownTriggerType = block.getFieldValue("TriggerType");
    this.valueToCode(block, "Expressions", this.ORDER_NONE);
    let statementBlocks = block.getInputTargetBlock("Actions");
    if (statementBlocks) {
      do {
        this.blockToCode(statementBlocks, true);
        // eslint-disable-next-line no-cond-assign
      } while ((statementBlocks = statementBlocks.getNextBlock()));
    }
    return JSON.stringify({
      type: dropdownTriggerType,
      expressions: this.expressions,
      actions: this.actions,
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
