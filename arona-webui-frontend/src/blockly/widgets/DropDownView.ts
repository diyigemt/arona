import Blockly, { FieldDropdown } from "blockly";
import { FieldConfig } from "blockly/core/field";
import { FieldDropdownValidator, MenuGenerator, MenuOption } from "blockly/core/field_dropdown";
import { Data } from "blockly/core/browser_events";
import BlocklyUtil from "@/blockly/BlocklyUtil";
import { mountAsyncComponent } from "@/utils/vueTools";

// @ts-ignore
export default class DropDownView extends FieldDropdown {
  isSearch = false;

  isMultiple = false;

  private store: MenuOption[] = [["请选择", ""]];

  private isAsyncGenerator = false;

  private declare generatedOptions_: MenuOption[] | null;

  private mouseScrollWrapper: Data | undefined;

  /* eslint-disable camelcase,@typescript-eslint/no-unused-vars */
  constructor(
    menuGenerator: MenuGenerator | ((this: DropDownView) => Promise<MenuOption[]>),
    opt_validator?: FieldDropdownValidator,
    opt_config?: FieldConfig,
    isSearch?: boolean,
    isMultiple?: boolean,
  ) {
    // @ts-ignore
    if (!Array.isArray(menuGenerator) && menuGenerator().constructor.name === "Promise") {
      super([["请选择", ""]], opt_validator, opt_config);
      this.isAsyncGenerator = true;
      // @ts-ignore
      (menuGenerator() as Promise<MenuOption[]>).then((r) => {
        this.store = r;
        this.menuGenerator_ = () => {
          return this.store;
        };
      });
    } else super(<MenuOption[] | ((this: FieldDropdown) => MenuOption[])>menuGenerator, opt_validator, opt_config);
    if (isSearch !== undefined) this.isSearch = isSearch;
    if (isMultiple !== undefined) this.isMultiple = isMultiple;
  }

  protected override showEditor_(opt_e?: MouseEvent) {
    const container = mountAsyncComponent(() => import("./DropDownView.vue"), {
      blockly: this,
      searchInput: this.isSearch,
      isMultiple: this.isMultiple,
    });
    Blockly.DropDownDiv.getContentDiv().appendChild(container);
  }

  protected override bindEvents_() {
    super.bindEvents_();
    if (!this.isMultiple) {
      this.mouseScrollWrapper = Blockly.browserEvents.conditionalBind(
        this.getClickTarget_()!,
        "mousewheel",
        this,
        (event: WheelEvent) => {
          const len = this.getOptions(false).length;
          if (len !== 1 && this.isEnabled()) {
            const currentIndex = this.getOptions().findIndex(
              (value) => value[0] === this.getText() && value[1] === this.getValue(),
            )!;
            // @ts-ignore
            if (event.wheelDelta < 0) {
              const nextIndex = (currentIndex + 1) % len;
              this.setValue(this.getOptions()[nextIndex][1]);
            } else {
              const nextIndex = currentIndex - 1 < 0 ? len - 1 : currentIndex - 1;
              this.setValue(this.getOptions()[nextIndex][1]);
            }
          }
        },
      );
    }
  }

  override loadState(state: string) {
    if (this.isOptionListDynamic()) this.getOptions(false);
    if (this.isMultiple) {
      let res = "";
      let msg = "";
      state.split(",").forEach((value) => {
        if (
          this.getOptions()
            .map((item) => item[1])
            .includes(value)
        )
          res += `${value},`;
        else msg += `${value},`;
      });
      res = res.slice(undefined, res.length - 1);
      if (msg !== "") BlocklyUtil.disableBlock(this.getSourceBlock()!, `目标: ${msg} 已不存在`);
      this.updateOptions(res, 0);
      this.setValue(res);
    } else {
      if (
        !this.getOptions()
          .map((value) => value[1])
          .includes(state)
      ) {
        BlocklyUtil.disableBlock(this.getSourceBlock()!, `目标${state}已不存在`);
        return;
      }
      this.setValue(state);
    }
  }

