<script setup lang="ts">
import zhCn from "element-plus/lib/locale/lang/zh-cn";
import MainBanner from "@/components/MainBanner.vue";
import useSettingStore from "./store/setting";
import { warningMessage } from "./utils/message";
import { heartbeat } from "./api";

const locale = zhCn;
const settingStore = useSettingStore();
const router = useRouter();
// TODO
// if (!settingStore.isRestoreBackend) {
//   warningMessage("未配置后端地址,将会跳转到配置界面");
//   router.push("/setting/setting-api");
// } else {
//   heartbeat().then((res) => {
//     if (!res) {
//       warningMessage("后端连接失败,将会跳转到配置界面");
//       router.push("/setting/setting-api");
//     }
//   });
// }
</script>

<template>
  <el-config-provider :locale="locale">
    <MainBanner />
    <el-container class="main-container">
      <el-aside width="250px">
        <MainAsideMenu />
      </el-aside>
      <el-main class="container">
        <el-scrollbar class="scrollbar-hide-horizontal">
          <div style="margin-top: 60px">
            <router-view />
          </div>
        </el-scrollbar>
      </el-main>
    </el-container>
  </el-config-provider>
</template>

<style lang="scss" scoped>
.main-container {
  height: calc(100vh - 60px);
}
.container {
  height: 100vh;
  transform: translateY(-60px);
  overflow: hidden;
}
</style>
