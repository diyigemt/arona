<template>
  <VideoBackground :src="src" poster="/image/arona_home_poster.jpg" style="height: 100vh">
    <div class="menu">
      <div class="filter">
        <el-space class="menu-content" wrap>
          <ShadowCard class="button-card kivotos" role="button" tabindex="0" @click="routerJump('/')">
            <div>基沃托斯</div>
          </ShadowCard>
          <ShadowCard class="button-card arona" role="button" tabindex="1" @click="routerJump('/config')">
            <div>设置</div>
          </ShadowCard>
          <ShadowCard class="button-card" style="opacity: 0; user-select: none; pointer-events: none">
            <div>管理</div>
          </ShadowCard>
          <ShadowCard class="button-card setting" role="button" tabindex="2" @click="routerJump('/admin')">
            <div>管理</div>
          </ShadowCard>
        </el-space>
      </div>
    </div>
  </VideoBackground>
</template>

<script setup lang="ts">
// @ts-ignore
import VideoBackground from "vue-responsive-video-background-player";

const srcList = ["desk", "sleep", "look"].map((it) => `/video/arona_${it}.mp4`);
const srcIndex = Math.floor(Math.random() * srcList.length);
const src = srcList[srcIndex];
const router = useRouter();
function routerJump(path: string) {
  router.push(path);
}
</script>

<style lang="scss" scoped>
* {
  font-family: "GameFont", serif;
  font-size: 1.2rem;
}
$button-card-size: 200px;
.menu {
  position: absolute;
  top: 20%;
  right: 10%;
  transform: skewX(-10deg);
  .filter {
    position: relative;
    overflow: hidden;
    .menu-content {
      max-width: calc(2 * #{$button-card-size} + 2 * 8px);
      position: relative;
      z-index: 1;
    }
  }
}
.button-card {
  cursor: pointer;
  width: $button-card-size;
  height: $button-card-size;
  justify-content: center;
  border-radius: 6px;
  background: url("/image/poli-light.png") rgba(164, 216, 237, 0.3) no-repeat 0 40%;
}
.kivotos {
  background: linear-gradient(
      340deg,
      rgba(240, 240, 240, 0.1) 0%,
      rgba(240, 240, 240, 0.1) 30%,
      rgba(240, 240, 240, 1) 70%
    ),
    url("/image/poli-light.png"), url("/image/BG_View_Kivotos.jpg");
  background-repeat: no-repeat;
  background-position: 100% 100%, 20% 20%, 35% 75%;
  background-size: auto, auto, 300% 300%;
}
.setting {
  background: linear-gradient(340deg, rgba(240, 240, 240, 0.1) 0%, rgba(240, 240, 240, 0.1) 30%, rgb(240, 240, 240) 70%), url("/image/poli-light.png"), url("/image/BG_CS_PR_05.jpg");
  background-repeat: no-repeat;
  background-position: 100% 100%, 20% 20%, 50%;
  background-size: auto, auto, 200% 150%;
}
.arona {
  background: linear-gradient(340deg, rgba(240, 240, 240, 0.1) 0%, rgba(240, 240, 240, 0.1) 30%, rgb(240, 240, 240) 70%), url("/image/poli-light.png"), url("/image/BG_CS_PR_03.jpg");
  background-repeat: no-repeat;
  background-position: 100% 100%, 20% 20%, 50%;
  background-size: auto, auto, 200% 200%;
}
</style>
