import {
    LayoutDashboardIcon,
    Package,
    LucideFolderTree,
    ShoppingCartIcon,
    Users
} from "lucide-react";

export const menuData = {
    navGroups: [
        {
            label: 'Genel',
            items: [
                {title: "Dashboard", url: "/dashboard", icon: <LayoutDashboardIcon />}
            ]
        },
        {
            label: 'Mağaza',
            items: [
                {title: "Ürünler", url: '/dashboard/products', icon: <Package />},
                {title: "Kategoriler", url: '/dashboard/categories', icon: <LucideFolderTree />},
                {title: 'Siparişler', url: '/dashboard/orders', icon: <ShoppingCartIcon />}
            ]
        },
        {
            label: 'Kullanıcı Yönetimi',
            items: [
                {title: "Kullanıcılar", url: '/dashboard/users', icon: <Users />},
            ]
        }
    ]
};