import { RouteRecordRaw } from "vue-router";

const ConfigRouter: Array<RouteRecordRaw> = [
  {
    path: "/config",
    meta: {
      title: "配置文件",
    },
    component: () => import("@/views/config/ConfigIndex.vue"),
    children: [
      {
        path: "",
        redirect: "config-arona",
      },
      {
        path: "config-arona",
        name: "config-arona",
        meta: {
          title: "主配置文件",
        },
        component: () => import("@/views/config/ConfigArona.vue"),
      },
      {
        path: "config-arona-gacha",
        name: "config-arona-gacha",
        meta: {
          title: "抽卡服务配置文件",
        },
        component: () => import("@/views/config/ConfigAronaGacha.vue"),
      },
    ],
  },
];
export default ConfigRouter;
