import Blockly, { FieldDropdown } from "blockly";
import { createVNode, render } from "vue";
import BlocklyUtil from "@/blockly/BlocklyUtil";

export default class FieldSearchView extends FieldDropdown {
  /* eslint-disable camelcase,@typescript-eslint/no-unused-vars,no-underscore-dangle */
  protected override showEditor_(opt_e?: MouseEvent) {
    const component = defineAsyncComponent(() => import("./SearchView.vue"));
    const componentVNode = createVNode(component, { blockly: this });
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

BlocklyUtil.registerField("field_search_view", FieldSearchView);
