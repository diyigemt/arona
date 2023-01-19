import Blockly, { FieldDropdown } from "blockly";
import { createVNode, h, render } from "vue";
import BlocklyUtil from "@/blockly/BlocklyUtil";

export default class FieldSearchView extends FieldDropdown {
  // eslint-disable-next-line camelcase,@typescript-eslint/no-unused-vars,no-underscore-dangle
  protected override showEditor_(opt_e?: MouseEvent) {
    const component = defineAsyncComponent(() => import("./SearchView.vue"));
    const componentVNode = createVNode(component, { blockly: this });
    // eslint-disable-next-line no-underscore-dangle
    componentVNode.appContext = window.app._context;
    const container = document.createElement("div");
    render(componentVNode, container);
    const div = Blockly.DropDownDiv.getContentDiv();
    div.appendChild(container);
    // eslint-disable-next-line no-underscore-dangle
    Blockly.DropDownDiv.showPositionedByField(this);
  }
}

BlocklyUtil.registerField("field_search_view", FieldSearchView);
