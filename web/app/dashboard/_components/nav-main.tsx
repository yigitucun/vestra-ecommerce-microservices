"use client"

import {
  SidebarGroup,
  SidebarGroupContent, SidebarGroupLabel,
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
} from "@/components/ui/sidebar"
import Link from "next/link";
import {usePathname} from "next/navigation";
import { menuData } from "@/app/dashboard/_components/sidebar-menu-data"


export function NavMain() {
  const pathname = usePathname()

  return (
      <>
        {menuData.navGroups.map((group) => (
            <SidebarGroup key={group.label}>
              <SidebarGroupLabel>
                {group.label}
              </SidebarGroupLabel>

              <SidebarGroupContent>
                <SidebarMenu>
                  {group.items.map((item) => {
                    const isActive =
                        item.url === '/dashboard'
                            ? pathname === item.url
                            : pathname === item.url || pathname.startsWith(`${item.url}/`);
                    return (
                        <SidebarMenuItem key={item.title}>
                          <SidebarMenuButton tooltip={item.title} isActive={isActive} render={<Link href={item.url} />}>
                            {item.icon}
                            <span>{item.title}</span>
                          </SidebarMenuButton>
                        </SidebarMenuItem>
                    )
                  })}
                </SidebarMenu>
              </SidebarGroupContent>
            </SidebarGroup>
        ))}
      </>
  )
}