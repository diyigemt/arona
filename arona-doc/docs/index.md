---
layout: home

title: シッテムの箱
titleTemplate: Arona 配置手册

hero:
  name: Arona 配置手册
  text: ""
  tagline: '"是常驻这个「シッテムの箱」的系统管理员和主操作系统, 也是今后协助老师的秘书!"'
  image:
    src: /logo.png
    alt: VitePress
  actions:
    - theme: brand
      text: Get Started
      link: /terms/
    - theme: alt
      text: View on Github
      link: https://github.com/diyigemt/arona
---

<script setup lang="ts">
import { onMounted } from "vue";

onMounted(() => {
    const link = document.createElement("link");
    link.rel = "icon";
    link.type = "image/png";
    link.href = "/icon.png";
    document.head.appendChild(link);
});
</script>
