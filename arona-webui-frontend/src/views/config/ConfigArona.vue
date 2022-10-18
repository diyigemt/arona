<template>
  <div>{{ t("config.arona.desc") }}</div>
  <div style="max-width: 500px">
    <el-form :model="data.form">
      <el-form-item label="qq">
        <el-input v-model.number="data.form.qq" />
      </el-form-item>
      <el-form-item label="groups">
        <el-select v-model="data.form.managerGroup">
          <el-option v-for="(e, index) in data.select.groups" :key="index" :label="e" :value="e"></el-option>
        </el-select>
      </el-form-item>
    </el-form>
  </div>
</template>

<script setup lang="ts">
const { t } = useI18n();
const data = reactive<Data>({
  form: {
    qq: "",
    groups: [],
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
  },
  select: {
    groups: [],
  },
});
interface Data {
  form: {
    qq: string;
    groups: number[];
    managerGroup: number[];
    permissionDeniedMessage: string;
    sendOnlineMessage: boolean;
    onlineMessage: string;
    sendOfflineMessage: boolean;
    offlineMessage: string;
    updateCheckTime: number;
    endWithSensei: string;
    sendStatus: false;
    uuid: string;
    remoteCheckInterval: number;
  };
  select: {
    groups: number[];
  };
}
</script>

<style lang="scss" scoped>

</style>
