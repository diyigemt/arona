import { Block, BlockSvg, FieldImage } from "blockly";
import * as Blockly from "blockly";
import { minus, plus } from "@/blockly/images";

export default abstract class AbstractMutator {
  itemCount = 0;

  loadExtraState(this: MutatorBlockSvg, extraState: ExtraState) {
    this.updateShape(extraState.itemCount);
  }

  saveExtraState(this: MutatorBlockSvg): ExtraState {
    return { itemCount: this.itemCount };
  }

  abstract controlsHelper(this: MutatorBlockSvg): void;

  protected updateShape(this: MutatorBlockSvg, count: number) {
    while (this.itemCount < count) {
      this.addPart();
    }
    while (this.itemCount > count) {
      this.removePart();
    }
  }

  abstract addPart(this: MutatorBlockSvg): void;

  abstract removePart(this: MutatorBlockSvg): void;

  protected static createPlusField() {
    return new FieldImage(plus, 15, 15, undefined, AbstractMutator.onClick);
  }

  protected static createMinusField() {
    return new FieldImage(minus, 15, 15, undefined, AbstractMutator.onClick);
  }

  private static onClick(this: FieldImage, field: FieldImage) {
    const sourceBlock = field.getSourceBlock()! as MutatorBlockSvg;
    if (sourceBlock.isInFlyout) return;
    Blockly.Events.setGroup(true);
    const oldState = AbstractMutator.getExtraState(sourceBlock);
    const count = this.name === "plus" ? sourceBlock.itemCount + 1 : sourceBlock.itemCount - 1;
    sourceBlock.updateShape(count);
    const newState = AbstractMutator.getExtraState(sourceBlock);
    if (oldState !== newState) {
      Blockly.Events.fire(new Blockly.Events.BlockChange(sourceBlock, "mutation", null, oldState, newState));
    }
    Blockly.Events.setGroup(false);
  }

  private static getExtraState(block: Block) {
    if (block.saveExtraState) {
      const state = block.saveExtraState();
      return state ? JSON.stringify(state) : "";
    }
    return "";
  }

  toObject() {
    return {
      itemCount: this.itemCount,
      loadExtraState: this.loadExtraState,
      saveExtraState: this.saveExtraState,
      updateShape: this.updateShape,
      addPart: this.addPart,
      removePart: this.removePart,
    };
  }
}

export interface ExtraState {
  itemCount: number;
}

export interface MutatorBlockSvg extends BlockSvg {
  itemCount: number;

  loadExtraState: () => void;

  saveExtraState: () => ExtraState;

  updateShape: (count: number) => void;

  addPart: () => void;

  removePart: () => void;
}
