<template>
  <div id="ddlBorder">
    <el-space direction="vertical" fill>
      <el-autocomplete></el-autocomplete>
      <div id="ddlDivider" />
      <el-scrollbar max-height="200px">
        <el-button v-for="item in 20" :key="item" type="primary" plain class="scrollbar-item">
          {{ item }}
        </el-button>
      </el-scrollbar>
    </el-space>
  </div>
</template>

<script setup lang="ts">
import FieldSearchView from "@/blockly/widgets/FieldSearchView";

const props = defineProps<{
  blockly: FieldSearchView;
}>();
onMounted(() => {
  console.log(props.blockly.container);
});
</script>

<style scoped lang="scss">
#ddlBorder {
  margin: 4px;
  overflow: hidden;
}
#ddlDivider {
  margin-top: 2px;
  border-top: 1px solid var(--el-border-color);
}
.scrollbar-item {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 200px;
  height: 30px;
  margin: 4px;
  text-align: center;
  //border-radius: 4px;
  //background: var(--el-color-primary-light-9);
  //color: var(--el-color-primary);
}
</style>
