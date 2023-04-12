import {DefaultTheme} from 'vitepress';

const MainSideBar: DefaultTheme.Sidebar = [{
  text: '开始',
  items: [{
    text: '简介',
    link: '/guide/what-is',
  }, {
    text: '声明',
    link: '/guide/announcement',
  }, {
    text: '名词',
    link: '/guide/glossary',
  }],
}, {
  text: '安装',
  collapsed: false,
  items: [{
    text: '安装mirai-console',
    link: '/install/mirai-console',
  }, {
    text: '登录bot',
    link: '/install/login-bot',
  }, {
    text: '安装chat-command',
    link: '/install/chat-command',
  }, {
    text: '安装arona',
    link: '/install/arona',
  }],
}, {
  text: '配置',
  collapsed: false,
  items: [{
    text: '基础配置',
    link: '/config/base-config'
  }, {
    text: '数据库',
    link: '/config/database'
  }]
}, {
  text: '功能',
  collapsed: false,
  items: [{
    text: '主动触发',
    link: '/command/manual'
  }, {
    text: '非主动触发',
    link: '/command/auto'
  }, {
    text: '远端服务',
    link: '/command/remote'
  }]
}, {
  text: '简版用户手册',
  collapsed: false,
  items: [{
    text: '',
    link: '/manual/'
  }]
}, {
  text: '其他',
  collapsed: false,
  items: [{
    text: '帮助',
    link: '/other/help'
  }, {
    text: '鸣谢',
    link: '/other/thanks'
  }]
}];

export const SidebarItem: DefaultTheme.Sidebar = {
  '/guide/': MainSideBar,
  '/install/': MainSideBar,
  '/config/': MainSideBar,
  '/command/': MainSideBar,
  '/other/': MainSideBar,
};

export const NavItem: DefaultTheme.NavItem[] = [{
  text: '首页',
  link: '/',
}, {
  text: '简介',
  link: '/guide/what-is',
}, {
  text: '1.1.0',
  items: [{
    text: 'Changelog',
    link: '/other/changelog',
  }],
}];
