<script setup lang="ts">
import zhCn from "element-plus/lib/locale/lang/zh-cn";
import MainBanner from "@/components/MainBanner.vue";

const locale = zhCn;
</script>

<template>
  <el-config-provider :locale="locale">
    <el-container class="container">
      <el-header>
        <MainBanner />
      </el-header>
      <el-container>
        <el-aside width="200px">
          <MainAsideMenu />
        </el-aside>
        <el-main>
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </el-config-provider>
</template>

<style lang="scss" scoped>
.container {
  height: 100%;
}
</style>
