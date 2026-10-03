"use client"

import {
  Avatar,
  AvatarFallback,
} from "@/components/ui/avatar"
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuGroup,
  DropdownMenuItem,
  DropdownMenuLabel, DropdownMenuPortal,
  DropdownMenuSeparator, DropdownMenuSub, DropdownMenuSubContent,
  DropdownMenuSubTrigger,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu"
import {
  SidebarMenu,
  SidebarMenuButton,
  SidebarMenuItem,
  useSidebar,
} from "@/components/ui/sidebar"
import {EllipsisVerticalIcon, CircleUserRoundIcon, LogOutIcon, Palette, Sun, Moon, Computer} from "lucide-react"
import {useMe} from "@/hooks/auth/use-me";
import {useLogout} from "@/hooks/auth/use-logout";
import {Skeleton} from "@/components/ui/skeleton";
import {useTheme} from "next-themes";

export function NavUser(){
  const { isMobile } = useSidebar()
  const {data:user,isPending} = useMe()
  const {mutate: logout, isPending: isLoggingOut} = useLogout()
  const fullName= `${user?.firstName} ${user?.lastName}`
  const { theme, setTheme } = useTheme()
  const getInitials = (name?: string) => {
    if (!name) return "UK";
    return name
        .split(" ")
        .map((n) => n[0])
        .join("")
        .substring(0, 2)
        .toUpperCase();
  };


  if (isPending) {
   return (
       <div className="flex w-fit items-center gap-4">
         <Skeleton className="size-10 shrink-0 rounded-full" />
         <div className="grid gap-2">
           <Skeleton className="h-4 w-[100px]" />
           <Skeleton className="h-4 w-[150px]" />
         </div>
       </div>
   )
  }
  return (
    <SidebarMenu>
      <SidebarMenuItem>
        <DropdownMenu>
          <DropdownMenuTrigger render={<SidebarMenuButton size="lg" className="aria-expanded:bg-muted" />}>
            <Avatar className="size-8 rounded-lg grayscale">
              <AvatarFallback className="rounded-lg">{getInitials(fullName)}</AvatarFallback>
            </Avatar>
            <div className="grid flex-1 text-left text-sm leading-tight">
              <span className="truncate font-medium">{fullName}</span>
              <span className="truncate text-xs text-foreground/70">
                {user?.email}
              </span>
            </div>
            <EllipsisVerticalIcon className="ml-auto size-4" />
          </DropdownMenuTrigger>
          <DropdownMenuContent className="min-w-56" side={isMobile ? "bottom" : "right"} align="end" sideOffset={4}>
            <DropdownMenuGroup>
              <DropdownMenuLabel className="p-0 font-normal">
                <div className="flex items-center gap-2 px-1 py-1.5 text-left text-sm">
                  <Avatar className="size-8">
                    <AvatarFallback className="rounded-lg">{getInitials(fullName)}</AvatarFallback>
                  </Avatar>
                  <div className="grid flex-1 text-left text-sm leading-tight">
                    <span className="truncate font-medium">{fullName}</span>
                    <span className="truncate text-xs text-muted-foreground">
                      {user?.email}
                    </span>
                  </div>
                </div>
              </DropdownMenuLabel>
            </DropdownMenuGroup>
            <DropdownMenuSeparator />
            <DropdownMenuGroup>
              <DropdownMenuItem>
                <CircleUserRoundIcon/>
                Hesap
              </DropdownMenuItem>
              <DropdownMenuSub>
                <DropdownMenuSubTrigger><Palette/>Tema</DropdownMenuSubTrigger>
                <DropdownMenuPortal>
                  <DropdownMenuSubContent>
                    <DropdownMenuItem onClick={() => setTheme("dark")} ><Moon/> Karanlık</DropdownMenuItem>
                    <DropdownMenuItem onClick={() => setTheme("light")}><Sun/> Aydınlık</DropdownMenuItem>
                    <DropdownMenuItem onClick={() => setTheme("system")}><Computer/> Sistem</DropdownMenuItem>
                  </DropdownMenuSubContent>
                </DropdownMenuPortal>
              </DropdownMenuSub>

            </DropdownMenuGroup>
            <DropdownMenuSeparator />
            <DropdownMenuItem variant={"destructive"} disabled={isLoggingOut} onClick={() => logout()}>
              <LogOutIcon/>
              Çıkış yap
            </DropdownMenuItem>
          </DropdownMenuContent>
        </DropdownMenu>
      </SidebarMenuItem>
    </SidebarMenu>
  )
}
