<template>
  <div class="ddl-border">
    <div>
      <el-input
        v-if="searchInput"
        v-model="inputText"
        placeholder="搜索"
        :prefix-icon="Search"
        clearable
        @input="onInput"
      />
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
            {{ peakyBlinder(item[0]) }}
          </el-button>
        </div>
        <el-empty v-if="options && options.length === 0" description="未找到结果" :image-size="75" />
      </el-scrollbar>
    </div>
  </div>
</template>

<script setup lang="ts">
import Blockly, { MenuOption } from "blockly";
import { Search } from "@element-plus/icons-vue";
import DropDownView from "@/blockly/widgets/DropDownView";

const props = defineProps<{
  blockly: DropDownView;
  searchInput: boolean;
}>();
const options = ref<MenuOption[]>(props.blockly.getOptions());
const selected = ref<string>(props.blockly.getValue());
const inputText = ref<string>("");

onMounted(() => {
  Blockly.DropDownDiv.showPositionedByField(props.blockly);
});

function onClickItem(id: string) {
  props.blockly.setValue(id);
  props.blockly.close();
}

function onInput() {
  options.value = props.blockly.getOptions().filter((value) => {
    return (value[0] as string).includes(inputText.value);
  });
}

function peakyBlinder(value: string) {
  if (value.length >= 20) {
    return `${value.slice(0, 20)}...`;
  }
  return value;
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
}
</style>
