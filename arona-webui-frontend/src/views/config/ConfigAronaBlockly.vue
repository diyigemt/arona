<template>
  <div>
    <el-select v-model="selectBlockIndex" style="margin-right: 16px" @change="setBlock">
      <el-option v-for="(e, index) in blockList" :key="index" :label="e.name" :value="index" @change="setBlock" />
    </el-select>
    <el-button type="primary" @click="onCreateNewProject">新建项目</el-button>
    <el-button type="primary" @click="onSaveCurrentProject">保存当前项目</el-button>
    <el-button type="danger" @click="onResetWorkspace">重置</el-button>
    <el-button type="danger" @click="onDeleteProject">删除</el-button>
    <el-button type="danger" @click="onDebug">Debug</el-button>
    <el-switch v-model="debugMode" active-text="format" inactive-text="raw" style="margin-left: 16px" />
  </div>
  <el-row :gutter="16" style="margin-top: 16px">
    <el-col :span="16">
      <div id="blockDiv" class="container">
        <div ref="blocklyDiv" class="blocklyDiv"></div>
      </div>
    </el-col>
    <el-col :span="8">
      <el-input v-model="output" type="textarea" :autosize="{ minRows: 20 }" />
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import Blockly from "blockly";
import BlocklyConfig, { blocks, workspaceBlocks } from "@/blockly";
import aronaGenerator from "@/blockly/generator";
import { BlocklyProject, BlocklyProjectWorkspace } from "@/interface/modules/blockly";
import {
  deleteBlocklyProject,
  fetchBlocklyProjectList,
  saveBlocklyProject,
  updateBlocklyProject,
} from "@/api/modules/blockly";
import { errorMessage, IConfirm, infoMessage, IPrompt, successMessage, warningMessage } from "@/utils/message";
import addBlocks from "@/blockly/blocks";
import injectExtensions from "@/blockly/extensions";
import { doFetchContacts } from "@/blockly/BlocklyUtil";
import addMutators from "@/blockly/mutators";

const blocklyDiv = ref();
const output = ref<string>();
const workspace = shallowRef();
const blockList = ref<BlocklyProject[]>();
const selectBlockIndex = ref<number>();
const debugMode = ref(true); // false 原生 true format好看
const isNewProject = ref(false);
onMounted(() => {
  Blockly.defineBlocksWithJsonArray(blocks);
  doFetchContacts();
  addBlocks();
  addMutators();
  workspace.value = Blockly.inject(blocklyDiv.value, BlocklyConfig);
  injectExtensions();
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
      // Blockly.Xml.domToWorkspace(workspaceBlocks, workspace.value);
      setBlock(0);
    })
    .catch((e) => {
      console.error(e);
      warningMessage("后端连接失败, 将新建空白项目");
      onCreateNewProject(true);
    });
}
function setBlock(index: number) {
  isNewProject.value = false;
  if (blockList.value && blockList.value.length <= index) {
    infoMessage("没有获取到已存在的blockly项目, 将会新建项目");
    Blockly.Xml.domToWorkspace(workspaceBlocks, workspace.value);
    return;
  }
  workspace.value.clear();
  const block = (blockList.value || [])[index];
  Blockly.serialization.workspaces.load(block.blocklyProject as BlocklyProjectWorkspace, workspace.value);
  selectBlockIndex.value = index;
}
function onSaveCurrentProject() {
  const code = aronaGenerator.workspaceToCode(workspace.value);
  onDebug();
  if (isNewProject.value || blockList.value?.length === 0) {
    IPrompt("保存项目", "请输入项目名称:", {
      confirmButtonText: "保存",
      cancelButtonText: "取消",
      inputPattern: /.+/,
      inputErrorMessage: "不能为空",
    }).then(({ value }) => {
      saveBlocklyProject({
        trigger: JSON.parse(code),
        projectName: value,
        uuid: null,
        blocklyProject: JSON.stringify(Blockly.serialization.workspaces.save(workspace.value)),
        // TODO: userData List<String>
        userData: "{ids: []}",
      })
        .then(() => {
          doFetchBlocklyProjectList();
          successMessage("保存成功");
        })
        .catch((reason) => {
          errorMessage(reason);
        });
    });
  } else {
    updateBlocklyProject({
      trigger: JSON.parse(code),
      projectName: (blockList.value || [])[selectBlockIndex.value as number].name,
      uuid: (blockList.value || [])[selectBlockIndex.value as number].uuid,
      blocklyProject: JSON.stringify(Blockly.serialization.workspaces.save(workspace.value)),
      userData: "{ids: []}",
    }).then(() => {
      doFetchBlocklyProjectList();
      successMessage("保存成功");
    });
  }
}
function onResetWorkspace() {
  IConfirm("警告", "重置后所有未保存的内容都将丢失,是否确认?", {
    type: "warning",
  }).then(() => {
    setBlock(selectBlockIndex.value!);
  });
}
function onCreateNewProject(skipWarning: boolean) {
  isNewProject.value = true;
  if (skipWarning) {
    doCreate();
  } else {
    IConfirm("警告", "所有未保存的内容都将丢失,是否确认?", {
      type: "warning",
    }).then(() => {
      doCreate();
    });
  }
}

function onDeleteProject() {
  if (!isNewProject.value) {
    const projName = (blockList.value || [])[selectBlockIndex.value as number].name;
    // eslint-disable-next-line no-template-curly-in-string
    IConfirm("警告", `确定删除${projName}`, {
      type: "warning",
    }).then(() => {
      deleteBlocklyProject({
        trigger: JSON.parse('{"id": 0,"type":"GroupMessageEvent","expressions": [],"actions": []}'),
        projectName: projName,
        // TODO:只有UUID是有用的，其余的都可以不给，但不能给NULL，后端能过反序列化就行
        uuid: (blockList.value || [])[selectBlockIndex.value as number].uuid,
        blocklyProject: "",
        userData: "{ids: []}",
      }).then(() => {
        doFetchBlocklyProjectList();
        successMessage("删除成功");
      });
    });
  } else {
    errorMessage("不能删除新建的存档");
  }
}

function doCreate() {
  workspace.value.clear();
  Blockly.Xml.domToWorkspace(workspaceBlocks, workspace.value);
}
function onDebug() {
  const code = aronaGenerator.workspaceToCode(workspace.value);
  const row = Blockly.serialization.workspaces.save(workspace.value);
  const proj: BlocklyProject | null = (blockList.value || [])[selectBlockIndex.value as number];
  output.value = JSON.stringify(
    {
      trigger: JSON.parse(code),
      projectName: proj?.name,
      uuid: proj?.uuid,
      blocklyProject: debugMode.value ? row : JSON.stringify(row),
      userData: "[]",
    },
    null,
    2,
  );
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
