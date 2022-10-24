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
    name: "arona-emergency.yml",
    path: "/config/config-arona-emergency",
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
// eslint-disable-next-line import/prefer-default-export
export const MenuConfig: MenuItem[] = [
  {
    name: "side menu config",
    children: ConfigMenu,
  },
  {
    name: "side menu config",
    children: DatabaseMenu,
  },
];
