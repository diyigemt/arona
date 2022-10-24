// 需要鉴权的业务路由
import { RouteRecordRaw } from "vue-router";
import { MenuConfig } from "@/constant";
import ConfigMenu from "@/router/config";
import DatabaseMenu from "@/router/database";

const asyncRoutes: Array<RouteRecordRaw> = [
  {
    path: "/",
    redirect: "/home",
  },
  {
    path: "/home",
    name: "home",
    meta: {
      title: "",
      icon: "",
    },
    component: () => import("@/views/home/HomeIndex.vue"),
  },
  ...ConfigMenu,
  ...DatabaseMenu,
];

export default asyncRoutes;
