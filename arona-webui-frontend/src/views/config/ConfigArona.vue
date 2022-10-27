<template>
  <el-form :model="form" label-width="180px" label-position="left">
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
          <el-select v-model="form.groups" class="w-full">
            <el-option v-for="(e, index) in select.groups" :key="index" :value="e.id" :label="e.name" />
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
          <el-select v-model="form.managerGroup" class="w-full">
            <el-option v-for="(e, index) in select.friends" :key="index" :value="e.id" :label="e.name" />
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
  </el-form>
</template>

<script setup lang="ts">
import { AronaConfigForm } from "@/interface";
import { Friend, Group } from "@/types/contact";
import useBaseStore from "@/store/base";

const { t } = useI18n();
const baseStore = useBaseStore();
const form = ref<AronaConfigForm>({
  qq: null,
  groups: [123123],
  managerGroup: [],
  permissionDeniedMessage: "",
  sendOnlineMessage: false,
  onlineMessage: "",
  sendOfflineMessage: false,
  offlineMessage: "",
  updateCheckTime: 8,
  endWithSensei: "老师",
  sendStatus: false,
  uuid: "",
  remoteCheckInterval: 1,
});
const select = reactive<Select>({
  groups: [
    {
      id: 123123,
      name: "测试",
    },
  ],
  friends: [
    {
      id: 123,
      name: "nick name",
      remark: "",
    },
  ],
});
onMounted(() => {
  select.groups = baseStore.botGroups;
  select.friends = baseStore.botFriends;
});
interface Select {
  groups: Omit<Group, "permission">[];
  friends: Friend[];
}
</script>

<style lang="scss" scoped></style>
