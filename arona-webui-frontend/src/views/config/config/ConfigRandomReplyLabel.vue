<template>
  <el-row justify="space-between">
    <el-col :span="20">
      <el-form v-model="filterForm" inline>
        <el-form-item label="内容包含:" prop="content">
          <el-input v-model="filterForm.content" clearable />
        </el-form-item>
      </el-form>
    </el-col>
    <el-col :span="4" class="text-right">
      <el-button type="primary" :icon="Plus" @click="onCreateNewLabel">新增标签</el-button>
    </el-col>
  </el-row>
  <el-row>
    <el-table :data="filteredTableData" row-key="id" border stripe>
      <el-table-column type="index" label="编号" width="60" />
      <el-table-column prop="value" label="标签名" />
      <el-table-column prop="weight" label="权重" width="60" />
      <el-table-column prop="label" label="操作" width="120">
        <template #default="{ row }">
          <el-button type="primary" link @click="onEditLabel(row)">编辑</el-button>
          <el-button type="danger" link @click="onDeleteLabel(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-row>
  <el-dialog v-model="showEditDialog" show-close append-to-body :title="editOrCreate ? '编辑' : '新建'" width="600">
    <el-form :model="formData" label-width="100">
      <el-form-item label="标签名" prop="value">
        <el-input v-model="formData.value" placeholder="请输入标签名" />
      </el-form-item>
      <el-form-item label="权重" prop="weight">
        <el-input-number v-model="formData.weight" placeholder="请输入权重" style="width: 160px" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="text-center">
        <el-button type="primary" @click="onConfirmEditOrCreate">保存</el-button>
        <el-button @click="showEditDialog = false">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { Plus } from "@element-plus/icons-vue";
import { dataFilterChain, deepCopy, fillForm } from "@/utils";
import ReplyApi from "@/api/modules/reply";
import { ReplyLabel } from "@/interface/modules/reply";
import { IWarningConfirm, successMessage } from "@/utils/message";

const DefaultFormValue: ReplyLabel = {
  id: 0,
  value: "",
  weight: 1,
};
const tableData = ref<ReplyLabel[]>([]);
const filterForm = reactive<IFilterForm>({
  content: "",
  weight: undefined,
});
const showEditDialog = ref(false);
const formData = reactive<ReplyLabel>(deepCopy(DefaultFormValue));

function onConfirmEditOrCreate() {
  new Promise<string>((resolve) => {
    if (editOrCreate.value) {
      ReplyApi.updateReplyLabel(formData).then(() => {
        resolve("更新成功");
      });
    } else {
      ReplyApi.createReplyLabel(formData).then(() => {
        resolve("创建成功");
      });
    }
  }).then((msg) => {
    successMessage(msg);
    fetchData();
  });
}
function onCreateNewLabel() {
  fillForm<ReplyLabel>(formData, deepCopy(DefaultFormValue), ["id", "value", "weight"]);
  showEditDialog.value = true;
}
function onEditLabel(label: ReplyLabel) {
  fillForm<ReplyLabel>(formData, deepCopy(label), ["id", "value", "weight"]);
  showEditDialog.value = true;
}
function onDeleteLabel(label: ReplyLabel) {
  IWarningConfirm("删除", `确认要删除标签: ${label.value} 吗?`).then(() => {
    ReplyApi.deleteReplyLabel(label.id).then(() => {
      successMessage(`标签: ${label.value} 删除成功`);
    });
  });
}

/**
 * 判断当前是在编辑还是在新建 true为编辑
 */
const editOrCreate = computed(() => formData.id !== 0);
const filteredTableData = computed(() =>
  dataFilterChain(tableData.value, filterForm, [filterContent, filterWeight], ["content", "weight"]),
);
function fetchData() {
  ReplyApi.fetchReplyLabels().then(({ data: labelData }) => {
    tableData.value = labelData;
  });
}
onMounted(() => {
  fetchData();
});
function filterContent(data: ReplyLabel[], value: string): ReplyLabel[] {
  return data.filter((label) => label.value.indexOf(value) !== -1);
}
function filterWeight(data: ReplyLabel[], value: number): ReplyLabel[] {
  return data.filter((label) => label.weight === value);
}
interface IFilterForm {
  content: string;
  weight: number | undefined;
}
</script>

<style lang="scss" scoped></style>
