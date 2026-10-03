"use client"

import {Button} from "@/components/ui/button";
import {useLogout} from "@/hooks/auth/use-logout";
import {LogOut} from "lucide-react";
import {Spinner} from "@/components/ui/spinner";


export function LogoutButton(){

    const {mutate:logout, isPending} = useLogout()

    return (
        <Button variant={"destructive"} disabled={isPending} onClick={() =>logout()}>
            {isPending? <Spinner /> : <LogOut/>}
            Çıkış yap
        </Button>
    )
}