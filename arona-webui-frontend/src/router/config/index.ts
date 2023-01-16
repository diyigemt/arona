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
        redirect: "config-arona-blockly",
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
