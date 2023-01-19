<template>
  <div class="ddl-border">
    <el-space direction="vertical" fill>
      <el-input placeholder="搜索" :prefix-icon="Search" />
      <div class="ddl-divider" />
      <el-scrollbar max-height="200px">
        <el-button
          v-for="(item, index) in options"
          :key="index"
          type="primary"
          plain
          class="scrollbar-item"
          :disabled="item[1] === selected"
          @click="onClickItem(item[1])"
        >
          {{ item[0] }}
        </el-button>
      </el-scrollbar>
    </el-space>
  </div>
</template>

<script setup lang="ts">
import Blockly from "blockly";
import { Search } from "@element-plus/icons-vue";
import FieldSearchView from "@/blockly/widgets/FieldSearchView";

const props = defineProps<{
  blockly: FieldSearchView;
}>();
const options = computed(() => props.blockly.getOptions());
const selected = ref<string>(props.blockly.getValue());

onMounted(() => {
  Blockly.DropDownDiv.showPositionedByField(props.blockly);
});

function onClickItem(id: number) {
  props.blockly.setValue(id);
  props.blockly.close();
}
</script>

<style scoped lang="scss">
.ddl-border {
  margin: 4px;
  overflow: hidden;
}
.ddl-divider {
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
  text-align: start;
  //border-radius: 4px;
  //background: var(--el-color-primary-light-9);
  //color: var(--el-color-primary);
}
</style>
