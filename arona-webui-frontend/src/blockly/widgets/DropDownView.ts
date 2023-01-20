import Blockly, { FieldDropdown } from "blockly";
import { createVNode, render } from "vue";
import { FieldConfig } from "blockly/core/field";
import { FieldDropdownValidator, MenuGenerator } from "blockly/core/field_dropdown";
import BlocklyUtil from "@/blockly/BlocklyUtil";

export default class DropDownView extends FieldDropdown {
  isSearch: boolean | undefined = false;

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
    const component = defineAsyncComponent(() => import("./SearchView.vue"));
    const componentVNode = createVNode(component, { blockly: this, searchInput: this.isSearch });
    componentVNode.appContext = window.app._context;
    const container = document.createElement("div");
    render(componentVNode, container);
    const div = Blockly.DropDownDiv.getContentDiv();
    div.appendChild(container);
  }

  // eslint-disable-next-line class-methods-use-this
  close() {
    Blockly.DropDownDiv.hide();
  }
}

BlocklyUtil.registerField("field_dropdown_view", DropDownView);
