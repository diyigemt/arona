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
  items: [{
    text: '',
    link: '/config/'
  }]
}, {
  text: '指令',
  items: [{
    text: '',
    link: '/command/'
  }]
}];

export const SidebarItem: DefaultTheme.Sidebar = {
  '/guide/': MainSideBar,
  '/install/': MainSideBar,
  '/config/': MainSideBar,
  '/command/': MainSideBar,
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
