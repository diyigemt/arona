<template>
  <div class="dot-bg">
    <el-page-header :content="routerTitle" class="page-header" @back="goBack">
      <template v-if="!routerTitle" #title>
        <slot name="title" />
      </template>
    </el-page-header>
    <el-divider v-if="props.title" />
  </div>
</template>

<script setup lang="ts">
const route = useRoute();
const router = useRouter();
const props = defineProps<Props>();
const goBack = () => {
  router.go(-1);
};
const routerTitle = computed(() => route.meta.title || props.title || "");
interface Props {
  title?: string;
}
</script>

<style lang="scss">
.page-header {
  margin-bottom: 20px;
}
</style>
