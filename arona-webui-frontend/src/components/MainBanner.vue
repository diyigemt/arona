<template>
  <div class="banner-container">
    <div class="font-bold text-left antialiased font-sans banner-text dot-bg cursor-pointer" @click="goHome">Arona</div>
    <div>
      <el-form-item label="当前群:">
        <el-select v-model="select" @change="updateActiveGroup" placeholder="全部">
          <el-option v-for="(e, index) in groups" :key="index" :value="e.id" :label="e.name" />
        </el-select>
      </el-form-item>
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
  > div {
    flex: 1;
    &:last-child {
      text-align: right;
    }
  }
  .banner-text {
    $font-size: 36px;
    font-size: $font-size;
    line-height: $font-size + 12px;
    position: sticky;
    z-index: 999;
    top: 0;
    width: 100%;
    left: 0;
    padding: 0 20px;
  }
}
</style>
