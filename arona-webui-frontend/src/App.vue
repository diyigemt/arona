<script setup lang="ts">
import zhCn from "element-plus/lib/locale/lang/zh-cn";
import MainBanner from "@/components/MainBanner.vue";
import useBaseStore from "@/store/base";
import { warningMessage } from "./utils/message";
const store = useBaseStore();
const router = useRouter();
if (!store.host) {
  warningMessage("api地址未配置,将跳转到配置界面");
  router.push("/setting/setting-api");
} else {
  store.syncContacts();
}
const locale = zhCn;
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
