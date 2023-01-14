<template>
  <el-menu :default-active="currentActiveMenu" :default-openeds="mapExpand" router class="main-menu custom-menu">
    <el-sub-menu v-for="(menu, index) in MenuConfig" :key="index" :index="String(index)">
      <template #title>
        <span>{{ t(menu.name) }}</span>
      </template>
      <el-menu-item
        v-for="(item, jIndex) in menu.children"
        v-show="item.path"
        :key="index + '-' + jIndex + '-sub'"
        :index="item.path"
        >{{ item.name }}</el-menu-item
      >
    </el-sub-menu>
  </el-menu>
</template>

<script setup lang="ts">
import { MenuConfig } from "@/constant";

const { t } = useI18n();
const route = useRoute();
const mapExpand = MenuConfig.map((_, index) => String(index));
const currentActiveMenu = computed(() => route.path);
</script>

<style lang="scss" scoped>
.main-menu {
  height: 100%;
  border-right: none;
  :deep(.el-sub-menu) {
    .el-menu {
      .el-menu-item {
        border-radius: 4px;
      }
    }
  }
}
</style>

<i18n>
{
  "en": {
    "side menu config": "config",
    "side menu database": "database",
    "side menu setting": "setting"
  },
  "zh-cn": {
    "side menu config": "配置文件",
    "side menu database": "数据库文件",
    "side menu setting": "设置"
  }
}
</i18n>
