import { BlocklyOptions } from "blockly";
import ToolBox from "@/blockly/toolbox";

const BlocklyConfig: BlocklyOptions = {
  media: "../media/",
  grid: {
    spacing: 25,
    length: 3,
    colour: "#ccc",
    snap: true,
  },
  toolbox: ToolBox,
  theme: "Zelos",
};

export default BlocklyConfig;
