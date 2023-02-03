<template>
  <div>
    <el-select
      v-model="selectBlockIndex"
      style="margin-right: 16px"
      :disabled="projectList.length === 0"
      @change="setBlock"
    >
      <el-option v-for="(e, index) in projectList" :key="index" :label="e.name" :value="index" @change="setBlock" />
    </el-select>
    <el-button type="primary" @click="onCreateNewProject()">新建项目</el-button>
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
import Blockly, { Block } from "blockly";
import { ElMessageBox } from "element-plus";
import BlocklyConfig, { initBlockly, workspaceBlocks } from "@/blockly";
import aronaGenerator from "@/blockly/generator";
import { BlocklyProject, BlocklyProjectWorkspace } from "@/interface/modules/blockly";
import BlocklyApi from "@/api/modules/blockly";
import {
  errorMessage,
  infoMessage,
  IPrompt,
  IWarningConfirm,
  successMessage,
  warningMessage
} from "@/utils/message";
import useBaseStore from "@/store/base";

const blocklyDiv = ref();
const output = ref<string>();
const workspace = shallowRef();
const projectList = ref<BlocklyProject[]>([]);
const selectBlockIndex = ref<number>();
const debugMode = ref(true); // false 原生 true format好看
const isNewProject = ref(false);
onMounted(() => {
  initBlockly();
  const ws = Blockly.inject(blocklyDiv.value, BlocklyConfig);
  ws.addChangeListener(onBlockClick);
  workspace.value = ws;
  doFetchBlocklyProjectList();
});
function onBlockClick(event: Blockly.Events.ClickJson) {
  if (event.targetType === "block" && event.type === "click") {
    const { blockId } = event;
    const block: Block = workspace.value.getBlockById(blockId);
    if (block.data === "select") {
      ElMessageBox.prompt("请输入", "").then(({ value }) => {
        block.getField("NAME")?.setValue(value);
      });
    }
  }
}
function doFetchBlocklyProjectList() {
  BlocklyApi.fetchBlocklyProjectList()
    .then((res) => {
      projectList.value = res.data.map((item) => {
        item.blocklyProject = JSON.parse(item.blocklyProject as string);
        return item;
      });
    })
    .then(() => {
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
  if (projectList.value && projectList.value.length <= index) {
    infoMessage("没有获取到已存在的blockly项目, 将会新建项目");
    Blockly.Xml.domToWorkspace(workspaceBlocks, workspace.value);
    return;
  }
  workspace.value.clear();
  const block = (projectList.value || [])[index];
  const baseStore = useBaseStore();
  baseStore
    .loadDataFromSave(block.userData)
    .then(() => {
      Blockly.serialization.workspaces.load(block.blocklyProject as BlocklyProjectWorkspace, workspace.value);
      selectBlockIndex.value = index;
    })
    .catch((e) => {
      console.error(e);
      errorMessage("存档加载失败");
    });
}
function onSaveCurrentProject() {
  const code = aronaGenerator.workspaceToCode(workspace.value);
  const groupBlocks = (workspace.value as Blockly.WorkspaceSvg)
    .getAllBlocks(false)
    .filter((targetBlock) => targetBlock.type === "groupIDBlock")
    .map((targetBlock) => {
      return Number(targetBlock.getFieldValue("groupIDInput"));
    })
    .filter((value, index, array) => array.indexOf(value) === index);
  onDebug();
  if (isNewProject.value || projectList.value?.length === 0) {
    IPrompt("保存项目", "请输入项目名称:", {
      confirmButtonText: "保存",
      cancelButtonText: "取消",
      inputPattern: /.+/,
      inputErrorMessage: "不能为空",
    }).then(({ value }) => {
      BlocklyApi.saveBlocklyProject({
        trigger: JSON.parse(code),
        projectName: value,
        uuid: null,
        blocklyProject: JSON.stringify(Blockly.serialization.workspaces.save(workspace.value)),
        userData: JSON.stringify({
          groups: groupBlocks,
        }),
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
    BlocklyApi.updateBlocklyProject({
      trigger: JSON.parse(code),
      projectName: (projectList.value || [])[selectBlockIndex.value as number].name,
      uuid: (projectList.value || [])[selectBlockIndex.value as number].uuid,
      blocklyProject: JSON.stringify(Blockly.serialization.workspaces.save(workspace.value)),
      userData: JSON.stringify({
        groups: groupBlocks,
      }),
    }).then(() => {
      doFetchBlocklyProjectList();
      successMessage("保存成功");
    });
  }
}
function onResetWorkspace() {
  IWarningConfirm("警告", "重置后所有未保存的内容都将丢失,是否确认?").then(() => {
    if (selectBlockIndex.value) {
      setBlock(selectBlockIndex.value!);
    } else {
      onCreateNewProject(true);
    }
  });
}
function onCreateNewProject(skipWarning = false) {
  isNewProject.value = true;
  if (skipWarning) {
    doCreate();
  } else {
    IWarningConfirm("警告", "所有未保存的内容都将丢失,是否确认?").then(() => {
      doCreate();
    });
  }
}

function onDeleteProject() {
  if (!isNewProject.value) {
    const projName = (projectList.value || [])[selectBlockIndex.value as number].name;
    // eslint-disable-next-line no-template-curly-in-string
    IWarningConfirm("警告", `确定删除${projName}`).then(() => {
      BlocklyApi.deleteBlocklyProject({
        trigger: JSON.parse('{"type":"GroupMessageEvent","expressions": [],"actions": []}'),
        projectName: projName,
        // 只有UUID是有用的，其余的都可以不给，但不能给NULL，后端能过反序列化就行
        uuid: (projectList.value || [])[selectBlockIndex.value as number].uuid,
        blocklyProject: "",
        userData: "",
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
  selectBlockIndex.value = undefined;
  Blockly.Xml.domToWorkspace(workspaceBlocks, workspace.value);
}
function onDebug() {
  const code = aronaGenerator.workspaceToCode(workspace.value);
  const row = Blockly.serialization.workspaces.save(workspace.value);
  const proj: BlocklyProject | null = (projectList.value || [])[selectBlockIndex.value as number];
  const groupBlocks = (workspace.value as Blockly.WorkspaceSvg)
    .getAllBlocks(false)
    .filter((targetBlock) => targetBlock.type === "groupIDBlock")
    .map((targetBlock) => {
      return Number(targetBlock.getFieldValue("groupIDInput"));
    })
    .filter((value, index, array) => array.indexOf(value) === index);
  output.value = JSON.stringify(
    {
      trigger: JSON.parse(code),
      projectName: proj?.name,
      uuid: proj?.uuid,
      blocklyProject: debugMode.value ? row : JSON.stringify(row),
      userData: {
        groups: groupBlocks,
      },
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
