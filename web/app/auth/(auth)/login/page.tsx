import {Metadata} from "next";
import {LoginForm} from "@/app/auth/(auth)/login/_component/login-form";

export const metadata:Metadata = {
    title: "Giriş yap",
}

export default async function Page(){
    return <LoginForm/>
}