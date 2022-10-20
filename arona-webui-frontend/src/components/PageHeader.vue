<template>
  <div class="dot-bg">
    <el-page-header :content="props.title" class="page-header" @back="goBack">
      <template v-if="!props.title" #title>
        <slot name="title" />
      </template>
    </el-page-header>
    <el-divider v-if="props.title" />
  </div>
</template>

<script setup lang="ts">
const props = defineProps<Props>();
const router = useRouter();
const goBack = () => {
  router.go(-1);
};
interface Props {
  title?: string;
}
</script>

<style lang="scss">
.page-header {
  margin-bottom: 20px;
}
</style>
