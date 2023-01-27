import { RouteRecordRaw } from "vue-router";

const DatabaseRouter: Array<RouteRecordRaw> = [
  {
    path: "/database",
    meta: {
      title: "数据库",
    },
    component: () => import("@/components/SubPageIndex.vue"),
    children: [
      {
        path: "",
        redirect: "database-gacha-pool",
      },
      {
        path: "database-gacha-pool",
        name: "database-gacha-pool",
        meta: {
          title: "卡池信息数据库",
        },
        component: () => import("@/views/database/DatabaseGachaPool.vue"),
      },
      {
        path: "database-gacha-history",
        name: "database-gacha-history",
        meta: {
          title: "抽卡记录数据库",
        },
        component: () => import("@/views/database/DatabaseGachaHistory.vue"),
      },
    ],
  },
];
export default DatabaseRouter;
