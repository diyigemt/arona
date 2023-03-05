import {DefaultTheme} from "vitepress";

export const SidebarItem: DefaultTheme.SidebarItem[] = [{
  text: "简介",
  collapsed: false,
  items: [{
    text: '她能干嘛',
    link: '/guide/what-is',
  }],
}];

export const NavItem: DefaultTheme.NavItem[] = [{
  text: '首页',
  link: '/',
}, {
  text: "简介",
  link: "/guide/what-is",
}, {
  text: "1.0.14",
  items: [{
    text: "Changelog",
    link: "https://github.com/diyigemt/arona#%E6%9B%B4%E6%96%B0%E6%97%A5%E5%BF%97",
  }],
}];
