<template>
  <div class="banner-container">
    <div class="font-bold text-left antialiased font-sans banner-text dot-bg cursor-pointer" @click="goHome">
      Arona
    </div>
    <div>
      测试
    </div>
  </div>
</template>

<script setup lang="ts">
const router = useRouter();
function goHome() {
  router.push("/home");
}
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
