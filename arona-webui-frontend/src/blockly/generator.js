import Blockly from "blockly";

const aronaGenerator = new Blockly.Generator("aronaGenerator");

export default aronaGenerator;

aronaGenerator.ORDER_ATOMIC = 0; // 0 "" ...
aronaGenerator.ORDER_FUNCTION_CALL = 2; // ()
aronaGenerator.ORDER_LOGICAL_NOT = 4.4; // !
aronaGenerator.ORDER_LOGICAL_AND = 13; // &&
aronaGenerator.ORDER_LOGICAL_OR = 14; // ||
aronaGenerator.ORDER_NONE = 99; // (...)

// expressions用头插，actions用尾插
aronaGenerator.expressions = [];
aronaGenerator.actions = [];

/* eslint-disable func-names */
aronaGenerator.master_block = function (block) {
  aronaGenerator.expressions = [];
  aronaGenerator.actions = [];
  const dropdownTriggerType = block.getFieldValue("TriggerType");
  aronaGenerator.valueToCode(block, "Expressions", aronaGenerator.ORDER_ATOMIC);
  // aronaGenerator.statementToCode(block, "Actions");
  let statementBlocks = block.getInputTargetBlock("Actions");
  if (statementBlocks) {
    do {
      aronaGenerator.blockToCode(statementBlocks, true);
      // eslint-disable-next-line no-cond-assign
    } while ((statementBlocks = statementBlocks.getNextBlock()));
  }
  const code = JSON.stringify({
    // TODO: 闲置ID，打算未来做分类用
    id: 0,
    type: dropdownTriggerType,
    expressions: aronaGenerator.expressions,
    actions: aronaGenerator.actions,
  });
  console.log(code);

  return code;
};

aronaGenerator.id_block = function (block) {
  const textId = block.getFieldValue("ID");
  aronaGenerator.valueToCode(block, "IDInput", aronaGenerator.ORDER_ATOMIC);
  aronaGenerator.expressions.unshift({
    id: "ID",
    value: Number(textId),
  });

  return ["", aronaGenerator.ORDER_NONE];
};

aronaGenerator.sender_block = function (block) {
  const textId = block.getFieldValue("ID");
  aronaGenerator.valueToCode(block, "NAME", aronaGenerator.ORDER_ATOMIC);
  aronaGenerator.expressions.unshift({
    id: "SENDER",
    value: Number(textId),
  });

  return ["", aronaGenerator.ORDER_NONE];
};

aronaGenerator.logic_block = function (block) {
  let dropdownLogic = block.getFieldValue("Logic");
  aronaGenerator.valueToCode(block, "NAME", aronaGenerator.ORDER_ATOMIC);
  if (dropdownLogic === "And" || dropdownLogic === "Or" || dropdownLogic === "Not") {
    dropdownLogic = dropdownLogic.toUpperCase();
  } else throw new Error(`Undefined logic type: ${dropdownLogic}`);
  aronaGenerator.expressions.unshift({
    id: dropdownLogic,
    value: "",
  });

  return ["", aronaGenerator.ORDER_NONE];
};

aronaGenerator.send_msg_block = function (block) {
  const textMsg = block.getFieldValue("Msg");
  aronaGenerator.actions.push({
    id: "SEND_MSG",
    value: [textMsg],
  });

  return "";
};
