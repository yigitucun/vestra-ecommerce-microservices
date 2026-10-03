"use client"
import {
    Command,
    CommandDialog,
    CommandEmpty,
    CommandGroup,
    CommandInput,
    CommandItem,
    CommandList
} from "@/components/ui/command";
import {useEffect, useState} from "react";
import {useRouter} from "next/navigation";
import {Button} from "@/components/ui/button";
import {Kbd, KbdGroup} from "@/components/ui/kbd";
import {Search} from "lucide-react";
import { menuData } from "@/app/dashboard/_components/sidebar-menu-data"


export default function CommandSearch(){
    const router = useRouter()
    const [open, setOpen] = useState(false)
    useEffect(() => {
        const handleKeyDown = (e: KeyboardEvent) => {
            if ((e.ctrlKey || e.metaKey) && e.key === "k") {
                e.preventDefault()
                setOpen(true)
            }
        }

        window.addEventListener("keydown", handleKeyDown)
        return () => window.removeEventListener("keydown", handleKeyDown)
    }, [])
    return (
        <div>
           <div className="px-2">
               <Button variant="outline" className="w-full justify-between text-muted-foreground" onClick={() => setOpen(true)}>
                   <span className={"flex gap-2 items-center"}><Search/>Ara...</span>
                   <KbdGroup className="text-sm" >
                       <Kbd className={"dark:bg-neutral-700"}>Ctrl</Kbd>
                       <span>+</span>
                       <Kbd className={"dark:bg-neutral-700"}>K</Kbd>
                   </KbdGroup>
               </Button>
           </div>

            <CommandDialog className="sm:max-w-[450px]"   open={open} onOpenChange={setOpen}>
                <Command  className="w-[450px]"  >
                    <CommandInput placeholder={"Ara..."} />
                    <CommandList>
                        <CommandEmpty>Sonuç bulunamadı.</CommandEmpty>
                        {menuData.navGroups.map((group) => (
                            <CommandGroup key={group.label} heading={group.label}>
                                {group.items.map((item) => (
                                    <CommandItem
                                        key={item.url}
                                        value={item.title}
                                        onSelect={() => {
                                            setOpen(false)
                                            router.push(item.url)
                                        }}
                                    >
                                        {item.icon}
                                        <span>{item.title}</span>
                                    </CommandItem>
                                ))}
                            </CommandGroup>
                        ))}
                    </CommandList>
                </Command>
            </CommandDialog>
        </div>
    )
}