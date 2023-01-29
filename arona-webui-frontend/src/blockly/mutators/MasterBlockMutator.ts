import AbstractMutator, { MutatorBlockSvg } from "@/blockly/mutators/AbstractMutator";

export default class MasterBlockMutator extends AbstractMutator {
  controlsHelper(this: MutatorBlockSvg): void {
    this.getInput("Expressions0")!.insertFieldAt(0, MasterBlockMutator.createPlusField(), "plus");
  }

  addPart(this: MutatorBlockSvg) {
    this.itemCount++;
    this.appendValueInput(`Expressions${this.itemCount}`)
      .appendField(MasterBlockMutator.createMinusField(), "minus")
      .appendField("                                当");

    this.appendStatementInput(`Actions${this.itemCount}`).appendField("执行");
  }

  removePart(this: MutatorBlockSvg) {
    if (this.itemCount !== 0) {
      const input = this.getInput(`Expressions${this.itemCount}`)!.connection!;
      if (input.isConnected()) input.disconnect();
      this.removeInput(`Expressions${this.itemCount}`);
      this.removeInput(`Actions${this.itemCount}`);
      this.itemCount--;
    }
  }
}
