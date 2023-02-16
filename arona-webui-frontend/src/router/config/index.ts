// 需要鉴权的业务路由
import { RouteRecordRaw } from "vue-router";
import ConfigMenu from "@/router/config/config";
import DatabaseMenu from "@/router/config/database";
import SettingRouter from "@/router/config/setting";

const ConfigRoutes: Array<RouteRecordRaw> = [
  {
    path: "/config",
    name: "config-index",
    redirect: "/config/home",
    meta: {
      title: "",
      icon: "",
    },
    component: () => import("@/views/config/ConfigIndex.vue"),
    children: [
      {
        path: "/config/home",
        name: "config-home",
        meta: {
          title: "",
          icon: "",
        },
        component: () => import("@/views/config/home/ConfigHome.vue"),
      },
      ...ConfigMenu,
      ...DatabaseMenu,
      ...SettingRouter,
    ],
  },
];

export default ConfigRoutes;
