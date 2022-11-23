<template>
  <div class="container">
    <div ref="blocklyDiv" class="blocklyDiv"></div>
  </div>
</template>

<script setup lang="ts">
import Blockly from "blockly";
import BlocklyConfig from "@/blockly";

const blocklyDiv = ref();
const workspace = shallowRef();

onMounted(() => {
  workspace.value = Blockly.inject(blocklyDiv.value, BlocklyConfig);
});
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
