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
      <el-scrollbar v-if="isMultiple" max-height="75px" class="tag-container">
        <div v-if="tags && tags.length === 0" class="tag-info">点击下方成员以添加</div>
        <el-tag
          v-for="(item, index) in tags"
          :key="index"
          closable
          size="small"
          style="margin-left: 4px"
          :disable-transitions="true"
          @close="removeTag(item)"
        >
          {{ item }}
        </el-tag>
      </el-scrollbar>
      <el-divider v-if="isMultiple" class="divider" />
      <el-scrollbar max-height="200px" style="margin-top: 4px">
        <div v-for="(item, index) in options" :key="index">
          <el-button
            type="primary"
            plain
            class="scrollbar-item"
            :disabled="item[1] === selected && !props.isMultiple"
            @click="onClickItem(item)"
          >
            {{ peakyBlinder(item[0]) }}
          </el-button>
        </div>
        <el-empty v-if="options && options.length === 0" description="未找到结果" :image-size="50" />
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
  isMultiple: boolean;
}>();
const tags = ref<string[]>(props.blockly.isMultiple ? props.blockly.splitTags() : []);
const options = ref<MenuOption[]>(optionFilter());
const selected = ref<string>(props.blockly.getValue());
const inputText = ref<string>("");

onMounted(() => {
  Blockly.DropDownDiv.showPositionedByField(props.blockly);
});

function onClickItem(option: MenuOption) {
  if (props.isMultiple) {
    if (tags.value.find((value) => value === option[0]) === undefined) {
      tags.value.unshift(option[0] as string);
      props.blockly.updateOptions(JSON.stringify(tags.value), 1);
      props.blockly.setValue(props.blockly.getOptions(true)[0][1]);
      options.value = optionFilter();
    }
  } else {
    props.blockly.setValue(option[1]);
    props.blockly.close();
  }
}

function onInput() {
  options.value = optionFilter().filter((value) => {
    return (value[0] as string).includes(inputText.value);
  });
}

function peakyBlinder(value: string) {
  if (value.length >= 20) {
    return `${value.slice(0, 20)}...`;
  }
  return value;
}

function removeTag(tag: string) {
  tags.value.splice(tags.value.indexOf(tag), 1);
  props.blockly.updateOptions(JSON.stringify(tags.value), 1);
  props.blockly.setValue(props.blockly.getOptions(true)[0][1]);
  options.value = optionFilter();
}

function optionFilter() {
  return props.blockly.getOptions().filter((value) => !tags.value.includes(value[0] as string));
}
</script>

<style scoped lang="scss">
.ddl-border {
  margin: 4px;
  overflow: hidden;
  width: min-content;
  max-height: 290px;
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
.tag-container {
  border: 1px solid var(--el-border-color);
  width: 100%;
  height: auto;
  border-radius: 4px;
  padding: 6px;
}
.tag-info {
  text-align: center;
  font-size: 15px;
  color: var(--el-text-color-disabled);
  min-width: 150px;
}
</style>
