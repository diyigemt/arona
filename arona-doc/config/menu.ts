import { NavItemWithMeta } from '../docs/types/NavItems';

export const navItems: NavItemWithMeta[] = [
  {
    text: 'Home',
    link: '/',
    meta: {
      shouldShowInNavbar: true,
      shouldShowInSidebar: false,
    },
  },
  {
    text: '其他',
    meta: {
      shouldShowInNavbar: true,
      shouldShowInSidebar: true,
    },
    collapsed: false,
    items: [
      {
        text: '其他',
        link: '/terms/others',
      },
    ],
  },
];
