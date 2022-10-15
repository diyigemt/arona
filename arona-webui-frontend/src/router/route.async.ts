// 需要鉴权的业务路由
import { RouteRecordRaw } from "vue-router";
import ConfigRouter from "@/router/config";
import DatabaseRouter from "@/router/database";

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
  ...ConfigRouter,
  ...DatabaseRouter,
];

export default asyncRoutes;
