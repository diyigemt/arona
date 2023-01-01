<template>
  <button style="height: 50px; width: 100px" @click="listener">Generate</button>
  <div class="container">
    <div ref="blocklyDiv" class="blocklyDiv"></div>
  </div>
</template>

<script setup lang="ts">
import Blockly from "blockly";
import BlocklyConfig, { workspaceBlocks, blocks } from "@/blockly";
// @ts-ignore
import aronaGenerator from "@/blockly/generator";

const blocklyDiv = ref();
const workspace = shallowRef();

onMounted(() => {
  Blockly.defineBlocksWithJsonArray(blocks);
  workspace.value = Blockly.inject(blocklyDiv.value, BlocklyConfig);
  Blockly.Xml.domToWorkspace(workspaceBlocks, workspace.value);
});
function listener() {
  const code = aronaGenerator.workspaceToCode(workspace.value);
  // eslint-disable-next-line no-alert
  alert(code);
}
</script>

<style scoped lang="scss">
.container {
  width: 50vw;
  height: 50vh;
  min-height: 500px;
}
.blocklyDiv {
  height: 100%;
  width: 100%;
  text-align: left;
}
</style>
