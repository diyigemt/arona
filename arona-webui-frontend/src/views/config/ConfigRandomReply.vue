<template>
  <el-row justify="space-between">
    <el-col :span="20">
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
    </el-col>
    <el-col :span="4" class="text-right">
      <el-button type="primary" :icon="Plus" @click="onCreateReply">新增标签</el-button>
    </el-col>
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
        <template #default="{ row }">
          <el-button type="primary" link @click="onEditReply(row)">编辑</el-button>
          <el-button type="danger" link @click="onDeleteReply(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-row>
  <el-dialog v-model="showEditDialog" show-close append-to-body :title="editOrCreate ? '编辑' : '新建'" width="600">
    <el-form :model="formData" label-width="100">
      <el-form-item label="标签名" prop="value">
        <el-input v-model="formData.value" placeholder="请输入标签名" />
      </el-form-item>
      <el-form-item label="权重" prop="value">
        <el-input-number v-model="formData.weight" placeholder="请输入权重" style="width: 160px" />
      </el-form-item>
    </el-form>
    <template #footer>
      <div class="text-center">
        <el-button type="primary">保存</el-button>
        <el-button @click="showEditDialog = false">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
import { Plus } from "@element-plus/icons-vue";
import { ReplyGroup, ReplyItem, ReplyLabel } from "@/interface/modules/reply";
import { dataFilterChain, deepCopy, fillForm } from "@/utils";
import ReplyApi from "@/api/modules/reply";

const DefaultFormValue: ReplyGroup = {
  id: 0,
  label: [],
  weight: 1,
  content: [],
};
const tableData = ref<MapReplyGroup[]>([]);
const labels = ref<ReplyLabel[]>([]);
const filterForm = reactive<IFilterForm>({
  content: "",
  weight: undefined,
  label: [],
});
const showEditDialog = ref(false);
const formData = reactive<ReplyGroup>(deepCopy(DefaultFormValue));

function onCreateReply() {
  fillForm<ReplyGroup>(formData, deepCopy(DefaultFormValue), ["id", "label", "weight", "content"]);
  showEditDialog.value = true;
}
function onEditReply(group: MapReplyGroup) {
  formData.id = group.id;
  formData.weight = group.weight;
  formData.label = group.label;
  formData.content = group.children && group.children.length > 1 ? group.children : [group.content];
  showEditDialog.value = true;
}
function onDeleteReply(group: MapReplyGroup) {}
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
/**
 * 判断当前是在编辑还是在新建 true为编辑
 */
const editOrCreate = computed(() => formData.id !== 0);
onMounted(() => {
  ReplyApi.fetchReplyLabels().then(({ data: labelData }) => {
    labels.value = labelData;
    ReplyApi.fetchReplyGroups().then(({ data }) => {
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
