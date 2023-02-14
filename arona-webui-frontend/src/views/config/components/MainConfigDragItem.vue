<template>
  <div class="main-container">
    <div>
      <el-select v-model="select" placeholder="全部" clearable @change="changeContact">
        <el-option v-for="(e, index) in options" :key="index" :label="e.name || e.remark" :value="e.id" />
      </el-select>
    </div>
  </div>
</template>

<script setup lang="ts">
import mainEmitter from "@/views/config/event/main";
import { Friend, Group } from "@/types/contact";

const props = withDefaults(defineProps<IProps>(), {
  uuid: "",
  type: true,
  bots: () => [],
  groups: () => [],
});
const select = ref<number>();
function changeContact(contact: number) {
  mainEmitter.emit("contact-update", {
    uuid: props.uuid,
    value: contact || 0,
  });
}
const options = computed(() => (props.type ? props.bots : props.groups));
interface IProps {
  uuid: string;
  type: boolean; // 是群选择还是bot选择 true: bot选择
  bots: Friend[];
  groups: Group[];
}
</script>

<style lang="scss" scoped>
.main-container {
  background-color: white;
  border: 1px solid #346789;
  text-align: center;
  cursor: pointer;
  box-shadow: 2px 2px 19px #aaa;
  border-radius: 6px;
  color: black;
  width: 260px;
  height: 150px;
  line-height: 1;
  user-select: none;
  display: flex;
  > div {
    margin: auto;
  }
}
</style>
