<template>
  <el-select v-model="selectBlockIndex">
    <el-option v-for="(e, index) in blockList" :key="index" :label="e.name" :value="index" @change="setBlock" />
  </el-select>
  <el-button type="primary" @click="listener">CreateProject</el-button>
  <el-row :gutter="16">
    <el-col :span="16">
      <div class="container">
        <div ref="blocklyDiv" class="blocklyDiv"></div>
      </div>
    </el-col>
    <el-col :span="8">
      <el-input v-model="output" type="textarea" :autosize="{ minRows: 10 }" />
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import Blockly from "blockly";
import axios from "axios";
import BlocklyConfig, { blocks } from "@/blockly";
// @ts-ignore
import aronaGenerator from "@/blockly/generator";
import { BlocklyBlock } from "@/interface/modules/blockly";
import { fetchBlocklyBlockList } from "@/api/modules/blockly";

const blocklyDiv = ref();
const output = ref<string>();
const workspace = shallowRef();
const blockList = ref<BlocklyBlock[]>();
const selectBlockIndex = ref<number>();
onMounted(() => {
  fetchBlocklyBlockList()
    .then((res) => {
      blockList.value = res.data.map((item) => {
        item.blocklyProject = JSON.parse(item.blocklyProject);
        return item;
      });
    })
    .then(() => {
      Blockly.defineBlocksWithJsonArray(blocks);
      workspace.value = Blockly.inject(blocklyDiv.value, BlocklyConfig);
      // Blockly.Xml.domToWorkspace(workspaceBlocks, workspace.value);
      setBlock(0);
    });
});
function setBlock(index: number) {
  const block = (blockList.value || [])[index];
  Blockly.serialization.workspaces.load(block, workspace.value);
}
function listener() {
  const code = aronaGenerator.workspaceToCode(workspace.value);
  output.value = JSON.stringify({
    mode: "CREATE",
    trigger: JSON.parse(code),
    projectName: "",
    blocklyProject: JSON.stringify(Blockly.serialization.workspaces.save(workspace.value)),
  });
  axios({
    method: "post",
    url: "http://localhost:57920/api/v1/blockly/commit",
    data: {
      mode: "CREATE",
      trigger: JSON.parse(code),
      projectName: "",
      blocklyProject: JSON.stringify(Blockly.serialization.workspaces.save(workspace.value)),
    },
  }).then((response) => {
    console.log(response.data);
  });
}
</script>

<style scoped lang="scss">
.container {
  width: 100%vw;
  height: 50vh;
  min-height: 500px;
}
.blocklyDiv {
  height: 100%;
  width: 100%;
  text-align: left;
}
</style>
