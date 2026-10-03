"use client"

import {useMe} from "@/hooks/auth/use-me";

export default function UserInfo(){
    const {data:user} = useMe()
    return (
        <>
            {user?.firstName}
        </>
    )
}