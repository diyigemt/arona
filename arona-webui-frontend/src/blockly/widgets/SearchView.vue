<template>
  <div class="ddl-border">
    <div>
      <el-input placeholder="搜索" :prefix-icon="Search" />
      <el-divider class="divider" />
      <el-scrollbar max-height="200px" style="margin-top: 4px">
        <div v-for="(item, index) in options" :key="index">
          <el-button
            type="primary"
            plain
            class="scrollbar-item"
            :disabled="item[1] === selected"
            @click="onClickItem(item[1])"
          >
            {{ item[0] }}
          </el-button>
        </div>
      </el-scrollbar>
    </div>
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
  width: 200px;
}
.divider {
  margin: 4px 0;
}
.scrollbar-item {
  width: 100%;
  height: 30px;
  margin-bottom: 4px;
  :deep(span) {
    width: 100%;
    text-align: left;
  }
  //border-radius: 4px;
  //background: var(--el-color-primary-light-9);
  //color: var(--el-color-primary);
}
</style>
