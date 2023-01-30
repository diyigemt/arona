import { RouteRecordRaw } from "vue-router";

const ConfigRouter: Array<RouteRecordRaw> = [
  {
    path: "/config",
    meta: {
      title: "配置文件",
    },
    component: () => import("@/components/SubPageIndex.vue"),
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
      {
        path: "config-arona-main",
        name: "config-arona-main",
        meta: {
          title: "主配置文件",
        },
        component: () => import("@/views/config/ConfigAronaMain.vue"),
      },
      {
        path: "config-arona-reply-group",
        name: "config-arona-reply-group",
        meta: {
          title: "随机回复语句",
        },
        component: () => import("@/views/config/ConfigRandomReply.vue"),
      },
      {
        path: "config-arona-reply-label",
        name: "config-arona-reply-label",
        meta: {
          title: "随机回复标签",
        },
        component: () => import("@/views/config/ConfigRandomReplyLabel.vue"),
      },
    ],
  },
];
export default ConfigRouter;
