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
        path: "config-arona-emergency",
        name: "config-arona-emergency",
        meta: {
          title: "紧急停止服务配置文件",
        },
        component: () => import("@/views/config/ConfigAronaEmergency.vue"),
      },
    ],
  },
];
export default ConfigRouter;
