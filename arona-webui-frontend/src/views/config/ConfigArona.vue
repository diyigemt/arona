<template>
  <el-form v-loading="loading.loading" :model="form" label-width="180px" label-position="left">
    <el-form-item label="qq">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input v-model.number="form.qq" />
        </el-col>
        <el-col :span="12">
          <div>指明运行arona的机器人qq号</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="groups" prop="groups">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-select v-model="form.groups" multiple class="w-full">
            <el-option
              v-for="(e, index) in select.groups"
              :key="index"
              :value="e.id"
              :label="e.name + '(' + e.id + ')'"
            >
              {{ e.name }}({{ e.id }})
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="12">
          <div>指明arona响应的qq群信息</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="managerGroup" prop="managerGroup">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-select v-model="form.managerGroup" multiple class="w-full">
            <el-option
              v-for="(e, index) in select.friends"
              :key="index"
              :value="e.id"
              :label="e.name + '(' + e.id + ')'"
            >
              {{ e.name }}({{ e.id }})
            </el-option>
          </el-select>
        </el-col>
        <el-col :span="12">
          <div>指明具有arona管理权限的qq号</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="permissionDeniedMessage" prop="permissionDeniedMessage">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input v-model="form.permissionDeniedMessage" />
        </el-col>
        <el-col :span="12">
          <div>当不具备管理员权限的用户尝试执行管理员权限时的回应信息</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="sendOnlineMessage" prop="sendOnlineMessage">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-switch v-model="form.sendOnlineMessage" />
        </el-col>
        <el-col :span="12">
          <div>发送上线消息</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="onlineMessage" prop="onlineMessage">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input v-model="form.onlineMessage" />
        </el-col>
        <el-col :span="12">
          <div>上线消息内容</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="sendOfflineMessage" prop="sendOfflineMessage">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-switch v-model="form.sendOfflineMessage" />
        </el-col>
        <el-col :span="12">
          <div>发送下线消息</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="offlineMessage" prop="offlineMessage">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input v-model="form.offlineMessage" />
        </el-col>
        <el-col :span="12">
          <div>上线消息内容</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="updateCheckTime" prop="updateCheckTime">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input-number v-model="form.updateCheckTime" :max="23" :min="0" />
        </el-col>
        <el-col :span="12">
          <div>每日检查更新时间</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="endWithSensei" prop="endWithSensei">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input v-model="form.endWithSensei" />
        </el-col>
        <el-col :span="12">
          <div>昵称后缀</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="sendStatus" prop="sendStatus">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-switch v-model="form.sendStatus" />
        </el-col>
        <el-col :span="12">
          <div>允许arona向后端发送匿名统计数据</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="uuid" prop="uuid">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input v-model="form.uuid" disabled />
        </el-col>
        <el-col :span="12">
          <div>uuid</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item label="remoteCheckInterval" prop="remoteCheckInterval">
      <el-row :gutter="16" class="w-full">
        <el-col :span="12">
          <el-input-number v-model="form.remoteCheckInterval" :max="23" :min="1" />
        </el-col>
        <el-col :span="12">
          <div>公告检查间隔</div>
        </el-col>
      </el-row>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" :loading="loading.confirmLoading" @click="doSave">保存</el-button>
      <el-button @click="doFetchConfig">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { AronaConfigForm, AvailableConfig } from "@/interface";
import { Friend, Group } from "@/types/contact";
import { fetchBotContacts } from "@/api/modules/contact";
import { warningMessage } from "@/utils/message";
import { buildConfigForm } from "@/views/config/util";

const { form, loading, doFetchConfig, doSave } = buildConfigForm<AronaConfigForm>(AvailableConfig.AronaConfig);
const select = reactive<Select>({
  groups: [],
  friends: [],
});
onMounted(() => {
  loading.loading = true;
  fetchBotContacts()
    .then((res) => {
      select.groups = res.data.groups;
      select.friends = res.data.friends;
    })
    .catch((err) => {
      warningMessage("获取bot联系人列表失败");
      console.log(err);
    });
  doFetchConfig();
});

interface Select {
  groups: Omit<Group, "permission">[];
  friends: Friend[];
}
</script>

<style lang="scss" scoped></style>
