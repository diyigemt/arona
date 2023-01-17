import Blockly, { BlockSvg, Field, FieldDropdown } from "blockly";
import { createApp } from "vue";
import { FieldConfig } from "blockly/core/field";
import { FieldDropdownFromJsonConfig, FieldDropdownValidator, MenuGenerator } from "blockly/core/field_dropdown";
import BlocklyUtil from "@/blockly/BlocklyUtil";
import SearchView from "@/blockly/widgets/SearchView.vue";

export default class FieldSearchView extends FieldDropdown {
  vm = createApp(SearchView);

  container = Blockly.DropDownDiv.getContentDiv() as HTMLElement;

  // eslint-disable-next-line camelcase
  constructor(menuGenerator: MenuGenerator, opt_validator?: FieldDropdownValidator, opt_config?: FieldConfig) {
    super(menuGenerator, opt_validator, opt_config);
    // Blockly.DropDownDiv.getContentDiv().appendChild(this.container);
    // this.container.id = "fieldSearchViewContainer";
    this.vm.mount(this.container);
  }

  // eslint-disable-next-line camelcase,no-underscore-dangle,class-methods-use-this,@typescript-eslint/no-unused-vars
  protected showEditor_(opt_e?: MouseEvent) {
    // const container = document.createElement("div") as HTMLDivElement;
    // Blockly.DropDownDiv.getContentDiv().appendChild(container);
    // container.id = "fieldSearchViewContainer";
    // this.vm.mount("#fieldSearchViewContainer");
    this.container.style.width = "100";
    Blockly.DropDownDiv.showPositionedByField(this);
    // eslint-disable-next-line no-underscore-dangle
    // super.showEditor_(opt_e);
  }
}

BlocklyUtil.registerField("field_search_view", FieldSearchView);
