<template>
  <div class="container">
    <div ref="blocklyDiv" class="blocklyDiv"></div>
  </div>
</template>

<script setup lang="ts">
// import Blockly from "blockly";
// import BlocklyConfig from "@/blockly";
//
// const blocklyDiv = ref();
// const workspace = shallowRef();
//
// onMounted(() => {
//   workspace.value = Blockly.inject(blocklyDiv.value, BlocklyConfig);
// });
</script>

<style scoped lang="scss">
.container {
  width: 100%;
  height: 100%;
}
</style>
