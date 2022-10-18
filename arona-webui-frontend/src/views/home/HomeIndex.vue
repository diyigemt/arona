<template>
  <el-form :model="formData">
    <el-form-item prop="api" label="api:">
      <el-input v-model="formData.api" />
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
interface FormData {
  api: string;
  params: any;
  response: any;
}
const formData = reactive<FormData>({
  api: "",
  params: {},
  response: {},
});
</script>

<style lang="scss" scoped></style>
