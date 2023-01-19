<template>
  <div id="ddlBorder">
    <el-space direction="vertical" fill>
      <el-input placeholder="搜索" :prefix-icon="Search" />
      <div id="ddlDivider" />
      <el-scrollbar id="ddlScrollBar" max-height="200px">
        <el-button
          v-for="item in options"
          :id="generateId(item)"
          :key="item"
          ref="btn"
          type="primary"
          plain
          class="scrollbar-item"
          :disabled="item[1] === selected"
          @click="onClickListener($event)"
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
const btn = ref();
const options = ref(props.blockly.getOptions());
const selected = ref(props.blockly.getValue());

onMounted(() => {
  Blockly.DropDownDiv.showPositionedByField(props.blockly);
});

function generateId(index: number) {
  return `ddlBtn${index}`;
}

function onClickListener(event: Event) {
  const target = (event.target as HTMLSpanElement).innerText;
  const res = props.blockly.getOptions().find((value) => {
    return value[0] === target;
  })!;
  props.blockly.setValue(res[1]);
  props.blockly.close();
}
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
  text-align: start;
  //border-radius: 4px;
  //background: var(--el-color-primary-light-9);
  //color: var(--el-color-primary);
}
</style>
