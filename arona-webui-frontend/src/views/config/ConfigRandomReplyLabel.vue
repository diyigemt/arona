<template>
  <el-row>
    <el-form v-model="filterForm" inline>
      <el-form-item label="内容包含:" prop="content">
        <el-input v-model="filterForm.content" clearable />
      </el-form-item>
    </el-form>
  </el-row>
  <el-row>
    <el-table :data="filteredTableData" row-key="id" border stripe>
      <el-table-column type="index" label="编号" width="60" />
      <el-table-column prop="value" label="标签" />
      <el-table-column prop="weight" label="权重" width="60" />
      <el-table-column prop="label" label="操作" width="120">
        <template #default>
          <el-button>没做</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-row>
</template>

<script setup lang="ts">
import { dataFilterChain } from "@/utils";
import { fetchReplyLabels } from "@/api/modules/reply";
import { ReplyLabel } from "@/interface/modules/reply";

const tableData = ref<ReplyLabel[]>([]);
const filterForm = reactive<IFilterForm>({
  content: "",
  weight: undefined,
});

onMounted(() => {
  fetchReplyLabels().then(({ data: labelData }) => {
    tableData.value = labelData;
  });
});

const filteredTableData = computed(() =>
  dataFilterChain(tableData.value, filterForm, [filterContent, filterWeight], ["content", "weight"]),
);
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
