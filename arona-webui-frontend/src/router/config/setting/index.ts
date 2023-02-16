import { RouteRecordRaw } from "vue-router";

const SettingRouter: Array<RouteRecordRaw> = [
  {
    path: "setting",
    meta: {
      title: "设置",
    },
    component: () => import("@/components/SubPageIndex.vue"),
    children: [
      {
        path: "",
        redirect: "setting-api",
      },
      {
        path: "setting-api",
        name: "setting-api",
        meta: {
          title: "api设置",
        },
        component: () => import("@/views/config/setting/SettingAPI.vue"),
      },
    ],
  },
];
export default SettingRouter;
