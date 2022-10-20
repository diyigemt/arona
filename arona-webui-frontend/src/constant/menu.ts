import { RouteRecordRaw } from "vue-router";

interface _SubMenuItem {
  menuName: string;
  menuPath: string;
}

type SubMenuItem = _SubMenuItem & RouteRecordRaw;

interface _MenuItem {
  menuName: string;
  children: SubMenuItem[];
}

type MenuItem = _MenuItem & RouteRecordRaw;

const ConfigMenu: SubMenuItem[] = [
  {
    name: "config",
    path: "",
    menuName: "config-home",
    menuPath: "config-home",
    component: () => import("@/views/config/ConfigIndex.vue"),
  },
  {
    name: "config-arona",
    path: "config-arona",
    menuName: "arona.yml",
    menuPath: "/config/config-arona",
    component: () => import("@/views/config/ConfigArona.vue"),
  },
  {
    name: "config-arona-emergency",
    path: "config-arona-emergency",
    menuName: "arona-emergency.yml",
    menuPath: "/config/config-arona-emergency",
    meta: {
      title: "紧急停止服务配置文件",
    },
    component: () => import("@/views/config/ConfigAronaEmergency.vue"),
  },
];
const DatabaseMenu: SubMenuItem[] = [
  {
    name: "database",
    path: "",
    menuName: "database-home",
    menuPath: "database-home",
    component: () => import("@/views/database/DatabaseIndex.vue"),
  },
  {
    name: "database-gacha-pool",
    path: "database-gacha-pool",
    menuName: "GachaPool",
    menuPath: "/database/database-gacha-pool",
    meta: {
      title: "卡池信息数据库",
    },
    component: () => import("@/views/database/DatabaseGachaPool.vue"),
  },
  {
    name: "database-gacha-history",
    path: "database-gacha-history",
    menuName: "GachaHistory",
    menuPath: "/database/database-gacha-history",
    meta: {
      title: "抽卡记录数据库",
    },
    component: () => import("@/views/database/DatabaseGachaHistory.vue"),
  },
];
// eslint-disable-next-line import/prefer-default-export
export const MenuConfig: MenuItem[] = [
  {
    path: "/config",
    meta: {
      title: "配置文件",
    },
    menuName: "side menu config",
    children: ConfigMenu,
  },
  {
    path: "/database",
    meta: {
      title: "数据库",
    },
    menuName: "side menu database",
    children: DatabaseMenu,
  },
];
