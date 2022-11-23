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
      {
        path: "config-arona-reply",
        name: "config-arona-reply",
        meta: {
          title: "关键词回复配置文件",
        },
        component: () => import("@/views/config/ConfigAronaReply.vue"),
      },
      {
        path: "config-arona-blockly",
        name: "config-arona-blockly",
        meta: {
          title: "条件执行配置文件",
        },
        component: () => import("@/views/config/ConfigAronaBlockly.vue"),
      },
    ],
  },
];
export default ConfigRouter;
