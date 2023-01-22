import Blockly, { FieldDropdown } from "blockly";
import { createVNode, render } from "vue";
import { FieldConfig } from "blockly/core/field";
import { FieldDropdownValidator, MenuGenerator } from "blockly/core/field_dropdown";
import { Data } from "blockly/core/browser_events";
import BlocklyUtil from "@/blockly/BlocklyUtil";

export default class DropDownView extends FieldDropdown {
  isSearch: boolean | undefined = false;

  private mouseScrollWrapper: Data | undefined;

  /* eslint-disable camelcase,@typescript-eslint/no-unused-vars,no-underscore-dangle */
  constructor(
    menuGenerator: MenuGenerator,
    opt_validator?: FieldDropdownValidator,
    opt_config?: FieldConfig,
    isSearch?: boolean,
  ) {
    super(menuGenerator, opt_validator, opt_config);
    if (isSearch !== undefined) {
      this.isSearch = isSearch;
    }
  }

  protected override showEditor_(opt_e?: MouseEvent) {
    const component = defineAsyncComponent(() => import("./DropDownView.vue"));
    const componentVNode = createVNode(component, { blockly: this, searchInput: this.isSearch });
    componentVNode.appContext = window.app._context;
    const container = document.createElement("div");
    render(componentVNode, container);
    const div = Blockly.DropDownDiv.getContentDiv();
    div.appendChild(container);
  }

  protected override bindEvents_() {
    super.bindEvents_();
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
          console.log(event);
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

  // eslint-disable-next-line class-methods-use-this
  close() {
    Blockly.DropDownDiv.hide();
  }
}

BlocklyUtil.registerField("field_dropdown_view", DropDownView);
