<template>
  <el-row :gutter="16">
    <el-col :span="20">
      <el-form :model="form" label-width="180px" label-position="left">
        <el-form-item label="qq">
          <div>
            <el-input v-model.number="form.qq" />
          </div>
          <div>指明运行arona的机器人qq号</div>
        </el-form-item>
        <el-form-item label="groups" prop="groups">
          <div>
            <el-transfer
              v-model="form.groups"
              :data="select.groups"
              filterable
              :titles="['群列表', '服务的群']"
              :props="{ key: 'id', label: 'name' }"
              :filter-method="groupFilter"
            >
              <template #default="{ option }"> {{ option.name }} ({{ option.id }}) </template>
            </el-transfer>
          </div>
          <div>指明arona响应的qq群信息</div>
        </el-form-item>
        <el-form-item label="managerGroup" prop="managerGroup">
          <el-transfer
            v-model="form.managerGroup"
            :data="select.friends"
            filterable
            :titles="['好友列表', '管理员列表']"
            :props="{ key: 'id', label: 'nickname' }"
            :filter-method="friendFilter"
          >
            <template #default="{ option }"> {{ option.remark || option.nickname }}({{ option.id }}) </template>
          </el-transfer>
        </el-form-item>
        <el-form-item label="permissionDeniedMessage" prop="permissionDeniedMessage">
          <el-input v-model="form.permissionDeniedMessage" />
          <div>当不具备管理员权限的用户尝试执行管理员权限时的回应信息</div>
        </el-form-item>
        <el-form-item label="sendOnlineMessage" prop="sendOnlineMessage">
          <el-switch v-model="form.sendOnlineMessage" />
          <div>发送上线消息</div>
        </el-form-item>
        <el-form-item label="onlineMessage" prop="onlineMessage">
          <el-input v-model="form.onlineMessage" />
          <div>上线消息内容</div>
        </el-form-item>
        <el-form-item label="sendOfflineMessage" prop="sendOfflineMessage">
          <el-switch v-model="form.sendOfflineMessage" />
          <div>发送下线消息</div>
        </el-form-item>
        <el-form-item label="offlineMessage" prop="offlineMessage">
          <el-input v-model="form.offlineMessage" />
          <div>下线消息内容</div>
        </el-form-item>
        <el-form-item label="updateCheckTime" prop="updateCheckTime">
          <el-input-number v-model="form.updateCheckTime" :max="23" :min="0" />
          <div>每日检查更新时间</div>
        </el-form-item>
        <el-form-item label="endWithSensei" prop="endWithSensei">
          <el-input v-model="form.endWithSensei" />
          <div>昵称后缀</div>
        </el-form-item>
        <el-form-item label="sendStatus" prop="sendStatus">
          <el-switch v-model="form.sendStatus" />
          <div>允许arona向后端发送匿名统计数据</div>
        </el-form-item>
        <el-form-item label="uuid" prop="uuid">
          <el-input v-model="form.uuid" disabled />
          <div>uuid</div>
        </el-form-item>
        <el-form-item label="remoteCheckInterval" prop="remoteCheckInterval">
          <el-input-number v-model="form.remoteCheckInterval" :max="23" :min="1" />
          <div>公告检查间隔</div>
        </el-form-item>
      </el-form>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { ElMessage } from "element-plus";
import { fetchBotGroupList } from "@/api/modules/config";
import { AronaConfigForm, AronaConfigMap } from "@/interface/modules/config";
import { Friend, Group } from "@/types/contact";

const { t } = useI18n();
const form = reactive<AronaConfigForm>({
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
      nickname: "nick name",
      remark: "",
    },
  ],
});
const cache = reactive<AronaConfigMap>({} as any);
function groupFilter(query: string, item: Group) {
  return String(item.id).includes(query) || item.name.includes(query);
}
function friendFilter(query: string, item: Friend) {
  return String(item.id).includes(query) || (item.remark || item.nickname).includes(query);
}
onMounted(() => {
  fetchBotGroupList().then((groups) => {
    if (!groups || !groups.data || groups.data.length === 0) {
      ElMessage.warning("获取群列表失败");
    } else {
      select.groups = groups.data;
    }
  });
});
interface Select {
  groups: Omit<Group, "permission">[];
  friends: Friend[];
}
</script>

<style lang="scss" scoped></style>
