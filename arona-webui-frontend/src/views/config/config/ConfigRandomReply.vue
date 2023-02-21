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
      <el-button type="primary" :icon="Plus" @click="onCreateReplyGroup">新增回复</el-button>
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
          <span v-else>
            <el-image :src="loadImage(row.content.content || row.content)" lazy fit="contain" style="width: 200px" />
          </span>
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
      <el-table-column prop="operation" label="操作" width="120">
        <template #default="{ row }">
          <div v-if="row.children">
            <el-button type="primary" link @click="onEditReplyGroup(row)">编辑</el-button>
            <el-button type="danger" link @click="onDeleteReplyGroup(row)">删除</el-button>
          </div>
        </template>
      </el-table-column>
    </el-table>
  </el-row>
  <el-dialog v-model="showEditDialog" show-close append-to-body :title="editOrCreate ? '编辑' : '新建'" width="600">
    <el-form :model="formData" label-width="60" label-position="left">
      <el-form-item label="标签" prop="label">
        <el-select v-model="formData.label" clearable multiple>
          <el-option v-for="(e, index) in labels" :key="index" :label="e.value" :value="e.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="权重" prop="weight">
        <el-input-number v-model="formData.weight" placeholder="请输入权重" style="width: 160px" />
      </el-form-item>
    </el-form>
    <el-row style="margin-bottom: 16px">
      <el-button type="primary" @click="onCreateReplyItem">新增回复</el-button>
    </el-row>
    <el-table :data="formData.content" row-key="id" border stripe default-expand-all>
      <el-table-column type="index" label="编号" width="60" />
      <el-table-column prop="content" label="内容">
        <template #default="{ row }">
          <span v-if="row.type === 'String'">
            {{ row.content.content || row.content }}
          </span>
          <el-image :src="loadImage(row.content.content || row.content)" lazy fit="contain" style="width: 200px" />
        </template>
      </el-table-column>
      <el-table-column prop="label" label="操作" width="120">
        <template #default="{ row, $index }">
          <el-button type="primary" link @click="onEditReplyItem(row, $index)">编辑</el-button>
          <el-button type="danger" link @click="onDeleteReplyItem(row, $index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-dialog
      v-model="showEditItemDialog"
      show-close
      :title="itemEditOrCreate ? '编辑' : '新建'"
      width="500"
      class="dialog-body-padding-bottom-0"
    >
      <el-form :model="formDataItem" label-width="100" label-position="left">
        <el-form-item label="消息类型" prop="type">
          <el-select v-model="formDataItem.type">
            <el-option v-for="(e, index) in replyItemTypeList" :key="index" :label="e.label" :value="e.value" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="formDataItem.type === 'Image'" label="图片" prop="content">
          <el-upload
            ref="uploadRef"
            class="avatar-uploader"
            :show-file-list="false"
            :limit="1"
            :auto-upload="false"
            :on-exceed="onExceed"
            :on-change="onImageChange"
            :http-request="uploadImage"
          >
            <img v-if="imageUrl" :src="imageUrl" class="avatar" alt="" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </el-form-item>
        <el-form-item v-if="formDataItem.type === 'String'" label="文字" prop="content">
          <el-input v-model="formDataItem.content" placeholder="请输入内容" style="width: 160px" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="text-center">
          <el-button type="primary" @click="onConfirmEditOrCreateItem">保存</el-button>
          <el-button @click="showEditItemDialog = false">取消</el-button>
        </div>
      </template>
    </el-dialog>
    <template #footer>
      <div class="text-center">
        <el-button type="primary" @click="onConfirmEditOrCreateGroup">保存</el-button>
        <el-button @click="showEditDialog = false">取消</el-button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
// @ts-ignore
import { Plus } from "@element-plus/icons-vue";
import { genFileId, UploadFile, UploadProps, UploadRequestOptions } from "element-plus";
import { UploadRawFile, UploadUserFile } from "element-plus/es/components/upload/src/upload";
import { ReplyGroup, ReplyItem, ReplyItemType, ReplyItemTypeList, ReplyLabel } from "@/interface/modules/reply";
import { dataFilterChain, deepCopy, fillForm } from "@/utils";
import ReplyApi from "@/api/modules/reply";
import { IWarningConfirm, successMessage, warningMessage } from "@/utils/message";
import service from "@/api/http";
import FileApi from "@/api/modules/file";

