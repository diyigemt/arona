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
    name: "arona.yml",
    path: "/config/config-arona",
  },
  {
    name: "arona-gacha.yml",
    path: "/config/config-arona-gacha",
  },
  {
    name: "arona-reply.yml",
    path: "/config/config-arona-reply",
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
