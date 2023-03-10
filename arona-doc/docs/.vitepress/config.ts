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
      socialLinks: [
        { icon: 'github', link: 'https://github.com/diyigemt/arona' },
        {
          icon: {
            svg: '<svg role="img" viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg"><title>第一个mt的窝</title><path d="M512 22C241.38 22 22 241.38 22 512s219.38 490 490 490 490-219.38 490-490S782.62 22 512 22z m226.06 277.73L517.54 809.14c-3.18 7.28-13.72 7.08-17.21-0.41L410.4 616.57a5.966 5.966 0 0 0-2.97-2.97L215.6 523.58c-7.58-3.49-7.79-14.05-0.41-17.23l508.74-220.76c8.91-3.91 18.03 5.22 14.13 14.14z" p-id="4967"></path></svg>'
          },
          link: 'https://blog.diyigemt.com'
        }
      ],
      footer: {
        message: '桂ICP备2022008499号',
        copyright: 'Copyright © 2022-present <a href="https://github.com/diyigemt">第一个mt</a>'
      }
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
