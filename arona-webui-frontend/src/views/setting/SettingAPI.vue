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
      <el-button>保存</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { heartbeat } from "@/api";
import { currentAPI } from "@/api/http";
import { APISettingForm } from "@/interface/modules/setting";
import useBaseStore from "@/store/base";
import { successMessage } from "@/utils/message";

const baseStore = useBaseStore();
const form = reactive<APISettingForm>({
  host: baseStore.host || "http://127.0.0.1",
  port: baseStore.port || 8080,
});
function testConnection() {
  heartbeat(currentAPI(form.host!, form.port!)).then((res) => {
    if (res.data === "pong") {
      successMessage("连接成功");
      baseStore.saveAPISetting(form.host!, form.port!);
      baseStore.syncContacts();
    }
  });
}
function saveAPISetting() {}
</script>

<style lang="scss" scoped></style>
