<template>
  <el-menu default-active="1" :default-openeds="mapExpand" class="main-menu">
    <el-sub-menu v-for="(menu, index) in MenuConfig" :key="index" :index="String(index)">
      <template #title>
        <span>{{ t(menu.menuName) }}</span>
      </template>
      <el-menu-item
        v-for="(item, jIndex) in menu.children"
        v-show="item.path"
        :key="'1-' + jIndex + 'sub'"
        :index="index + '-' + jIndex"
        @click="jump(item.menuPath)"
        >{{ item.menuName }}</el-menu-item
      >
    </el-sub-menu>
  </el-menu>
</template>

<script setup lang="ts">
import { MenuConfig } from "@/constant";

const { t } = useI18n();
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

<i18n>
{
  "en": {
    "side menu config": "config",
    "side menu database": "database"
  },
  "zh-cn": {
    "side menu config": "配置文件",
    "side menu database": "数据库文件"
  }
}
</i18n>
