import Blockly, { FieldDropdown } from "blockly";
import { App, createApp } from "vue";
import { FieldConfig } from "blockly/core/field";
import { FieldDropdownValidator, MenuGenerator } from "blockly/core/field_dropdown";
import BlocklyUtil from "@/blockly/BlocklyUtil";
import SearchView from "@/blockly/widgets/SearchView.vue";

export default class FieldSearchView extends FieldDropdown {
  dropDownDiv = Blockly.DropDownDiv;

  container = document.createElement("div") as HTMLDivElement;

  vm: App<Element> | null = this.createVueApp();

  // eslint-disable-next-line camelcase
  constructor(menuGenerator: MenuGenerator, opt_validator?: FieldDropdownValidator, opt_config?: FieldConfig) {
    super(menuGenerator, opt_validator, opt_config);
    this.container.id = "fieldSearchViewContainer";
    this.vm!.mount(this.container);
  }

  // eslint-disable-next-line camelcase,@typescript-eslint/no-unused-vars,no-underscore-dangle
  protected override showEditor_(opt_e?: MouseEvent) {
    if (this.vm == null) {
      this.vm = this.createVueApp();
      this.vm!.mount(this.container);
    }
    this.dropDownDiv.getContentDiv().appendChild(this.container);
    // eslint-disable-next-line no-underscore-dangle
    this.dropDownDiv.showPositionedByField(this, this.dropdownDispose.bind(this));
  }

  private dropdownDispose() {
    this.vm!.unmount();
    this.vm = null;
  }

  private createVueApp() {
    return createApp(SearchView, { blockly: this });
  }
}

BlocklyUtil.registerField("field_search_view", FieldSearchView);
