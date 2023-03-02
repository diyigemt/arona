import { DefaultTheme, defineConfig } from 'vitepress';
import { navItems } from '../../config/menu';
import { withPwa } from '@vite-pwa/vitepress';

function getMenuItems(
  itemType: 'navbar' | 'sidebar' = 'navbar'
): DefaultTheme.NavItem[] | DefaultTheme.Sidebar {
  return navItems.filter(
    item =>
      item.meta[`shouldShowIn${itemType[0].toUpperCase()}${itemType.slice(1)}`]
  );
}

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
      nav: getMenuItems('navbar') as DefaultTheme.NavItem[],
      sidebar: getMenuItems('sidebar') as DefaultTheme.Sidebar,
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
