<template>
  <div class="banner-container cursor-pointer" @click="goHome">
    <div class="font-bold text-left antialiased font-sans banner-text dot-bg">Arona</div>
    <div>
      <el-select v-model="select" placeholder="全部" @change="updateActiveGroup">
        <el-option v-for="(e, index) in groups" :key="index" :value="e.id" :label="e.name" />
      </el-select>
    </div>
  </div>
</template>

<script setup lang="ts">
import useBaseStore from "@/store/base";

const router = useRouter();
const baseStore = useBaseStore();
const select = ref<number>();
function goHome() {
  router.push("/home");
}
function updateActiveGroup(group: number) {
  baseStore.setActiveGroupId(group);
}
const groups = computed(() => baseStore.groups());
</script>

<style lang="scss" scoped>
.banner-container {
  display: flex;
  justify-content: space-between;
  position: sticky;
  z-index: 999;
  $font-size: 36px;
  line-height: $font-size + 12px;
  top: 0;
  width: 100%;
  left: 0;
  padding: 0 20px;
  > div {
    flex: 1;
    &:last-child {
      text-align: right;
    }
  }
  .banner-text {
    font-size: $font-size;
  }
}
</style>
