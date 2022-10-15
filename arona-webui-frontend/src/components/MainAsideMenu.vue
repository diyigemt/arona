<template>
  <el-menu default-active="1" :default-openeds="mapExpand" class="main-menu">
    <el-sub-menu v-for="(menu, index) in MenuConfig" :key="index" :index="String(index)">
      <template #title>
        <span>{{ menu.name }}</span>
      </template>
      <el-menu-item
        v-for="(item, jIndex) in menu.children"
        :key="'1-' + jIndex + 'sub'"
        :index="index + '-' + jIndex"
        @click="jump(item.path)"
        >{{ item.name }}</el-menu-item
      >
    </el-sub-menu>
  </el-menu>
</template>

<script setup lang="ts">
import { MenuConfig } from "@/constant";

const router = useRouter();
const mapExpand = MenuConfig.map((_, index) => String(index));
function jump(path: string) {
  router.push(path);
}
</script>

<style lang="scss" scoped>
.main-menu {
  height: 100%;
}
</style>
