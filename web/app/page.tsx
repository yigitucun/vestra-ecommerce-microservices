import {Metadata} from "next";
import UserInfo from "@/components/auth/user-info";
import {LogoutButton} from "@/components/auth/logout-button";

export const metadata:Metadata = {
  title: "Home",
}

export default async function Page() {
    return (
        <div className="flex gap-5 items-center">
            <UserInfo/>
            <LogoutButton/>
        </div>
    )
}
