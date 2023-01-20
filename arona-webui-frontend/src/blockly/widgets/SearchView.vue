<template>
  <div class="ddl-border">
    <div>
      <el-input v-if="searchInput" placeholder="搜索" :prefix-icon="Search" />
      <el-divider v-if="searchInput" class="divider" />
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
import DropDownView from "@/blockly/widgets/DropDownView";

const props = defineProps<{
  blockly: DropDownView;
  searchInput: boolean;
}>();
const options = computed(() => props.blockly.getOptions());
const selected = ref<string>(props.blockly.getValue());

onMounted(() => {
  Blockly.DropDownDiv.showPositionedByField(props.blockly);
});

function onClickItem(id: string) {
  props.blockly.setValue(id);
  props.blockly.close();
}
</script>

<style scoped lang="scss">
.ddl-border {
  margin: 4px;
  overflow: hidden;
  width: auto;
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
