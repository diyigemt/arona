<template>
  <div>
    <el-select v-model="selectBlockIndex" style="margin-right: 16px">
      <el-option v-for="(e, index) in blockList" :key="index" :label="e.name" :value="index" @change="setBlock" />
    </el-select>
    <el-button type="primary" @click="onCreateNewProject">新建项目</el-button>
    <el-button type="primary" @click="onSaveCurrentProject">保存当前项目</el-button>
    <el-button type="danger" @click="onResetWorkspace">重置</el-button>
  </div>
  <el-row :gutter="16" style="margin-top: 16px">
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
import BlocklyConfig, { blocks, workspaceBlocks } from "@/blockly";
// @ts-ignore
import aronaGenerator from "@/blockly/generator";
import { BlocklyProject, BlocklyProjectWorkspace } from "@/interface/modules/blockly";
import { fetchBlocklyProjectList, saveBlocklyProject } from "@/api/modules/blockly";
import { IConfirm, infoMessage, IPrompt, successMessage } from "@/utils/message";

const blocklyDiv = ref();
const output = ref<string>();
const workspace = shallowRef();
const blockList = ref<BlocklyProject[]>();
const selectBlockIndex = ref<number>();
onMounted(() => {
  doFetchBlocklyProjectList();
});
function doFetchBlocklyProjectList() {
  fetchBlocklyProjectList()
    .then((res) => {
      blockList.value = res.data.map((item) => {
        item.blocklyProject = JSON.parse(item.blocklyProject as string);
        return item;
      });
    })
    .then(() => {
      Blockly.defineBlocksWithJsonArray(blocks);
      workspace.value = Blockly.inject(blocklyDiv.value, BlocklyConfig);
      // Blockly.Xml.domToWorkspace(workspaceBlocks, workspace.value);
      setBlock(0);
    });
}
function setBlock(index: number) {
  if (blockList.value && blockList.value.length <= index) {
    infoMessage("没有获取到已存在的blockly项目, 将会新建项目");
    Blockly.Xml.domToWorkspace(workspaceBlocks, workspace.value);
    return;
  }
  const block = (blockList.value || [])[index];
  Blockly.serialization.workspaces.load(block.blocklyProject as BlocklyProjectWorkspace, workspace.value);
  selectBlockIndex.value = index;
}
function onSaveCurrentProject() {
  const code = aronaGenerator.workspaceToCode(workspace.value);
  output.value = JSON.stringify({
    trigger: JSON.parse(code),
    blocklyProject: JSON.stringify(Blockly.serialization.workspaces.save(workspace.value)),
  });
  IPrompt("保存项目", "请输入项目名称:", {
    confirmButtonText: "保存",
    cancelButtonText: "取消",
    inputPattern: /.+/,
    inputErrorMessage: "不能为空",
  }).then(({ value }) => {
    saveBlocklyProject({
      trigger: JSON.parse(code),
      projectName: value,
      blocklyProject: JSON.stringify(Blockly.serialization.workspaces.save(workspace.value)),
    }).then(() => {
      doFetchBlocklyProjectList();
      successMessage("保存成功");
    });
  });
}
function onResetWorkspace() {
  doReset("重置后所有未保存的内容都将丢失,是否确认?");
}
function onCreateNewProject() {
  doReset("所有未保存的内容都将丢失,是否确认?");
}
function doReset(message: string) {
  IConfirm("警告", message, {
    type: "warning",
  }).then(() => {
    workspace.value.clear();
    Blockly.Xml.domToWorkspace(workspaceBlocks, workspace.value);
  });
}
</script>

<style scoped lang="scss">
.container {
  width: 100%;
  height: 50vh;
  min-height: 500px;
}
.blocklyDiv {
  height: 100%;
  width: 100%;
  text-align: left;
}
</style>
