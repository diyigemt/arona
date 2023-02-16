<template>
  <MainBanner />
  <el-container class="main-container">
    <el-aside width="250px">
      <MainAsideMenu />
    </el-aside>
    <el-main class="container">
      <el-scrollbar class="scrollbar-hide-horizontal">
        <div style="margin-top: 50px; padding-top: 20px">
          <router-view />
        </div>
      </el-scrollbar>
    </el-main>
  </el-container>
</template>

<script setup lang="ts">
import MainBanner from "@/components/MainBanner.vue";
import useSettingStore from "@/store/setting";
import { warningMessage } from "@/utils/message";
import { heartbeat } from "@/api";
import { initEventBus } from "@/utils/config/emitter";

const settingStore = useSettingStore();
const router = useRouter();
initEventBus();
if (!settingStore.isRestoreBackend) {
  warningAndRedirect("未配置后端地址,将会跳转到配置界面");
} else {
  heartbeat().then((res) => {
    if (!res) {
      warningAndRedirect("后端连接失败,将会跳转到配置界面");
    }
  });
}
function warningAndRedirect(msg: string) {
  warningMessage(msg);
  router.push("/config/setting/setting-api");
}
</script>

<style lang="scss" scoped>
.main-container {
  height: calc(100vh - 60px);
}
.container {
  height: 100vh;
  max-width: 100%;
  padding-right: 0;
  padding-top: 0;
  transform: translateY(-60px);
  overflow: hidden;
}
</style>
