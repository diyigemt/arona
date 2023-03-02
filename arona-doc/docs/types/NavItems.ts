import { DefaultTheme } from 'vitepress';

export type NavItemWithMeta = DefaultTheme.SidebarItem &
  DefaultTheme.NavItem & {
    meta: {
      [key: string]: boolean;
    };
  };
