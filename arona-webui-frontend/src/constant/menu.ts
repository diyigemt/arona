interface SubMenuItem {
  name: string;
  path: string;
}
interface MenuItem {
  name: string;
  children: SubMenuItem[];
}
const ConfigMenu: SubMenuItem[] = [
  {
    name: "主配置",
    path: "/config/config-arona-main",
  },
  {
    name: "条件执行",
    path: "/config/config-arona-blockly",
  },
  {
    name: "随机回复语句",
    path: "/config/config-arona-reply-group",
  },
  {
    name: "随机回复语句标签",
    path: "/config/config-arona-reply-label",
  },
];
const DatabaseMenu: SubMenuItem[] = [
  {
    name: "GachaPool",
    path: "/database/database-gacha-pool",
  },
  {
    name: "GachaHistory",
    path: "/database/database-gacha-history",
  },
];
const SettingMenu: SubMenuItem[] = [
  {
    name: "api",
    path: "/setting/setting-api",
  },
];
// eslint-disable-next-line import/prefer-default-export
export const MenuConfig: MenuItem[] = [
  {
    name: "side menu config",
    children: ConfigMenu,
  },
  {
    name: "side menu database",
    children: DatabaseMenu,
  },
  {
    name: "side menu setting",
    children: SettingMenu,
  },
];
