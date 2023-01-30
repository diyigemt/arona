<template>
  <el-row>
    <el-form v-model="filterForm" inline>
      <el-form-item label="内容包含:" prop="content">
        <el-input v-model="filterForm.content" clearable />
      </el-form-item>
      <el-form-item label="权重为:" prop="weight">
        <el-input v-model.number="filterForm.weight" clearable />
      </el-form-item>
      <el-form-item label="属于标签:" prop="label">
        <el-select v-model="filterForm.label" clearable multiple>
          <el-option v-for="(e, index) in labels" :key="index" :label="e.value" :value="e.id" />
        </el-select>
      </el-form-item>
    </el-form>
  </el-row>
  <el-row>
    <el-table :data="filteredTableData" row-key="id" border stripe default-expand-all>
      <el-table-column type="index" label="编号" width="60" />
      <el-table-column prop="content" label="内容">
        <template #default="{ row }">
          <span v-if="(row.content.type || row.type) === 'String'">
            {{ row.content.content || row.content }}
          </span>
          <span v-else>这里本来应该是图片的, 但是没做</span>
        </template>
      </el-table-column>
      <el-table-column prop="weight" label="权重" width="60" />
      <el-table-column prop="label" label="标签" width="250">
        <template #default="{ row }">
          <div style="display: flex; flex-direction: row; gap: 10px">
            <el-tag v-for="(e, index) in row.mapLabel" :key="index">
              {{ e.value }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="label" label="操作" width="120">
        <template #default>
          <el-button>没做</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-row>
</template>

<script setup lang="ts">
import { ReplyGroup, ReplyItem, ReplyLabel } from "@/interface/modules/reply";
import { dataFilterChain } from "@/utils";
import { fetchReplyGroups, fetchReplyLabels } from "@/api/modules/reply";

const tableData = ref<MapReplyGroup[]>([]);
const labels = ref<ReplyLabel[]>([]);
const filterForm = reactive<IFilterForm>({
  content: "",
  weight: undefined,
  label: [],
});

onMounted(() => {
  fetchReplyLabels().then(({ data: labelData }) => {
    labels.value = labelData;
    fetchReplyGroups().then(({ data }) => {
      tableData.value = data.map((group) => {
        const mapData: MapReplyGroup = {
          id: group.id,
          weight: group.weight,
          label: group.label,
          content:
            group.content.length === 1
              ? {
                  ...group.content[0],
                  id: String(group.id),
                }
              : {
                  id: String(group.id),
                  type: "String",
                  content: "多个内容",
                },
          mapLabel: group.label.map((label) => {
            const tmp = labels.value.filter((item) => item.id === label);
            if (tmp.length > 0) {
              return tmp[0];
            }
            return {
              id: -1,
              weight: -1,
              value: "错误",
            };
          }),
          children:
            group.content.length === 1
              ? []
              : group.content.map((content, index) => ({
                  ...content,
                  id: `${group.id}-${index}`,
                })),
        };
        return mapData;
      });
    });
  });
});

const filteredTableData = computed(() =>
  dataFilterChain(
    tableData.value,
    filterForm,
    [filterContent, filterWeight, filterLabel],
    ["content", "weight", "label"],
  ),
);
function filterContent(data: MapReplyGroup[], value: string): MapReplyGroup[] {
  return data.filter((group) => {
    if (!group.children || (group.children && group.children.length === 0)) {
      return group.content.content.indexOf(value) !== -1;
    }
    return group.children.filter((item) => item.type === "String").some((item) => item.content.indexOf(value) !== -1);
  });
}
function filterWeight(data: MapReplyGroup[], value: number): MapReplyGroup[] {
  return data.filter((group) => group.weight === value);
}
function filterLabel(data: MapReplyGroup[], value: number[]): MapReplyGroup[] {
  return data.filter((group) => group.label.some((label) => value.indexOf(label) !== -1));
}
interface IFilterForm {
  content: string;
  weight: number | undefined;
  label: number[];
}
interface IdReplyItem extends ReplyItem {
  id: string;
}
interface MapReplyGroup extends Pick<ReplyGroup, "id" | "weight" | "label"> {
  content: IdReplyItem;
  mapLabel: ReplyLabel[];
  children?: IdReplyItem[];
}
</script>

<style lang="scss" scoped></style>
