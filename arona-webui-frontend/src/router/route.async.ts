// 需要鉴权的业务路由
import { RouteRecordRaw } from "vue-router";
import { MenuConfig } from "@/constant";

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
  ...MenuConfig,
];

export default asyncRoutes;