const DefaultFormValue: ReplyGroup = {
  id: 0,
  label: [],
  weight: 1,
  content: [],
};
const DefaultItemFormValue: ReplyItem = {
  type: "String",
  content: "",
};
const tableData = ref<MapReplyGroup[]>([]);
const labels = ref<ReplyLabel[]>([]);
const filterForm = reactive<IFilterForm>({
  content: "",
  weight: undefined,
  label: [],
});
const showEditDialog = ref(false);
const showEditItemDialog = ref(false);
const itemEditOrCreate = ref(false); // 回复条目编辑类型 true 编辑 false 新建
const formData = reactive<ReplyGroup>(deepCopy(DefaultFormValue));
const formDataItem = reactive<ReplyItem>(deepCopy(DefaultItemFormValue));
const replyItemTypeList = ref(ReplyItemTypeList);
const imageUrl = ref<string>("");
const uploadRef = ref();
let imageRawFile: UploadRawFile | undefined;
let imageDirty = false;
let formDataItemIndex: number;
function onCreateReplyGroup() {
  fillForm<ReplyGroup>(formData, deepCopy(DefaultFormValue), ["id", "label", "weight", "content"]);
  showEditDialog.value = true;
}
function onEditReplyGroup(group: MapReplyGroup) {
  formData.id = group.id;
  formData.weight = group.weight;
  formData.label = group.label;
  formData.content = group.children && group.children.length > 1 ? group.children : [group.content];
  showEditDialog.value = true;
}
function onDeleteReplyGroup(group: MapReplyGroup) {
  IWarningConfirm("删除", `确认要删除回复吗?`).then(() => {
    ReplyApi.deleteReplyGroup(group.id).then(() => {
      successMessage(`删除成功`);
      fetchData();
      showEditDialog.value = false;
    });
  });
}
function onCreateReplyItem() {
  fillForm<ReplyItem>(formDataItem, deepCopy(DefaultItemFormValue), ["type", "content"]);
  imageUrl.value = "";
  imageRawFile = undefined;
  showEditItemDialog.value = true;
  imageDirty = true;
  itemEditOrCreate.value = false;
}
function onEditReplyItem(item: ReplyItem, index: number) {
  fillForm<ReplyItem>(formDataItem, deepCopy(item), ["type", "content"]);
  itemEditOrCreate.value = true;
  formDataItemIndex = index;
  showEditItemDialog.value = true;
  imageDirty = false;
}
function onDeleteReplyItem(item: ReplyItem, index: number) {
  IWarningConfirm("删除", `确认要删除回复吗?`).then(() => {
    formData.content.splice(index, 1);
  });
}
function onConfirmEditOrCreateGroup() {
  new Promise<string>((resolve) => {
    if (editOrCreate.value) {
      ReplyApi.updateReplyGroup(formData).then(() => {
        showEditDialog.value = false;
        resolve("更新成功");
      });
    } else {
      ReplyApi.createReplyGroup(formData).then(() => {
        showEditDialog.value = false;
        resolve("创建成功");
      });
    }
  }).then((msg) => {
    successMessage(msg);
    fetchData();
  });
}
function onConfirmEditOrCreateItem() {
  const { type } = formDataItem;
  new Promise<ReplyItem>((resolve) => {
    if (type === "Image" && imageDirty) {
      service.upload("/file/image", imageRawFile!).then(({ data }) => {
        successMessage("上传成功");
        showEditItemDialog.value = false;
        resolve({
          type: formDataItem.type,
          content: data,
        });
      });
      // TODO 上传图片
    } else if (type === "String") {
      showEditItemDialog.value = false;
      resolve({
        type: formDataItem.type,
        content: formDataItem.content,
      });
    }
  }).then((data) => {
    if (itemEditOrCreate.value) {
      formData.content.splice(formDataItemIndex, 1, data);
    } else {
      formData.content.push(data);
    }
  });
}
function onExceed(files: File[]): ReturnType<UploadProps["onExceed"]> {
  uploadRef.value!.clearFiles();
  const file = files[0] as UploadRawFile;
  file.uid = genFileId();
  uploadRef.value!.handleStart(file);
  doChangeImage(file);
}
function onImageChange(file: UploadFile): ReturnType<UploadProps["onChange"]> {
  const fileType = file.raw?.type || "";
  if (!["image/jpeg", "image/png", "image/gif"].includes(fileType)) {
    warningMessage("不支持jpg/png/gif格式以外的图片");
    return;
  }
  doChangeImage(file.raw!);
}
function doChangeImage(file: UploadRawFile) {
  imageUrl.value = URL.createObjectURL(file);
  imageRawFile = file;
  imageDirty = true;
}
function uploadImage(options: UploadRequestOptions): Promise<void> {
  return new Promise((resolve) => {
    resolve();
  });
}
function loadImage(id: string) {
  return FileApi.buildFileDownloadPath(id);
}
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
function fetchData() {
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
              ? group.content[0]
              : {
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
          children: group.content.length === 1 ? [] : group.content,
        };
        return mapData;
      });
    });
  });
}
onMounted(() => {
  fetchData();
});
interface IFilterForm {
  content: string;
  weight: number | undefined;
  label: number[];
}
interface MapReplyGroup extends Pick<ReplyGroup, "id" | "weight" | "label"> {
  content: ReplyItem;
  mapLabel: ReplyLabel[];
  children?: ReplyItem[];
}
</script>

<style lang="scss" scoped>
.avatar-uploader {
  :deep(.el-upload) {
    border: 1px dashed var(--el-border-color);
    border-radius: 6px;
    cursor: pointer;
    position: relative;
    overflow: hidden;
    transition: var(--el-transition-duration-fast);
    &:hover {
      border-color: var(--el-color-primary);
    }
  }
  .avatar-uploader-icon {
    font-size: 28px;
    color: #8c939d;
    width: 200px;
    height: 178px;
    text-align: center;
  }
  .avatar {
    width: 200px;
  }
}
</style>