  override getOptions(opt_useCache?: boolean): MenuOption[] {
    if (!this.menuGenerator_) throw TypeError("A menu generator was never defined.");
    if (Array.isArray(this.menuGenerator_)) {
      if (!this.isMultiple) return this.menuGenerator_;
      const res = this.menuGenerator_;
      if (this.generatedOptions_ != null) res.unshift(this.generatedOptions_[0]);
      this.generatedOptions_ = res;
      return res;
    }
    if (opt_useCache && this.generatedOptions_) return this.generatedOptions_;
    if (this.isMultiple) {
      if (this.generatedOptions_ != null) {
        if (this.isAsyncGenerator) {
          // @ts-ignore
          this.generatedOptions_ = this.menuGenerator_().concat();
          this.generatedOptions_.unshift(this.generatedOptions_[0]);
        } else {
          // @ts-ignore
          const res = this.menuGenerator_();
          res.unshift(this.generatedOptions_[0]);
          this.generatedOptions_ = res;
        }
      } else return this.getOptions(false);
    } else {
      // @ts-ignore
      this.generatedOptions_ = this.menuGenerator_();
    }
    this.validateOptions(this.generatedOptions_);
    return this.isMultiple ? this.generatedOptions_.slice(1) : this.generatedOptions_;
  }

  updateOptions(data: string, index: number) {
    if (index < 0 || index > 1) throw RangeError(`function: updateOption only accept 0 or 1. Found: ${index}`);
    if (this.isOptionListDynamic()) this.getOptions(false);
    // eslint-disable-next-line no-bitwise
    const map = this.generatedOptions_!.map((value) => value[index ^ 1]);
    let res = "";
    const res2: string[] = [];
    (JSON.parse(index ? data : `[${data}]`) as string[]).forEach((value) => {
      const tmp = this.generatedOptions_![map.indexOf(value.toString())][index];
      if (index === 0) {
        res += `${tmp}、`.replace(` (${value})`, "");
      } else {
        res2.push(tmp as string);
        res += `${value}、`.replace(` (${tmp})`, "");
      }
    });
    if (index === 1 && res.length > 20) res = `${res.slice(0, 20)}...`;
    // eslint-disable-next-line no-unused-expressions
    index === 0 ? (this.generatedOptions_![0] = [res, data]) : (this.generatedOptions_![0] = [res, res2.toString()]);
    this.validateOptions(this.generatedOptions_!);
  }

  // eslint-disable-next-line class-methods-use-this
  private validateOptions(options: MenuOption[]) {
    if (!Array.isArray(options)) throw TypeError("FieldDropdown options must be an array.");
    if (!options.length) throw TypeError("FieldDropdown options must not be an empty array.");
    options.forEach((value, index) => {
      if (!Array.isArray(value)) {
        throw TypeError(`Invalid option[${index}]: Each FieldDropdown option must be an array. Found:${value}`);
      } else if (typeof value[1] !== "string") {
        throw TypeError(
          `Invalid option[${index}]: Each FieldDropdown option id must be a string. Found ${value[1]} in: ${value}`,
        );
      } else if (value[0] && typeof value[0] !== "string" && typeof value[0].src !== "string") {
        throw TypeError(
          `Invalid option[${index}]: Each FieldDropdown option must have a string label or image description. Found${value[0]} in: ${value}`,
        );
      }
    });
  }

  // eslint-disable-next-line class-methods-use-this
  close() {
    Blockly.DropDownDiv.hide();
  }

  splitTags(): string[] {
    const res: string[] = [];
    if (!this.isMultiple || this.generatedOptions_![0][1] === "") return [];
    this.getOptions();
    this.generatedOptions_![0][1].split(",").forEach((value) => {
      const index = this.generatedOptions_!.map((value1) => value1[1]).findIndex(
        (value1, index1) => value1 === value && index1 !== 0,
      );
      // @ts-ignore
      if (index !== -1) res.push(this.generatedOptions_![index][0]);
    });
    this.updateOptions(JSON.stringify(res), 1);
    this.setValue(this.generatedOptions_![0][1]);
    return res;
  }
}

BlocklyUtil.registerField("field_dropdown_view", DropDownView);
