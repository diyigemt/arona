import { defineConfig } from 'vitepress';
import { NavItem, SidebarItem } from '../config/menu';
import { withPwa } from '@vite-pwa/vitepress';

export default withPwa(
  defineConfig({
    lang: 'en-US',
    title: "arona配置手册",
    description: "mirai-arona的配置手册",

    lastUpdated: true,
    cleanUrls: true,

    head: [['meta', { name: 'theme-color', content: '#3f88f2' }]],

    markdown: {
      headers: {
        level: [0, 0],
      },
    },

    themeConfig: {
      nav: NavItem,
      sidebar: SidebarItem,
    },

    pwa: {
      selfDestroying: true,
      // workbox: {
      //   runtimeCaching: [
      //     {
      //       urlPattern: /(.*?)\.(js|css|json|md)$/i,
      //       handler: 'StaleWhileRevalidate',
      //       options: {
      //         cacheName: 'asset-cache',
      //       },
      //     },
      //   ],
      // },
    },
  })
);
