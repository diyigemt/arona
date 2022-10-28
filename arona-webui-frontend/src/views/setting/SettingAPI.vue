<template>
  <el-form :model="form" label-width="180px" label-position="left">
    <el-form-item label="host">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input v-model="form.host" />
        </el-col>
        <el-col :span="12">
          <div>指明arona的后端地址</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="port">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input v-model.number="form.port" />
        </el-col>
        <el-col :span="12">
          <div>指明arona的后端端口</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="testConnection">测试</el-button>
      <el-button @click="doSave">保存</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ElForm, ElFormItem, ElRow, ElCol, ElInput, ElButton } from "element-plus";
import { heartbeat } from "@/api";
import { updateAPIService } from "@/api/http";
import { APISettingForm } from "@/interface/modules/setting";
import useSettingStore from "@/store/setting";
import { successMessage, warningMessage } from "@/utils/message";

const settingStore = useSettingStore();
const form = reactive<APISettingForm>({
  host: settingStore.api.host || "http://127.0.0.1",
  port: settingStore.api.port || 8080,
});
function testConnection() {
  updateAPIService(form.host!, form.port!);
  heartbeat().then((res) => {
    if (res) {
      settingStore.saveAPISetting(form.host!, form.port!);
    } else {
      warningMessage("连接失败");
    }
  });
}
function doSave() {
  settingStore.saveAPISetting(form.host!, form.port!);
  successMessage("保存成功!");
}
</script>

<style lang="scss" scoped></style>
