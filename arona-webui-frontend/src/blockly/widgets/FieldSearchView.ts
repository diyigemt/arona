import Blockly, { BlockSvg, FieldDropdown } from "blockly";
import { createApp } from "vue";
import BlocklyUtil from "@/blockly/BlocklyUtil";
import SearchView from "@/blockly/widgets/SearchView.vue";

export default class FieldSearchView extends FieldDropdown {
  // eslint-disable-next-line camelcase,no-underscore-dangle,class-methods-use-this,@typescript-eslint/no-unused-vars
  protected showEditor_(opt_e?: MouseEvent) {
    const container = document.createElement("div") as HTMLDivElement;
    container.id = "fieldSearchViewContainer";
    Blockly.DropDownDiv.getContentDiv().appendChild(container);
    createApp(SearchView).mount("#fieldSearchViewContainer");
    Blockly.DropDownDiv.showPositionedByField(this, this.dropdownDispose.bind(this));
    // eslint-disable-next-line no-underscore-dangle
    // super.showEditor_(opt_e);
  }

  protected dropdownDispose() {

  }
}

BlocklyUtil.registerField("field_search_view", FieldSearchView);

// Blockly.Css.register(`
// .fieldSearchViewContainer {
//   box-sizing: border-box;
//   align-items: center;
//   display: block;
//   height: auto;
//   justify-content: center;
//   width: auto;
//   margin-left: 5px;
//   margin-right: 5px;
// }
//
// #searchInput {
//   height: 30px;
//   border: 2px solid #e8e1e1;
//   border-radius: 5px;
//   padding-inline-start: 10px;
//   margin-bottom: 10px;
// }
// `);
