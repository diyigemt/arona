<template>
  <div class="main-container">
    <div>
      {{ props.content }}
    </div>
  </div>
</template>

<script setup lang="ts">
const props = withDefaults(defineProps<{ content: string }>(), {
  content: "",
});
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
  width: 140px;
  height: 60px;
  line-height: 1;
  user-select: none;
  display: flex;
  > div {
    margin: auto;
  }
}
</style>
