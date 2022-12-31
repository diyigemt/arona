import Blockly from "blockly";

const aronaGenerator = new Blockly.Generator("aronaGenerator");

export default aronaGenerator;

aronaGenerator.ORDER_ATOMIC = 0; // 0 "" ...
aronaGenerator.ORDER_NEW = 1.1; // new
aronaGenerator.ORDER_MEMBER = 1.2; // . []
aronaGenerator.ORDER_FUNCTION_CALL = 2; // ()
aronaGenerator.ORDER_INCREMENT = 3; // ++
aronaGenerator.ORDER_DECREMENT = 3; // --
aronaGenerator.ORDER_BITWISE_NOT = 4.1; // ~
aronaGenerator.ORDER_UNARY_PLUS = 4.2; // +
aronaGenerator.ORDER_UNARY_NEGATION = 4.3; // -
aronaGenerator.ORDER_LOGICAL_NOT = 4.4; // !
aronaGenerator.ORDER_TYPEOF = 4.5; // typeof
aronaGenerator.ORDER_VOID = 4.6; // void
aronaGenerator.ORDER_DELETE = 4.7; // delete
aronaGenerator.ORDER_AWAIT = 4.8; // await
aronaGenerator.ORDER_EXPONENTIATION = 5.0; // **
aronaGenerator.ORDER_MULTIPLICATION = 5.1; // *
aronaGenerator.ORDER_DIVISION = 5.2; // /
aronaGenerator.ORDER_MODULUS = 5.3; // %
aronaGenerator.ORDER_SUBTRACTION = 6.1; // -
aronaGenerator.ORDER_ADDITION = 6.2; // +
aronaGenerator.ORDER_BITWISE_SHIFT = 7; // << >> >>>
aronaGenerator.ORDER_RELATIONAL = 8; // < <= > >=
aronaGenerator.ORDER_IN = 8; // in
aronaGenerator.ORDER_INSTANCEOF = 8; // instanceof
aronaGenerator.ORDER_EQUALITY = 9; // == != === !==
aronaGenerator.ORDER_BITWISE_AND = 10; // &
aronaGenerator.ORDER_BITWISE_XOR = 11; // ^
aronaGenerator.ORDER_BITWISE_OR = 12; // |
aronaGenerator.ORDER_LOGICAL_AND = 13; // &&
aronaGenerator.ORDER_LOGICAL_OR = 14; // ||
aronaGenerator.ORDER_CONDITIONAL = 15; // ?:
aronaGenerator.ORDER_ASSIGNMENT = 16; // = += -= **= *= /= %= <<= >>= ...
aronaGenerator.ORDER_YIELD = 16.5; // yield
aronaGenerator.ORDER_COMMA = 17; // ,
aronaGenerator.ORDER_NONE = 99; // (...)

// expressions用头插，actions用尾插
aronaGenerator.expressions = [];
aronaGenerator.actions = [];

/* eslint-disable func-names */
aronaGenerator.master_block = function (block) {
  aronaGenerator.expressions = [];
  aronaGenerator.actions = [];
  // TODO: 目前只支持群消息，日后对接其它类型消息时需要使用该变量
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
