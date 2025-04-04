import { NavItem } from "@/types";

export const navItems: NavItem[] = [
    {
        title: 'Dashboard',
        url: '/dashboard/overview',
        icon: 'dashboard',
        isActive: false,
        shortcut: ['d', 'd'],
        items: []
    },
    {
        title: 'User',
        url: '/dashboard/user',
        icon: 'user',
        shortcut: ['u', 'u'],
        isActive: false,
        items: []
    },
    {
        title: 'Category',
        url: '/dashboard/category',
        icon: 'category',
        shortcut: ['c', 'c'],
        isActive: false,
        items: []
    },
    {
        title: "Promotion",
        url: "/dashboard/promotion",
        icon: "promotion",
        shortcut: ["p", "p"],
        isActive: false,
        items: [],
    },
    {
        title: 'Product',
        url: '/dashboard/product',
        icon: 'product',
        shortcut: ['p', 'p'],
        isActive: false,
        items: []
    },
    {
        title: 'Discount',
        url: '/dashboard/discount',
        icon: 'discount',
        shortcut: ['d', 'd'],
        isActive: false,
        items: []
    },
    {
        title: 'Order',
        url: "/dashboard/order",
        icon: "order",
        shortcut: ["o", "o"],
        isActive: false,
        items: [],
    },
    {
        title: "Invoice",
        url: "/dashboard/invoice",
        icon: "receipt",
        shortcut: ["i", "i"],
        isActive: false,
        items: [],
    },
    {
        title: 'Chat',
        url: '/dashboard/chat',
        icon: 'chat',
        shortcut: ['c', 'c'],
        isActive: false,
        items: []
    },
    {
        title: 'Account',
        url: '#',
        icon: 'billing',
        isActive: true,

        items: [
            {
                title: 'Profile',
                url: '/dashboard/profile',
                icon: 'userPen',
                shortcut: ['m', 'm']
            },
            {
                title: 'Login',
                shortcut: ['l', 'l'],
                url: '/',
                icon: 'login'
            }
        ]
    },
    {
        title: 'Kanban',
        url: '/dashboard/kanban',
        icon: 'kanban',
        shortcut: ['k', 'k'],
        isActive: false,
        items: []
    }
];
