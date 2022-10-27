// 需要鉴权的业务路由
import { RouteRecordRaw } from "vue-router";
import ConfigMenu from "@/router/config";
import DatabaseMenu from "@/router/database";
import SettingRouter from "./setting";

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
  ...SettingRouter,
];

export default asyncRoutes;
